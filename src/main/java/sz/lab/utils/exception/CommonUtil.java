package sz.lab.utils.exception;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import sz.lab.config.cfg.CommonJsonException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.List;
import java.util.Random;

/**
 * 本后台接口系统常用的json工具类
 */
public class CommonUtil {

    /**
     * 返回一个info为空对象的成功消息的json
     */
    public static JSONObject successJson() {
        return successJson(new JSONObject());
    }

    /**
     * 返回一个返回码为200的json
     */
    public static JSONObject successJson(Object info) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("status", ExceptionConstants.SUCCESS_CODE);
        resultJson.put("message", ExceptionConstants.SUCCESS_MSG);
        resultJson.put("result", info);
        return resultJson;
    }

    /**
     * 返回错误信息JSON
     */
    public static JSONObject errorJson(ErrorEnum errorEnum) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("status", errorEnum.getErrorCode());
        resultJson.put("message", errorEnum.getErrorMsg());
        resultJson.put("result", null);
        return resultJson;
    }

    /**
     * 自定义返回异常说明
     */
    public static JSONObject errorJsonStr(String msg) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("status", 10099L);
        resultJson.put("message", msg);
        resultJson.put("result", null);
        return resultJson;
    }

    public static JSONObject errorJsonStr(Integer errCode, String msg) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("status", errCode);
        resultJson.put("message", msg);
        resultJson.put("result", null);
        return resultJson;
    }

    /**
     * 查询分页结果后的封装工具方法
     *
     * @param requestJson 请求参数json,此json在之前调用fillPageParam 方法时,已经将pageRow放入
     * @param list        查询分页对象list
     * @param totalCount  查询出记录的总条数
     */
    public static JSONObject successPage(final JSONObject requestJson, List<JSONObject> list, int totalCount) {
        int pageRow = requestJson.getIntValue("pageRow");
        int totalPage = getPageCounts(pageRow, totalCount);
        JSONObject result = successJson();
        JSONObject info = new JSONObject();
        info.put("list", list);
        info.put("totalCount", totalCount);
        info.put("totalPage", totalPage);
        result.put("result", info);
        return result;
    }

    /**
     * 查询分页结果后的封装工具方法
     *
     * @param list 查询分页对象list
     */
    public static JSONObject successPage(List<JSONObject> list) {
        JSONObject result = successJson();
        JSONObject info = new JSONObject();
        info.put("list", list);
        result.put("result", info);
        return result;
    }

    /**
     * 获取总页数
     *
     * @param pageRow   每页行数
     * @param itemCount 结果的总条数
     */
    private static int getPageCounts(int pageRow, int itemCount) {
        if (itemCount == 0) {
            return 1;
        }
        return itemCount % pageRow > 0 ?
                itemCount / pageRow + 1 :
                itemCount / pageRow;
    }

    /**
     * 将request参数值转为json
     */
    public static JSONObject request2Json(HttpServletRequest request) {
        JSONObject requestJson = new JSONObject();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] pv = request.getParameterValues(paramName);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pv.length; i++) {
                if (pv[i].length() > 0) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(pv[i]);
                }
            }
            requestJson.put(paramName, sb.toString());
        }
        return requestJson;
    }

    /**
     * 获取json字符串
     *
     * @param req
     * @return
     * @throws Exception
     */
    public static JSONObject getJsonObject(HttpServletRequest req) throws Exception {
        ServletInputStream is;
        is = req.getInputStream();
        int nRead = 1;
        int nTotalRead = 0;
        byte[] bytes = new byte[10240];
        while (nRead > 0) {
            nRead = is.read(bytes, nTotalRead, bytes.length - nTotalRead);
            if (nRead > 0) {
                nTotalRead = nTotalRead + nRead;
            }
        }
        String str = new String(bytes, 0, nTotalRead, "utf-8");
        return JSON.parseObject(str);
    }


    /**
     * 获取字符串请求
     *
     * @return String
     * @throws Exception
     * @author ckd
     * @date 2021/12/1 3:31 下午
     */
    public static String getString(HttpServletRequest req) throws Exception {
        ServletInputStream is;
        is = req.getInputStream();
        int nRead = 1;
        int nTotalRead = 0;
        byte[] bytes = new byte[102400];
        while (nRead > 0) {
            nRead = is.read(bytes, nTotalRead, bytes.length - nTotalRead);
            if (nRead > 0) {
                nTotalRead = nTotalRead + nRead;
            }
        }
        return new String(bytes, 0, nTotalRead, "utf-8");
    }

    /**
     * 将request转JSON
     * 并且验证非空字段
     */
    public static JSONObject convert2JsonAndCheckRequiredColumns(HttpServletRequest request, String requiredColumns) {
        JSONObject jsonObject = request2Json(request);
        hasAllRequired(jsonObject, requiredColumns);
        return jsonObject;
    }

    /**
     * 验证是否含有全部必填字段
     *
     * @param requiredColumns 必填的参数字段名称 逗号隔开 比如"userId,name,telephone"
     */
    public static void hasAllRequired(final JSONObject jsonObject, String requiredColumns) {
        if (!ExceptionConstants.isNullOrEmpty(requiredColumns)) {
            //验证字段非空
            String[] columns = requiredColumns.split(",");
            String missCol = "";
            for (String column : columns) {
                Object val = jsonObject.get(column.trim());
                if (ExceptionConstants.isNullOrEmpty(val)) {
                    missCol += column + "  ";
                }
            }
            if (!ExceptionConstants.isNullOrEmpty(missCol)) {
                jsonObject.clear();
                jsonObject.put("status", ErrorEnum.E_90003.getErrorCode());
                jsonObject.put("message", "缺少必填参数");
                jsonObject.put("result", new JSONObject());
                throw new CommonJsonException(jsonObject);
            }
        }
    }

    /**
     * 在分页查询之前,为查询条件里加上分页参数
     *
     * @param paramObject    查询条件json
     * @param defaultPageRow 默认的每页条数,即前端不传pageRow参数时的每页条数
     */
    private static void fillPageParam(final JSONObject paramObject, int defaultPageRow) {
        int pageNum = paramObject.getIntValue("pageNum");
        pageNum = pageNum == 0 ? 1 : pageNum;
        int pageRow = paramObject.getIntValue("pageRow");
        pageRow = pageRow == 0 ? defaultPageRow : pageRow;
        paramObject.put("offSet", (pageNum - 1) * pageRow);
        paramObject.put("pageRow", pageRow);
        paramObject.put("pageNum", pageNum);
        //删除此参数,防止前端传了这个参数,pageHelper分页插件检测到之后,拦截导致SQL错误
        paramObject.remove("pageSize");
    }

//    public static Map<Long, String> placeToMap(List<PlaceEntity> placeEntities) {
//        Map<Long, String> map = new HashMap<>();
//        if (placeEntities != null && placeEntities.size() > 0) {
//            map = placeEntities.stream()
//                    .filter(t -> t.getPlace() != null)
//                    .collect(Collectors.toMap(PlaceEntity::getId, PlaceEntity::getPlace, (k1, k2) -> k2));
//        }
//        return map;
//    }


    /**
     * 信息**加密
     */
    public static String encryptionInfo(String info) {
        if (info != null && info.length() > 4) {
            return info.substring(0, 3) + "****" + info.substring(info.length() - 3);
        } else {
            return "";
        }
    }

    /**
     * 获取随机字符串
     *
     * @param length
     * @return
     */
    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 获取jsp签名
     *
     * @param str
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public static String getWxSing(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(str.getBytes(StandardCharsets.UTF_8));
        return byteToHex(crypt.digest());
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    /**
     * 分页查询之前的处理参数
     * 没有传pageRow参数时,默认每页10条.
     */
    public static void fillPageParam(final JSONObject paramObject) {
        fillPageParam(paramObject, 10);
    }
}
