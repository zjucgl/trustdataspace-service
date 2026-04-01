package sz.lab.service.trace;

import sz.lab.dto.system.OperateResultDTO;
import sz.lab.utils.contract.AssetTraceability;

import java.math.BigInteger;
import java.util.List;

public interface AssetTraceService {
    /**
     * @Description: 注册资产
     * @param assetId
     * @param name
     * @param description
     * @param price
     * @throws Exception
     */
    void registerAsset(String assetId, String name, String description, BigInteger price, String ethAccount) throws Exception;

    // 更新资产价格
    void updatePrice(String assetId, BigInteger newPrice) throws Exception;

    // 购买并转移资产
    OperateResultDTO purchaseAsset(String assetId, Integer userId) throws Exception;

    // 更新资产状态
    AssetTraceability.Asset updateStatus(String assetId, String newStatus) throws Exception;

    // 查询资产信息
    AssetTraceability.Asset getAsset(String assetId, Integer userId) throws Exception;

    // 查询资产历史
    List getAssetHistory(String assetId,Integer userId) throws Exception;

    //检查资金是否足够
    OperateResultDTO checkBalance(String assetId,Integer userId) throws Exception;

    /**
     * 判断是否生产者
     * @return {@link OperateResultDTO }
     * @throws Exception
     */
    OperateResultDTO isProvider(Integer userId , String assetId) throws Exception;
}
