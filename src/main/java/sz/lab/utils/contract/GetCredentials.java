package sz.lab.utils.contract;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GetCredentials {

    public static void main(String[] args) {
        try {
            // Keystore 文件路径
            String keystoreFilePath = "E:\\Users\\master\\Desktop\\fsdownload\\keystore\\UTC--2025-04-11T01-27-45.833943540Z--0f566bb5ccc7f6f72a6dd69a4ddd7618cbeab4d3";
            // 提供密码
            String password = "123";

            // 读取 Keystore 文件内容
            byte[] keystoreBytes = Files.readAllBytes(Paths.get(keystoreFilePath));
            String keystoreJson = new String(keystoreBytes);

            // 解密 Keystore 文件
            Credentials credentials = WalletUtils.loadCredentials(password, new File(keystoreFilePath));

            // 提取私钥
            String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);
            System.out.println("Private Key: " + privateKey);
        } catch (IOException | CipherException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
