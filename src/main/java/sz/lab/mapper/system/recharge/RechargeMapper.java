package sz.lab.mapper.system.recharge;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigInteger;

@Mapper
public interface RechargeMapper {

    @Update("UPDATE orga_user SET user_deposits = COALESCE(user_deposits, 0) + #{amount}, " +
            "user_deposits_extra = COALESCE(user_deposits_extra, 0) + #{extra} WHERE user_id = #{userId}")
    void recharge(@Param("userId") Integer userId, @Param("amount") BigInteger amount, @Param("extra") BigInteger extra);


}
