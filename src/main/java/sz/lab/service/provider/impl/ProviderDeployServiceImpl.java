package sz.lab.service.provider.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import sz.lab.entity.provider.ProviderConfigEntity;
import sz.lab.mapper.provider.ProviderConfigMapper;
import sz.lab.service.provider.ProviderDeployService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ProviderDeployServiceImpl implements ProviderDeployService {

    private static final Pattern CONDITIONAL_BLOCK = Pattern.compile(
            "(?s)# @if:(\\w+)\\s*\\r?\\n(.*?)# @endif\\s*\\r?\\n");

    @Resource
    private ProviderConfigMapper providerConfigMapper;

    @Value("${edc.tractusx.runtime-image:tractusx/edc-runtime-memory:0.12.0}")
    private String tractusxRuntimeImage;

    @Override
    public void generateAndDownload(Long providerId, HttpServletResponse response) throws IOException {
        ProviderConfigEntity config = providerConfigMapper.selectById(providerId);
        if (config == null) {
            response.sendError(404, "Provider not found");
            return;
        }

        Set<String> extensions = parseExtensions(config.getEnabledDataplaneExtensions());
        Map<String, String> vars = buildVars(config, extensions);

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + config.getProviderName() + "-deploy.zip");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            addTemplateToZip(zos, "templates/deploy/docker-compose.yml.ftl",
                    "docker-compose.yml", vars, extensions);
            addTemplateToZip(zos, "templates/deploy/deploy.sh.ftl",
                    "deploy.sh", vars, extensions);
            addTemplateToZip(zos, "templates/deploy/nginx-provider.conf.ftl",
                    "nginx-provider.conf", vars, extensions);
            addTemplateToZip(zos, "templates/deploy/did.json.template",
                    "did.json.template", vars, extensions);
        }
    }

    private Set<String> parseExtensions(String raw) {
        Set<String> result = new HashSet<>();
        result.add("http");
        if (raw == null || raw.isEmpty()) {
            return result;
        }
        Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(result::add);
        return result;
    }

    private Map<String, String> buildVars(ProviderConfigEntity config, Set<String> extensions) {
        Map<String, String> vars = new HashMap<>();
        String providerName = config.getProviderName();
        String deployHost = config.getDeployHost() != null ? config.getDeployHost() : "127.0.0.1";
        vars.put("providerName", providerName);
        vars.put("providerId", String.valueOf(config.getId()));
        vars.put("controlplaneMgmtPort", String.valueOf(config.getControlplaneMgmtPort()));
        vars.put("controlplaneProtocolPort", String.valueOf(config.getControlplaneProtocolPort()));
        vars.put("controlplanePublicPort", String.valueOf(config.getControlplanePublicPort()));
        vars.put("dataplanePublicPort", String.valueOf(config.getDataplanePublicPort()));
        vars.put("identityHubPort", String.valueOf(config.getIdentityHubPort()));
        vars.put("stsPort", String.valueOf(config.getStsPort()));
        vars.put("deployHost", deployHost);
        vars.put("callbackUrl", "https://ds.huayihui.art/api");
        vars.put("enabledDataplaneExtensions", String.join(",", extensions));
        vars.put("tractusxRuntimeImage", tractusxRuntimeImage);
        vars.put("participantDid",
                config.getParticipantId() != null && !config.getParticipantId().isEmpty()
                        ? config.getParticipantId()
                        : "did:web:" + deployHost.replace(":", "%3A") + ":" + providerName);
        vars.put("participantContextId", UUID.randomUUID().toString());
        vars.put("participantBpn", "BPN" + providerName.toUpperCase());
        vars.put("managementAuthKey", "password");
        vars.put("generatedAt", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return vars;
    }

    private void addTemplateToZip(ZipOutputStream zos, String templatePath,
                                  String entryName, Map<String, String> vars,
                                  Set<String> extensions) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        String content;
        try (InputStream is = resource.getInputStream()) {
            content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        content = applyConditionals(content, extensions);
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            content = content.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        zos.putNextEntry(new ZipEntry(entryName));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private String applyConditionals(String content, Set<String> extensions) {
        Matcher m = CONDITIONAL_BLOCK.matcher(content);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String key = m.group(1);
            String body = m.group(2);
            String replacement = extensions.contains(key) ? body : "";
            m.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(out);
        return out.toString();
    }
}
