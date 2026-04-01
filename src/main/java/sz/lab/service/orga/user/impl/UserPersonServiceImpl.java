package sz.lab.service.orga.user.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.orga.user.UserPersonEntity;
import sz.lab.mapper.orga.user.UserPersonMapper;
import sz.lab.service.orga.user.UserPersonService;

import javax.annotation.Resource;

/**
 * <p>
 * 账号人员信息表，用于记录账号分配的人员id。 服务实现类
 * </p>
 */
@Service
public class UserPersonServiceImpl extends ServiceImpl<UserPersonMapper, UserPersonEntity> implements UserPersonService {
    @Resource
    private UserPersonMapper userPersonMapper;

    @Override
    public OperateResultDTO changeUserPerson(UserPersonEntity userPerson) {
        //一对一关系修改，先删除原有数据
        userPersonMapper.delete(Wrappers.lambdaQuery(UserPersonEntity.class)
                .eq(UserPersonEntity::getUserId, userPerson.getUserId()));
        save(userPerson);
        return new OperateResultDTO(true,"分配成功","分配成功");
    }
}
