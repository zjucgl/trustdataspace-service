package sz.lab.service.system.ethnode;

import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.system.SystemNodeinfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ISystemNodeinfoService extends IService<SystemNodeinfo> {
    OperateResultDTO getNodeList(String nodeName);
}
