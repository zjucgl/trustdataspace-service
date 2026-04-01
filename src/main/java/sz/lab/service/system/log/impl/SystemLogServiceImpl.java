package sz.lab.service.system.log.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TablePagingDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.dto.system.log.LogDTO;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.entity.system.SystemLogEntity;
import sz.lab.mapper.orga.user.UserMapper;
import sz.lab.mapper.system.log.SystemLogMapper;
import sz.lab.service.system.log.SystemLogService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SystemLogServiceImpl extends ServiceImpl<SystemLogMapper, SystemLogEntity> implements SystemLogService {
    @Resource
    private UserMapper userMapper;

    @Override
    public OperateResultDTO pageList(TableRequestDTO tableRequestDTO) {
        //查询参数获取
        JSONObject param = tableRequestDTO.getJsonParam();
        String logContent= param.getString("logContent");
        String logStatus = param.getString("logStatus");
        Integer userId = param.getInteger("userId");
        // 启动自动分页
        PageHelper.startPage(tableRequestDTO.getPageNo(), tableRequestDTO.getPageSize());
        //查询日志列表
        List<SystemLogEntity> entityList = baseMapper.selectList(Wrappers.lambdaQuery(SystemLogEntity.class)
                .like(StrUtil.isNotBlank(logContent),SystemLogEntity::getLogContent,logContent)
                .eq(StrUtil.isNotBlank(logStatus),SystemLogEntity::getLogStatus,logStatus)
                .eq(userId!=null,SystemLogEntity::getUserId,userId)
                .orderByDesc(SystemLogEntity::getGmtCreate));
        //其他表
        Map<Integer,String> userMap = getUserMap(entityList.stream().map(SystemLogEntity::getUserId).collect(Collectors.toList()));
        //赋值
        List<LogDTO> list = entityToDTO(entityList,userMap);
        //将数组封装到通用分页dto类
        TablePagingDTO pagingDTO = new TablePagingDTO(tableRequestDTO.getPageNo(), tableRequestDTO.getPageSize(),
                new PageInfo<>(entityList).getTotal(), list);
        //返回通用操作返回结果类
        return new OperateResultDTO(true,"成功",pagingDTO);
    }
    private List<LogDTO> entityToDTO(List<SystemLogEntity> entityList,Map<Integer,String> userMap){
        List<LogDTO> list = new ArrayList<>();
        for(SystemLogEntity entity:entityList){
            LogDTO dto = new LogDTO();
            //对相同名称的字段进行赋值，左到右
            BeanUtils.copyProperties(entity,dto);
            if(entity.getUserId()!=null){
                if(userMap.containsKey(entity.getUserId())){
                    dto.setUserName(userMap.get(entity.getUserId()));
                }
            }
            list.add(dto);
        }
        return list;
    }
    private Map<Integer,String> getUserMap(List<Integer> userIds){
        if(userIds.isEmpty()){
            return Collections.emptyMap();
        }
        return userMapper.selectList(Wrappers.lambdaQuery(UserEntity.class)
                .in(UserEntity::getUserId,userIds))
                .stream()
                .collect(Collectors.toMap(UserEntity::getUserId,UserEntity::getUserName));
    }

}
