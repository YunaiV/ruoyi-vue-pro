package cn.iocoder.yudao.module.system.dal.mysql.dept;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface DeptMapper extends BaseMapperX<DeptDO> {

    default List<DeptDO> selectList(DeptListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<DeptDO>().likeIfPresent(DeptDO::getName, reqVO.getName())
                .eqIfPresent(DeptDO::getStatus, reqVO.getStatus()));
    }

    default DeptDO selectByParentIdAndName(Long parentId, String name) {
        return selectOne(new LambdaQueryWrapper<DeptDO>().eq(DeptDO::getParentId, parentId)
                .eq(DeptDO::getName, name));
    }

    default Integer selectCountByParentId(Long parentId) {
        return selectCount(DeptDO::getParentId, parentId);
    }

    @Select("SELECT id FROM system_dept WHERE update_time > #{maxUpdateTime} LIMIT 1")
    @InterceptorIgnore(tenantLine = "true") // 该方法忽略多租户。原因：该方法被异步 task 调用，此时获取不到租户编号
    Long selectExistsByUpdateTimeAfter(Date maxUpdateTime);

    // TODO 芋艿：后续想想，有没可能优化下。大体思路，是支持某个方法，忽略租户
    @Select("SELECT * FROM system_dept")
    @InterceptorIgnore(tenantLine = "true")
    List<DeptDO> selectListIgnoreTenant();

}
