package sz.lab.dto.system.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogDTO {
    private Long logId;
    private Integer userId;
    private String userName;
    private String logType;
    private String logContent;
    private String logStatus;
    private String logSource;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;
}
