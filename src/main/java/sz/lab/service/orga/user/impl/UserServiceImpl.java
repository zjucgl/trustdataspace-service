package sz.lab.service.orga.user.impl;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import sz.lab.dto.orga.ParticipantOptionDTO;
import sz.lab.dto.orga.UserDTO;
import sz.lab.dto.orga.UserOptionDTO;
import sz.lab.dto.orga.UserOutputDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TablePagingDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.dto.system.role.RoleOptionDTO;
import sz.lab.entity.orga.dept.DeptEntity;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.entity.orga.user.UserRoleEntity;
import sz.lab.entity.system.IAssetEntity;
import sz.lab.entity.system.RoleEntity;
import sz.lab.mapper.login.LoginMapper;
import sz.lab.mapper.orga.dept.DeptMapper;
import sz.lab.mapper.orga.user.UserMapper;
import sz.lab.mapper.orga.user.UserRoleMapper;
import sz.lab.mapper.system.asset.IAssetMapper;
import sz.lab.mapper.system.role.RoleMapper;
import sz.lab.service.orga.user.UserRoleService;
import sz.lab.service.orga.user.UserService;
import sz.lab.service.trace.AssetTraceService;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户信息表，用于记录用户账号信息。 服务实现类
 * </p>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Resource
    private LoginMapper loginMapper;
    @Resource
    private DeptMapper deptMapper;

    @Resource
    private RoleMapper roleMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private AssetTraceService assetTraceService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private IAssetMapper assetMapper;

    @Override
    public OperateResultDTO pageList(TableRequestDTO tableRequestDTO,Integer userId,String type) {
//        //获取该项目的部门列表
//        List<Integer> deptList = getDeptList(deptId);
//        deptList.add(deptId);
        UserEntity user = userMapper.selectById(userId);
        //查询参数获取
        JSONObject param = tableRequestDTO.getJsonParam();
        String userName = param.getString("userName");
        String userPhone = param.getString("userPhone");
        // 启动自动分页
        PageHelper.startPage(tableRequestDTO.getPageNo(), tableRequestDTO.getPageSize());
        //查询用户列表
        List<UserEntity> userEntityList;
        userEntityList = userMapper.selectPageList(param);
//        if(type.equals("user")){
//            userEntityList = userMapper.selectList(Wrappers.lambdaQuery(UserEntity.class)
//                    .like(StrUtil.isNotBlank(userName),UserEntity::getUserName, userName)
//                    .like(StrUtil.isNotBlank(userPhone),UserEntity::getUserPhone, userPhone)
//                    .eq(UserEntity::getDeptId, user.getDeptId()));
//        }else{
//            userEntityList = userMapper.selectPageList(param);
//        }

        // 其他表的名字
        Map<Integer, DeptEntity> deptEntityMap = queryDept(userEntityList.stream().map(UserEntity::getDeptId).collect(Collectors.toList()));
        Map<Integer, List<Integer>> userRoleMap = queryRoleId(userEntityList.stream().map(UserEntity::getUserId).collect(Collectors.toList()));
        //返回到前端的数组
        List<UserOutputDTO> list = entitiesToDTOs(userEntityList, deptEntityMap, userRoleMap);
        //将数组封装到通用分页dto类
        TablePagingDTO pagingDTO = new TablePagingDTO(tableRequestDTO.getPageNo(), tableRequestDTO.getPageSize(),
                new PageInfo<>(userEntityList).getTotal(), list);
        //返回通用操作返回结果类
        return new OperateResultDTO(true,"成功",pagingDTO);
    }

    @Override
    public OperateResultDTO roleOptionList() {
        List<RoleEntity> list = roleMapper.selectRoleList();

        List<RoleOptionDTO> ret = getRoleOptions(list);
        return new OperateResultDTO(true,"成功",ret);
    }

    @Override
    public OperateResultDTO userOptionList() {
//        List<UserEntity> list = userMapper.selectList(Wrappers.lambdaQuery(UserEntity.class)
//                .select(UserEntity::getUserId,UserEntity::getUserName));
        List<UserEntity> list = userMapper.selectUserOptions();
        List<UserOptionDTO> ret = getUserOptions(list);
        return new OperateResultDTO(true,"成功",ret);
    }

    @Override
    public OperateResultDTO participantOptionList() {
        List<DeptEntity> list =deptMapper.selectList(Wrappers.lambdaQuery(DeptEntity.class)
                .ne(DeptEntity::getDeptFather,0));
        List<ParticipantOptionDTO> ret = getParticipantOptions(list);
        return new OperateResultDTO(true,"成功",ret);
    }

    @Override
    public OperateResultDTO add(UserDTO userDTO) throws Exception {
        UserEntity userEntity = dtoToEntity(userDTO);
        userMapper.insertUser(userEntity);
        //判断是否有角色
        if(userDTO.getRoleIdList()!= null && !userDTO.getRoleIdList().isEmpty()) {
            userRoleService.updateUserRoleCode(userEntity.getUserId(), userDTO.getRoleIdList());
        }
        UserEntity user = userMapper.selectUserByLoginCode(userEntity.getLoginCode());
        //返回userId是执行下面的updateSignatureImage方法
        return new OperateResultDTO(true,"成功",user.getUserId());
    }

    @Override
    public OperateResultDTO update(UserDTO userDTO) {
        UserEntity userEntity = dtoToEntity(userDTO);
        userMapper.updateUser(userEntity);
        return new OperateResultDTO(true,"成功",userEntity.getUserId());
    }

    @Override
    public OperateResultDTO delete(List<Integer> ids) {
        userMapper.deleteUser(ids);
        return new OperateResultDTO(true,"成功",null);
    }

    @Override
    public OperateResultDTO codeIsExist(UserDTO input) {
        boolean isAdd = (input.getUserId() == null);
        UserEntity user;
        if(isAdd){
            //新增，查询登录code是否存在
            user = userMapper.selectUserByLoginCode(input.getLoginCode());
        }else {
            //编辑，查询这个userId以外的登录code是否存在
            user = userMapper.selectOtherUserLoginCode(input.getLoginCode(), input.getUserId());
        }
        //true则存在，false则不存在
        if(user == null){
            return new OperateResultDTO(true,"账号不存在",false);
        }else{
            return new OperateResultDTO(true,"账号已存在",true);
        }
    }

    @Override
    public OperateResultDTO updateprice(Integer userId, String assetId) throws Exception {
        IAssetEntity assetEntity = assetMapper.selectOne(Wrappers.lambdaQuery(IAssetEntity.class).eq(IAssetEntity::getAssetId, assetId));
        UserEntity userEntity = userMapper.selectById(userId);
        //先判断用户余额是否足够
        if (userEntity.getUserDeposits() < assetEntity.getAssetPrice()) {
            return new OperateResultDTO(false,"用户余额不足",null);
        }
        if (assetEntity.getAssetQuantity() == 0) {
            return new OperateResultDTO(false,"该商品已售完",null);
        }
        OperateResultDTO operateResultDTO = assetTraceService.purchaseAsset(assetId, userId);
        int code0 = 0;

        if (operateResultDTO.isSuccess()) {
            code0 = userMapper.update(null,Wrappers.lambdaUpdate(UserEntity.class)
                    .eq(UserEntity::getUserId, userId)
                    .set(UserEntity::getUserDeposits, userEntity.getUserDeposits()-assetEntity.getAssetPrice())
                    .set(UserEntity::getUserDepositsExtra, userEntity.getUserDepositsExtra()));
            //修改卖家余额
            UserEntity userEntitySeller = userMapper.selectById(assetEntity.getUserId());
            code0 = userMapper.update(null,Wrappers.lambdaUpdate(UserEntity.class)
                    .eq(UserEntity::getUserId, assetEntity.getUserId())
                    .set(UserEntity::getUserDeposits, userEntitySeller.getUserDeposits()+assetEntity.getAssetPrice()));
        }
        int code2 = userMapper.updatequantity(assetEntity.getAssetId());

        if (code0 > 0 && code2 > 0) {
            if (assetEntity.getAssetQuantity() == 1) {
                assetMapper.delete(Wrappers.lambdaQuery(IAssetEntity.class).eq(IAssetEntity::getAssetId, assetId));
            }
            return new OperateResultDTO(true,"修改成功",operateResultDTO);
        } else {
            return new OperateResultDTO(false, "修改失败", null);
        }
    }
    @Override
    public OperateResultDTO updatequantity(String assetId) {
        int code = userMapper.updatequantity(assetId);
        if(code == 0){
            return new OperateResultDTO(false,"库存扣减失败",null);
        }
        return new OperateResultDTO(true,"成功",null);
    }

    @Override
    public OperateResultDTO getUserDeposits(Integer userId) {
        UserEntity userEntity = userMapper.selectById(userId);
        return new OperateResultDTO(true,"成功",userEntity.getUserDeposits());
    }

    private List<UserOutputDTO> entitiesToDTOs(List<UserEntity> list,
                                               Map<Integer, DeptEntity> deptEntityMap,
                                               Map<Integer, List<Integer>> userRoleMap) {
        List<UserOutputDTO> ret = new ArrayList<>(list.size());
        for (UserEntity userEntity : list) {
            UserOutputDTO dto = new UserOutputDTO();
//            dto.setLoginCode(userEntity.getLoginCode());
//            dto.setParticipantKeyId(userEntity.getParticipantKeyId());
//            dto.setUserId(userEntity.getUserId());
//            dto.setUserInfo(userEntity.getUserInfo());
//            dto.setUserName(userEntity.getUserName());
//            dto.setIsDeleted(userEntity.getIsDeleted());
//            dto.setDeptId(userEntity.getDeptId());
//            dto.setUserPhone(userEntity.getUserPhone());
            BeanUtils.copyProperties(userEntity,dto);
            if(deptEntityMap.containsKey(userEntity.getDeptId())){
                dto.setDeptName(deptEntityMap.get(userEntity.getDeptId()).getDeptName());
                dto.setParticipantId(deptEntityMap.get(userEntity.getDeptId()).getParticipantId());
            }
            if(userRoleMap.containsKey(userEntity.getUserId())){
                dto.setRoleIdList(userRoleMap.get(userEntity.getUserId()));
            }else{
                dto.setRoleIdList(new ArrayList<>());
            }
            ret.add(dto);
        }
        return ret;
    }
    private List<RoleOptionDTO> getRoleOptions(List<RoleEntity> list) {
        List<RoleOptionDTO> ret = new ArrayList<>(list.size());
        for (RoleEntity roleEntity : list) {
            RoleOptionDTO dto = new RoleOptionDTO();
            dto.setRoleName(roleEntity.getRoleName());
            dto.setRoleId(roleEntity.getRoleId());
            ret.add(dto);
        }
        return ret;
    }
    private List<UserOptionDTO> getUserOptions(List<UserEntity> list) {
        List<UserOptionDTO> ret = new ArrayList<>(list.size());
        for (UserEntity userEntity : list) {
            UserOptionDTO dto = new UserOptionDTO();
            dto.setUserId(userEntity.getUserId());
            dto.setUserName(userEntity.getUserName());
            ret.add(dto);
        }
        return ret;
    }
    private List<ParticipantOptionDTO> getParticipantOptions(List<DeptEntity> list) {
        List<ParticipantOptionDTO> ret = new ArrayList<>(list.size());
        for (DeptEntity entity : list) {
            ParticipantOptionDTO dto = new ParticipantOptionDTO();
            dto.setId(entity.getDeptId());
            dto.setParticipantId(entity.getParticipantId());
            ret.add(dto);
        }
        return ret;
    }
    private Map<Integer, DeptEntity> queryDept(List<Integer> deptIds) {
        if (deptIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return deptMapper.selectListByDeptIds(deptIds)
                .stream()
                .collect(Collectors.toMap(DeptEntity::getDeptId, dept -> dept));
    }
    private Map<Integer, List<Integer>> queryRoleId(List<Integer> userIds) {
        return userRoleMapper.selectRoleListByUserIds(userIds)
                .stream()
                .collect(Collectors.groupingBy(UserRoleEntity::getUserId,
                        Collectors.mapping(UserRoleEntity::getRoleId, Collectors.toList())));
    }
    private List<Integer> getDeptList(Integer deptId){
        List<Integer> list = deptMapper.selectList(Wrappers.lambdaQuery(DeptEntity.class)
                .eq(DeptEntity::getDeptFather,deptId))
                .stream()
                .map(DeptEntity::getDeptId)
                .collect(Collectors.toList());
        return list;
    }
    private UserEntity dtoToEntity(UserDTO dto) {
        UserEntity ret = new UserEntity();
        //复制dto属性到entity
        BeanUtils.copyProperties(dto,ret);
        //新增用户或重置密码,设置密码.修改则跳过
        if(dto.getUserId() == null || StrUtil.isNotBlank(dto.getLoginPwd())){
            ret.setLoginPwd(dto.getLoginPwd());
            ret.setPwdLastUpdate(LocalDateTime.now());
        }else {
            ret.setLoginPwd(null);
        }
        return ret;
    }
}
