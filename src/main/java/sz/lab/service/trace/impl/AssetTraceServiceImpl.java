package sz.lab.service.trace.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;
import sz.lab.config.annotation.TraceLog;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.basic.PersonalAssetEntity;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.entity.system.IAssetEntity;
import sz.lab.mapper.basic.PersonalAssetMapper;
import sz.lab.mapper.orga.user.UserMapper;
import sz.lab.mapper.system.asset.IAssetMapper;
import sz.lab.service.trace.AssetTraceService;
import sz.lab.utils.contract.AssetTraceability;
import sz.lab.utils.contract.Web3jClient;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AssetTraceServiceImpl implements AssetTraceService {
    @Resource
    private IAssetMapper assetMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PersonalAssetMapper personalAssetMapper;

    @Value("${keystore.dir}")
    private String KEYSTORE_DIR;
    @Value("${gethAddress}")
    private String gethAddress;
    // 1. 连接到 Geth 节点
    Web3j web3 = Web3jClient.getWeb3jInstance();

    // 2. 创建凭据（解锁账户）
    Credentials credentials = Credentials.create("32206945c4a86ac089c8f749f15f986cc62341021372781d359eaedd0e77c2e4");//管理员凭证
//    Credentials credentials = Credentials.create("74a68e60a78e83a1b524d04c8b4f38a029fefea84b1ee324474d8c6195d62da4");//消费者（普通人)

    // 3. 加载已部署的合约
    String contractAddress = "0x187250E9EC0D39Bb6e0c828eF0b9b5d198D4EdF0";

    /*
    * @Description: 注册资产
    * @Param: [assetId, name, description, price]
    * @Return: void
    *
    */
    AssetTraceability contract = AssetTraceability.load(contractAddress, web3, credentials, new DefaultGasProvider());
    StaticGasProvider gasProvider = new StaticGasProvider(BigInteger.valueOf(20_000L), BigInteger.valueOf(8_000_000L));// 设置 gasProvider

    /**
     * @param assetId 资产ID
     * @param name 资产名字
     * @param description 资产描述
     * @param price 资产价格
     * @param ethAccount 生产者Account
     * @throws Exception 异常抛出
     */
    @Override
    // 注册资产
    public void registerAsset(String assetId, String name, String description, BigInteger price, String ethAccount) throws Exception {
        System.out.println("Registering Asset...");
        AssetTraceability contract = reloadContract(contractAddress, gethAddress, "32206945c4a86ac089c8f749f15f986cc62341021372781d359eaedd0e77c2e4");
        // 异步调用写法
        RemoteFunctionCall<TransactionReceipt> setWord = contract.registerAsset(assetId, name, description, price, ethAccount);
        TransactionReceipt transactionReceipt = setWord.sendAsync().get();
        System.out.println("Registed Asset: " + transactionReceipt.getTransactionHash());
    }

    @Override
    // 更新资产价格
    public void updatePrice(String assetId, BigInteger newPrice) throws Exception {
        TransactionReceipt receipt = contract.updatePrice(assetId, newPrice).send();
        System.out.println("Price Updated: " + receipt.getTransactionHash());
    }

    /**
     * description: 第一次购买转移资产
     * @param assetId 资产ID
     * @param userId 购买人ID
     * @return {@link OperateResultDTO }
     * @throws Exception 异常
     */
    @TraceLog(value = "资产上链接，第一次购买转移资产")
    @Override
    public OperateResultDTO purchaseAsset(String assetId, Integer userId) throws Exception {
        //获取资产信息
        IAssetEntity assetEntity = assetMapper.selectOne(Wrappers.lambdaQuery(IAssetEntity.class)
                .eq(IAssetEntity::getAssetId, assetId)
                .eq(IAssetEntity::getIsFirst, 1));
        if (assetEntity == null) {
            return new OperateResultDTO(false, "资产不存在",null);
        }
        UserEntity userEntity = userMapper.selectOne(Wrappers.lambdaQuery(UserEntity.class)
                .eq(UserEntity::getUserId, userId));
        if (userEntity == null) {
            return new OperateResultDTO(false, "用户不存在",null);
        }
        //字符串拼接，在assetId后加上时间戳
        assetId = assetId + System.currentTimeMillis();
        System.out.println("assetId:"+assetId);
        registerAsset(assetId,assetEntity.getAssetId(),"交易",BigInteger.valueOf(assetEntity.getAssetPrice()),"0x28c42475002a8df985873d9ed12c9a35f501eee2");
        //重载合约
        AssetTraceability contract = reloadContract(contractAddress, gethAddress, userEntity.getEthCredentials());
        // 获取当前 gasPrice
        EthGasPrice ethGasPrice = web3.ethGasPrice().send();
        BigInteger gasPriceWei = ethGasPrice.getGasPrice();
        System.out.println("当前 Gas 价格 (Wei): " + gasPriceWei);
        System.out.println("当前 Gas 价格 (Gwei): " + Convert.fromWei(gasPriceWei.toString(), Convert.Unit.GWEI));
        TransactionReceipt receipt = contract.purchaseAsset(assetId, BigInteger.valueOf(assetEntity.getAssetPrice()*1000000000)).sendAsync().get();
        // 计算 Gas 费用
        BigInteger gasUsed = receipt.getGasUsed(); // 获取交易使用的 Gas
        BigInteger totalCostWei = gasUsed.multiply(gasPriceWei); // 计算总费用

        System.out.println("交易消耗 Gas: " + gasUsed);
        System.out.println("交易总费用 (Wei): " + totalCostWei);
        System.out.println("交易总费用 (ETH): " + Convert.fromWei(totalCostWei.toString(), Convert.Unit.ETHER));
        System.out.println("Ownership Transferred: " + receipt.getTransactionHash());
        return new OperateResultDTO(true,"购买成功", new Object[]{assetId,userId,receipt.getTransactionHash(),totalCostWei,assetEntity.getAssetPrice(),userEntity.getUserName()});
    }

    @Override
    // 更新资产状态
    public AssetTraceability.Asset updateStatus(String assetId, String newStatus) throws Exception {
        TransactionReceipt receipt = contract.updateStatus(assetId, newStatus).send();
        System.out.println("Status Updated: " + receipt.getTransactionHash());
        return null;
    }

    @Override
    // 查询资产
    public AssetTraceability.Asset getAsset(String assetId, Integer userId) throws Exception {
        if (assetId == null || userId == null) {
            return null;
        }
        UserEntity userEntity = userMapper.selectOne(Wrappers.lambdaQuery(UserEntity.class)
                .eq(UserEntity::getUserId, userId));
        AssetTraceability contract = reloadContract(contractAddress, gethAddress, userEntity.getEthCredentials());
        return contract.getAsset(assetId).send();
    }

    /**
     * 查询资产历史
     * @param assetId 资产ID
     * @param userId 用户ID
     * @return {@link List }
     * @throws Exception 异常抛出
     */
    @Override
    public List getAssetHistory(String assetId,Integer userId) throws Exception {
        PersonalAssetEntity personalAssetEntity = personalAssetMapper.selectOne(Wrappers.lambdaQuery(PersonalAssetEntity.class)
                .eq(PersonalAssetEntity::getAssetId, assetId)
                .eq(PersonalAssetEntity::getUserId, userId)
        );
        if (personalAssetEntity == null)
            return null;
        List<AssetTraceability.AssetHistory> assetHistoryList = getAssetHistoryList(assetId, userId);
        List<Object> ownerHistoryList = new ArrayList<>();
        if (assetHistoryList != null) {
            assetHistoryList.forEach(assetHistory -> {
                UserEntity userEntity = userMapper.selectOne(Wrappers.lambdaQuery(UserEntity.class)
                        .eq(UserEntity::getEthAccount, assetHistory.owner)
                );
                if (userEntity != null) {
                    ownerHistoryList.add(userEntity.getUserName());
                }
            });
        }


        return ownerHistoryList;
    }

    @Override
    public OperateResultDTO checkBalance(String assetId, Integer userId) throws Exception {
        IAssetEntity assetEntity = assetMapper.selectOne(Wrappers.lambdaQuery(IAssetEntity.class).eq(IAssetEntity::getAssetId, assetId));
        UserEntity userEntity = userMapper.selectById(userId);
        //先判断用户余额是否足够
        if (userEntity.getUserDeposits() < assetEntity.getAssetPrice()) {
            return new OperateResultDTO(false,"用户余额不足",null);
        }
        return new OperateResultDTO(true,"用户余额足够",null);
    }

    @Override
    public OperateResultDTO isProvider(Integer userId , String assetId) throws Exception {
        IAssetEntity assetEntity = assetMapper.selectOne(Wrappers.lambdaQuery(IAssetEntity.class).eq(IAssetEntity::getAssetId, assetId));
        if (assetEntity == null) {
            return new OperateResultDTO(false,"资产不存在",null);
        }
        if (Objects.equals(assetEntity.getUserId(), userId)) {
            return new OperateResultDTO(false,"资产拥有者不能购买自己的资产",null);
        } else {
            return new OperateResultDTO(true,"不是资产拥有者",null);
        }
    }

    private List getAssetHistoryList(String assetId,Integer userId) throws Exception {
        PersonalAssetEntity personalAssetEntity = personalAssetMapper.selectOne(Wrappers.lambdaQuery(PersonalAssetEntity.class)
                .eq(PersonalAssetEntity::getAssetId, assetId)
                .eq(PersonalAssetEntity::getUserId, userId)
        );
        if(personalAssetEntity == null){
            return null;
        }

        return contract.getAssetHistory(personalAssetEntity.getEthAssetId()).send();
    }
    /**
     * &#064;Description:   重新加载合约方法
     * @param contractAddress 合约地址
     * @param web3jAddress 区块链地址
     * @param credentialString 区块链账户私钥
     * @return {@link AssetTraceability }
     * @throws Exception 异常抛出
     */
    private AssetTraceability reloadContract(String contractAddress, String web3jAddress, String credentialString) throws Exception {
        // 重新加载合约
        Web3j web3java = Web3j.build(new HttpService(web3jAddress));
        //搜索密码库中搜索用户密钥
//        Credentials credential = Credentials.create("7273e13ffa7ce57ef18630aa6acb61dc652c86474aecec79dd96efd11551f0ca");
        Credentials credential = Credentials.create(credentialString);
        return AssetTraceability.load(contractAddress, web3java, credential, gasProvider);
    }

    /**
     * 根据账户解密私钥
     * @param ethAccount Geth账户
     * @param password 密码
     * @return {@link String }
     */
    public String decryptPrivateKey(String ethAccount, String password) {
        ethAccount = ethAccount.toLowerCase().replace("0x", "");

        try {
            File keystoreFile = findKeystoreFile(ethAccount);
            if (keystoreFile == null) {
                return "Keystore file not found for account: " + ethAccount;
            }

            System.out.println("Using keystore file: " + keystoreFile.getAbsolutePath());

            // 这里假设所有账户使用相同密码 "123"，如果不同，则需要额外的密码管理机制
            String passworded = "123";

            Credentials credentials = WalletUtils.loadCredentials(passworded, keystoreFile);
            return "Private Key: " + credentials.getEcKeyPair().getPrivateKey().toString(16);
        } catch (IOException | CipherException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 查找密钥库文件
     * @param ethAccount 处理过后的Geth账户
     * @return {@link File }
     */
    private File findKeystoreFile(String ethAccount) {
        File keystoreDir = new File(KEYSTORE_DIR);
        if (!keystoreDir.exists() || !keystoreDir.isDirectory()) {
            System.err.println("Invalid keystore directory: " + KEYSTORE_DIR);
            return null;
        }

        for (File file : Objects.requireNonNull(keystoreDir.listFiles())) {
            if (file.getName().contains(ethAccount)) {
                return file;
            }
        }

        return null;
    }

//    // 监听事件
//    public void listenToAssetRegisteredEvent() throws Exception {
//        // 创建事件过滤器，监听 AssetRegistered 事件
//        String assetRegisteredEventSignature = "AssetRegistered(string,string,address)"; // 事件签名
//
//        // 过滤器：从区块 0 开始，监听所有事件
//        EthLog ethLog = web3j.ethGetLogs(new org.web3j.protocol.core.methods.request.EthFilter(
//                org.web3j.protocol.core.DefaultBlockParameter.valueOf("latest"), // 最新区块
//                org.web3j.protocol.core.DefaultBlockParameter.valueOf("latest"), // 最新区块
//                contractAddress)
//        ).send();
//
//        // 获取事件日志
//        for (Log log : ethLog.getLogs()) {
//            String assetId = log.getTopics().get(1);  // 事件参数 (assetId)
//            String name = log.getTopics().get(2);  // 事件参数 (name)
//            String owner = log.getTopics().get(3);  // 事件参数 (owner)
//            System.out.println("Asset Registered - assetId: " + assetId + ", name: " + name + ", owner: " + owner);
//        }
//    }
}
