package sz.lab.service.system.asset;

import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.asset.IAssetDTO;
import sz.lab.entity.system.IAssetEntity;

import java.util.List;

public interface AssetService extends IService<IAssetEntity> {
    //修改价格
    OperateResultDTO updateAssetprice(String assetId ,long newPrice);
    //更新是否有政策状态
    OperateResultDTO updateHasPolicy(String assetId,Integer hasPolicy);
    //根据ID查询
    OperateResultDTO getAssetPricebyId(String assetId);
    //创建资产
    OperateResultDTO createAsset(IAssetDTO input);
    //删除资产
    OperateResultDTO deleteAsset(IAssetDTO input);
    //查询资产
    OperateResultDTO queryAsset();
    //查询资产
    OperateResultDTO queryAssetByCatalog();
    //查询资产通过用户ID
    OperateResultDTO queryAssetByUserId(Integer userId);
    //查询资产通过资产ID
    OperateResultDTO queryAssetByAssetId(String assetId);
    //数据概览页面，查询我提供的资产
    OperateResultDTO queryAssetByBoard(Integer userId);

    /**
     * @Description: 获取文件列表通过名称列表
     **/
    OperateResultDTO getFileByNameList(List<String> nameList) throws Exception;

    /**
     * 更新资产信息
     * @param input
     * @return {@link OperateResultDTO }
     * @throws Exception
     */
    OperateResultDTO updateFile(IAssetDTO input) throws Exception;
}
