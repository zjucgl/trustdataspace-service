package sz.lab.service.system.menu.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import sz.lab.dto.login.route.RouteDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.menu.MenuTreeDTO;
import sz.lab.entity.orga.user.UserRoleEntity;
import sz.lab.entity.system.RoleEntity;
import sz.lab.entity.system.SystemMenuEntity;
import sz.lab.entity.system.SystemPermissionEntity;
import sz.lab.mapper.orga.user.UserRoleMapper;
import sz.lab.mapper.system.menu.SystemMenuMapper;
import sz.lab.mapper.system.permission.SystemPermissionMapper;
import sz.lab.mapper.system.role.RoleMapper;
import sz.lab.service.system.menu.SystemMenuService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统菜单表表，用于记录菜单信息。 服务实现类
 * </p>
 */
@Service
public class SystemMenuServiceImpl extends ServiceImpl<SystemMenuMapper, SystemMenuEntity> implements SystemMenuService {

    @Resource
    private SystemMenuMapper menuMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private SystemPermissionMapper permissionMapper;
    @Resource
    private RoleMapper roleMapper;

    @Override
    public OperateResultDTO getAsyncRoutes(Integer userId) {
        //获取角色权限菜单列表
        Map<Integer,String> roleMap = getRoleMap();
        List<Integer> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        //获取菜单
        List<SystemMenuEntity> systemMenuEntityList = menuMapper.getAllMenus();
        //一级菜单数组
        List<SystemMenuEntity> fatherMenuList = systemMenuEntityList
                .stream()
                .filter(menu -> menu.getMenuType() == 1)
                .collect(Collectors.toList());
        //二级菜单map，key为menuFather值，value为对应的数组
        Map<Integer,List<SystemMenuEntity>> childrenMenuList = systemMenuEntityList
                .stream()
                .collect(Collectors.groupingBy(SystemMenuEntity::getMenuFather));
        //组合父菜单和子菜单数据
        List<RouteDTO> routeList = new ArrayList<>();
        for(SystemMenuEntity entity:fatherMenuList){
            RouteDTO fatherRoute = getRouteDTO(entity,roleMap);
            //如果该父菜单存在子菜单数组则赋值
            if(childrenMenuList.containsKey(entity.getMenuId())){
                List<JSONObject> childrenRouteList = new ArrayList<>();
                for(SystemMenuEntity children:childrenMenuList.get(entity.getMenuId())){
                    JSONObject childrenRoute = getChildrenRouteDTO(entity,children,roleMap);
                    childrenRouteList.add(childrenRoute);
                }
                //赋值
                fatherRoute.setChildren(childrenRouteList);
            }
            routeList.add(fatherRoute);
        }

        return new OperateResultDTO(true,"成功", routeList);
    }
    @Override
    public OperateResultDTO getMenuTree(){
        List<SystemMenuEntity> menuEntityList = menuMapper.getAllMenus();
        //一级菜单数组
        List<SystemMenuEntity> fatherMenuList = menuEntityList
                .stream()
                .filter(menu -> menu.getMenuType() == 1)
                .collect(Collectors.toList());
        //二级菜单map，key为menuFather值，value为对应的数组
        Map<Integer,List<SystemMenuEntity>> childrenMenuList = menuEntityList
                .stream()
                .collect(Collectors.groupingBy(SystemMenuEntity::getMenuFather));
        //组合父子菜单
        List<MenuTreeDTO> list =new ArrayList<>();
        for(SystemMenuEntity entity:fatherMenuList){
            MenuTreeDTO fatherTree = getTreeDTO(entity);
            //如果该父菜单存在子菜单数组则赋值
            if(childrenMenuList.containsKey(entity.getMenuId())){
                List<MenuTreeDTO> childrenTreeList = new ArrayList<>();
                for(SystemMenuEntity children:childrenMenuList.get(entity.getMenuId())){
                    MenuTreeDTO childrenTree = getTreeDTO(children);
                    childrenTreeList.add(childrenTree);
                }
                //赋值
                fatherTree.setChildren(childrenTreeList);
            }
            //添加到数组
            list.add(fatherTree);
        }
        return new OperateResultDTO(true,"成功", list);
    }
    private MenuTreeDTO getTreeDTO(SystemMenuEntity entity){
        MenuTreeDTO dto = new MenuTreeDTO();
        dto.setMenuId(entity.getMenuId());
        dto.setMenuName(entity.getMenuName());
        dto.setChildren(new ArrayList<>());
        return dto;
    }
    private RouteDTO getRouteDTO(SystemMenuEntity entity,Map<Integer,String> roleMap){
        RouteDTO routeDTO = new RouteDTO();
        routeDTO.setPath("/"+entity.getMenuCode());
        routeDTO.setMeta(getMetaJson(entity,roleMap));
        return routeDTO;
    }
    private JSONObject getChildrenRouteDTO(SystemMenuEntity father,SystemMenuEntity children,Map<Integer,String> roleMap){
        JSONObject routeJson = new JSONObject();
        routeJson.put("path", "/"+father.getMenuCode() + "/"+children.getMenuCode()+"/index");
        routeJson.put("component", father.getMenuCode() + "/"+children.getMenuCode()+"/index");
        routeJson.put("name", father.getMenuCode() + children.getMenuCode());
        //额外字段
        JSONObject metaJson = getMetaJson(children,roleMap);
        metaJson.put("showParent", true);

        routeJson.put("meta", metaJson);
        return routeJson;
    }
    private JSONObject getMetaJson(SystemMenuEntity entity,Map<Integer,String> roleMap){
        JSONObject metaJson = new JSONObject();
        metaJson.put("title", entity.getMenuName());
        metaJson.put("icon", entity.getMenuIcon());
        //只有父菜单才有rank属性，子菜单赋值rank属性会导致路由定位错误
        if(entity.getMenuType()==1){
            metaJson.put("rank", entity.getMenuRank());
        }else{
            List<Integer> roleList = getRoleList(entity.getMenuId());
            List<String> roles = getRoleCodeList(roleList,roleMap);
            metaJson.put("roles", roles);
        }
//        if(entity.getMenuRoles()!=null){
//            metaJson.put("roles", entity.getMenuRoles());
//        }
        if(entity.getMenuShow()==0){
            metaJson.put("showLink", false);
        }
        return metaJson;
    }
    private List<String> getRoleCodeList(List<Integer> roleList,Map<Integer,String> roleMap){
        List<String> roles = new ArrayList<>();
        for(Integer roleId:roleList){
            if(roleMap.containsKey(roleId)){
                roles.add(roleMap.get(roleId));
            }
        }
        if(roles.isEmpty()){
            roles.add("none");
        }
        return roles;
    }
    private List<Integer> getRoleList(Integer menuId){
        return permissionMapper.selectPermissionRoleIdsByMenuId(menuId);
    }
    private Map<Integer,String> getRoleMap(){
        return roleMapper.selectRoleList()
                .stream()
                .collect(Collectors.toMap(RoleEntity::getRoleId,RoleEntity::getRoleCode));
    }
}
