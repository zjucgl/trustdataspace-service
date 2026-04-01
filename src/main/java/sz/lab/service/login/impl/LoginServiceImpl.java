package sz.lab.service.login.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import sz.lab.config.cfg.CommonJsonException;
import sz.lab.config.constants.ApiError;
import sz.lab.dto.login.*;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.orga.dept.DeptEntity;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.entity.orga.user.UserRoleEntity;
import sz.lab.entity.system.RoleEntity;
import sz.lab.entity.system.SystemFunctionEntity;
import sz.lab.mapper.login.LoginMapper;
import sz.lab.mapper.orga.dept.DeptMapper;
import sz.lab.mapper.orga.user.UserRoleMapper;
import sz.lab.mapper.system.function.SystemFunctionMapper;
import sz.lab.mapper.system.role.RoleMapper;
import sz.lab.service.login.LoginService;
import sz.lab.service.orga.user.UserService;
import sz.lab.service.system.menu.SystemMenuService;
import sz.lab.utils.JWTUtil;
import sz.lab.utils.RedisUtil;
import sz.lab.utils.exception.ApiException;
import sz.lab.utils.exception.ErrorEnum;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: 宋光慧
 * @Date: 2023/07/26 15:53
 **/
@Slf4j
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    // 验证码超期时间
    @Value(value = "${custom-attribute.verification-code-expire}")
    private Integer verificationCodeExpire;
    // Token超期时间
    @Value(value = "${custom-attribute.jwt.token-expire}")
    private Integer tokenExpire;
    //锁定时间
    @Value(value= "${custom-attribute.lock-time}")
    private Integer lockTime;
    //密码重置时限
    @Value(value= "${custom-attribute.pwd-reset-time}")
    private Integer pwdResetTime;
    @Resource
    private Producer kaptchaProducer;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private LoginMapper loginMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private DeptMapper deptMapper;
    @Resource
    private UserService userService;
    @Resource
    private SystemMenuService systemMenuService;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private SystemFunctionMapper functionMapper;

    @Override
    public OperateResultDTO getKaptcha() throws Exception {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage bufferedImage = kaptchaProducer.createImage(text);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", outputStream);

        String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        String kaptchaBase64 = "data:image/png;base64," + base64.replaceAll("\r\n", "");
        // 存入Redis数据库
        String randomId = RandomStringUtils.random(15, true, true);
        redisUtil.set(randomId, text, verificationCodeExpire);
        // 生成验证码返回数据
        KaptchaDTO kaptchaDTO = new KaptchaDTO();
        kaptchaDTO.setImgUuid(randomId);
        kaptchaDTO.setImgCode(kaptchaBase64);
        return new OperateResultDTO(true,"成功", kaptchaDTO);
    }

    @Override
    public OperateResultDTO check(LoginRequestDTO loginRequestDTO) {
        OperateResultDTO opResultDTO = new OperateResultDTO(false,"账号不存在",false);
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setPwdExpire(false);
        //首先判断用户输入的账号是否存在，若不存在则直接返回给前端。
        LoginDTO loginDTO = loginMapper.getByLoginCode(loginRequestDTO.getLoginCode());
//        AccountEntity account = accountMapper.selectOne(Wrappers.lambdaQuery(AccountEntity.class)
//                .eq(AccountEntity::getLoginCode,loginRequestDTO.getLoginCode())
//                .last("limit 1"));
//        if(account==null){
//            return opResultDTO;
//        }
        if(loginDTO==null){
            return opResultDTO;
        }
        if(loginDTO.getVerifyStatus().equals(0)){
            opResultDTO.setMessage("该账号未通过审核，请联系管理员");
            return opResultDTO;
        }
        long remainingTime = CalculateRemainingTime(lockTime, loginDTO);
        //判断redis是否有该key存在
        Boolean isExist = redisUtil.hasKey(loginDTO.getLoginCode());
        //如果存在判断错误次数是否大于等于5,如果大于等于5,则直接返回
        if(isExist){
            Boolean isExpire = redisUtil.isExpire(loginDTO.getLoginCode());
            if(isExpire){
                //过期则删除redis保存的key
                redisUtil.del(loginDTO.getLoginCode());
            }else{
                if (Integer.parseInt(redisUtil.get(loginDTO.getLoginCode()).toString())>=5){
//                throw new ApiException(500L,"该账号被锁定，"+remainingTime+"分钟后再试");
                    opResultDTO.setMessage("该账号被锁定，"+remainingTime+"分钟后再试");
                    return opResultDTO;
                }
            }
        }
        if (loginRequestDTO.getImgUuid() == null || redisUtil.get(loginRequestDTO.getImgUuid()) == null) {
            opResultDTO.setMessage("验证码已过期，请单击刷新");
            opResultDTO.setResult(loginResponseDTO);
        } else if (!((String) redisUtil.get(loginRequestDTO.getImgUuid())).equalsIgnoreCase(loginRequestDTO.getVerifyCode())) {
            opResultDTO.setMessage("验证码错误，请重新输入");
            opResultDTO.setResult(loginResponseDTO);
        } else {
            if (loginDTO.getIsDeleted().equals(1)) {
                opResultDTO.setMessage("用户状态已停用，请联系管理员");
                opResultDTO.setResult(loginResponseDTO);
            }
//            else if (loginDTO.getPwdErrorNum() == 3 && remainingTime > 0) {// 密码错误次数达到3, 并且账号违背锁定
//                opResultDTO.setMessage("账号已锁定, " + remainingTime + "分钟后重试");
//                log.info("账号解锁时间剩余 {} min", remainingTime);
//                opResultDTO.setResult(loginResponseDTO);
//            }
            else {
                UserEntity userEntity = new UserEntity();
                userEntity.setUserId(loginDTO.getUserId());
                userEntity.setGmtLastLogin(LocalDateTime.now());
                // 密码错误
                if (!loginDTO.getLoginPwd().equals(loginRequestDTO.getLoginPwd())) {
                    //userService.updateById(userEntity);
                    if(isExist){
                        //redis中有此key,获取key的数值并加1
                        Integer errNum = Integer.parseInt(redisUtil.get(loginDTO.getLoginCode()).toString());
                        errNum++;
                        redisUtil.set(loginDTO.getLoginCode(), errNum + "",lockTime*60);
                    } else {
                        //redis中没有此key,密码是第一次错误,根据用户的账号设置一个独属于该账号的key值
                        redisUtil.set(loginDTO.getLoginCode(), 1 + "");
                        //redis中设置key的过期时间
                        redisUtil.expire(loginDTO.getLoginCode(), lockTime*60);
                    }
                    Integer errNum =Integer.parseInt(redisUtil.get(loginDTO.getLoginCode()).toString());
                    if (errNum>=5){
                        opResultDTO.setMessage("密码错误5次，账号已被锁定"+lockTime+"分钟");
                    }else{
//                        throw new ApiException(ApiError.PASSWORD_NOT_MATCH);
                        opResultDTO.setMessage("密码错误,错误"+errNum+"次，5次将锁定账号" );
                    }
                } else { //密码正确
                    //登录成功则删除redis保存的key
                    redisUtil.del(loginDTO.getLoginCode());
                    //判断密码更新功能是否开启，再判断密码是否需要重置
                    if (isResetPwd(loginDTO)){
                        loginResponseDTO.setPwdExpire(true);
                        loginResponseDTO.setUserId(loginDTO.getUserId());
                        opResultDTO.setSuccess(false);
                        opResultDTO.setMessage("距离上次修改密码已"+pwdResetTime+"天未修改，请立即修改");
                        opResultDTO.setResult(loginResponseDTO);
                        return opResultDTO;
                    }
                    // 记录用户登录成功的时间
                    userEntity.setGmtLastLogin(LocalDateTime.now());
                    //userService.updateById(userEntity);
                    loginMapper.updateLastLogin(userEntity.getUserId(),userEntity.getGmtLastLogin());
                    log.info("用户<{}>登录成功,时间{} ", loginDTO.getUserName(), userEntity.getGmtLastLogin());
                    opResultDTO.setMessage("用户登录成功");
                    loginResponseDTO.setUsername(loginDTO.getUserName());
                    //获取账号角色列表
//                    List<Integer> roleIds = userRoleMapper.selectList(Wrappers.lambdaQuery(UserRoleEntity.class)
//                            .eq(UserRoleEntity::getUserId,loginDTO.getUserId()))
//                            .stream()
//                            .map(UserRoleEntity::getRoleId)
//                            .collect(Collectors.toList());
                    List<Integer> roleIds = userRoleMapper.selectRoleIdsByUserId(loginDTO.getUserId());
                    Map<Integer,RoleEntity> roleMap = getRoleMap();
                    List<LoginRoleDTO> roleDTOS = new ArrayList<>();
//                    DeptEntity deptEntity = deptMapper.selectById(loginDTO.getDeptId());
                    DeptEntity deptEntity = deptMapper.getDeptById(loginDTO.getDeptId());
                    Integer index = 0;
                    // 循环角色列表，获取角色信息
                    for (Integer roleId : roleIds) {
                        String role = roleMap.get(roleId).getRoleCode();
                        LoginRoleDTO loginRoleDTO = new LoginRoleDTO();
                        loginRoleDTO.setRoles(Collections.singletonList(role));
                        loginRoleDTO.setUserId(loginDTO.getUserId());
                        loginRoleDTO.setUsername(loginDTO.getUserName());
                        loginRoleDTO.setNickname(loginDTO.getUserName());
                        loginRoleDTO.setRoleName(roleMap.get(roleId).getRoleName());
                        loginRoleDTO.setDeptId(loginDTO.getDeptId());
                        if(deptEntity!=null){
                            loginRoleDTO.setDeptName(deptEntity.getDeptName());
                            loginRoleDTO.setParticipantId(deptEntity.getParticipantId());
                        }
                        loginRoleDTO.setAvatar("https://avatars.githubusercontent.com/u/44761321");
                        roleDTOS.add(loginRoleDTO);
                        TokenDTO tokenDTO = new TokenDTO();
                        // 生成签名DTO，包含账号(用户)ID，可以根据需要自行添加
                        tokenDTO.setUserId(loginDTO.getUserId());
                        tokenDTO.setRoleCodes(new String[]{role});
                        loginRoleDTO.setAccessToken(JWTUtil.createSign(tokenDTO.toString(), tokenExpire));
                        loginRoleDTO.setRefreshToken(JWTUtil.createSign(tokenDTO.toString(), 2 * tokenExpire));
                        roleDTOS.set(index,loginRoleDTO);
                        // 计算超期时间
                        Date date = new Date();
                        Calendar now = Calendar.getInstance();
                        now.setTime(date);
                        now.add(Calendar.MINUTE, tokenExpire);
                        Date dateExpire = now.getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            Date expires = sdf.parse(sdf.format(dateExpire));
                            loginRoleDTO.setExpires(expires);
                            roleDTOS.set(index,loginRoleDTO);
                        } catch (ParseException e) {
                            log.error("登录失败：{}", e.getMessage());
                            log.error(Arrays.toString(e.getStackTrace()));
                            throw new CommonJsonException(ErrorEnum.E_504);
                        }
                        index +=1;
                    }
                    // 全部角色信息赋值
                    loginResponseDTO.setRoles(roleDTOS);
                    //通用返回类赋值
                    opResultDTO.setSuccess(true);
                    opResultDTO.setMessage("登录成功");
                    opResultDTO.setResult(loginResponseDTO);
                }
            }
        }
        return opResultDTO;
    }
    /**
     * @Description: 计算当前时间与最后登录时间的差值
     */
    private long CalculateRemainingTime(int lockTime, LoginDTO loginDTO)
    {
        Date lastLoginDate = loginDTO.getGmtLastLogin();
        long lastLoginTimeMillis = lastLoginDate.getTime();
        long currentTimeMillis = System.currentTimeMillis();
        long timeDifferenceMillis = currentTimeMillis - lastLoginTimeMillis; // 计算时间差（毫秒）
        return lockTime - timeDifferenceMillis / (1000 * 60); // 将时间差转换为分钟
    }

    @Override
    public OperateResultDTO refreshToken(String refreshToken) {
        TokenDTO tokenDTO = JWTUtil.verifyToken(refreshToken);
        if (tokenDTO == null || tokenDTO.getUserId() == null || tokenDTO.getUserId() < 0) {
            throw new CommonJsonException(ErrorEnum.TOKEN_REFRESH_FAILED);
        } else {
            RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO();
            refreshTokenDTO.setAccessToken(JWTUtil.createSign(tokenDTO.toString(), tokenExpire));
            refreshTokenDTO.setRefreshToken(JWTUtil.createSign(tokenDTO.toString(), 2 * tokenExpire));
            // 计算超期时间
            Date date = new Date();
            Calendar now = Calendar.getInstance();
            now.setTime(date);
            now.add(Calendar.MINUTE, tokenExpire);
            Date dateExpire = now.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date expires = sdf.parse(sdf.format(dateExpire));
                refreshTokenDTO.setExpires(expires);
            } catch (ParseException e) {
                log.error("刷新Token失败：{}", e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
                throw new ApiException(ApiError.SYSTEM_ERROR);
            }
            return new OperateResultDTO(true,"成功", refreshTokenDTO);
        }
    }

    @Override
    public OperateResultDTO getAsyncRoutes(Integer userId) {
        return systemMenuService.getAsyncRoutes(userId);
    }

    private Map<Integer,RoleEntity> getRoleMap(){
        return roleMapper.selectRoleList()
                .stream()
                .collect(Collectors.toMap(RoleEntity::getRoleId,role->role));
    }
    private Boolean isResetPwd(LoginDTO user){
        SystemFunctionEntity functionEntity = functionMapper.selectFunctionByCode("pwd_update");
        if(functionEntity.getFunctionStatus()==0){
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastUpdate = user.getPwdLastUpdate();
        //是否过期，过期返回true，没过期返回false
        return lastUpdate.isBefore(now.minusDays(pwdResetTime));
    }
}
