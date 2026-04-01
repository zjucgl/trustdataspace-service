package sz.lab.controller.system;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sz.lab.controller.BaseController;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.asset.IAssetDTO;
import sz.lab.service.system.asset.AssetService;

import javax.annotation.Resource;

/**
 * 资产管理
 */
@Validated
@RequestMapping("/asset")
@RestController
public class AssetController extends BaseController {
    @Resource
    private AssetService assetService;
    @PostMapping("/updatePrice")
    public OperateResultDTO updatePrice(@RequestBody IAssetDTO input)
    {
        return assetService.updateAssetprice(input.getAssetId(),input.getAssetPrice());
    }
    @PostMapping("/updateHasPolicy")
    public OperateResultDTO updateHasPolicy(@RequestBody IAssetDTO input)
    {
        return assetService.updateHasPolicy(input.getAssetId(),input.getHasPolicy());
    }
    @GetMapping("/getAssetPriceById/{id}")
    public OperateResultDTO getAssetPriceById(@PathVariable("id") String assetId)
    {
        return assetService.getAssetPricebyId(assetId);
    }
    @GetMapping("/queryAsset")
    public OperateResultDTO queryAsset()
    {
        return assetService.queryAsset();
    }
    @PostMapping("/createAsset")
    public OperateResultDTO createAsset(@RequestBody IAssetDTO input) {
        input.setUserId(userId.get());
        return assetService.createAsset(input);
    }
    @PostMapping("/deleteAsset")
    public OperateResultDTO deleteAsset(@RequestBody IAssetDTO input) {
        return assetService.deleteAsset(input);
    }

    @GetMapping("/queryAssetByCatalog")
    public OperateResultDTO queryAssetByCatalog()
    {
        return assetService.queryAssetByCatalog();
    }

    @PostMapping("/queryAssetByUserId")
    public OperateResultDTO queryAssetByUserId()
    {
        return assetService.queryAssetByUserId(userId.get());
    }
    @PostMapping("/queryAssetByBoard")
    public OperateResultDTO queryAssetByBoard()
    {
        return assetService.queryAssetByBoard(userId.get());
    }

    @PostMapping("/updateFile")
    public OperateResultDTO updateFile(@RequestBody IAssetDTO input) throws Exception {
        return assetService.updateFile(input);
    }
}
