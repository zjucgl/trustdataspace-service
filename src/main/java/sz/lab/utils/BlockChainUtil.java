package sz.lab.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.File;

@Component
public class BlockChainUtil {

    @Value("${keystore.dir}")
    private String keystoreDiraction;
//    private static final String NODE_URL = "http://192.168.3.115:8545"; // Geth/Infura/测试节点
    private static Web3j web3j;

    public Web3j getWeb3jInstance(String nodeUrl) {
        if (web3j == null) {
            web3j = Web3j.build(new HttpService(nodeUrl));
        }
        return web3j;
    }
    public Object[] generateWallet(String password) throws Exception {
        // 1. 生成新的以太坊钱包密钥对
        ECKeyPair keyPair = Keys.createEcKeyPair();
        String privateKey = keyPair.getPrivateKey().toString(16);
        String publicKey = keyPair.getPublicKey().toString(16);

        // 2. 设置钱包密码（用于加密私钥）
//        String password = "your-secure-password";
        if (password == null || password.isEmpty())
            return null;

        // 3. 指定 keystore 存储目录（例如当前目录下的 "keystore" 文件夹）
        File keystoreDir = new File(keystoreDiraction);
        if (!keystoreDir.exists()) {
            keystoreDir.mkdirs();
        }

        // 4. 创建 keystore 文件
        String walletFileName = WalletUtils.generateWalletFile(password, keyPair, keystoreDir, true);

        // 5. 输出钱包信息
        String walletAddress = "0x" + Keys.getAddress(keyPair);
        System.out.println("钱包地址: " + walletAddress);
        System.out.println("私钥: " + privateKey);
        System.out.println("公钥: " + publicKey);
        System.out.println("Keystore 文件名: " + walletFileName);
        System.out.println("Keystore 文件路径: " + keystoreDir.getAbsolutePath() + File.separator + walletFileName);
        return new Object[]{walletAddress,privateKey};
    }

//    public static void main(String[] args) throws Exception {
//        // 生成一个新的以太坊钱包
//        ECKeyPair keyPair = Keys.createEcKeyPair();
//        String privateKey = keyPair.getPrivateKey().toString(16);  // 私钥
//        String publicKey = keyPair.getPublicKey().toString(16);    // 公钥
//
//        // 通过公钥生成钱包地址
//        String walletAddress = Keys.getAddress(keyPair);
//
//        // 输出钱包信息
//        System.out.println("新生成的钱包地址: 0x" + walletAddress);
//        System.out.println("私钥: " + privateKey);
//        System.out.println("公钥: " + publicKey);
//    }

}
