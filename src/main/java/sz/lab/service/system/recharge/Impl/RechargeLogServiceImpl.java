package sz.lab.service.system.recharge.Impl;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sz.lab.config.constants.RoleEnum;
import sz.lab.dto.orga.UserDTO;
import sz.lab.dto.recharge.RechargeLogDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TablePagingDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.entity.system.RechargeLogEntity;
import sz.lab.mapper.orga.user.UserMapper;
import sz.lab.mapper.system.recharge.RechargeLogMapper;
import sz.lab.service.system.recharge.PaymentService;
import sz.lab.service.system.recharge.RechargeLogService;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RechargeLogServiceImpl extends ServiceImpl<RechargeLogMapper, RechargeLogEntity> implements RechargeLogService {

    @Resource
    private RechargeLogMapper rechargeLogMapper;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public TablePagingDTO getRechargeLogById(TableRequestDTO tableRequestDTO, String type, RoleEnum roles, UserDTO userDTO) {
        // 准备分页条件
        IPage<RechargeLogEntity> page = new Page<>(tableRequestDTO.getPageNo(), tableRequestDTO.getPageSize());
        // 构建查询条件
        LambdaQueryWrapper<RechargeLogEntity> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，例如根据用户名模糊查询
        JSONObject jsonObject = tableRequestDTO.getJsonParam();
        Integer userId = userDTO.getUserId();
        String userName = jsonObject.getString("userName");
        if (roles == null && userId != null) {
            queryWrapper.eq(RechargeLogEntity::getUserId, userId);
        }
        if(userName != null) {
            queryWrapper.like(RechargeLogEntity::getUserName, userName);
        }
        // 按时间倒序排列（最新的在最前面）
        queryWrapper.orderByDesc(RechargeLogEntity::getRechargeTime);

        // 分页查询
        IPage<RechargeLogEntity> rechargeLogpage = page(page, queryWrapper);
        TablePagingDTO tableRspDTO = new TablePagingDTO();
        tableRspDTO.setPageNo(tableRequestDTO.getPageNo());
        tableRspDTO.setPageSize(tableRequestDTO.getPageSize());
        //获取总页数
        tableRspDTO.setTotalCount(rechargeLogpage.getTotal());
        tableRspDTO.setData(rechargeLogpage.getRecords());
        return tableRspDTO;
    }


    /**
     * 新增充值记录
     * @return
     */
    @Override
    public OperateResultDTO insertRechargeLog(RechargeLogDTO rechargeLogDTO) {
        Integer userId = rechargeLogDTO.getUserId();
        BigInteger amount = rechargeLogDTO.getAmount();
        System.out.println("amount: "+ amount);
        UserEntity user = userMapper.selectUserById(userId);
        String userName = user.getUserName();

        LocalDateTime dateTime = LocalDateTime.now();

        RechargeLogEntity rechargeLogEntity = new RechargeLogEntity();
        rechargeLogEntity.setUserId(userId);
        rechargeLogEntity.setUserName(userName);
        rechargeLogEntity.setAmount(amount);
        rechargeLogEntity.setRechargeTime(dateTime);
        rechargeLogEntity.setStatus(0);
        rechargeLogMapper.insert(rechargeLogEntity);
        return new OperateResultDTO(true,"审核中",null);
    }

    /**
     * 更新充值记录
     * @param rechargeLogDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRechargeLog(RechargeLogDTO rechargeLogDTO) {
        Integer fromId = rechargeLogDTO.getFromId();
        Integer userId = rechargeLogDTO.getUserId();
        BigInteger amount = rechargeLogDTO.getAmount();
        Integer status = rechargeLogDTO.getStatus();
        Integer logId = rechargeLogDTO.getLogId();
        if(status == 1) {
            //1.完成区块链和数据库的充值
            try {
                paymentService.sendPayment(logId,userId,amount,fromId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else {
            rechargeLogMapper.updateRechargeLog(status,logId);
        }
    }

}
