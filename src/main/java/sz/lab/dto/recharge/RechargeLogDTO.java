package sz.lab.dto.recharge;

import lombok.Data;

import java.math.BigInteger;


@Data
public class RechargeLogDTO {
    private Integer logId;
    private Integer fromId;
    private Integer userId;
    private BigInteger amount;
    private Integer status;
}
