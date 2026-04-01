package sz.lab.config.constants;

public enum ApiError implements IApiError{
    SUCCESS(200L, "成功"),
    SYSTEM_ERROR(500L, "服务端异常"),
    LOGIN_CODE_ERROR(42104L, "验证码错误"),
    LOGIN_CODE_EXPIRE(42105L, "验证码过期"),
    LOGIN_NAME_NOT_EXIST(42106L, "用户名不存在"),
    PASSWORD_NOT_MATCH(42107L, "密码错误"),
    API_DISABLED(42108L, "API已被禁用"),
    ACCOUNT_DISABLED(42109L, "账号已被禁用"),
    HTTP_BASIC_AUTH_ERROR(42110L, "未通过HTTP Basic Auth"),
    LOGIN_AUTH_ERROR(42111L, "登录认证失败"),
    PERMISSION_AUTH_ERROR(42112L, "权限认证失败"),
    ROLE_AUTH_ERROR(42113L, "角色认证失败"),
    SAFE_AUTH_ERROR(42114L, "二级认证失败"),
    SAME_TOKEN_AUTH_ERROR(42115L, "Same-Token认证失败"),
    SA_SIGN_ERROR(42116L, "Sa Api参数签名异常"),
    TICKET_ERROR(42117L, "ticket无效"),
    PARAMS_TYPE_ERROR(90004L, "参数类型异常"),
    PARAMS_READ_ERROR(90005L, "参数读取异常"),
    PARAMS_VALID_ERROR(90006L, "参数验证异常"),
    SERVER_BUSY(90007L, "服务器繁忙，请勿重复请求"),
    SERVER_UNDER_MAINTENANCE(90008L, "系统维护升级中，此模块暂时不可用"),
    PAGE_MANDATORY_NEED(90009L, "接口必须分页"),
    PAGE_OVER_LIMIT(90010L, "超过分页限制");

    private final Long status;
    private final String message;

    private ApiError(Long status, String message) {
        this.status = status;
        this.message = message;
    }

    public Long getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}
