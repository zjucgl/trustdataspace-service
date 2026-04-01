package sz.lab.service.orga.user;

import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.orga.user.UserRoleEntity;

import java.util.List;

/**
 * <p>
 *  用户角色表，用于记录用户的角色信息。 服务类
 * </p>
 */

public interface UserRoleService extends IService<UserRoleEntity> {
    /**
     * @Description: 修改用户角色
     **/
    OperateResultDTO updateUserRoleCode(Integer userId, List<Integer> roleIdList);
}
