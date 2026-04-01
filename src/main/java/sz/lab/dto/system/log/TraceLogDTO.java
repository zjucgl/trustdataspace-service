package sz.lab.dto.system.log;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TraceLogDTO {
    private Long logId;
    private Long userId;
    private String logType;
    private String logStatus;
    private String logContent;
    private String logError;
    private LocalDateTime gmtCreate;
    private Long totalAmount;
    private String userName;
}
