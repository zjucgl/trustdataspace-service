package sz.lab.dto.login.route;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class RouteDTO {
    private String path;
    private JSONObject meta;
    private List<JSONObject> children;
}
