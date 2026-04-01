package sz.lab.config.cfg;


import com.alibaba.fastjson2.JSONObject;
import sz.lab.utils.exception.CommonUtil;
import sz.lab.utils.exception.ErrorEnum;

/**
 * 拦截器可以统一拦截此错误,将其中json返回给前端
 */
public class CommonJsonException extends RuntimeException {
    private final JSONObject resultJson;

    /**
     * 调用时可以在任何代码处直接throws这个Exception,
     * 都会统一被拦截,并封装好json返回给前台
     *
     * @param errorEnum 以错误的ErrorEnum做参数
     */
    public CommonJsonException(ErrorEnum errorEnum) {
        this.resultJson = CommonUtil.errorJson(errorEnum);
    }

    public CommonJsonException(String errorMsg) {
        this.resultJson = CommonUtil.errorJsonStr(errorMsg);
    }

    public CommonJsonException(JSONObject resultJson) {
        this.resultJson = resultJson;
    }

    public JSONObject getResultJson() {
        return resultJson;
    }
}
