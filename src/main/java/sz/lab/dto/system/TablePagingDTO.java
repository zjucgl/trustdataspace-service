package sz.lab.dto.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TablePagingDTO {
    private Integer pageNo;
    private Integer pageSize;
//    private Integer currentPage;
    private Long totalCount;
    private List data;
}
