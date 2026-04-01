package sz.lab.controller.system;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sz.lab.controller.BaseController;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.service.system.log.SystemLogService;

import javax.annotation.Resource;

/**
 * 系统日志管理
 */
@Validated
@RequestMapping("/log")
@RestController
public class SystemLogController extends BaseController {
    @Resource
    private SystemLogService logService;
    /**
     * @Description: 分页查询
     **/
    @PostMapping("/list")
    public OperateResultDTO list(@RequestBody TableRequestDTO tableRequestDTO) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = logService.pageList(tableRequestDTO);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
}
