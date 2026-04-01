package sz.lab.mapper.basic;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import sz.lab.entity.basic.PersonalAssetEntity;
import sz.lab.entity.orga.user.UserEntity;

import java.util.List;

@Mapper
public interface PersonalAssetMapper extends BaseMapper<PersonalAssetEntity> {
    /**
     * @Description: 分页查询个人资产列表
     **/
    @Select({"<script>SELECT id, asset_id, " +
            "participant_id, endpoint_url, user_id, " +
            "authorization, gmt_create " +
            "FROM basic_personal_asset " +
            "WHERE TRUE " +
            "<if test='queryParam.assetId != null'> AND asset_id LIKE CONCAT('%', #{queryParam.assetId}, '%') </if>" +
            "<if test='queryParam.userId != null'> AND user_id = #{queryParam.userId} </if>" +
            "ORDER BY id DESC " +
            "</script>"})
    List<PersonalAssetEntity> selectPageList(@Param("queryParam") JSONObject queryParam);

    /**
     * @Description: 查询是否存在该资产
     **/
    @Select({"<script>SELECT id, asset_id, " +
            "participant_id, endpoint_url, user_id, " +
            "authorization, gmt_create " +
            "FROM basic_personal_asset " +
            "WHERE TRUE " +
            "<if test='entity.assetId != null'> AND asset_id = #{entity.assetId} </if>" +
            "<if test='entity.userId != null'> AND user_id = #{entity.userId} </if>" +
            "ORDER BY id DESC " +
            "</script>"})
    List<PersonalAssetEntity> selectByAssetId(@Param("entity") PersonalAssetEntity entity);

    /**
     * @Description: 插入个人资产信息
     **/
    @Insert({"INSERT INTO basic_personal_asset " +
            "(asset_id,eth_asset_id,participant_id, endpoint_url, user_id, authorization,gmt_create ) " +
            "VALUES (#{entity.assetId},#{entity.ethAssetId}, #{entity.participantId}, #{entity.endpointURL}, " +
            "#{entity.userId}, #{entity.authorization}, " +
            "CURRENT_TIMESTAMP)"})
    @Options(useGeneratedKeys = true, keyProperty = "entity.id", keyColumn = "id")
    void insertPersonalAsset(@Param("entity") PersonalAssetEntity entity);
}
