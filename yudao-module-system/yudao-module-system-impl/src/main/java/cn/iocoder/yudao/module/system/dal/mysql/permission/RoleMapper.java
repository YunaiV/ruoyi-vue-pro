package cn.iocoder.yudao.module.system.dal.mysql.permission;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RoleExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapperX<RoleDO> {

    default PageResult<RoleDO> selectPage(RolePageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<RoleDO>().likeIfPresent("name", reqVO.getName())
                .likeIfPresent("code", reqVO.getCode())
                .eqIfPresent("status", reqVO.getStatus())
                .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime()));
    }

    default List<RoleDO> listRoles(RoleExportReqVO reqVO) {
        return selectList(new QueryWrapperX<RoleDO>().likeIfPresent("name", reqVO.getName())
                .likeIfPresent("code", reqVO.getCode())
                .eqIfPresent("status", reqVO.getStatus())
                .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime()));
    }

    default RoleDO selectByName(String name) {
        return selectOne(new QueryWrapperX<RoleDO>().eq("name", name));
    }

    default RoleDO selectByCode(String code) {
        return selectOne(new QueryWrapperX<RoleDO>().eq("code", code));
    }

    default List<RoleDO> selectListByStatus(@Nullable Collection<Integer> statuses) {
        return selectList(new LambdaQueryWrapperX<RoleDO>().inIfPresent(RoleDO::getStatus, statuses));
    }

    @InterceptorIgnore(tenantLine = "true") // 该方法忽略多租户。原因：该方法被异步 task 调用，此时获取不到租户编号
    default boolean selectExistsByUpdateTimeAfter(Date maxUpdateTime) {
        return selectOne(new QueryWrapper<RoleDO>().select("id")
                .gt("update_time", maxUpdateTime).last("LIMIT 1")) != null;
    }

}
