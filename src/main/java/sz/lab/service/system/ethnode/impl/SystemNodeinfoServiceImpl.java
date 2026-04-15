package sz.lab.service.system.ethnode.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.system.SystemNodeinfo;
import sz.lab.mapper.system.ethnode.SystemNodeinfoMapper;
import sz.lab.service.system.ethnode.ISystemNodeinfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SystemNodeinfoServiceImpl extends ServiceImpl<SystemNodeinfoMapper, SystemNodeinfo> implements ISystemNodeinfoService {

    @Resource
    private SystemNodeinfoMapper systemNodeinfoMapper;

    @Override
    public OperateResultDTO getNodeList(String nodeName) {
        LambdaQueryWrapper<SystemNodeinfo> queryWrapper = new LambdaQueryWrapper<>();
        if (nodeName != null) {
            queryWrapper.like(SystemNodeinfo::getNodeName, nodeName);
        } else {
            queryWrapper.eq(SystemNodeinfo::getIsDelete, 0);
        }
        List<SystemNodeinfo> nodeinfo = systemNodeinfoMapper.selectList(queryWrapper);
        return new OperateResultDTO(true, "获取节点信息成功", nodeinfo);
    }
}
