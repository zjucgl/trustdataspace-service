package sz.lab.service.system.function.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.system.SystemFunctionEntity;
import sz.lab.mapper.system.function.SystemFunctionMapper;
import sz.lab.service.system.function.SystemFunctionService;

import javax.annotation.Resource;

@Service
public class SystemFunctionServiceImpl extends ServiceImpl<SystemFunctionMapper, SystemFunctionEntity> implements SystemFunctionService {

    @Resource
    private SystemFunctionMapper systemFunctionMapper;

    @Override
    public OperateResultDTO getFunction(String functionCode) {
        SystemFunctionEntity function = new SystemFunctionEntity();
        if(StrUtil.isNotBlank(functionCode)){
            SystemFunctionEntity entity = systemFunctionMapper.selectFunctionByCode(functionCode);
            BeanUtils.copyProperties(entity,function);
            return new OperateResultDTO(true,"查询成功",function);
        }

        return new OperateResultDTO(false,"代号不存在",function);
    }

    @Override
    public OperateResultDTO changeFunctionStatus(String functionCode) {
        if(StrUtil.isNotBlank(functionCode)){
            SystemFunctionEntity functionEntity = systemFunctionMapper.selectFunctionByCode(functionCode);
            functionEntity.setFunctionStatus(1-functionEntity.getFunctionStatus());
            systemFunctionMapper.updateFunctionStatus(functionEntity);
            return new OperateResultDTO(true,"修改成功",true);
        }
        return new OperateResultDTO(false,"代号不存在",false);
    }
}
