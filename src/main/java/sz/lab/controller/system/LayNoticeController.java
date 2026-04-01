package sz.lab.controller.system;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sz.lab.controller.BaseController;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.service.system.lay.LayNoticeService;

import javax.annotation.Resource;
/**
 * 通知管理
 */
@Validated
@RequestMapping("/lay/notice")
@RestController
public class LayNoticeController extends BaseController {
    @Resource
    private LayNoticeService layNoticeService;
    /**
     * @Description: 上次密码修改时间判断
     **/
    @PostMapping("/pwdCheck")
    public OperateResultDTO list() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = layNoticeService.checkPwdUpdate(userId.get());
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
}
