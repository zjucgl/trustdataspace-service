package sz.lab.service.provider.impl;

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
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ProviderDeployServiceImpl implements ProviderDeployService {

    @Resource
    private ProviderConfigMapper providerConfigMapper;

    @Override
    public void generateAndDownload(Long providerId, HttpServletResponse response) throws IOException {
        ProviderConfigEntity config = providerConfigMapper.selectById(providerId);
        if (config == null) {
            response.sendError(404, "Provider not found");
            return;
        }

        Map<String, String> vars = buildVars(config);

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + config.getProviderName() + "-deploy.zip");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            addTemplateToZip(zos, "templates/deploy/docker-compose.yml.ftl",
                    "docker-compose.yml", vars);
            addTemplateToZip(zos, "templates/deploy/deploy.sh.ftl",
                    "deploy.sh", vars);
            addTemplateToZip(zos, "templates/deploy/nginx-provider.conf.ftl",
                    "nginx-provider.conf", vars);
        }
    }

    private Map<String, String> buildVars(ProviderConfigEntity config) {
        Map<String, String> vars = new HashMap<>();
        vars.put("providerName", config.getProviderName());
        vars.put("providerId", String.valueOf(config.getId()));
        vars.put("controlplaneMgmtPort", String.valueOf(config.getControlplaneMgmtPort()));
        vars.put("controlplaneProtocolPort", String.valueOf(config.getControlplaneProtocolPort()));
        vars.put("controlplanePublicPort", String.valueOf(config.getControlplanePublicPort()));
        vars.put("dataplanePublicPort", String.valueOf(config.getDataplanePublicPort()));
        vars.put("identityHubPort", String.valueOf(config.getIdentityHubPort()));
        vars.put("stsPort", String.valueOf(config.getStsPort()));
        vars.put("deployHost", config.getDeployHost() != null ? config.getDeployHost() : "127.0.0.1");
        vars.put("callbackUrl", "https://ds.huayihui.art/api");
        vars.put("generatedAt", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return vars;
    }

    private void addTemplateToZip(ZipOutputStream zos, String templatePath,
                                   String entryName, Map<String, String> vars) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        String content;
        try (InputStream is = resource.getInputStream()) {
            content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            content = content.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        zos.putNextEntry(new ZipEntry(entryName));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }
}
