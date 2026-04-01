package sz.lab.service.system.recharge;


import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.config.constants.RoleEnum;
import sz.lab.dto.orga.UserDTO;
import sz.lab.dto.recharge.RechargeLogDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TablePagingDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.entity.system.RechargeLogEntity;

import java.math.BigInteger;


public interface RechargeLogService extends IService<RechargeLogEntity> {
    /**
     * 根据id查询充值记录
     * @param tableRequestDTO
     * @param type
     * @return
     */
    TablePagingDTO getRechargeLogById(TableRequestDTO tableRequestDTO, String type, RoleEnum roles, UserDTO userDTO);

    /**
     * 插入充值记录
     */
    OperateResultDTO insertRechargeLog(RechargeLogDTO rechargeLogDTO);


    /**
     * 更新充值记录
     * @param rechargeLogDTO
     */
    void updateRechargeLog(RechargeLogDTO rechargeLogDTO);
}
