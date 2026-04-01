package sz.lab.controller.system;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sz.lab.config.constants.RoleEnum;
import sz.lab.controller.BaseController;
import sz.lab.dto.orga.UserDTO;
import sz.lab.dto.recharge.RechargeLogDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TablePagingDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.service.system.recharge.RechargeLogService;
import javax.annotation.Resource;


@Validated
@RequestMapping("/rechargeLog")
@RestController
public class RechargeLogController extends BaseController {

    @Resource
    private RechargeLogService rechargeLogService;

    /**
     * 根据id查询充值记录
     * @param tableRequestDTO
     * @return
     */
    @PostMapping("/listById")
    public TablePagingDTO listById(@RequestBody TableRequestDTO tableRequestDTO) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId.get());
        RoleEnum roles = role.get();
        return rechargeLogService.getRechargeLogById(tableRequestDTO, "rechargeLog", roles, userDTO);
    }

    /**
     * 更新充值记录
     * @param rechargeLogDTO
     */
    @RequestMapping("/update")
    public void updateRechargeLog(@RequestBody RechargeLogDTO rechargeLogDTO) {
        rechargeLogDTO.setFromId(userId.get());
        rechargeLogService.updateRechargeLog(rechargeLogDTO);
    }

    /**
     * 充值
     * @param
     * @param
     * @return
     */
    @RequestMapping("/add")
    public OperateResultDTO recharge(@RequestBody RechargeLogDTO rechargeLogDTO) {
        rechargeLogDTO.setUserId(userId.get());
        return rechargeLogService.insertRechargeLog(rechargeLogDTO);
    }

}
