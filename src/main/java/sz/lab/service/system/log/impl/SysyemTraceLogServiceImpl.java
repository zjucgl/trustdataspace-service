package sz.lab.service.system.log.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import sz.lab.dto.system.TablePagingDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.dto.system.log.TraceLogDTO;
import sz.lab.entity.system.SysytemTraceLog;
import sz.lab.mapper.system.log.SysyemTraceLogMapper;
import sz.lab.service.system.log.ISysyemTraceLogService;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author master
 * @since 2025-04-05
 */
@Service
public class SysyemTraceLogServiceImpl extends ServiceImpl<SysyemTraceLogMapper, SysytemTraceLog> implements ISysyemTraceLogService {

    @Resource
    private SysyemTraceLogMapper sysLogMapper;
    @Override
    public boolean saveTraceLog(TraceLogDTO traceLogDTO) {
        if (traceLogDTO != null) {
            // 将DTO对象转换为实体对象
            SysytemTraceLog traceLog = new SysytemTraceLog();
            BeanUtils.copyProperties(traceLogDTO, traceLog);
            return sysLogMapper.insert(traceLog) > 0;
        }
        return false;
    }

    @Override
    public TablePagingDTO getTraceLogById(TableRequestDTO tableRequestDTO, String type) {
        // 准备分页条件
        IPage<SysytemTraceLog> page = new Page<>(tableRequestDTO.getPageNo(), tableRequestDTO.getPageSize());
        // 构建查询条件
        LambdaQueryWrapper<SysytemTraceLog> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，例如根据用户名模糊查询
        JSONObject jsonObject = tableRequestDTO.getJsonParam();
        Long userId = jsonObject.getLong("userId");
        String logContent = jsonObject.getString("logContent");
        String logStatus = jsonObject.getString("logStatus");
        if (userId != null) {
            queryWrapper.eq(SysytemTraceLog::getUserId, userId).orderByDesc(SysytemTraceLog::getGmtCreate);

        }
        if (logStatus != null) {
            queryWrapper.like(SysytemTraceLog::getLogStatus, logStatus).orderByDesc(SysytemTraceLog::getGmtCreate);
        }
        if (logContent != null) {
            queryWrapper.like(SysytemTraceLog::getLogContent, logContent).orderByDesc(SysytemTraceLog::getGmtCreate);
        }
        // 分页查询
        IPage<SysytemTraceLog> tracelogpage = page(page, queryWrapper);
        TablePagingDTO tableRspDTO = new TablePagingDTO();
        tableRspDTO.setPageNo(tableRequestDTO.getPageNo());
        tableRspDTO.setPageSize(tableRequestDTO.getPageSize());
        //获取总页数
//        System.out.println(tracelogpage.getTotal());
        tableRspDTO.setTotalCount(tracelogpage.getTotal());
        tableRspDTO.setData(tracelogpage.getRecords());
        return tableRspDTO;
    }
}
