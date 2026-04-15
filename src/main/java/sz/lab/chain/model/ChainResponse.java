package sz.lab.chain.model;

import lombok.Data;

@Data
public class ChainResponse {
    private Integer code;
    private String message;
    private ChainData data;

    @Data
    public static class ChainData {
        private Boolean success;
        private String transactionHash;
        private String tokenId;
        private String owner;
        private Long gasUsed;
        private String taskId;
    }

    public String getTxHash() {
        return data != null ? data.getTransactionHash() : null;
    }

    public String getStatus() {
        return code != null && code == 200 ? "SUCCESS" : "FAILED";
    }
}
