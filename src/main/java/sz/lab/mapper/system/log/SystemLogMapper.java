package sz.lab.mapper.system.log;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sz.lab.entity.system.SystemLogEntity;

@Mapper
public interface SystemLogMapper extends BaseMapper<SystemLogEntity> {
}
