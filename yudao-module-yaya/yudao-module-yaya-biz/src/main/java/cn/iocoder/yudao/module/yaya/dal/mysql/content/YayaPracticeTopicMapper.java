package cn.iocoder.yudao.module.yaya.dal.mysql.content;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicPageReqVO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaPracticeTopicDO;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicPageReqVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface YayaPracticeTopicMapper extends BaseMapperX<YayaPracticeTopicDO> {

    default YayaPracticeTopicDO selectBySeasonPartStableKey(Long seasonId, Integer part, String stableKey) {
        return selectOne(YayaPracticeTopicDO::getSeasonId, seasonId,
                YayaPracticeTopicDO::getPart, part,
                YayaPracticeTopicDO::getStableKey, stableKey);
    }

    default PageResult<YayaPracticeTopicDO> selectPage(YayaTopicPageReqVO reqVO, Long seasonId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<YayaPracticeTopicDO>()
                .eqIfPresent(YayaPracticeTopicDO::getSeasonId, seasonId)
                .eqIfPresent(YayaPracticeTopicDO::getPart, reqVO.getPart())
                .eqIfPresent(YayaPracticeTopicDO::getPublishStatus, reqVO.getPublishStatus())
                .likeIfPresent(YayaPracticeTopicDO::getTitleEn, reqVO.getKeyword())
                .orderByDesc(YayaPracticeTopicDO::getId));
    }

    default PageResult<YayaPracticeTopicDO> selectAppPublishedPage(YayaAppPracticeTopicPageReqVO reqVO, Long seasonId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<YayaPracticeTopicDO>()
                .eq(YayaPracticeTopicDO::getSeasonId, seasonId)
                .eqIfPresent(YayaPracticeTopicDO::getPart, reqVO.getPart())
                .eq(YayaPracticeTopicDO::getPublishStatus, "published")
                .orderByDesc(YayaPracticeTopicDO::getId));
    }

}
