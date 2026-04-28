package sz.lab.dto.provider;

import lombok.Data;

@Data
public class HealthCheckResultDTO {
    private CheckItem startup;
    private CheckItem management;
    private CheckItem dsp;
    private CheckItem did;
    private String overall;
    private String checkedAt;

    @Data
    public static class CheckItem {
        private boolean ok;
        private Long latencyMs;
        private Integer httpStatus;
        private String error;
    }
}
