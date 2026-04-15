package sz.lab.controller.system;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sz.lab.controller.BaseController;
import sz.lab.dto.system.IpFileDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.service.system.asset.AssetService;
import sz.lab.service.system.file.IpFileService;
import sz.lab.utils.Base64Utils;
import sz.lab.utils.OssUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 通用管理
 */
@Validated
@RequestMapping("/utils")
@RestController
public class UtilsController extends BaseController {
    @Value("${aliyun.oss.endpoint}")
    private String address;
    private String bucketName = "data";
    @Resource
    private OssUtils ossUtils;
    @Resource
    private IpFileService ipFileService;
    @Resource
    private AssetService assetService;
    /**
     * @Description: minio外链查询
     **/
    @PostMapping("/minio/address")
    public OperateResultDTO list() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = new OperateResultDTO(true,"成功",address);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
    /**
     * @Description: minio上传
     **/
    @RequestMapping(value = "/minio/upload", method = RequestMethod.POST)
    public OperateResultDTO upload(@RequestParam("file") MultipartFile file,
                              HttpServletRequest request){

        List<String> upload = ossUtils.upload(new MultipartFile[]{file});
//        String url = address + "/" + bucketName + "/" + upload.get(0);
        String url = bucketName + "/" + upload.get(0);

        return new OperateResultDTO(true,"成功",url);
    }
    /**
     * @Description: minio上传没有时间戳
     **/
    @RequestMapping(value = "/minio/uploadWithoutTime", method = RequestMethod.POST)
    public OperateResultDTO uploadWithoutTime(@RequestParam("file") MultipartFile file,
                                   HttpServletRequest request){

        List<String> upload = ossUtils.uploadWithoutTime(new MultipartFile[]{file});
//        String url = address + "/" + bucketName + "/" + upload.get(0);
        String url = bucketName + "/" + upload.get(0);

        return new OperateResultDTO(true,"成功",url);
    }
    /**
     * @Description: 获取文件列表
     **/
    @PostMapping("/fileList")
    public OperateResultDTO getFileList() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
//            operateResultDTO = workFileService.getWorkFile();
            operateResultDTO = ipFileService.getIpFile();
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 获取文件
     **/
    @PostMapping("/fileByName")
    public OperateResultDTO getFileByName(@RequestBody IpFileDTO fileDTO) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
//            operateResultDTO = workFileService.getWorkFile();
//            operateResultDTO = ipFileService.getIpFileByName(fileDTO.getId());
            operateResultDTO = assetService.queryAssetByAssetId(fileDTO.getId());
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 获取文件列表
     **/
    @PostMapping("/fileByNameList")
    public OperateResultDTO getFileByName(@RequestBody List<String> nameList) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
//            operateResultDTO = workFileService.getWorkFile();
//            operateResultDTO = ipFileService.getIpFileByNameList(nameList);
              operateResultDTO = assetService.getFileByNameList(nameList);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
}
