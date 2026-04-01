package sz.lab.mapper.orga.dept;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sz.lab.entity.orga.dept.DeptEntity;

import java.util.List;


/**
 * <p>
 * 部门（实验室）信息表，公司ID和部门ID在配置文件中设置，以实验室为基本使用单位，设计实验室支持二级组织架构，不支持三级，程序中提示加锁定 Mapper 接口
 * </p>
 *
 * @author ckd
 * @since 2023-11-24
 */
@Mapper
public interface DeptMapper extends BaseMapper<DeptEntity> {
    /**
     * @Description: 根据部门ID查询部门信息
     **/
    @Select({"SELECT dept_id, participant_id, dept_name, dept_info, dept_father, dept_sort, " +
            "gmt_create, gmt_modify " +
            "FROM orga_dept " +
            "WHERE is_deleted != 1 " +
            "AND dept_id = #{deptId}"})
    DeptEntity getDeptById(@Param("deptId") Integer deptId);

    @Select({"<script>SELECT dept_id,participant_id, dept_name, dept_info, " +
            "dept_father, dept_sort, " +
            "gmt_create, gmt_modify " +
            "FROM orga_dept " +
            "WHERE is_deleted != 1 " +
            "AND dept_id IN " +
            "<foreach item='deptId' index='index' collection='deptIds' open='(' separator=',' close=')'>" +
            "#{deptId}" +
            "</foreach>" +
            "</script>"})
    List<DeptEntity> selectListByDeptIds(@Param("deptIds") List<Integer> deptIds);

}
