package sz.lab.dto.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperateResultDTO {
    private boolean success;
    private String message;
    private Object result;
}
