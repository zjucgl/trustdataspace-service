package sz.lab.service.system.menu;

import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.system.SystemMenuEntity;
/**
 * <p>
 * 系统菜单表，用于记录菜单信息。 服务类
 * </p>
 */
public interface SystemMenuService extends IService<SystemMenuEntity> {
    /**
     * 获取动态路由
     */
    OperateResultDTO getAsyncRoutes(Integer userId);
    /**
     * 获取菜单树
     */
    OperateResultDTO getMenuTree();
}
