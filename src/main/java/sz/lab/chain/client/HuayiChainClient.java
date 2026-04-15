package sz.lab.chain.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import sz.lab.chain.model.ChainResponse;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class HuayiChainClient {

    @Value("${huayichain.base-url}")
    private String baseUrl;

    @Value("${huayichain.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public ChainResponse submitRecord(String artifactId, String artifactNo,
                                       String action, String operator,
                                       String dataHash, String remark) {
        String url = baseUrl + "/api/v1/records";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("X-API-Key", apiKey);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("artifact_id", artifactId);
        body.put("artifact_no", artifactNo);
        body.put("action", action);
        body.put("operator", operator);
        body.put("timestamp", Instant.now().toString());
        body.put("data_hash", dataHash);
        body.put("remark", remark);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(url, entity, ChainResponse.class);
    }
}
