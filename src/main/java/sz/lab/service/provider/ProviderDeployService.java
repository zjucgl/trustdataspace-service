package sz.lab.service.provider;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ProviderDeployService {
    void generateAndDownload(Long providerId, HttpServletResponse response) throws IOException;
}
