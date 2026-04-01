package sz.lab.controller.orga;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sz.lab.dto.orga.DeptDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.service.orga.dept.DeptService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Validated
@RequestMapping("/dept")
@RestController
public class DeptController {

    @Resource
    DeptService deptService;
    /**
     * @Description: 查询部门树
     **/
    @PostMapping("/tree")
    public OperateResultDTO tree() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = deptService.tree();
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 新增部门
     **/
    @PostMapping("/add")
    public OperateResultDTO add(@RequestBody DeptDTO input) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = deptService.add(input);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }

    /**
     * @Description: 修改部门
     **/
    @PostMapping("/modify")
    public OperateResultDTO update(@RequestBody DeptDTO input) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = deptService.update(input);
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 删除部门
     **/
    @PostMapping("/remove")
    public OperateResultDTO remove(@RequestBody Map<String,List<Integer>> map) {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = deptService.removeTreeNodes(map.get("ids"));
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }
    /**
     * @Description: 获取参与者数量
     **/
    @PostMapping("/participantCount")
    public OperateResultDTO getParticipantCount() {
        OperateResultDTO operateResultDTO = new OperateResultDTO();
        try {
            operateResultDTO = deptService.participantCount();
        } catch (Exception e) {
            operateResultDTO.setSuccess(false);
            operateResultDTO.setMessage(e.getMessage());
        }
        return operateResultDTO;
    }



}
