package cn.iocoder.yudao.module.system.dal.mysql.user;

import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jodd.util.StringPool;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper extends BaseMapperX<UserDO> {

    default UserDO selectByUsername(String username) {
        return selectOne(new LambdaQueryWrapper<UserDO>().eq(UserDO::getUsername, username));
    }

    default UserDO selectByEmail(String email) {
        return selectOne(new LambdaQueryWrapper<UserDO>().eq(UserDO::getEmail, email));
    }

    default UserDO selectByMobile(String mobile) {
        return selectOne(new LambdaQueryWrapper<UserDO>().eq(UserDO::getMobile, mobile));
    }

    default PageResult<UserDO> selectPage(UserPageReqVO reqVO, Collection<Long> deptIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserDO>()
                .likeIfPresent(UserDO::getUsername, reqVO.getUsername())
                .likeIfPresent(UserDO::getMobile, reqVO.getMobile())
                .eqIfPresent(UserDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(UserDO::getCreateTime, reqVO.getBeginTime(), reqVO.getEndTime())
                .inIfPresent(UserDO::getDeptId, deptIds));
    }

    default List<UserDO> selectList(UserExportReqVO reqVO, Collection<Long> deptIds) {
        return selectList(new LambdaQueryWrapperX<UserDO>()
                .likeIfPresent(UserDO::getUsername, reqVO.getUsername())
                .likeIfPresent(UserDO::getMobile, reqVO.getMobile())
                .eqIfPresent(UserDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(UserDO::getCreateTime, reqVO.getBeginTime(), reqVO.getEndTime())
                .inIfPresent(UserDO::getDeptId, deptIds));
    }

    default List<UserDO> selectListByNickname(String nickname) {
        return selectList(new LambdaQueryWrapperX<UserDO>().like(UserDO::getNickname, nickname));
    }

    default List<UserDO> selectListByUsername(String username) {
        return selectList(new LambdaQueryWrapperX<UserDO>().like(UserDO::getUsername, username));
    }

    default List<UserDO> selectListByStatus(Integer status) {
        return selectList(UserDO::getStatus, status);
    }

    default List<UserDO> selectListByDeptIds(Collection<Long> deptIds) {
        return selectList(UserDO::getDeptId, deptIds);
    }

}

