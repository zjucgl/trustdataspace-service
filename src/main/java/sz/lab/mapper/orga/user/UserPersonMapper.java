package sz.lab.mapper.orga.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sz.lab.entity.orga.user.UserPersonEntity;

/**
 * <p>
 * 账号人员信息表，用于记录账号分配的人员id。 Mapper 接口
 * </p>
 */
@Mapper
public interface UserPersonMapper extends BaseMapper<UserPersonEntity> {
}
