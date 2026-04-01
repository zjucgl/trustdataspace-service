package sz.lab.controller.system;

import com.google.zxing.WriterException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.service.system.qrcode.QRService;

import javax.annotation.Resource;
import java.io.IOException;

@Controller
@RequestMapping("/qrcode")
public class QRController {
    @Resource
    private QRService qrService;

    //获取pdf证书下载
    @GetMapping("1233")
    @ResponseBody
    public OperateResultDTO generateV2(@RequestParam String assetId,@RequestParam Integer userId ) throws IOException, WriterException {
        return qrService.generateCertificate(assetId,userId);
    }

//    //扫码获取html网页模板
//    @GetMapping("/generateHtmlTemplate")
//    public OperateResultDTO generateHtmlTemplate(@RequestParam String assetId,@RequestParam Integer userId) {
//        return qrService.createHtmlTemplate(assetId,userId);
//    }
    @RequestMapping("/generateHtml")
    public String index(@RequestParam String assetId,@RequestParam Integer userId, Model model) {
        // 可以在这里将数据添加到模型中，供Thymeleaf模板使用
        qrService.createHtmlTemplate(assetId,userId,model);
//        model.addAttribute("message", "Hello, World!");
        // 返回Thymeleaf模板名称
        return "index";
    }
}
