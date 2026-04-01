package sz.lab.service.system.qrcode.impl;

import cn.hutool.extra.qrcode.QrConfig;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.basic.PersonalAssetEntity;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.mapper.basic.PersonalAssetMapper;
import sz.lab.mapper.orga.user.UserMapper;
import sz.lab.service.system.qrcode.QRService;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Service
public class QRServiceImpl implements QRService {
    @Resource
    QrConfig qrconig;
    @Resource
    PersonalAssetMapper personalAssetMapper;
    @Resource
    UserMapper userMapper;

    /**
     * 生成证书 PDF
     * @param assetId 资源ID
     * @param userId 用户ID
     * @throws IOException 生成 PDF 时可能抛出的异常
     * @throws WriterException 生成二维码时可能抛出的异常
     */
    @Override
    public OperateResultDTO generateCertificate(String assetId,Integer userId) throws IOException, WriterException {
        // 加载 PDF 文档模板
        PDDocument document = PDDocument.load(new File("./templates/证书.pdf"));
        PersonalAssetEntity entity  = personalAssetMapper.selectOne(Wrappers.lambdaQuery(PersonalAssetEntity.class)
                .eq(PersonalAssetEntity::getAssetId, assetId)
                .eq(PersonalAssetEntity::getUserId, userId));
        if(entity == null){
            return new OperateResultDTO(false, "未查找到资产", null);
        }
//        String ip = null;
//        try {
//            InetAddress localHost = InetAddress.getLocalHost();
//            ip = localHost.getHostAddress();
//            System.out.println("本机 IP 地址: " + ip);
//
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        String qrCodeContent =String.format("http://%s:9007/qrcode/generateHtml?assetId=%s&userId=%d", ip, assetId, userId);
        // 生成二维码的 URL
        assetId = URLEncoder.encode(assetId, "UTF-8");
        String qrCodeContent = "http://211.91.61.25:29007/qrcode/generateHtml?assetId="
                + assetId
                + "&userId="
                + userId;
        // 生成二维码的 BufferedImage 对象
        BufferedImage bufferedImage =  htmlQRCodeBufferImage(qrCodeContent, 300, 300);

        // 将 BufferedImage 对象转换为 PDImageXObject
        PDImageXObject pdImage = BufferedImageToPDImageXObject(document, bufferedImage);

        // 将二维码图片添加到 PDF 的左下角
        addImageToPDF(document, pdImage);

        //将pdf文档转换为base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
        document.close();
        return new OperateResultDTO(true, "成功", base64);
    }
    /**
     * 返回html文件
     * @param assetId 资源ID
     * @param userId 用户ID
     */
    @Override
    public void createHtmlTemplate(String assetId, Integer userId, Model model) {
        model.addAttribute("assetId", assetId);
        UserEntity userEntity = userMapper.selectOne(Wrappers.lambdaQuery(UserEntity.class)
                .eq(UserEntity::getUserId, userId));
        PersonalAssetEntity entity  = personalAssetMapper.selectOne(Wrappers.lambdaQuery(PersonalAssetEntity.class)
                .eq(PersonalAssetEntity::getAssetId, assetId)
                .eq(PersonalAssetEntity::getUserId, userId));
        if (userEntity == null || entity == null)
            return;

        model.addAttribute("username", userEntity.getUserName());
        model.addAttribute("voucherDate", entity.getGmtCreate());
    }

    /**
     * 生成二维码的 BufferedImage 对象
     * @param content 二维码内容
     * @param width 二维码宽度
     * @param height 二维码高度
     * @return BufferedImage 类型的二维码对象
     * @throws WriterException 生成二维码时可能抛出的异常
     */
    private BufferedImage generateQRCodeBufferedImage(String content, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        return MatrixToImageWriter.toBufferedImage(qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints));
    }

    /**
     * 将 BitMatrix 对象转换为 PDImageXObject 以便添加到 PDF 中
     * @param document PDF 文档对象
     * @param  bufferedImage 类型的二维码对象
     * @return PDImageXObject 类型的图像对象
     * @throws IOException 转换图像时可能抛出的异常
     */
    private PDImageXObject BufferedImageToPDImageXObject(PDDocument document, BufferedImage bufferedImage) throws IOException {
        //将BufferedImage转换为File
        File tempFile = File.createTempFile("qrcode", ".png");
        ImageIO.write(bufferedImage, "png", tempFile);
        // 将BufferedImage转换为PDImageXObject
        return PDImageXObject.createFromFileByContent(tempFile, document);
    }

    /**
     * 将图像添加到 PDF 的左下角
     * @param document PDF 文档对象
     * @param pdImage 要添加的图像（PDImageXObject 类型）
     * @throws IOException 添加图像时可能抛出的异常
     */
    private void addImageToPDF(PDDocument document, PDImageXObject pdImage) throws IOException {
        PDPage page = document.getPage(0);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            float x = 50; // 左下角 x 坐标
            float y = 50; // 左下角 y 坐标
            float width = 100; // 图片宽度
            float height = 100; // 图片高度
            contentStream.drawImage(pdImage, x, y, width, height);
        }
    }

    private BufferedImage htmlQRCodeBufferImage(String content, int width, int height) throws WriterException, IOException {
//        // 对URL进行编码
//        String encodedContent = URLEncoder.encode(content, "UTF-8");

        // 设置二维码参数，如编码类型、容错级别、字符集等
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        return MatrixToImageWriter.toBufferedImage(qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints));
    }
}
