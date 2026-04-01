package sz.lab.service.system.qrcode;


import com.google.zxing.WriterException;
import org.springframework.ui.Model;
import sz.lab.dto.system.OperateResultDTO;

import java.io.IOException;

public interface QRService {
    //传输pdf文件流
    OperateResultDTO generateCertificate(String qrCodeContent, Integer userId) throws IOException, WriterException;
    //生成html网页
    void createHtmlTemplate(String assetId,Integer userId, Model model);
}
