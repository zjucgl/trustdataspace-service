package sz.lab.service.system.recharge;

import sz.lab.dto.recharge.RechargeLogDTO;
import sz.lab.dto.system.OperateResultDTO;

import java.math.BigInteger;

public interface PaymentService {

    /**
     * ---区块链中管理员将矿工钱充值给用户
     * @param userId
     * @param amount
     * @return
     */
    OperateResultDTO sendPayment(Integer logId, Integer userId, BigInteger amount,Integer fromId) throws Exception;
}
