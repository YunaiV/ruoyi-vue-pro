package cn.iocoder.yudao.module.member.dal.mysql.level;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.experience.MemberExperienceLogExportReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.experience.MemberExperienceLogPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberExperienceLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 会员经验记录 Mapper
 *
 * @author owen
 */
@Mapper
public interface MemberExperienceLogMapper extends BaseMapperX<MemberExperienceLogDO> {

    default PageResult<MemberExperienceLogDO> selectPage(MemberExperienceLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MemberExperienceLogDO>()
                .eqIfPresent(MemberExperienceLogDO::getUserId, reqVO.getUserId())
                .eqIfPresent(MemberExperienceLogDO::getBizId, reqVO.getBizId())
                .eqIfPresent(MemberExperienceLogDO::getBizType, reqVO.getBizType())
                .eqIfPresent(MemberExperienceLogDO::getTitle, reqVO.getTitle())
                .betweenIfPresent(MemberExperienceLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MemberExperienceLogDO::getId));
    }

    default List<MemberExperienceLogDO> selectList(MemberExperienceLogExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<MemberExperienceLogDO>()
                .eqIfPresent(MemberExperienceLogDO::getUserId, reqVO.getUserId())
                .eqIfPresent(MemberExperienceLogDO::getBizId, reqVO.getBizId())
                .eqIfPresent(MemberExperienceLogDO::getBizType, reqVO.getBizType())
                .eqIfPresent(MemberExperienceLogDO::getTitle, reqVO.getTitle())
                .betweenIfPresent(MemberExperienceLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MemberExperienceLogDO::getId));
    }

}
