package sz.lab.service.basic;

import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.basic.PersonalAssetDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.entity.basic.PersonalAssetEntity;

public interface PersonalAssetService extends IService<PersonalAssetEntity> {
    /**
     * @Description: 分页查询
     **/
    OperateResultDTO pageList(TableRequestDTO tableRequestDTO);
    /**
     * @Description: 新增个人资产
     **/
    OperateResultDTO add(PersonalAssetDTO dto);
    /**
     * @Description: 判断是否已经购买该资产
     **/
    OperateResultDTO isExist(PersonalAssetDTO dto);
    /**
     * @Description: 根据资产ID查询个人资产信息
     **/
    PersonalAssetDTO getOneById(String assetId);

    /**
     * @Description: 查询热门资产
     **/
    OperateResultDTO selectHotAsset();
    /**
     * @Description: 获取资产图表
     **/
    OperateResultDTO getAssetChart();
    /**
     * @Description: 获取资产列表
     **/
    OperateResultDTO getAssetList(Integer userId);
    /**
     * @Description: 获取资产成交数
     **/
    OperateResultDTO getAssetListCount();
}
