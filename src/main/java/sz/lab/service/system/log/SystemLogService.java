package sz.lab.service.system.log;

import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.entity.system.SystemLogEntity;
/**
 * <p>
 * 系统日志表，用于记录系统信息。 服务类
 * </p>
 */
public interface SystemLogService extends IService<SystemLogEntity> {
    /**
     * @Description: 分页查询
     **/
    OperateResultDTO pageList(TableRequestDTO tableRequestDTO);
}
