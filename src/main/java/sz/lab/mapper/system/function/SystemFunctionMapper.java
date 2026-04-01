package sz.lab.mapper.system.function;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import sz.lab.entity.system.SystemFunctionEntity;

@Mapper
public interface SystemFunctionMapper extends BaseMapper<SystemFunctionEntity> {
    /**
     * @description: 根据功能代码查询功能信息
     */
    @Select({"SELECT function_id, function_code, function_name, function_status, " +
            "function_info, gmt_create " +
            "FROM system_function " +
            "WHERE function_code = #{functionCode} LIMIT 1"})
    SystemFunctionEntity selectFunctionByCode(@Param("functionCode") String functionCode);
    /**
     * @Description: 更新系统功能状态
     **/
    @Update({"UPDATE system_function SET " +
            "function_status = #{functionEntity.functionStatus} " +
            "WHERE function_id = #{functionEntity.functionId} "})
    void updateFunctionStatus(@Param("functionEntity") SystemFunctionEntity functionEntity);
}
