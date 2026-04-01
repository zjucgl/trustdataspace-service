package sz.lab.mapper.system.recharge;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import sz.lab.entity.system.RechargeLogEntity;


@Mapper
public interface RechargeLogMapper extends BaseMapper<RechargeLogEntity> {

    @Update("update recharge_log set from_id = #{rechargeLogEntity.fromId}," +
            "from_eth_account = #{rechargeLogEntity.fromEthAccount}," +
            "user_eth_account = #{rechargeLogEntity.userEthAccount}," +
            "tx_hash = #{rechargeLogEntity.txHash}, " +
            "status = #{rechargeLogEntity.status} where id = #{rechargeLogEntity.id}")
    void update(@Param("rechargeLogEntity") RechargeLogEntity rechargeLogEntity);

    /**
     * 没有通过的
     * @param
     */
    @Update("update recharge_log set status = #{status} where id = #{logId}")
    void updateRechargeLog(@Param("status") Integer status, @Param("logId") Integer logId);
}
