package sz.lab.utils;

import com.alibaba.fastjson2.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sz.lab.dto.login.TokenDTO;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * @Description: Token生成工具
 * @Author: 宋光慧
 * @Date 2023/07/16
 **/
@Component
public class JWTUtil {


    private static String secretKey = "ADB8E3D5838A0AE8E274014928CE2CEE";

    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);

    /**
     * 生成签名
     *
     * @param seed 种子值
     * @return 加密的Token
     */
    public static String createSign(String seed, Integer expire) {
        Date iatDate = new Date();
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.MINUTE, expire);
        Date expiresDate = nowTime.getTime();

        return JWT.create()
                .withClaim("seed", null == seed ? null : seed)
                .withIssuedAt(iatDate)           // sign time
                .withExpiresAt(expiresDate)      // expire time
                .sign(Algorithm.HMAC256(secretKey));
    }

    /**
     * 校验Token是否正确
     *
     * @param token 密钥
     * @return 是否正确
     */
    public static TokenDTO verifyToken(String token) {
        //根据传来的Token获取Seed
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();

        try {
            TokenDTO tokenDTO = new TokenDTO();
            Map<String, Claim> claims = verifier.verify(token).getClaims();
            Claim seed_claim = claims.get("seed");
            String jsonSign = seed_claim.asString();
            JSONObject jsonObject = JSONObject.parseObject(jsonSign);
            tokenDTO.setUserId(jsonObject.getInteger("userId"));
            tokenDTO.setRoleCodes(jsonObject.getJSONArray("roleCodes").toArray(new String[0]));
            return tokenDTO;
        } catch (Exception ignored) {
//            tokenDTO.setUserId(-1);
//            logger.error("Token验证======>" + tokenDTO.toString());
//            logger.error(ignored.toString());
        }
        return null;
    }

    /**
     * 无需secret解密，获得Token中的Seed信息
     *
     * @param token 密钥
     * @return Token中包含的种子值，通常是用户ID
     */
    public static String getSeed(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        Map<String, Claim> claims = verifier.verify(token).getClaims();
        Claim seed_claim = claims.get("seed");
        return seed_claim.asString();
    }

}
