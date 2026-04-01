package sz.lab.controller.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sz.lab.controller.BaseController;
import sz.lab.controller.orga.UserController;
import sz.lab.dto.basic.PersonalAssetDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.service.basic.PersonalAssetService;
import sz.lab.service.orga.user.UserService;
import sz.lab.service.trace.AssetTraceService;

import javax.annotation.Resource;

/**
 * 个人资产
 */
@Validated
@RequestMapping("/personalAsset")
@RestController
public class PersonalAssetController extends BaseController {
    //日志记录器
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Resource
    private PersonalAssetService personalAssetService;
    @Resource
    private UserService userService;
    @Resource
    private AssetTraceService assetTraceService;
    /**
     * @Description: 分页查询
     **/
    @PostMapping("/list")
    public OperateResultDTO list(@RequestBody TableRequestDTO tableRequestDTO) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            tableRequestDTO.getJsonParam().put("userId",userId.get());
            operateResultDTO = personalAssetService.pageList(tableRequestDTO);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 判断资产是否存在
     **/
    @PostMapping("/isExist")
    public OperateResultDTO isExist(@RequestBody PersonalAssetDTO dto) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            dto.setUserId(userId.get());
            operateResultDTO = personalAssetService.isExist(dto);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 新增
     **/
    @PostMapping("/add")
    public OperateResultDTO add(@RequestBody PersonalAssetDTO dto) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        OperateResultDTO userOperateResultDTO = new OperateResultDTO();
        try {
            dto.setUserId(userId.get());
            userOperateResultDTO = userService.updateprice(dto.getUserId(),dto.getAssetId());
            if (!userOperateResultDTO.isSuccess()) {
                operateResultDTO.setSuccess(false);
                operateResultDTO.setMessage(userOperateResultDTO.getMessage());
                return operateResultDTO;
            }
            OperateResultDTO assetOperateResultDTO = (OperateResultDTO) userOperateResultDTO.getResult();
            if (!assetOperateResultDTO.isSuccess()) {
                operateResultDTO.setSuccess(false);
                operateResultDTO.setMessage(assetOperateResultDTO.getMessage());
                return operateResultDTO;
            }
            Object[] result = (Object[]) assetOperateResultDTO.getResult();
            dto.setEthAssetId((String) result[0]);
            operateResultDTO = personalAssetService.add(dto);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 获取热门资产排行
     **/
    @PostMapping("/hotAsset")
    public OperateResultDTO hotAsset() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = personalAssetService.selectHotAsset();
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 获取资产图表
     **/
    @PostMapping("/assetChart")
    public OperateResultDTO assetChart() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = personalAssetService.getAssetChart();
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 获取资产成交数
     **/
    @PostMapping("/assetListCount")
    public OperateResultDTO assetList() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = personalAssetService.getAssetListCount();
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 获取我消费的资产
     **/
    @PostMapping("/assetListByUser")
    public OperateResultDTO assetListByUser() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = personalAssetService.getAssetList(userId.get());
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }

    @PostMapping("/isProvider")
    public OperateResultDTO isProvider(@RequestBody PersonalAssetDTO dto) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = assetTraceService.isProvider(userId.get(),dto.getAssetId());
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
}
