package sz.lab.service.system.log;

import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.system.TablePagingDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.dto.system.log.TraceLogDTO;
import sz.lab.entity.system.SysytemTraceLog;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author master
 * @since 2025-04-05
 */
public interface ISysyemTraceLogService extends IService<SysytemTraceLog> {


    /**
     * 保存日志
     * @param traceLogDTO traceLogDTO
     * @return boolean
     */
    boolean saveTraceLog(TraceLogDTO traceLogDTO);

    /**
     * 根据用户ID查询日志,分页返回
     * @param tableRequestDTO tableRequestDTO
     * @param type 登陆用户权限
     * @return TablePagingDTO
     */
    TablePagingDTO getTraceLogById(TableRequestDTO tableRequestDTO, String type);
}
