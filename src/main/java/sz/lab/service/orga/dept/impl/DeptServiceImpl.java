package sz.lab.service.orga.dept.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sz.lab.dto.orga.DeptDTO;
import sz.lab.dto.orga.DeptTreeDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.orga.dept.DeptEntity;
import sz.lab.mapper.orga.dept.DeptMapper;
import sz.lab.service.orga.dept.DeptService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 部门（实验室）信息表，公司ID和部门ID在配置文件中设置，以实验室为基本使用单位，设计实验室支持二级组织架构，不支持三级，程序中提示加锁定 服务实现类
 * </p>
 *
 * @author ckd
 * @since 2023-11-24
 */
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, DeptEntity> implements DeptService {

    @Value("${custom-attribute.dept-id}")
    private Integer deptId;
    @Resource
    private DeptMapper deptMapper;
    @Override
    public OperateResultDTO tree() {
        List<DeptEntity> list = deptMapper.selectList(Wrappers.lambdaQuery(DeptEntity.class)
                .eq(DeptEntity::getDeptId, deptId)
                .or()
                .eq(DeptEntity::getDeptFather, deptId));
        // 数组结构
        List<DeptTreeDTO> node = entitiesToDTO(list, false);
        // 树状结构
        List<DeptTreeDTO> ret = buildTree(node);
        // 遍历数组结构，更新叶子节点状态
        updateLeafStatus(node);
        return new OperateResultDTO(true,"成功", ret);
    }

    @Override
    public OperateResultDTO add(DeptDTO deptDTO) {
        DeptEntity deptEntity = dtoToEntity(deptDTO);
        save(deptEntity);
        return new OperateResultDTO(true,"成功", null);
    }

    @Override
    public OperateResultDTO update(DeptDTO deptDTO) {
        DeptEntity deptEntity = dtoToEntity(deptDTO);
        updateById(deptEntity);
        return new OperateResultDTO(true,"成功", null);
    }

    @Override
    public OperateResultDTO removeTreeNodes(List<Integer> ids) {
        baseMapper.deleteBatchIds(ids);
        List<Integer> children = selectChildrenIds(ids);
        while (children.size()>0){
            baseMapper.deleteBatchIds(children);
            ids = children;
            children = selectChildrenIds(ids);
        }
        return new OperateResultDTO(true,"成功", null);
    }

    @Override
    public OperateResultDTO participantCount() {
        List<DeptEntity> list = deptMapper.selectList(Wrappers.lambdaQuery(DeptEntity.class)
                .ne(DeptEntity::getDeptFather,0));
        return new OperateResultDTO(true,"成功",list.size());
    }

    private List<DeptTreeDTO> entitiesToDTO(List<DeptEntity> list, boolean isLeaf) {
        List<DeptTreeDTO> ret = new ArrayList<>();
        for (DeptEntity deptEntity : list) {
            ret.add(entityToDTO(deptEntity, isLeaf));
        }
        return ret;
    }
    private DeptTreeDTO entityToDTO(DeptEntity entity, boolean isLeaf) {
        DeptTreeDTO dto = new DeptTreeDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setLeaf(isLeaf);
        dto.setChildren(new ArrayList<>());
        return dto;
    }
    private List<DeptTreeDTO> buildTree(List<DeptTreeDTO> nodeList) {
        Map<Integer, DeptTreeDTO> nodeMap = new HashMap<>();

        // 节点map
        for (DeptTreeDTO node : nodeList) {
            nodeMap.put(node.getDeptId(), node);
        }

        List<DeptTreeDTO> tree = new ArrayList<>();

        // 连接节点
        for (DeptTreeDTO node : nodeList) {
            int fatherId = node.getDeptFather();
            if (fatherId != 0) {
                DeptTreeDTO parentNode = nodeMap.get(fatherId);
                if (parentNode != null) {
                    parentNode.getChildren().add(node);
                }
            } else {
                tree.add(node);
            }
        }

        return tree;
    }
    private void updateLeafStatus(List<DeptTreeDTO> list) {
        for (DeptTreeDTO dto : list) {
            if (dto.getChildren().isEmpty()) {
                dto.setLeaf(true);
            }
        }
    }
    private List<Integer> selectChildrenIds(List<Integer> ids) {
        return deptMapper.selectList(Wrappers.lambdaQuery(DeptEntity.class)
                        .in(DeptEntity::getDeptFather, ids)).stream()
                        .map(DeptEntity::getDeptId)
                        .collect(Collectors.toList());
    }
    private DeptEntity dtoToEntity(DeptDTO dto) {
        DeptEntity ret = new DeptEntity();
        ret.setDeptId(dto.getDeptId());
        ret.setDeptName(dto.getDeptName());
        ret.setDeptInfo(dto.getDeptInfo());
        ret.setDeptFather(dto.getDeptFather());
        ret.setDeptSort(dto.getDeptSort());
        return ret;
    }
}
