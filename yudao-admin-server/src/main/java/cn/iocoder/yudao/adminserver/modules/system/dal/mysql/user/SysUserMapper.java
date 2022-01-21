package cn.iocoder.yudao.adminserver.modules.system.dal.mysql.user;

import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.user.SysUserExportReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.user.SysUserPageReqVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
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
public interface SysUserMapper extends BaseMapperX<SysUserDO> {

    default SysUserDO selectByUsername(String username) {
        return selectOne(new LambdaQueryWrapper<SysUserDO>().eq(SysUserDO::getUsername, username));
    }

    default SysUserDO selectByEmail(String email) {
        return selectOne(new LambdaQueryWrapper<SysUserDO>().eq(SysUserDO::getEmail, email));
    }

    default SysUserDO selectByMobile(String mobile) {
        return selectOne(new LambdaQueryWrapper<SysUserDO>().eq(SysUserDO::getMobile, mobile));
    }

    default PageResult<SysUserDO> selectPage(SysUserPageReqVO reqVO, Collection<Long> deptIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SysUserDO>()
                .likeIfPresent(SysUserDO::getUsername, reqVO.getUsername())
                .likeIfPresent(SysUserDO::getMobile, reqVO.getMobile())
                .eqIfPresent(SysUserDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SysUserDO::getCreateTime, reqVO.getBeginTime(), reqVO.getEndTime())
                .inIfPresent(SysUserDO::getDeptId, deptIds));
    }

    default List<SysUserDO> selectList(SysUserExportReqVO reqVO, Collection<Long> deptIds) {
        return selectList(new LambdaQueryWrapperX<SysUserDO>()
                .likeIfPresent(SysUserDO::getUsername, reqVO.getUsername())
                .likeIfPresent(SysUserDO::getMobile, reqVO.getMobile())
                .eqIfPresent(SysUserDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SysUserDO::getCreateTime, reqVO.getBeginTime(), reqVO.getEndTime())
                .inIfPresent(SysUserDO::getDeptId, deptIds));
    }

    default List<SysUserDO> selectListByNickname(String nickname) {
        return selectList(new LambdaQueryWrapperX<SysUserDO>().like(SysUserDO::getNickname, nickname));
    }

    default List<SysUserDO> selectListByUsername(String username) {
        return selectList(new LambdaQueryWrapperX<SysUserDO>().like(SysUserDO::getUsername, username));
    }

    // TODO 芋艿：可废弃该方法
    default List<SysUserDO> selectListByDepartIdAndPostId(Long departId, Long postId) {
        return selectList(new LambdaQueryWrapperX<SysUserDO>()
                .eq(SysUserDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .eq(SysUserDO::getDeptId, departId)
                // TODO @jason: 封装一个 StringUtils .toString 。如果空的时候，设置为 null。会更简洁
                .likeIfPresent(SysUserDO::getPostIds, Optional.ofNullable(postId).map(t -> String.valueOf(postId)).orElse(StringPool.EMPTY)));
    }

    default List<SysUserDO> selectListByStatus(Integer status) {
        return selectList(SysUserDO::getStatus, status);
    }

    default List<SysUserDO> selectListByDeptIds(Collection<Long> deptIds) {
        return selectList(SysUserDO::getDeptId, deptIds);
    }

}

