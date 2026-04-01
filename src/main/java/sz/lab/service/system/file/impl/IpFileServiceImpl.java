package sz.lab.service.system.file.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import sz.lab.dto.system.IpFileDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.system.IpFileEntity;
import sz.lab.mapper.system.file.IpFileMapper;
import sz.lab.service.system.file.IpFileService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
@Service
public class IpFileServiceImpl extends ServiceImpl<IpFileMapper, IpFileEntity> implements IpFileService {
    @Override
    public OperateResultDTO getIpFile() {
        List<IpFileEntity> fileEntityList = baseMapper.selectList(Wrappers.lambdaQuery(IpFileEntity.class));

        return new OperateResultDTO(true,"获取成功",convertToDTO(fileEntityList));
    }

    @Override
    public OperateResultDTO getIpFileByName(String name) {
        IpFileEntity fileEntity = baseMapper.selectOne(Wrappers.lambdaQuery(IpFileEntity.class)
                .eq(IpFileEntity::getName, name)
                .last("LIMIT 1"));
        IpFileDTO dto = new IpFileDTO();
        if(fileEntity!=null){
            dto.setBaseUrl(fileEntity.getPath());
            dto.setId(fileEntity.getName());
            dto.setType(fileEntity.getSuffix());
        }
        return new OperateResultDTO(true,"获取成功",dto);
    }

    @Override
    public OperateResultDTO getIpFileByNameList(List<String> nameList) {
        List<IpFileEntity> fileEntityList = baseMapper.selectList(Wrappers.lambdaQuery(IpFileEntity.class)
                .in(IpFileEntity::getName, nameList));
        List<IpFileDTO> list = new ArrayList<>();
        for (IpFileEntity entity : fileEntityList) {
            IpFileDTO dto = new IpFileDTO();
            if(entity!=null){
                dto.setBaseUrl(entity.getPath());
                dto.setId(entity.getName());
                dto.setType(entity.getSuffix());
            }
            list.add(dto);
        }
        return new OperateResultDTO(true,"获取成功",list);
    }

    @Override
    public String getFileNameByPath(String path) {
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return path; // 如果没有找到"/"，则返回整个字符串
        }
        return path.substring(lastSlashIndex + 1);
    }
    private List<IpFileDTO> convertToDTO(List<IpFileEntity> workFileEntityList) {
        List<IpFileDTO> workFileDTOList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int fileIndex = 1;
        for (IpFileEntity entity : workFileEntityList) {
            IpFileDTO workFileDTO = new IpFileDTO();
            if(entity.getPath()!=null){
                if (entity.getName() == null) {
                    workFileDTO.setId("无名" + fileIndex);
                    fileIndex+=1;
                }else{
                    workFileDTO.setId(entity.getName());
                }
                workFileDTO.setBaseUrl(entity.getPath());
                LocalDateTime gmtCreate = entity.getCreateTime();
                workFileDTO.setDescription("上传时间:"+gmtCreate.format(formatter));
                workFileDTOList.add(workFileDTO);
            }
        }
        return workFileDTOList;
    }

}
