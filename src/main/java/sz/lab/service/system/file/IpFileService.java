package sz.lab.service.system.file;

import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.system.IpFileEntity;

import java.util.List;

public interface IpFileService extends IService<IpFileEntity> {
    /**
     * @Description: 获取所有文件列表
     **/
    OperateResultDTO getIpFile();

    /**
     * @Description: 获取文件
     **/
    OperateResultDTO getIpFileByName(String name);
    /**
     * @Description: 获取文件列表通过名称列表
     **/
    OperateResultDTO getIpFileByNameList(List<String> nameList);
    /**
     * @Description: 通过路径获取minio存储的文件名称
     **/
    String getFileNameByPath(String path);
}
