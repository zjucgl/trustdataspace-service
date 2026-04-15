package sz.lab.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chain_record")
public class ChainRecordEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String artifactId;
    private String artifactNo;
    private String action;
    private Integer operatorId;
    private String operatorName;
    private String dataHash;
    private String txHash;
    private String chainStatus;
    private String remark;
    private LocalDateTime chainTime;
    private LocalDateTime createdAt;
}
