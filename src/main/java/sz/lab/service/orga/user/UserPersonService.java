package sz.lab.service.orga.user;

import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.orga.user.UserPersonEntity;
/**
 * <p>
 * 账号人员信息表，用于记录账号分配的人员id。 服务类
 * </p>
 */
public interface UserPersonService extends IService<UserPersonEntity> {
    /**
     * @Description: 给账号分配人员
     **/
    OperateResultDTO changeUserPerson(UserPersonEntity userPerson);
}
