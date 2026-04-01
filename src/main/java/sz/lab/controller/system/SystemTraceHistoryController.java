package sz.lab.controller.system;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sz.lab.service.trace.AssetTraceService;

import javax.annotation.Resource;
import java.util.List;

@Validated
@RequestMapping("/trace")
@RestController
public class SystemTraceHistoryController {
    @Resource
    private AssetTraceService assetTraceService;

    @GetMapping("/getAssetHistory")
    public List<Object> getAssetHistory(@RequestParam("assetId") String assetId, @RequestParam("userId") Integer userId) throws Exception {

        return assetTraceService.getAssetHistory(assetId, userId);
    }
}
