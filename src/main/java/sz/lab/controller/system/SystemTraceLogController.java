package sz.lab.controller.system;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sz.lab.controller.BaseController;
import sz.lab.dto.system.TablePagingDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.service.system.log.ISysyemTraceLogService;

import javax.annotation.Resource;


@Validated
@RequestMapping("/traceLog")
@RestController
public class SystemTraceLogController  extends BaseController {
    @Resource
    private ISysyemTraceLogService sysTraceLogService;

    @PostMapping("/listById")
    public TablePagingDTO listById(@RequestBody TableRequestDTO tableRequestDTO) {
        return sysTraceLogService.getTraceLogById(tableRequestDTO, "traceLog");
    }
}
