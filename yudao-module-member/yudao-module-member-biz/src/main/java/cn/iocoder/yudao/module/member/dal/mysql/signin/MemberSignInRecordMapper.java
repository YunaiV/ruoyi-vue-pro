package cn.iocoder.yudao.module.member.dal.mysql.signin;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.record.MemberSignInRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

/**
 * 签到记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MemberSignInRecordMapper extends BaseMapperX<MemberSignInRecordDO> {

    default PageResult<MemberSignInRecordDO> selectPage(MemberSignInRecordPageReqVO reqVO, Set<Long> userIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MemberSignInRecordDO>()
                .inIfPresent(MemberSignInRecordDO::getUserId, userIds)
                .eqIfPresent(MemberSignInRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(MemberSignInRecordDO::getDay, reqVO.getDay())
                .betweenIfPresent(MemberSignInRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MemberSignInRecordDO::getId));
    }

    default PageResult<MemberSignInRecordDO> selectPage(Long userId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<MemberSignInRecordDO>()
                .eq(MemberSignInRecordDO::getUserId, userId)
                .orderByDesc(MemberSignInRecordDO::getId));
    }


    /**
     * 获取用户最近的签到记录信息,根据签到时间倒序
     *
     * @param userId 用户编号
     * @return 签到记录列表
     */
    default MemberSignInRecordDO selectLastRecordByUserIdDesc(Long userId) {
        return selectOne(new QueryWrapper<MemberSignInRecordDO>()
                .eq("user_id", userId)
                .orderByDesc("create_time")
                .last("limit 1"));
    }

    /**
     * 获取用户最早的签到记录信息,根据签到时间倒序
     *
     * @param userId 用户编号
     * @return 签到记录列表
     */
    default MemberSignInRecordDO selectLastRecordByUserIdAsc(Long userId) {
        return selectOne(new QueryWrapper<MemberSignInRecordDO>()
                .eq("user_id", userId)
                .orderByAsc("create_time")
                .last("limit 1"));
    }

    default Long selectCountByUserId(Long userId) {
        return selectCount(new LambdaQueryWrapperX<MemberSignInRecordDO>()
                .eq(MemberSignInRecordDO::getUserId, userId));
    }

    /**
     * 获取用户的签到记录列表信息,根据签到时间倒序
     *
     * @param userId 用户编号
     * @return 签到记录信息
     */
    default List<MemberSignInRecordDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<MemberSignInRecordDO>()
                .eq(MemberSignInRecordDO::getUserId, userId)
                .orderByDesc(MemberSignInRecordDO::getCreateTime));
    }

}
