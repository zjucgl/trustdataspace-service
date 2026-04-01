package sz.lab.service.orga.dept;


import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.orga.DeptDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.orga.dept.DeptEntity;

import java.util.List;

/**
 * <p>
 * 部门（实验室）信息表，公司ID和部门ID在配置文件中设置，以实验室为基本使用单位，设计实验室支持二级组织架构，不支持三级，程序中提示加锁定 服务类
 * </p>
 */
public interface DeptService extends IService<DeptEntity> {
    /**
     * @Description: 查询部门树
     **/
    OperateResultDTO tree();
    /**
     * @Description: 新增部门
     **/
    OperateResultDTO add(DeptDTO deptDTO);
    /**
     * @Description: 修改部门
     **/
    OperateResultDTO update(DeptDTO deptDTO);
    /**
     * @Description: 删除部门
     **/
    OperateResultDTO removeTreeNodes(List<Integer> ids);
    /**
     * @Description: 获取参与者数量
     **/
    OperateResultDTO participantCount();
}
