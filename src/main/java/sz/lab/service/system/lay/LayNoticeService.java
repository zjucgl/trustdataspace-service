package sz.lab.service.system.lay;

import sz.lab.dto.system.OperateResultDTO;
/**
 * <p>
 * 消息通知表，用于通知用户信息。 服务类
 * </p>
 */
public interface LayNoticeService {
    /**
     * @Description: 判断是否需要密码重置
     **/
    OperateResultDTO checkPwdUpdate(Integer userId);
}
