package sz.lab.service.system.ethnode.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.admin.AdminPeers;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.system.SystemNodeinfo;
import sz.lab.mapper.system.ethnode.SystemNodeinfoMapper;
import sz.lab.service.system.ethnode.ISystemNodeinfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import sz.lab.utils.contract.Web3jClient;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author master
 * @since 2025-04-15
 */
@Service
public class SystemNodeinfoServiceImpl extends ServiceImpl<SystemNodeinfoMapper, SystemNodeinfo> implements ISystemNodeinfoService {

    @Resource
    private SystemNodeinfoMapper systemNodeinfoMapper;
    Web3j web3j = Web3jClient.getWeb3jInstance();

    @Override
    //获取区块高度
    public BigInteger getBlockNumber() throws IOException {
        return web3j.ethBlockNumber().send().getBlockNumber();
    }

    @Override
    public OperateResultDTO getMinerList() throws IOException {
        AdminPeers peers = getAndSyncNodeInfo();
        if (peers != null) {
            System.out.println("节点数量: " + peers.getResult().size());
            SystemNodeinfo systemNodeinfo = new SystemNodeinfo();
            for (AdminPeers.Peer peer : peers.getResult()) {
                systemNodeinfo.setBlockNumber(getBlockNumber());
                systemNodeinfo.setEnCode(peer.getEnode());
                systemNodeinfo.setNodeId(peer.getId());
                // 去除网络端口号
                systemNodeinfo.setHeadId(peer.getNetwork().getRemoteAddress().split(":")[0]);
//                systemNodeinfo.setHeadId(peer.getNetwork().getRemoteAddress());
                systemNodeinfo.setNodeName(peer.getName());
                systemNodeinfo.setGmtCreate(LocalDateTime.now());
                systemNodeinfo.setIsDelete(0);
                //当数据库已有数据不存储
                if (getOne(new QueryWrapper<SystemNodeinfo>().eq("en_code", peer.getEnode())) != null) {
                    if (update(systemNodeinfo, new QueryWrapper<SystemNodeinfo>().eq("en_code", peer.getEnode()))) {
                        System.out.println("节点信息更新成功");
                    } else {
                        return new OperateResultDTO(false, "节点信息更新失败", null);
                    }
                    continue;
                }
                if (save(systemNodeinfo)) {
                    System.out.println("节点信息保存成功");
                } else {
                    return new OperateResultDTO(false, "节点信息保存失败", null);
                }
            }
            //更新完毕,返回数据库结果
            List<SystemNodeinfo> nodeinfo = systemNodeinfoMapper.selectList(new LambdaQueryWrapper<>(SystemNodeinfo.class));
            return new OperateResultDTO(true, "获取节点信息成功", nodeinfo);
        }
        return new OperateResultDTO(false, "获取节点信息失败", null);
    }

    @Override
    public OperateResultDTO getNodeList(String nodeName) throws IOException {
        LambdaQueryWrapper<SystemNodeinfo> queryWrapper = new LambdaQueryWrapper<>();
        if (nodeName != null) {
            queryWrapper.like(SystemNodeinfo::getNodeName, nodeName);
        } else {
            queryWrapper.eq(SystemNodeinfo::getIsDelete, 0);
        }
        List<SystemNodeinfo> nodeinfo = systemNodeinfoMapper.selectList(queryWrapper);
        return new OperateResultDTO(true, "获取节点信息成功", nodeinfo);
    }

    @Override
    public OperateResultDTO addNodeInfo(String nodeEnode) {
        return new OperateResultDTO(false, "enode格式不对", null);
    }

    //获取并同步节点信息
    private AdminPeers getAndSyncNodeInfo() throws IOException {
        AdminPeers peers = web3j.adminPeers().send();
        System.out.println("节点信息: " + peers);
        return peers;
    }
}
