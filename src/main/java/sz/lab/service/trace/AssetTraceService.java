package sz.lab.service.trace;

import sz.lab.dto.system.OperateResultDTO;

import java.math.BigInteger;
import java.util.List;

public interface AssetTraceService {
    void registerAsset(String assetId, String name, String description, BigInteger price, String operatorName) throws Exception;

    void updatePrice(String assetId, BigInteger newPrice) throws Exception;

    OperateResultDTO purchaseAsset(String assetId, Integer userId) throws Exception;

    void updateStatus(String assetId, String newStatus) throws Exception;

    List getAssetHistory(String assetId, Integer userId) throws Exception;

    OperateResultDTO checkBalance(String assetId, Integer userId) throws Exception;

    OperateResultDTO isProvider(Integer userId, String assetId) throws Exception;
}
