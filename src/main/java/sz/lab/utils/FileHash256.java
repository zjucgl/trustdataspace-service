package sz.lab.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class FileHash256 {

    /**
     * 获取文件的哈希值（特征码）
     *
     * @param filePath 文件路径
     * @param algorithm 哈希算法（例如：MD5, SHA-256）
     * @return 文件的哈希值
     * @throws NoSuchAlgorithmException 如果算法不支持
     * @throws IOException 如果文件读取失败
     */
    public static String getFileChecksum(String filePath, String algorithm) throws NoSuchAlgorithmException, IOException {
        // 创建 MessageDigest 实例
        MessageDigest digest = MessageDigest.getInstance(algorithm);

        // 读取文件
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] byteArray = new byte[1024];
            int bytesRead = -1;

            // 读取文件并更新哈希
            while ((bytesRead = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesRead);
            }
        }

        // 获取哈希字节数组
        byte[] hashBytes = digest.digest();

        // 将字节数组转换成十六进制字符串
        return byteArrayToHex(hashBytes);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param byteArray 字节数组
     * @return 十六进制字符串
     */
    private static String byteArrayToHex(byte[] byteArray) {
        try (Formatter formatter = new Formatter()) {
            for (byte b : byteArray) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }

//    public static void main(String[] args) {
//        String filePath = "C:\\Users\\master\\Pictures\\3-1-2.png"; // 替换为你自己的文件路径
//        String algorithm = "SHA-256"; // 选择算法（MD5, SHA-1, SHA-256）
//
//        try {
//            String checksum = getFileChecksum(filePath, algorithm);
//            System.out.println("文件的 " + algorithm + " 哈希值：" + checksum);
//        } catch (NoSuchAlgorithmException | IOException e) {
//            e.printStackTrace();
//        }
//        //82eaf25c3b90d7dc3194ac7a1b43b664f94f5563d9cd7c86280968cf1d1784b0
//        //eb1bdcc76581299be744b8686c336c94c768ad29f44fa222a5e659735f5baee6
//        //d27757eb56a767af08f801798cf0dc5d49e45196f7f7a86f2b57edf7dd793545
//    }
}
