package sz.lab.service.orga.user.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.orga.user.UserRoleEntity;
import sz.lab.mapper.orga.user.UserRoleMapper;
import sz.lab.service.orga.user.UserRoleService;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ckd
 * @since 2023-11-24
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRoleEntity> implements UserRoleService {
    @Resource
    private UserRoleMapper userRoleMapper;

    @Override
    public OperateResultDTO updateUserRoleCode(Integer userId, List<Integer> roleIdList){
           //userRoleMapper.delete(Wrappers.lambdaQuery(UserRoleEntity.class).eq(UserRoleEntity::getUserId,userId));
           userRoleMapper.deleteByUserId(userId);
//           userRoleMapper.addUserRoleId(roleIdList, userId);
           for(Integer roleId:roleIdList){
               UserRoleEntity entity = new UserRoleEntity();
               entity.setUserId(userId);
               entity.setRoleId(roleId);
               //userRoleMapper.insert(entity);
               userRoleMapper.insertUserRole(entity);
           }
           return new OperateResultDTO(true,"成功",null);
    }
}
