package sz.lab.service.provider.impl;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sz.lab.dto.provider.HealthCheckResultDTO;
import sz.lab.dto.provider.HealthCheckResultDTO.CheckItem;
import sz.lab.entity.provider.ProviderConfigEntity;
import sz.lab.service.provider.ProviderHealthService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Service
public class ProviderHealthServiceImpl implements ProviderHealthService {

    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 5000;

    private final RestTemplate restTemplate;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public ProviderHealthServiceImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public HealthCheckResultDTO check(ProviderConfigEntity config) {
        String host = config.getDeployHost() != null ? config.getDeployHost() : "127.0.0.1";

        CompletableFuture<CheckItem> startupF = runAsync(() -> probeGet(
                "http://" + host + ":" + config.getStsPort() + "/api/check/startup"));
        CompletableFuture<CheckItem> managementF = runAsync(() -> probeManagement(
                "http://" + host + ":" + config.getControlplaneMgmtPort() + "/management/v3/assets/request"));
        CompletableFuture<CheckItem> dspF = runAsync(() -> probeDsp(
                "http://" + host + ":" + config.getControlplaneProtocolPort() + "/api/v1/dsp"));
        CompletableFuture<CheckItem> didF = runAsync(() -> probeGet(
                "http://" + host + "/" + config.getProviderName() + "/did.json"));

        HealthCheckResultDTO result = new HealthCheckResultDTO();
        result.setStartup(joinUnchecked(startupF));
        result.setManagement(joinUnchecked(managementF));
        result.setDsp(joinUnchecked(dspF));
        result.setDid(joinUnchecked(didF));
        result.setCheckedAt(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.setOverall(deriveOverall(result));
        return result;
    }

    private CompletableFuture<CheckItem> runAsync(Supplier<CheckItem> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    private CheckItem joinUnchecked(CompletableFuture<CheckItem> f) {
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            CheckItem item = new CheckItem();
            item.setOk(false);
            item.setError(e.getMessage());
            return item;
        }
    }

    private String deriveOverall(HealthCheckResultDTO r) {
        boolean s = r.getStartup() != null && r.getStartup().isOk();
        boolean m = r.getManagement() != null && r.getManagement().isOk();
        boolean d = r.getDsp() != null && r.getDsp().isOk();
        boolean did = r.getDid() != null && r.getDid().isOk();
        if (s && m && d && did) return "ok";
        if (!s && !m && !d && !did) return "down";
        return "partial";
    }

    private CheckItem probeGet(String url) {
        long start = System.currentTimeMillis();
        CheckItem item = new CheckItem();
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
            item.setHttpStatus(resp.getStatusCodeValue());
            item.setOk(resp.getStatusCode().is2xxSuccessful());
            if (!item.isOk()) {
                item.setError("HTTP " + resp.getStatusCodeValue());
            }
        } catch (Exception e) {
            item.setOk(false);
            item.setError(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        item.setLatencyMs(System.currentTimeMillis() - start);
        return item;
    }

    private CheckItem probeManagement(String url) {
        long start = System.currentTimeMillis();
        CheckItem item = new CheckItem();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Api-Key", "password");
            String body = "{\"@context\":[\"https://w3id.org/edc/connector/management/v0.0.1\"],\"@type\":\"QuerySpec\"}";
            HttpEntity<String> req = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, req, String.class);
            item.setHttpStatus(resp.getStatusCodeValue());
            item.setOk(resp.getStatusCode().is2xxSuccessful());
            if (!item.isOk()) {
                item.setError("HTTP " + resp.getStatusCodeValue());
            }
        } catch (Exception e) {
            item.setOk(false);
            item.setError(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        item.setLatencyMs(System.currentTimeMillis() - start);
        return item;
    }

    private CheckItem probeDsp(String url) {
        long start = System.currentTimeMillis();
        CheckItem item = new CheckItem();
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
            item.setHttpStatus(resp.getStatusCodeValue());
            item.setOk(true);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            item.setHttpStatus(e.getStatusCode().value());
            item.setOk(true);
        } catch (Exception e) {
            item.setOk(false);
            item.setError(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        item.setLatencyMs(System.currentTimeMillis() - start);
        return item;
    }
}
