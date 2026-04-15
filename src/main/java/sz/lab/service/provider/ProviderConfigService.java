package sz.lab.service.provider;

import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.provider.ProviderConfigDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.provider.ProviderConfigEntity;

public interface ProviderConfigService extends IService<ProviderConfigEntity> {
    OperateResultDTO add(ProviderConfigDTO dto);
    OperateResultDTO listAll();
    OperateResultDTO detail(Long id);
    OperateResultDTO update(ProviderConfigDTO dto);
    OperateResultDTO remove(Long id);
    OperateResultDTO updateStatus(Long id, String status);
}
