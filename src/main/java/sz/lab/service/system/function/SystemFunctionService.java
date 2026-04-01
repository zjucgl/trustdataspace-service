package sz.lab.service.system.function;

import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.system.SystemFunctionEntity;
/**
 * <p>
 * 系统功能表，用于记录系统功能信息。 服务类
 * </p>
 */
public interface SystemFunctionService extends IService<SystemFunctionEntity> {
    /**
     * @Description: 查询功能信息
     **/
    OperateResultDTO getFunction(String functionCode);
    /**
     * @Description: 开启或关闭功能
     **/
    OperateResultDTO changeFunctionStatus(String functionCode);
}
