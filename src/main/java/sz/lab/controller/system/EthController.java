package sz.lab.controller.system;

import org.springframework.web.bind.annotation.*;
import sz.lab.controller.BaseController;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.service.system.ethnode.ISystemNodeinfoService;
import sz.lab.service.trace.AssetTraceService;

import javax.annotation.Resource;

@RestController
@RequestMapping("/eth")
public class EthController extends BaseController {

    @Resource
    private ISystemNodeinfoService systemNodeinfoService;

    @Resource
    private AssetTraceService assetTraceService;

    @RequestMapping("/get/NodeList")
    public OperateResultDTO getNodeList(@RequestParam("nodeName") String nodeName) {
        return systemNodeinfoService.getNodeList(nodeName);
    }

    @RequestMapping("/check/balance")
    public OperateResultDTO checkBalance(@RequestParam("assetId") String assetId) throws Exception {
        Integer userid = userId.get();
        return assetTraceService.checkBalance(assetId, userid);
    }
}
