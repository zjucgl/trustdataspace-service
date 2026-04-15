package sz.lab.chain.client;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import sz.lab.chain.model.ChainResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class HuayiChainClient {

    @Value("${huayichain.base-url}")
    private String baseUrl;

    @Value("${huayichain.app-key}")
    private String appKey;

    @Value("${huayichain.app-secret}")
    private String appSecret;

    @Value("${huayichain.contract-address}")
    private String contractAddress;

    @Value("${huayichain.admin-address}")
    private String adminAddress;

    @Value("${huayichain.admin-key}")
    private String adminKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicLong tokenIdCounter = new AtomicLong(System.currentTimeMillis());

    public ChainResponse submitRecord(String artifactId, String artifactNo,
                                       String action, String operator,
                                       String dataHash, String remark) {
        String url = baseUrl + "/nft/mint";

        HttpHeaders headers = buildHeaders();

        JSONObject uri = new JSONObject();
        uri.put("standard", "TDS-RECORD-v1");
        uri.put("artifactId", artifactId);
        uri.put("artifactNo", artifactNo);
        uri.put("action", action);
        uri.put("operator", operator);
        uri.put("dataHash", dataHash);
        uri.put("remark", remark);

        Map<String, Object> body = new HashMap<>();
        body.put("to", adminAddress);
        body.put("uri", uri.toJSONString());
        body.put("contractAddress", contractAddress);
        body.put("address", adminAddress);
        body.put("addressKey", adminKey);
        body.put("tokenId", String.valueOf(tokenIdCounter.incrementAndGet()));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            return restTemplate.postForObject(url, entity, ChainResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ChainResponse createWallet(String userSn, String password) {
        String url = baseUrl + "/create/wallet";
        HttpHeaders headers = buildHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("userSn", userSn);
        body.put("password", password);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(url, entity, ChainResponse.class);
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "");
        headers.set("app-key", appKey);
        headers.set("app-secret", appSecret);
        return headers;
    }
}
