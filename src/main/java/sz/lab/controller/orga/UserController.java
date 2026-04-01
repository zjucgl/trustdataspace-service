package sz.lab.controller.orga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sz.lab.config.annotation.SystemControllerLog;
import sz.lab.controller.BaseController;
import sz.lab.dto.orga.UserDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.service.orga.user.UserRoleService;
import sz.lab.service.orga.user.UserService;
import sz.lab.service.system.function.SystemFunctionService;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 人员管理
 */
@Validated
@RequestMapping("/user")
@RestController
public class UserController extends BaseController {
    //日志记录器
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Resource
    private UserService userService;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private SystemFunctionService functionService;

    /**
     * @Description: 分页查询
     **/
    @PostMapping("/list")
    public OperateResultDTO list(@RequestBody TableRequestDTO tableRequestDTO) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = userService.pageList(tableRequestDTO,userId.get(),"user");
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 分页查询
     **/
    @PostMapping("/listByRoot")
    public OperateResultDTO listByRoot(@RequestBody TableRequestDTO tableRequestDTO) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = userService.pageList(tableRequestDTO,userId.get(),"root");
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 用户下拉选项查询
     **/
    @PostMapping("/userOption")
    public OperateResultDTO userOptionList() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = userService.userOptionList();
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }

    /**
     * @Description: 角色下拉选项查询
     **/
    @PostMapping("/roleOption")
    public OperateResultDTO roleOption() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = userService.roleOptionList();
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 参与者下拉选项查询
     **/
    @PostMapping("/participantOption")
    public OperateResultDTO participantOption() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = userService.participantOptionList();
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 新增用户
     **/
    @PostMapping("/add")
    public OperateResultDTO add(@RequestBody UserDTO input) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = userService.add(input);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 修改用户
     **/
    @SystemControllerLog(type="用户模块",content = "编辑用户信息")
    @PostMapping("/modify")
    public OperateResultDTO update(@RequestBody UserDTO input) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = userService.update(input);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 修改用户角色
     **/
    @PostMapping("/modifyRoleCode")
    public OperateResultDTO updateRoleCode(@RequestBody UserDTO input){
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = userRoleService.updateUserRoleCode(input.getUserId(), input.getRoleIdList());
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 判断账号是否存在
     **/
    @PostMapping("/codeIsExist")
    public OperateResultDTO codeIsExist(@RequestBody UserDTO input) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = userService.codeIsExist(input);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 删除用户
     **/
    @PostMapping("/remove")
    public OperateResultDTO remove(@NotNull(message = "ids不能为空") @RequestBody Map<String,List<Integer>> map) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            //userService.removeByIds(map.get("ids"));
            operateResultDTO = userService.delete(map.get("ids"));
            operateResultDTO = new OperateResultDTO(true,"成功",null);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 查看密码强制更新功能
     **/
    @PostMapping("/getPwdFunction")
    public OperateResultDTO getPwdUpdate() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = functionService.getFunction("pwd_update");
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 密码强制更新功能的开或关
     **/
    @PostMapping("/updatePwdFunction")
    public OperateResultDTO changePwdUpdate() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = functionService.changeFunctionStatus("pwd_update");
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }

    /**
     * 获取用户余额
     * @return {@link OperateResultDTO }
     */
    @GetMapping("/getUserDeposits")
    public OperateResultDTO getUserDeposits() {
        Integer userid = userId.get();
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = userService.getUserDeposits(userid);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
            logger.error(e.toString());
        }
        return operateResultDTO;
    }
}
