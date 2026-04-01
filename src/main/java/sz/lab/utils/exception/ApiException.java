package sz.lab.utils.exception;

import sz.lab.config.constants.IApiError;

public class  ApiException extends RuntimeException{
    private Long status;
    /**
     * 异常构造方法 在使用时方便传入错误码和信息
     */
    public ApiException(Long status, String message) {
        super(message);
        this.status = status;
    }
    public ApiException(IApiError error) {
        super(error.getMessage());
        this.status = error.getStatus();
    }

    public Long getStatus() {
        return this.status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }
}
