package sz.lab.service.system.lay.impl;

import org.springframework.stereotype.Service;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.lay.LayNoticeDTO;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.mapper.orga.user.UserMapper;
import sz.lab.service.system.lay.LayNoticeService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
@Service
public class LayNoticeServiceImpl implements LayNoticeService {
    @Resource
    private UserMapper userMapper;
    @Override
    public OperateResultDTO checkPwdUpdate(Integer userId) {
        UserEntity user = userMapper.selectUserById(userId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastUpdate = user.getPwdLastUpdate();
        LayNoticeDTO noticeDTO = new LayNoticeDTO();
        if(lastUpdate.isBefore(now.minusMonths(3))){
            noticeDTO.setType("1");
            noticeDTO.setAvatar("");
            noticeDTO.setDatetime("");
            noticeDTO.setTitle("账号安全");
            noticeDTO.setDescription("距离您上次更新密码已超过三个月，为了保证您的账号安全，请尽快更新密码。");
            noticeDTO.setStatus("danger");
            noticeDTO.setExtra("重要");
            return new OperateResultDTO(true,"成功", noticeDTO);
        }
        return new OperateResultDTO(true,"成功", null);
    }
}
