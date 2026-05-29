package cn.iocoder.yudao.module.yaya.service.practice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeAttemptCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicDetailRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicPageReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicRespVO;

public interface YayaPracticeService {

    PageResult<YayaAppPracticeTopicRespVO> getTopicPage(YayaAppPracticeTopicPageReqVO reqVO, Long memberUserId);

    YayaAppPracticeTopicDetailRespVO getTopicDetail(Long topicId, Long memberUserId);

    Long createFavorite(Long memberUserId, Long topicId);

    Long setFavorite(Long memberUserId, Long topicId, Boolean active);

    Long createAttempt(Long memberUserId, YayaAppPracticeAttemptCreateReqVO reqVO);

}
