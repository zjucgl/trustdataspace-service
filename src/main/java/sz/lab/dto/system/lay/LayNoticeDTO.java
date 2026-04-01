package sz.lab.dto.system.lay;

import lombok.Data;

@Data
public class LayNoticeDTO {
    private String avatar;
    private String title;
    private String datetime;
    private String type;
    private String description;
    private String status;
    private String extra;

    public LayNoticeDTO() {
        this.avatar = "";
        this.title = "";
        this.datetime = "";
        this.type = "";
        this.description = "";
        this.status = "primary";
        this.extra = "";
    }
}
