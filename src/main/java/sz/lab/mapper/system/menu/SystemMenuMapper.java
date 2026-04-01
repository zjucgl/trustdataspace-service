package sz.lab.mapper.system.menu;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import sz.lab.entity.system.SystemMenuEntity;

import java.util.List;

/**
 * <p>
 * 系统菜单表，用于记录菜单信息。 Mapper 接口
 * </p>
 */
@Mapper
public interface SystemMenuMapper extends BaseMapper<SystemMenuEntity> {

    @Select({"SELECT menu_id, menu_code, menu_name, menu_icon, " +
            "menu_rank, menu_father, menu_type, menu_show " +
            "FROM system_menu"
            })
    List<SystemMenuEntity> getAllMenus();

}
