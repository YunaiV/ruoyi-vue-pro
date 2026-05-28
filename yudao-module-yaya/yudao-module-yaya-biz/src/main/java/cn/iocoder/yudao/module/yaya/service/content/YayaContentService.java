package cn.iocoder.yudao.module.yaya.service.content;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaContentSeasonCreateReq;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaQuestionSaveReq;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicCreateReq;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicDetailResp;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicListItemResp;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicPageReqVO;

import java.util.List;

public interface YayaContentService {

    Long createSeason(YayaContentSeasonCreateReq req);

    Long createTopic(YayaTopicCreateReq req);

    void replaceQuestions(Long topicId, List<YayaQuestionSaveReq> questions);

    PageResult<YayaTopicListItemResp> getTopicPage(YayaTopicPageReqVO req);

    YayaTopicDetailResp getTopicDetail(Long id);

    void updateTopicPublishStatus(Long id, String publishStatus);

}
