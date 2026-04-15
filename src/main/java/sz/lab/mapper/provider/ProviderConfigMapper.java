package sz.lab.mapper.provider;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import sz.lab.entity.provider.ProviderConfigEntity;

@Mapper
public interface ProviderConfigMapper extends BaseMapper<ProviderConfigEntity> {

    @Select("SELECT COALESCE(MAX(controlplane_mgmt_port), 9999) FROM provider_config WHERE is_deleted = 0")
    Integer getMaxMgmtPort();
}
