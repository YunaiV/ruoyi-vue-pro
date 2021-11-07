package cn.iocoder.yudao.adminserver.modules.system.dal.mysql.user;

import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.user.SysUserBaseVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.user.SysUserExportReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.user.SysUserPageReqVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.logging.log4j.util.Strings;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Mapper
public interface SysUserMapper extends BaseMapperX<SysUserDO> {

    default SysUserDO selectByUsername(String username) {
        return selectOne(new QueryWrapper<SysUserDO>().eq("username", username));
    }

    default SysUserDO selectByEmail(String email) {
        return selectOne(new QueryWrapper<SysUserDO>().eq("email", email));
    }

    default SysUserDO selectByMobile(String mobile) {
        return selectOne(new QueryWrapper<SysUserDO>().eq("mobile", mobile));
    }

    default PageResult<SysUserDO> selectPage(SysUserPageReqVO reqVO, Collection<Long> deptIds) {
        return selectPage(reqVO, new QueryWrapperX<SysUserDO>()
                .likeIfPresent("username", reqVO.getUsername())
                .likeIfPresent("mobile", reqVO.getMobile())
                .eqIfPresent("status", reqVO.getStatus())
                .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime())
                .inIfPresent("dept_id", deptIds));
    }

    default List<SysUserDO> selectList(SysUserExportReqVO reqVO, Collection<Long> deptIds) {
        return selectList(new QueryWrapperX<SysUserDO>().likeIfPresent("username", reqVO.getUsername())
                .likeIfPresent("mobile", reqVO.getMobile())
                .eqIfPresent("status", reqVO.getStatus())
                .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime())
                .inIfPresent("dept_id", deptIds));
    }

    default List<SysUserDO> selectListByNickname(String nickname) {
        return selectList(new QueryWrapperX<SysUserDO>().like("nickname", nickname));
    }

    default List<SysUserDO> selectListByUsername(String username) {
        return selectList(new QueryWrapperX<SysUserDO>().like("username", username));
    }

    default List<SysUserDO> selectListByBaseVO(SysUserBaseVO reqVO) {
        return selectList(new QueryWrapperX<SysUserDO>().likeIfPresent("username", reqVO.getUsername())
                .likeIfPresent("nickname", reqVO.getNickname())
                .eq("status", CommonStatusEnum.ENABLE.getStatus())
                .eq("dept_id", reqVO.getDeptId())
                .likeIfPresent("post_ids", Optional.ofNullable(reqVO.getPostIds()).map(t -> Strings.join(t, ',')).orElse("")));
    }

}

