package sz.lab.config.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2023/11/24 16:53
 */
@Getter
@AllArgsConstructor
public enum RoleEnum {

    Admin("管理员", "admin"),
    User("用户", "user"),
    ;

    private final String zhName;
    private final String code;

    private static final Map<String, RoleEnum> CODE_TO_NAME_MAP = new HashMap<>();
    static {
        for (RoleEnum roleEnum : RoleEnum.values()) {
            CODE_TO_NAME_MAP.put(roleEnum.getCode(), roleEnum);
        }
    }
    public static String getNameByCode(String code) {
        return CODE_TO_NAME_MAP.get(code).zhName;
    }
    public static RoleEnum getRoleByCode(String code) {
        return CODE_TO_NAME_MAP.get(code);
    }


}
