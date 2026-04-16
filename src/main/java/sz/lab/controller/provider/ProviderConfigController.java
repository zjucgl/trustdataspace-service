package sz.lab.controller.provider;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sz.lab.controller.BaseController;
import sz.lab.dto.provider.ProviderConfigDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.service.provider.ProviderConfigService;
import sz.lab.service.provider.ProviderDeployService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Validated
@RequestMapping("/provider")
@RestController
public class ProviderConfigController extends BaseController {

    @Resource
    private ProviderConfigService providerConfigService;

    @Resource
    private ProviderDeployService providerDeployService;

    @PostMapping("/add")
    public OperateResultDTO add(@RequestBody ProviderConfigDTO input) {
        return providerConfigService.add(input);
    }

    @PostMapping("/list")
    public OperateResultDTO list() {
        return providerConfigService.listAll();
    }

    @GetMapping("/detail/{id}")
    public OperateResultDTO detail(@PathVariable("id") Long id) {
        return providerConfigService.detail(id);
    }

    @PostMapping("/update")
    public OperateResultDTO update(@RequestBody ProviderConfigDTO input) {
        return providerConfigService.update(input);
    }

    @PostMapping("/remove/{id}")
    public OperateResultDTO remove(@PathVariable("id") Long id) {
        return providerConfigService.remove(id);
    }

    @PostMapping("/updateStatus")
    public OperateResultDTO updateStatus(@RequestParam Long id, @RequestParam String status) {
        return providerConfigService.updateStatus(id, status);
    }

    @GetMapping("/deploy-script/{id}")
    public void downloadDeployScript(@PathVariable("id") Long id,
                                     HttpServletResponse response) throws IOException {
        providerDeployService.generateAndDownload(id, response);
    }
}
