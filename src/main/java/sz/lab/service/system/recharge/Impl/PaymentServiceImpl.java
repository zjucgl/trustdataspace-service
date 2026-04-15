package sz.lab.service.system.recharge.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sz.lab.chain.client.HuayiChainClient;
import sz.lab.chain.model.ChainResponse;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.entity.system.RechargeLogEntity;
import sz.lab.mapper.orga.user.UserMapper;
import sz.lab.mapper.system.recharge.RechargeLogMapper;
import sz.lab.mapper.system.recharge.RechargeMapper;
import sz.lab.service.system.recharge.PaymentService;
import sz.lab.utils.BlockChainUtil;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private RechargeLogMapper rechargeLogMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RechargeMapper rechargeMapper;
    @Autowired
    private HuayiChainClient chainClient;
    @Autowired
    private BlockChainUtil blockChainUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperateResultDTO sendPayment(Integer logId, Integer userId, BigInteger amount, Integer fromId) {
        BigInteger extra = amount;
        rechargeMapper.recharge(userId, amount, extra);

        UserEntity userEntity = userMapper.selectUserById(userId);
        String userName = userEntity.getUserName();

        UserEntity adminEntity = userMapper.selectUserById(fromId);

        try {
            String dataHash = blockChainUtil.sha256(
                    "recharge-" + userId + "-" + amount + "-" + System.currentTimeMillis());
            ChainResponse response = chainClient.submitRecord(
                    "recharge-" + logId, "recharge-" + logId,
                    "TRANSFER", adminEntity.getUserName(), dataHash,
                    "充值 " + amount + " 给 " + userName);

            String txHash = response != null ? response.getTxHash() : "";
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            RechargeLogEntity rechargeLogEntity = new RechargeLogEntity();
            rechargeLogEntity.setId(logId);
            rechargeLogEntity.setFromEthAccount(adminEntity.getEthAccount());
            rechargeLogEntity.setFromId(fromId);
            rechargeLogEntity.setUserId(userId);
            rechargeLogEntity.setUserName(userName);
            rechargeLogEntity.setUserEthAccount(userEntity.getEthAccount());
            rechargeLogEntity.setAmount(amount);
            rechargeLogEntity.setTxHash(txHash);
            rechargeLogEntity.setStatus(1);
            rechargeLogMapper.update(rechargeLogEntity);

            return new OperateResultDTO(true, "充值成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("充值失败", e);
        }
    }
}
