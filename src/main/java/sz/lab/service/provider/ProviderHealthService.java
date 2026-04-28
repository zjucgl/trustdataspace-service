package sz.lab.service.provider;

import sz.lab.dto.provider.HealthCheckResultDTO;
import sz.lab.entity.provider.ProviderConfigEntity;

public interface ProviderHealthService {
    HealthCheckResultDTO check(ProviderConfigEntity config);
}
