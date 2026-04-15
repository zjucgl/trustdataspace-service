package sz.lab.service.trace.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import sz.lab.chain.client.HuayiChainClient;
import sz.lab.chain.model.ChainResponse;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.basic.PersonalAssetEntity;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.entity.system.ChainRecordEntity;
import sz.lab.entity.system.IAssetEntity;
import sz.lab.mapper.basic.PersonalAssetMapper;
import sz.lab.mapper.orga.user.UserMapper;
import sz.lab.mapper.system.asset.IAssetMapper;
import sz.lab.mapper.system.chain.ChainRecordMapper;
import sz.lab.service.trace.AssetTraceService;
import sz.lab.utils.BlockChainUtil;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AssetTraceServiceImpl implements AssetTraceService {
    @Resource
    private IAssetMapper assetMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private PersonalAssetMapper personalAssetMapper;
    @Resource
    private ChainRecordMapper chainRecordMapper;
    @Resource
    private HuayiChainClient chainClient;
    @Resource
    private BlockChainUtil blockChainUtil;

    @Override
    public void registerAsset(String assetId, String name, String description,
                              BigInteger price, String operatorName) throws Exception {
        String dataHash = blockChainUtil.sha256(assetId + name + description + price);
        ChainResponse response = chainClient.submitRecord(
                assetId, assetId, "CREATE", operatorName, dataHash,
                "注册资产: " + name);
        saveChainRecord(assetId, "CREATE", null, operatorName, dataHash, response);
    }

    @Override
    public void updatePrice(String assetId, BigInteger newPrice) throws Exception {
        String dataHash = blockChainUtil.sha256(assetId + newPrice);
        ChainResponse response = chainClient.submitRecord(
                assetId, assetId, "UPDATE", "system", dataHash,
                "更新价格: " + newPrice);
        saveChainRecord(assetId, "UPDATE", null, "system", dataHash, response);
    }

    @Override
    public OperateResultDTO purchaseAsset(String assetId, Integer userId) throws Exception {
        IAssetEntity assetEntity = assetMapper.selectOne(Wrappers.lambdaQuery(IAssetEntity.class)
                .eq(IAssetEntity::getAssetId, assetId)
                .eq(IAssetEntity::getIsFirst, 1));
        if (assetEntity == null) {
            return new OperateResultDTO(false, "资产不存在", null);
        }
        UserEntity userEntity = userMapper.selectOne(Wrappers.lambdaQuery(UserEntity.class)
                .eq(UserEntity::getUserId, userId));
        if (userEntity == null) {
            return new OperateResultDTO(false, "用户不存在", null);
        }

        String traceAssetId = assetId + System.currentTimeMillis();
        String dataHash = blockChainUtil.sha256(traceAssetId + userId + assetEntity.getAssetPrice());

        ChainResponse registerResponse = chainClient.submitRecord(
                traceAssetId, assetEntity.getAssetId(), "CREATE", "system", dataHash,
                "交易资产注册");
        saveChainRecord(traceAssetId, "CREATE", null, "system", dataHash, registerResponse);

        ChainResponse transferResponse = chainClient.submitRecord(
                traceAssetId, assetEntity.getAssetId(), "TRANSFER", userEntity.getUserName(), dataHash,
                "资产交易转移");
        saveChainRecord(traceAssetId, "TRANSFER", userId, userEntity.getUserName(), dataHash, transferResponse);

        String txHash = transferResponse != null ? transferResponse.getTxHash() : "";
        return new OperateResultDTO(true, "购买成功",
                new Object[]{traceAssetId, userId, txHash,
                        BigInteger.ZERO, assetEntity.getAssetPrice(), userEntity.getUserName()});
    }

    @Override
    public void updateStatus(String assetId, String newStatus) throws Exception {
        String dataHash = blockChainUtil.sha256(assetId + newStatus);
        ChainResponse response = chainClient.submitRecord(
                assetId, assetId, "UPDATE", "system", dataHash,
                "更新状态: " + newStatus);
        saveChainRecord(assetId, "UPDATE", null, "system", dataHash, response);
    }

    @Override
    public List getAssetHistory(String assetId, Integer userId) throws Exception {
        PersonalAssetEntity personalAssetEntity = personalAssetMapper.selectOne(
                Wrappers.lambdaQuery(PersonalAssetEntity.class)
                        .eq(PersonalAssetEntity::getAssetId, assetId)
                        .eq(PersonalAssetEntity::getUserId, userId));
        if (personalAssetEntity == null) {
            return null;
        }

        String ethAssetId = personalAssetEntity.getEthAssetId();
        if (ethAssetId == null || ethAssetId.isEmpty()) {
            ethAssetId = assetId;
        }

        List<ChainRecordEntity> records = chainRecordMapper.selectList(
                new LambdaQueryWrapper<ChainRecordEntity>()
                        .eq(ChainRecordEntity::getArtifactId, ethAssetId)
                        .orderByAsc(ChainRecordEntity::getCreatedAt));

        return records.stream()
                .map(r -> {
                    if (r.getOperatorId() != null) {
                        UserEntity user = userMapper.selectById(r.getOperatorId());
                        return user != null ? user.getUserName() : r.getOperatorName();
                    }
                    return r.getOperatorName();
                })
                .collect(Collectors.toList());
    }

    @Override
    public OperateResultDTO checkBalance(String assetId, Integer userId) throws Exception {
        IAssetEntity assetEntity = assetMapper.selectOne(
                Wrappers.lambdaQuery(IAssetEntity.class).eq(IAssetEntity::getAssetId, assetId));
        UserEntity userEntity = userMapper.selectById(userId);
        if (userEntity.getUserDeposits() < assetEntity.getAssetPrice()) {
            return new OperateResultDTO(false, "用户余额不足", null);
        }
        return new OperateResultDTO(true, "用户余额足够", null);
    }

    @Override
    public OperateResultDTO isProvider(Integer userId, String assetId) throws Exception {
        IAssetEntity assetEntity = assetMapper.selectOne(
                Wrappers.lambdaQuery(IAssetEntity.class).eq(IAssetEntity::getAssetId, assetId));
        if (assetEntity == null) {
            return new OperateResultDTO(false, "资产不存在", null);
        }
        if (Objects.equals(assetEntity.getUserId(), userId)) {
            return new OperateResultDTO(false, "资产拥有者不能购买自己的资产", null);
        }
        return new OperateResultDTO(true, "不是资产拥有者", null);
    }

    private void saveChainRecord(String artifactId, String action, Integer operatorId,
                                  String operatorName, String dataHash, ChainResponse response) {
        ChainRecordEntity record = new ChainRecordEntity();
        record.setArtifactId(artifactId);
        record.setArtifactNo(artifactId);
        record.setAction(action);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setDataHash(dataHash);
        if (response != null) {
            record.setTxHash(response.getTxHash());
            record.setChainStatus(response.getStatus());
            record.setChainTime(LocalDateTime.now());
        } else {
            record.setChainStatus("FAILED");
        }
        record.setCreatedAt(LocalDateTime.now());
        chainRecordMapper.insert(record);
    }
}
