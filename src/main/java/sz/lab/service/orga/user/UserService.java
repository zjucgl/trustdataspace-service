package sz.lab.service.orga.user;


import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.orga.UserDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.dto.system.TableRequestDTO;
import sz.lab.entity.orga.user.UserEntity;

import java.util.List;

/**
 * <p>
 * 用户信息表，用于记录用户账号信息。 服务类
 * </p>
 */
public interface UserService extends IService<UserEntity> {
    /**
     * @Description: 分页查询
     **/
    OperateResultDTO pageList(TableRequestDTO tableRequestDTO,Integer userId,String type);
    /**
     * @Description: 角色下拉选项查询
     **/
    OperateResultDTO roleOptionList();
    /**
     * @Description: 用户下拉选项查询
     **/
    OperateResultDTO userOptionList();
    /**
     * @Description: 参与者下拉选项查询
     **/
    OperateResultDTO participantOptionList();
    /**
     * @Description: 新增用户
     **/
    OperateResultDTO add(UserDTO userDTO) throws Exception ;
    /**
     * @Description: 修改用户
     **/
    OperateResultDTO update(UserDTO userDTO);
    /**
     * @Description: 删除用户
     **/
    OperateResultDTO delete(List<Integer> ids);
    /**
     * @Description: 判断账号是否存在
     **/
    OperateResultDTO codeIsExist(UserDTO input);


    /**
     * 更新用户余额和资产剩余数量
     * @param userId 用户id
     * @param assetId 资产ID
     * @return {@link OperateResultDTO }
     * @throws Exception
     */
    OperateResultDTO updateprice(Integer userId, String assetId) throws Exception;

    /**
     * @Description: 更新资产数量
     * @param assetId
     * @return {@link OperateResultDTO }
     */
    OperateResultDTO updatequantity(String assetId);

    /**
     * 获取用户余额
     * @param userId 用户Id
     * @return {@link OperateResultDTO }
     */
    OperateResultDTO getUserDeposits(Integer userId);

}
