package sz.lab.dto.system;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableRequestDTO {
    private Integer pageNo;
    private Integer pageSize;
//    private Integer currentPage;
    private JSONObject jsonParam;
}
