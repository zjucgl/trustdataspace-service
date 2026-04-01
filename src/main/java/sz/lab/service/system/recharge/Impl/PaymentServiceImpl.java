package sz.lab.service.system.recharge.Impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.StaticGasProvider;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.entity.system.RechargeLogEntity;
import sz.lab.mapper.orga.user.UserMapper;
import sz.lab.mapper.system.recharge.RechargeLogMapper;
import sz.lab.mapper.system.recharge.RechargeMapper;
import sz.lab.service.system.recharge.PaymentService;
import sz.lab.utils.BlockChainUtil;
import sz.lab.utils.contract.PaymentContract;
import sz.lab.utils.contract.Web3jClient;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private RechargeLogMapper rechargeLogMapper;
    @Autowired
    private UserMapper userMapper;
    //数据库的Mapper
    @Autowired
    private RechargeMapper rechargeMapper;

    @Value("${gethAddress}")
    private String gethAddress;

    Web3j web3j = Web3jClient.getWeb3jInstance();


    /**
     * 区块链中管理员将矿工钱充值给用户
     *
     * @param userId
     * @param amount
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)  // 这里开启事务
    public OperateResultDTO sendPayment(Integer logId, Integer userId, BigInteger amount, Integer fromId) {
        //给数据库对应账号充钱
        BigInteger extra = amount;
        rechargeMapper.recharge(userId, amount, extra);



        //根据userId查询eth_account
        UserEntity userEntity = userMapper.selectUserById(userId);
        String userName = userEntity.getUserName();
        String recipient =  userEntity.getEthAccount();

        UserEntity adminEntity = userMapper.selectUserById(fromId);
        String ethCredentials = adminEntity.getEthCredentials();

        try {
            BigInteger price = amount.multiply(BigInteger.valueOf(2)).multiply(BigInteger.TEN.pow(9));  // 价格
            System.out.println(price);
            BigInteger msgValue = price;  // 必须和 price 一致


            // 2. 创建凭据（解锁账户）
            Credentials credentials = Credentials.create(ethCredentials);

            // 3. 加载已部署的合约
            String contractAddress = "0x63bF7030D945b19a6eF84F5620c040380f01b5E6";
            StaticGasProvider gasProvider = new StaticGasProvider(BigInteger.valueOf(20_000L), BigInteger.valueOf(8_000_000L));// 设置 gasProvider
            PaymentContract contract = PaymentContract.load(contractAddress, web3j, new RawTransactionManager(web3j, credentials), gasProvider);

            TransactionReceipt receipt = contract.sendPayment(recipient, price, msgValue).send();
            System.out.println(receipt);

            String transactionHash = receipt.getTransactionHash();

            // 获取并解析事件日志
            List<PaymentContract.PaymentSentEventResponse> events = getPaymentSentEvents(transactionHash,contract);

            // 输出事件信息（这里假设你想将事件发送到后端）
            if (events.size() > 0) {
                PaymentContract.PaymentSentEventResponse event = events.get(0);

                String fromEthAccount = event.from;
                String userEthAccount = event.to;
                // 将 BigInteger 类型的 timestamp 转换为 long
                String timestamp = formatTimestamp(event.timestamp.longValue());

                RechargeLogEntity rechargeLogEntity = new RechargeLogEntity();
                rechargeLogEntity.setId(logId);
                rechargeLogEntity.setFromEthAccount(fromEthAccount);
                rechargeLogEntity.setFromId(fromId);
                rechargeLogEntity.setUserId(userId);
                rechargeLogEntity.setUserName(userName);
                rechargeLogEntity.setUserEthAccount(userEthAccount);
                rechargeLogEntity.setAmount(amount);
                rechargeLogEntity.setTxHash(transactionHash);
                rechargeLogEntity.setStatus(1);
                rechargeLogMapper.update(rechargeLogEntity);

               System.out.println("Payment Sent at " + timestamp + " from " + event.from + " to " + event.to + " with value: " + event.amount);
            }

            return new OperateResultDTO(true,"充值成功",null);

        } catch (Exception e) {
            e.printStackTrace();
            // 抛出运行时异常，触发事务回滚
            throw new RuntimeException("充值失败", e);
        }
    }

    private List<PaymentContract.PaymentSentEventResponse> getPaymentSentEvents(String transactionHash,PaymentContract contract ) throws Exception {
        // 获取事件日志
        TransactionReceipt receipt = web3j.ethGetTransactionReceipt(transactionHash).send().getResult();
        return contract.getPaymentSentEvents(receipt);
    }

    /**
     * 格式化时间戳为 "yyyy-MM-dd HH:mm:ss"
     *
     * @param timestamp 时间戳，单位为秒
     * @return 格式化后的时间字符串
     */
    private String formatTimestamp(long timestamp) {
        // 将时间戳转换为 Date 对象
        Date date = new Date(timestamp * 1000L); // 假设时间戳是以秒为单位传入的，乘以 1000 转换为毫秒

        // 使用 SimpleDateFormat 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}

