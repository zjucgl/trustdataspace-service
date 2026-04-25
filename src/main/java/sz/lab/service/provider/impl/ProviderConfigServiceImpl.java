package sz.lab.service.provider.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import sz.lab.dto.provider.ProviderConfigDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.orga.dept.DeptEntity;
import sz.lab.entity.provider.ProviderConfigEntity;
import sz.lab.mapper.orga.dept.DeptMapper;
import sz.lab.mapper.provider.ProviderConfigMapper;
import sz.lab.service.provider.ProviderConfigService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProviderConfigServiceImpl
        extends ServiceImpl<ProviderConfigMapper, ProviderConfigEntity>
        implements ProviderConfigService {

    @Resource
    private ProviderConfigMapper providerConfigMapper;
    @Resource
    private DeptMapper deptMapper;

    private static final int PORT_BASE = 10000;
    private static final int PORT_STEP = 100;

    @Override
    public OperateResultDTO add(ProviderConfigDTO dto) {
        ProviderConfigEntity existing = baseMapper.selectOne(
                Wrappers.lambdaQuery(ProviderConfigEntity.class)
                        .eq(ProviderConfigEntity::getProviderName, dto.getProviderName()));
        if (existing != null) {
            return new OperateResultDTO(false, "Provider名称已存在", null);
        }

        ProviderConfigEntity entity = new ProviderConfigEntity();
        BeanUtils.copyProperties(dto, entity);
        entity.setStatus("PENDING");
        if (entity.getEnabledDataplaneExtensions() == null
                || entity.getEnabledDataplaneExtensions().isEmpty()) {
            entity.setEnabledDataplaneExtensions("http");
        }

        Integer maxPort = providerConfigMapper.getMaxMgmtPort();
        int basePort = Math.max(maxPort + PORT_STEP, PORT_BASE);
        entity.setControlplaneMgmtPort(basePort);
        entity.setControlplaneProtocolPort(basePort + 1);
        entity.setControlplanePublicPort(basePort + 2);
        entity.setDataplanePublicPort(basePort + 3);
        entity.setIdentityHubPort(basePort + 4);
        entity.setStsPort(basePort + 5);

        baseMapper.insert(entity);
        return new OperateResultDTO(true, "创建成功", entity.getId());
    }

    @Override
    public OperateResultDTO listAll() {
        List<ProviderConfigEntity> entities = baseMapper.selectList(
                Wrappers.lambdaQuery(ProviderConfigEntity.class)
                        .orderByDesc(ProviderConfigEntity::getId));

        List<Integer> deptIds = entities.stream()
                .map(ProviderConfigEntity::getDeptId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, String> deptMap = new java.util.HashMap<>();
        if (!deptIds.isEmpty()) {
            List<DeptEntity> depts = deptMapper.selectListByDeptIds(deptIds);
            deptMap = depts.stream()
                    .collect(Collectors.toMap(DeptEntity::getDeptId, DeptEntity::getDeptName));
        }

        Map<Integer, String> finalDeptMap = deptMap;
        List<ProviderConfigDTO> dtos = entities.stream().map(e -> {
            ProviderConfigDTO d = new ProviderConfigDTO();
            BeanUtils.copyProperties(e, d);
            if (e.getDeptId() != null && finalDeptMap.containsKey(e.getDeptId())) {
                d.setDeptName(finalDeptMap.get(e.getDeptId()));
            }
            return d;
        }).collect(Collectors.toList());

        return new OperateResultDTO(true, "查询成功", dtos);
    }

    @Override
    public OperateResultDTO detail(Long id) {
        ProviderConfigEntity entity = baseMapper.selectById(id);
        if (entity == null) {
            return new OperateResultDTO(false, "Provider不存在", null);
        }
        ProviderConfigDTO dto = new ProviderConfigDTO();
        BeanUtils.copyProperties(entity, dto);
        return new OperateResultDTO(true, "查询成功", dto);
    }

    @Override
    public OperateResultDTO update(ProviderConfigDTO dto) {
        ProviderConfigEntity entity = baseMapper.selectById(dto.getId());
        if (entity == null) {
            return new OperateResultDTO(false, "Provider不存在", null);
        }
        entity.setProviderLabel(dto.getProviderLabel());
        entity.setDeployHost(dto.getDeployHost());
        entity.setEnabledDataplaneExtensions(
                dto.getEnabledDataplaneExtensions() == null
                        || dto.getEnabledDataplaneExtensions().isEmpty()
                        ? "http"
                        : dto.getEnabledDataplaneExtensions());
        entity.setRemark(dto.getRemark());
        baseMapper.updateById(entity);
        return new OperateResultDTO(true, "修改成功", null);
    }

    @Override
    public OperateResultDTO remove(Long id) {
        baseMapper.deleteById(id);
        return new OperateResultDTO(true, "删除成功", null);
    }

    @Override
    public OperateResultDTO updateStatus(Long id, String status) {
        ProviderConfigEntity entity = baseMapper.selectById(id);
        if (entity == null) {
            return new OperateResultDTO(false, "Provider不存在", null);
        }
        entity.setStatus(status);
        baseMapper.updateById(entity);
        return new OperateResultDTO(true, "状态更新成功", null);
    }
}
