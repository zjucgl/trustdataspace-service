package sz.lab.service.system.role.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TablePagingDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.dto.system.role.RoleDTO;
import sz.lab.entity.system.RoleEntity;
import sz.lab.entity.system.SystemPermissionEntity;
import sz.lab.mapper.system.menu.SystemMenuMapper;
import sz.lab.mapper.system.role.RoleMapper;
import sz.lab.service.system.permission.SystemPermissionService;
import sz.lab.service.system.role.RoleService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, RoleEntity> implements RoleService {

    @Resource
    private SystemMenuMapper menuMapper;
    @Resource
    private SystemPermissionService permissionService;
    @Override
    public OperateResultDTO pageList(TableRequestDTO tableRequestDTO) {
        //查询参数获取
        JSONObject param = tableRequestDTO.getJsonParam();
        String roleName= param.getString("roleName");
        // 启动自动分页
        PageHelper.startPage(tableRequestDTO.getPageNo(), tableRequestDTO.getPageSize());
        List<RoleEntity> roleEntityList = baseMapper.selectList(Wrappers.lambdaQuery(RoleEntity.class)
                .like(StrUtil.isNotBlank(roleName),RoleEntity::getRoleName,roleName));
        List<RoleDTO> list = entityToDTO(roleEntityList);
        //将数组封装到通用分页dto类
        TablePagingDTO pagingDTO = new TablePagingDTO(tableRequestDTO.getPageNo(), tableRequestDTO.getPageSize(),
                new PageInfo<>(roleEntityList).getTotal(), list);
        return new OperateResultDTO(true,"成功",pagingDTO);
    }

    @Override
    public OperateResultDTO getRoleMap() {
        JSONObject roleMap = new JSONObject();
        List<RoleEntity> roleEntityList = baseMapper.selectList(Wrappers.lambdaQuery(RoleEntity.class));
        for(RoleEntity entity:roleEntityList){
            roleMap.put(entity.getRoleCode(),entity.getRoleName());
        }
        return new OperateResultDTO(true,"成功",roleMap);
    }

    @Override
    public OperateResultDTO add(RoleDTO roleDTO) {
        RoleEntity roleEntity = new RoleEntity();
        BeanUtils.copyProperties(roleDTO,roleEntity);
        save(roleEntity);
        return new OperateResultDTO(true,"成功",true);
    }
    @Override
    public OperateResultDTO update(RoleDTO roleDTO) {
        RoleEntity roleEntity = new RoleEntity();
        BeanUtils.copyProperties(roleDTO,roleEntity);
        updateById(roleEntity);
        return new OperateResultDTO(true,"成功",true);
    }
    @Override
    public OperateResultDTO updatePermission(RoleDTO roleDTO) {
        return permissionService.updatePermission(roleDTO);
    }

    private List<RoleDTO> entityToDTO(List<RoleEntity> roleEntityList ){
        List<RoleDTO> list =new ArrayList<>();
        for(RoleEntity entity:roleEntityList){
            RoleDTO dto = new RoleDTO();
            BeanUtils.copyProperties(entity,dto);
            dto.setMenuList(getMenuList(dto.getRoleId()));
            list.add(dto);
        }
        return list;
    }
    private List<Integer> getMenuList(Integer roleId){
        List<Integer> list = permissionService.list(Wrappers.lambdaQuery(SystemPermissionEntity.class)
                .eq(SystemPermissionEntity::getRoleId,roleId))
                .stream()
                .map(SystemPermissionEntity::getMenuId)
                .collect(Collectors.toList());
        return list;
    }
}
