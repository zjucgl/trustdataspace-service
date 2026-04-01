package sz.lab.utils.exception;

/**
 * 错误码汇总
 */
public enum ErrorEnum {
    /*
     * 系统错误信息
     * */
    E_400(400L, "系统未获取到相关记录，请刷新后重试"),
    E_401(401L, "未获取到相关记录"),
    E_500(500L, "请求方式有误,请检查"),
    E_501(501L, "请求路径不存在"),
    E_502(502L, "权限不足"),
    E_503(503L, "导入表格格式有误，请参照最新的导入模板"),
    E_504(504L, "服务端异常"),

    /*
     * 业务错误信息
     */
    E_10001(10001L, ""),
    E_10038(10038L, "Excel表头与内容的列数不一致"),
    E_10039(10039L, "服务端出错，数据库字段不唯一"),
    E_10040(10040L, "表头需要两行，且列数一致"),
    E_10041(10041L, "表头不能为空"),
    E_10042(10042L, "数据为空，请检查Excel格式是否正确"),
    /*
     * 业务错误信息
     */
    E_20011(20011L, "登陆已过期,请重新登陆"),
    E_20012(20012L, "当前请求人数过多，请稍后再试"),
    E_20013(20013L, "已在其他设备登陆，请确认是否本人登陆"),
    E_20014(20014L, "获取参数信息失败，请通过浙里办app或支付宝内浙里办小程序登录"),

    /*
     * 业务错误信息
     */
    E_30001(30001L, "团队不存在"),


    /*
     * 数据校验提示
     */
    E_40001(40001L, "证件格式不符"),
    E_40002(40002L, "证件类型格式不符"),
    E_40003(40003L, "手机号格式不符"),
    E_40004(40004L, "缺少必填参数"),

    /**
     * 400xx 登录模块
     */
    CAPTCHA_CREATE_FAILED(40005L, "验证码创建失败"),
    CAPTCHA_EXPIRED(40006L, "验证码已过期，请单击刷新"),
    CAPTCHA_NOT_MATCH(40007L, "验证码错误，请重新输入"),
    ACCOUNT_DISABLED(40008L, "用户已停用，请联系管理员"),
    ACCOUNT_OR_PASSWORD_NOT_MATCH(40009L, "用户名或密码错误，请重新输入"),
    TOKEN_REFRESH_FAILED(40010L, "Token刷新失败"),
    /**
     * 403xx 测试模块
     */
    TEST_EXIST(40301L, "测试已存在"),
    TEST_NOT_EXIST(40302L, "测试不存在"),
    TEST_FATHER_NOT_EXIST(40303L, "父节点不存在"),

    E_90001(90001L, "文件大小超出系统限制，请重新选择"),
    E_90002(90002L, "缺少必要文件"),
    E_90003(90003L, "缺少必填参数"),
    E_90005(90005L, "暂无当前用户健康信息"),
    E_90006(90006L, "上传文件失败"),
;

    private final Long errorCode;

    private final String errorMsg;

    ErrorEnum(Long errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public Long getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
