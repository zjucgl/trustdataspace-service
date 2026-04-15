package sz.lab.service.system.asset.impl;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.asset.CatalogAssetDTO;
import sz.lab.dto.system.asset.IAssetDTO;
import sz.lab.entity.orga.dept.DeptEntity;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.entity.orga.user.UserRoleEntity;
import sz.lab.entity.system.IAssetEntity;
import sz.lab.mapper.orga.dept.DeptMapper;
import sz.lab.mapper.orga.user.UserMapper;
import sz.lab.mapper.orga.user.UserRoleMapper;
import sz.lab.mapper.system.asset.IAssetMapper;
import sz.lab.service.system.asset.AssetService;
import sz.lab.utils.Base64Utils;
import sz.lab.utils.OssUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IAssetServiceImpl extends ServiceImpl<IAssetMapper, IAssetEntity> implements AssetService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private DeptMapper deptMapper;
    @Resource
    private IAssetMapper iAssetMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Autowired
    private OssUtils ossUtils;

    @Override
    public OperateResultDTO updateAssetprice(String assetId ,long newPrice) {
        UpdateWrapper<IAssetEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("system_asset_id", assetId);
        IAssetEntity asset = new IAssetEntity();
        asset.setAssetPrice(newPrice);
        boolean updateResult = baseMapper.update(asset, updateWrapper) > 0;
        // 根据更新结果返回操作结果
        if (updateResult) {
            return new OperateResultDTO(true, "资产价格修改成功",null);
        } else {
            return new OperateResultDTO(false, "资产价格修改失败",null);
        }
    }

    @Override
    public OperateResultDTO updateHasPolicy(String assetId, Integer hasPolicy) {
        IAssetEntity iAssetEntity = baseMapper.selectOne(Wrappers.lambdaQuery(IAssetEntity.class)
                .eq(IAssetEntity::getAssetId, assetId)
                .eq(IAssetEntity::getIsDelete, 0)
                .last("LIMIT 1"));
        iAssetEntity.setHasPolicy(hasPolicy);
        updateById(iAssetEntity);
        return new OperateResultDTO(true, "修改成功",null);
    }

    @Override
    public OperateResultDTO getAssetPricebyId(String assetId) {
        IAssetEntity iAssetEntity = baseMapper.selectOne(Wrappers.lambdaQuery(IAssetEntity.class)
                        .eq(IAssetEntity::getAssetId, assetId)
                        .eq(IAssetEntity::getIsDelete, 0));
        System.out.println(iAssetEntity.getAssetDescription());
        if (iAssetEntity == null) {
            return new OperateResultDTO(false, "资产不存在",null);
        } else if (iAssetEntity.getIsDelete() == 1) {
            return new OperateResultDTO(false, "资产已被删除",null);
        } else {
            IAssetDTO iAssetDTO = new IAssetDTO();
            iAssetDTO.setAssetPrice(iAssetEntity.getAssetPrice());
            iAssetDTO.setId(iAssetEntity.getId());
            iAssetDTO.setAssetDescription(iAssetEntity.getAssetDescription());
            iAssetDTO.setAssetQuantity(iAssetEntity.getAssetQuantity());
            iAssetDTO.setAssetId(iAssetEntity.getAssetId());
            iAssetDTO.setUserId(iAssetEntity.getUserId());
            return new OperateResultDTO(true, "查询成功",iAssetDTO);
        }
    }

    @Override
    public OperateResultDTO createAsset(IAssetDTO input) {
        IAssetEntity iAssetEntity = new IAssetEntity();
        BeanUtils.copyProperties(input, iAssetEntity);
        iAssetEntity.setIsDelete(0);
        iAssetEntity.setIsFirst(1);
        boolean saveResult = baseMapper.insert(iAssetEntity) > 0;
        if (saveResult) {
            return new OperateResultDTO(true, "资产创建成功",null);
        } else {
            return new OperateResultDTO(false, "资产创建失败",null);
        }
    }

    @Override
    public OperateResultDTO deleteAsset(IAssetDTO input) {
        boolean res = baseMapper.delete(Wrappers.lambdaQuery(IAssetEntity.class)
                .eq(IAssetEntity::getAssetId,input.getAssetId())) > 0;
        if(res){
            return new OperateResultDTO(true, "资产删除成功",null);
        }else{
            return new OperateResultDTO(false, "资产删除失败",null);
        }
    }

    @Override
    public OperateResultDTO queryAsset() {
        List<IAssetEntity> iAssetEntities = baseMapper.selectList(Wrappers.lambdaQuery(IAssetEntity.class)
                .eq(IAssetEntity::getIsDelete, 0)
                .orderByDesc(IAssetEntity::getId));
        if (iAssetEntities.isEmpty()) {
            return new OperateResultDTO(false, "资产不存在",new ArrayList<>());
        }
        // 根据用户ID列表查询用户
        List<Integer> userIds = iAssetEntities.stream().map(IAssetEntity::getUserId).collect(Collectors.toList());
        List<UserEntity> userEntities = userMapper.selectListByUserIds(userIds);
        Map<Integer, UserEntity> userMap = userEntities
                .stream().collect(Collectors.toMap(UserEntity::getUserId, user->user));
        // 根据部门ID列表查询部门
        List<Integer> deptIds = userEntities.stream().map(UserEntity::getDeptId).collect(Collectors.toList());
        List<DeptEntity> deptEntities = deptMapper.selectListByDeptIds(deptIds);
        Map<Integer, String> deptMap = deptEntities.stream()
                .collect(Collectors.toMap(DeptEntity::getDeptId, DeptEntity::getDeptName));


        List<IAssetDTO> iAssetDTOS = iAssetEntities.stream().map(iAssetEntity -> {
            IAssetDTO iAssetDTO = new IAssetDTO();
            iAssetDTO.setId(iAssetEntity.getId());
            BeanUtils.copyProperties(iAssetEntity, iAssetDTO);
            if(userMap.containsKey(iAssetEntity.getUserId())){
                UserEntity userEntity = userMap.get(iAssetEntity.getUserId());
                iAssetDTO.setUserName(userEntity.getUserName());
                if(deptMap.containsKey(userEntity.getDeptId())){
                    iAssetDTO.setDeptName(deptMap.get(userEntity.getDeptId()));
                }
            }
            return iAssetDTO;
        }).collect(Collectors.toList());
        return new OperateResultDTO(true, "查询成功",iAssetDTOS);
    }

    @Override
    public OperateResultDTO queryAssetByCatalog() {
        List<IAssetEntity> iAssetEntities = baseMapper.selectList(Wrappers.lambdaQuery(IAssetEntity.class)
                .eq(IAssetEntity::getIsDelete, 0)
                .eq(IAssetEntity::getHasPolicy,1)
                .orderByDesc(IAssetEntity::getId));
        if (iAssetEntities.isEmpty()) {
            return new OperateResultDTO(false, "资产不存在",new ArrayList<>());
        }
        // 根据用户ID列表查询用户
        List<Integer> userIds = iAssetEntities.stream().map(IAssetEntity::getUserId).collect(Collectors.toList());
        List<UserEntity> userEntities = userMapper.selectListByUserIds(userIds);
        Map<Integer, UserEntity> userMap = userEntities
                .stream().collect(Collectors.toMap(UserEntity::getUserId, user->user));
        // 根据部门ID列表查询部门
        List<Integer> deptIds = userEntities.stream().map(UserEntity::getDeptId).collect(Collectors.toList());
        List<DeptEntity> deptEntities = deptMapper.selectListByDeptIds(deptIds);
        Map<Integer, String> deptMap = deptEntities.stream()
                .collect(Collectors.toMap(DeptEntity::getDeptId, DeptEntity::getDeptName));


        List<IAssetDTO> iAssetDTOS = iAssetEntities.stream().map(iAssetEntity -> {
            IAssetDTO iAssetDTO = new IAssetDTO();
            iAssetDTO.setId(iAssetEntity.getId());
            BeanUtils.copyProperties(iAssetEntity, iAssetDTO);
            if(userMap.containsKey(iAssetEntity.getUserId())){
                UserEntity userEntity = userMap.get(iAssetEntity.getUserId());
                iAssetDTO.setUserName(userEntity.getUserName());
                if(deptMap.containsKey(userEntity.getDeptId())){
                    iAssetDTO.setDeptName(deptMap.get(userEntity.getDeptId()));
                }
            }
            return iAssetDTO;
        }).collect(Collectors.toList());
        return new OperateResultDTO(true, "查询成功",iAssetDTOS);
    }

    @Override
    public OperateResultDTO queryAssetByUserId(Integer userId) {
        List<IAssetEntity> iAssetEntities = baseMapper.selectList(Wrappers.lambdaQuery(IAssetEntity.class)
                .eq(IAssetEntity::getUserId, userId));
        UserEntity userEntity = userMapper.selectById(userId);
        // 根据部门ID列表查询部门
        List<Integer> deptIds = new ArrayList<>();
        deptIds.add(userEntity.getDeptId());
        List<DeptEntity> deptEntities = deptMapper.selectListByDeptIds(deptIds);
        Map<Integer, String> deptMap = deptEntities.stream()
                .collect(Collectors.toMap(DeptEntity::getDeptId, DeptEntity::getDeptName));
        if (iAssetEntities.isEmpty()) {
            return new OperateResultDTO(false, "资产不存在",null);
        }
        List<IAssetDTO> iAssetDTOS = iAssetEntities.stream().map(iAssetEntity -> {
            IAssetDTO iAssetDTO = new IAssetDTO();
            iAssetDTO.setId(iAssetEntity.getId());
            BeanUtils.copyProperties(iAssetEntity, iAssetDTO);
            iAssetDTO.setUserName(userEntity.getUserName());
            if(deptMap.containsKey(userEntity.getDeptId())){
                iAssetDTO.setDeptName(deptMap.get(userEntity.getDeptId()));
            }
            return iAssetDTO;
        }).collect(Collectors.toList());
        return new OperateResultDTO(true, "查询成功",iAssetDTOS);
    }

    @Override
    public OperateResultDTO queryAssetByAssetId(String assetId) {
        IAssetEntity iAssetEntity = baseMapper.selectOne(Wrappers.lambdaQuery(IAssetEntity.class)
                        .eq(IAssetEntity::getAssetId, assetId)
                        .eq(IAssetEntity::getIsDelete, 0)
                .last("LIMIT 1"));
        CatalogAssetDTO dto = new CatalogAssetDTO();
        dto.setId(iAssetEntity.getAssetId());
        dto.setDescription(iAssetEntity.getAssetDescription());
        dto.setBaseUrl(iAssetEntity.getPath());
        return new OperateResultDTO(true, "查询成功",dto);
    }

    @Override
    public OperateResultDTO queryAssetByBoard(Integer userId) {
        UserRoleEntity userRoleEntity = userRoleMapper.selectOne(Wrappers.lambdaQuery(UserRoleEntity.class)
                .eq(UserRoleEntity::getUserId, userId)
                .last("LIMIT 1"));
        List<IAssetEntity> iAssetEntities;
        if (userRoleEntity.getRoleId() == 1) {
            iAssetEntities = baseMapper.selectList(Wrappers.lambdaQuery(IAssetEntity.class)
                    .orderByDesc(IAssetEntity::getId)
                    .last("LIMIT 5"));
        }else{
            iAssetEntities = baseMapper.selectList(Wrappers.lambdaQuery(IAssetEntity.class)
                    .eq(IAssetEntity::getUserId, userId));
        }
        List<IAssetDTO> iAssetDTOS = iAssetEntities.stream().map(iAssetEntity -> {
            IAssetDTO iAssetDTO = new IAssetDTO();
            BeanUtils.copyProperties(iAssetEntity, iAssetDTO);
            return iAssetDTO;
        }).collect(Collectors.toList());
        return new OperateResultDTO(true, "查询成功",iAssetDTOS);
    }

    @Override
    public OperateResultDTO getFileByNameList(List<String> nameList) throws Exception {
        List<IAssetEntity> entities = baseMapper.selectList(Wrappers.lambdaQuery(IAssetEntity.class)
                .in(IAssetEntity::getAssetId, nameList));
        List<CatalogAssetDTO> list = new ArrayList<>();
        for (IAssetEntity entity : entities) {
            CatalogAssetDTO dto = new CatalogAssetDTO();
            if(entity!=null){
                if(entity.getPath()!=null){
                    String path = entity.getPath();
                    String fileName = path.substring(path.lastIndexOf("/") + 1);
                    String url = ossUtils.createUrl(fileName);
//                    dto.setBaseUrl(url);
                    dto.setBaseUrl(path);
                }
                dto.setId(entity.getAssetId());
                dto.setDescription(entity.getAssetDescription());
                dto.setAssetPrice(entity.getAssetPrice());
                dto.setAssetQuantity(entity.getAssetQuantity());
            }
            list.add(dto);
        }
        return new OperateResultDTO(true,"获取成功",list);
    }

    @Override
    public OperateResultDTO updateFile(IAssetDTO input) throws Exception {
        if (input == null) {
            return new OperateResultDTO(false, "资产ID不能为空",null);
        }
        IAssetEntity iassetEntity = new IAssetEntity();
        BeanUtils.copyProperties(input, iassetEntity);

        boolean code = update(iassetEntity, Wrappers.lambdaUpdate(IAssetEntity.class).eq(IAssetEntity::getAssetId, input.getAssetId()));
        if (!code) {
            return new OperateResultDTO(false, "更新失败",null);
        } else {
            return new OperateResultDTO(true, "更新成功",null);
        }
    }

}
