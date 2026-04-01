package sz.lab.service.basic.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import sz.lab.dto.basic.HotAssetDTO;
import sz.lab.dto.basic.PersonalAssetDTO;
import sz.lab.dto.dashboard.AssetChartDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TablePagingDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.entity.basic.PersonalAssetEntity;
import sz.lab.entity.orga.dept.DeptEntity;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.entity.orga.user.UserRoleEntity;
import sz.lab.mapper.basic.PersonalAssetMapper;
import sz.lab.mapper.orga.dept.DeptMapper;
import sz.lab.mapper.orga.user.UserMapper;
import sz.lab.mapper.orga.user.UserRoleMapper;
import sz.lab.service.basic.PersonalAssetService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PersonalAssetServiceImpl extends ServiceImpl<PersonalAssetMapper, PersonalAssetEntity>
        implements PersonalAssetService {
    @Resource
    private PersonalAssetMapper personalAssetMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private DeptMapper deptMapper;
    @Resource
    private UserRoleMapper userRoleMapper;

    @Override
    public OperateResultDTO pageList(TableRequestDTO tableRequestDTO) {
        //查询参数获取
        JSONObject param = tableRequestDTO.getJsonParam();
        // 启动自动分页
        PageHelper.startPage(tableRequestDTO.getPageNo(), tableRequestDTO.getPageSize());
        //查询拥有资产列表
        List<PersonalAssetEntity> personalAssetEntityList = personalAssetMapper.selectPageList(param);

        // 其他表的名字
        Map<Integer, String> userEntityMap = queryUserName(personalAssetEntityList.stream()
                .map(PersonalAssetEntity::getUserId).collect(Collectors.toList()));
        Map<String, DeptEntity> deptEntityMap = queryDeptByParticipantIds(personalAssetEntityList.stream()
                .map(PersonalAssetEntity::getParticipantId).collect(Collectors.toList()));
        //返回到前端的数组
        List<PersonalAssetDTO> list = entitiesToDTOs(personalAssetEntityList, userEntityMap,deptEntityMap);
        //将数组封装到通用分页dto类
        TablePagingDTO pagingDTO = new TablePagingDTO(tableRequestDTO.getPageNo(), tableRequestDTO.getPageSize(),
                new PageInfo<>(personalAssetEntityList).getTotal(), list);
        //返回通用操作返回结果类
        return new OperateResultDTO(true,"成功",pagingDTO);
    }

    @Override
    public OperateResultDTO add(PersonalAssetDTO dto) {
        PersonalAssetEntity entity = dtoToEntity(dto);
        personalAssetMapper.insertPersonalAsset(entity);
        return new OperateResultDTO(true,"成功",null);
    }

    @Override
    public OperateResultDTO isExist(PersonalAssetDTO dto) {
        UserRoleEntity userRoleEntity = userRoleMapper.selectOne(Wrappers.lambdaQuery(UserRoleEntity.class)
                .eq(UserRoleEntity::getUserId, dto.getUserId())
                .eq(UserRoleEntity::getRoleId,1));
        if(userRoleEntity != null){
            return new OperateResultDTO(true,"管理员不能购买资产",null);
        }
        PersonalAssetEntity entity = dtoToEntity(dto);
        List<PersonalAssetEntity> list = personalAssetMapper.selectByAssetId(entity);
        if(list.isEmpty()){
            return new OperateResultDTO(false,"资产不存在",null);
        }
        return new OperateResultDTO(true,"该资产已购买，请前往[我的资产]->[资产消费]页面查看",1);
    }
    private Map<Integer, String> queryUserName(List<Integer> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectListByUserIds(userIds)
                .stream()
                .collect(Collectors.toMap(UserEntity::getUserId, UserEntity::getUserName));
    }
    private Map<String, DeptEntity> queryDeptByParticipantIds(List<String> participantIds) {
        if (participantIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, DeptEntity> deptEntityMap = deptMapper.selectList(Wrappers.lambdaQuery(DeptEntity.class)
                .in(DeptEntity::getParticipantId, participantIds))
                .stream()
                .collect(Collectors.toMap(DeptEntity::getParticipantId, dept -> dept));
        return deptEntityMap;
    }
    private List<PersonalAssetDTO> entitiesToDTOs(List<PersonalAssetEntity> list,
                                                  Map<Integer, String> userEntityMap,
                                                  Map<String, DeptEntity> deptEntityMap) {
        List<PersonalAssetDTO> ret = new ArrayList<>(list.size());
        for (PersonalAssetEntity entity : list) {
            PersonalAssetDTO dto = new PersonalAssetDTO();
            //复制entity属性到dto
            BeanUtils.copyProperties(entity, dto);
            if (userEntityMap.containsKey(entity.getUserId())) {
                dto.setUserName(userEntityMap.get(entity.getUserId()));
            }
            if (deptEntityMap.containsKey(entity.getParticipantId())) {
                DeptEntity deptEntity = deptEntityMap.get(entity.getParticipantId());
                dto.setDeptName(deptEntity.getDeptName());
            }
            ret.add(dto);
        }
        return ret;
    }
    private PersonalAssetEntity dtoToEntity(PersonalAssetDTO dto) {
        PersonalAssetEntity ret = new PersonalAssetEntity();
        //复制dto属性到entity
        BeanUtils.copyProperties(dto,ret);
        return ret;
    }
    @Override
    public PersonalAssetDTO getOneById(String assetId) {
        QueryWrapper<PersonalAssetEntity> queryWrapper = new QueryWrapper<>();
        PersonalAssetEntity entity = personalAssetMapper.selectOne(queryWrapper.like("asset_id",assetId));
        PersonalAssetDTO personalasset = new PersonalAssetDTO();
        if(entity == null){
            return null;
        } else {
            BeanUtils.copyProperties(entity, personalasset);
            return personalasset;
        }
    }

    @Override
    public OperateResultDTO selectHotAsset() {
        List<PersonalAssetEntity> list = personalAssetMapper.selectList(Wrappers.lambdaQuery(PersonalAssetEntity.class));

        // 根据assetId分组并计算每个assetId的数量
        Map<String, Long> assetIdCountMap = list.stream()
                .collect(Collectors.groupingBy(PersonalAssetEntity::getAssetId, Collectors.counting()));

        // 将结果转换为List<HotAssetDTO>
        List<HotAssetDTO> hotAssetDTOList = assetIdCountMap.entrySet().stream()
                .map(entry -> {
                    HotAssetDTO hotAssetDTO = new HotAssetDTO();
                    hotAssetDTO.setAssetId(entry.getKey());
                    hotAssetDTO.setCount(entry.getValue().intValue());
                    return hotAssetDTO;
                })
                .sorted((dto1, dto2) -> dto2.getCount().compareTo(dto1.getCount())) // 按照count值从大到小排序
                .collect(Collectors.toList());
        return new OperateResultDTO(true,"成功",hotAssetDTOList);
    }

    @Override
    public OperateResultDTO getAssetChart() {
        List<AssetChartDTO> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
//        // 循环12个月（包含当前月）
//        for (int i = 11; i >= 0; i--) {
//            // 计算当前处理月份
//            LocalDateTime current = now.minusMonths(i);
//            // 计算月份的开始时间（当月第一天 00:00:00）
//            LocalDateTime monthStart = current.withDayOfMonth(1)
//                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
//            // 计算月份的结束时间（当月最后一天 23:59:59.999）
//            LocalDateTime monthEnd = current.with(TemporalAdjusters.lastDayOfMonth())
//                    .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
//            // 查询当月数据
//            List<PersonalAssetEntity> assetEntities = personalAssetMapper.selectList(
//                    Wrappers.lambdaQuery(PersonalAssetEntity.class)
//                            .ge(PersonalAssetEntity::getGmtCreate, monthStart)
//                            .le(PersonalAssetEntity::getGmtCreate, monthEnd)
//            );
//            // 创建并填充DTO
//            AssetChartDTO dto = new AssetChartDTO();
//            dto.setDate(current.format(DateTimeFormatter.ofPattern("yyyy-MM")));
//            dto.setValue(assetEntities.size());
//            list.add(dto);
//        }
        // 循环7天（包含当天）
        for (int i = 6; i >= 0; i--) {
            LocalDateTime current = now.minusDays(i);
            // 计算天的开始时间（00:00:00）
            LocalDateTime dayStart = current.withHour(0).withMinute(0).withSecond(0).withNano(0);
            // 计算天的结束时间（23:59:59.999）
            LocalDateTime dayEnd = current.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

            List<PersonalAssetEntity> assetEntities = personalAssetMapper.selectList(
                    Wrappers.lambdaQuery(PersonalAssetEntity.class)
                            .ge(PersonalAssetEntity::getGmtCreate, dayStart)
                            .le(PersonalAssetEntity::getGmtCreate, dayEnd)
            );

            AssetChartDTO dto = new AssetChartDTO();
            dto.setDate(current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dto.setValue(assetEntities.size());
            list.add(dto);
        }
        return new OperateResultDTO(true,"成功",list);
    }

    @Override
    public OperateResultDTO getAssetListCount() {
        List<PersonalAssetEntity> list = personalAssetMapper.selectList(Wrappers.lambdaQuery());
        return new OperateResultDTO(true,"成功",list.size());
    }

    @Override
    public OperateResultDTO getAssetList(Integer userId) {
        UserRoleEntity userRoleEntity = userRoleMapper.selectOne(Wrappers.lambdaQuery(UserRoleEntity.class)
                        .eq(UserRoleEntity::getUserId,userId)
                        .last("limit 1"));
        List<PersonalAssetEntity> list;
        if(userRoleEntity.getRoleId() == 1){
            list = personalAssetMapper.selectList(Wrappers.lambdaQuery(PersonalAssetEntity.class)
                    .orderByDesc(PersonalAssetEntity::getGmtCreate)
                    .last("limit 5"));
        }else{
            list = personalAssetMapper.selectList(Wrappers.lambdaQuery(PersonalAssetEntity.class)
                    .eq(PersonalAssetEntity::getUserId,userId)
                    .orderByDesc(PersonalAssetEntity::getGmtCreate)
                    .last("limit 50"));
        }
        return new OperateResultDTO(true,"成功",list);
    }
}
