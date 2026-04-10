package cn.iocoder.yudao.module.member.dal.mysql.level;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.experience.MemberExperienceRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberExperienceRecordDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员经验记录 Mapper
 *
 * @author owen
 */
@Mapper
public interface MemberExperienceRecordMapper extends BaseMapperX<MemberExperienceRecordDO> {

    default PageResult<MemberExperienceRecordDO> selectPage(MemberExperienceRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MemberExperienceRecordDO>()
                .eqIfPresent(MemberExperienceRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(MemberExperienceRecordDO::getBizId, reqVO.getBizId())
                .eqIfPresent(MemberExperienceRecordDO::getBizType, reqVO.getBizType())
                .eqIfPresent(MemberExperienceRecordDO::getTitle, reqVO.getTitle())
                .betweenIfPresent(MemberExperienceRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MemberExperienceRecordDO::getId));
    }

    default PageResult<MemberExperienceRecordDO> selectPage(Long userId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapper<MemberExperienceRecordDO>()
                .eq(MemberExperienceRecordDO::getUserId, userId)
                .orderByDesc(MemberExperienceRecordDO::getId));
    }
}
