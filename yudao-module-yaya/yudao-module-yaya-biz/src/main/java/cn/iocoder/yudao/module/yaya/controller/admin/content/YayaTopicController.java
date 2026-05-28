package cn.iocoder.yudao.module.yaya.controller.admin.content;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.yaya.controller.admin.content.vo.YayaTopicPublishStatusReqVO;
import cn.iocoder.yudao.module.yaya.controller.admin.content.vo.YayaTopicQuestionsSaveReqVO;
import cn.iocoder.yudao.module.yaya.controller.admin.content.vo.YayaTopicSaveReqVO;
import cn.iocoder.yudao.module.yaya.service.content.YayaContentService;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicDetailResp;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicListItemResp;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicPageReqVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/yaya")
public class YayaTopicController {

    @Resource
    private YayaContentService contentService;

    @GetMapping("/topics")
    @PreAuthorize("@ss.hasPermission('yaya:topic:query')")
    public CommonResult<PageResult<YayaTopicListItemResp>> getTopicPage(@Valid YayaTopicPageReqVO pageReqVO) {
        return success(contentService.getTopicPage(pageReqVO));
    }

    @GetMapping("/topics/{id}")
    @PreAuthorize("@ss.hasPermission('yaya:topic:query')")
    public CommonResult<YayaTopicDetailResp> getTopic(@PathVariable("id") Long id) {
        return success(contentService.getTopicDetail(id));
    }

    @PostMapping("/topics")
    @PreAuthorize("@ss.hasPermission('yaya:topic:create')")
    public CommonResult<Long> createTopic(@Valid @RequestBody YayaTopicSaveReqVO reqVO) {
        return success(contentService.createTopic(reqVO.toCreateReq()));
    }

    @PatchMapping("/topics/{id}")
    @PreAuthorize("@ss.hasPermission('yaya:topic:update')")
    public CommonResult<Boolean> updateTopic(@PathVariable("id") Long id,
                                             @RequestBody YayaTopicSaveReqVO reqVO) {
        contentService.updateTopic(id, reqVO.toCreateReq());
        return success(true);
    }

    @PutMapping("/topics/{id}/questions")
    @PreAuthorize("@ss.hasPermission('yaya:topic:update')")
    public CommonResult<Boolean> replaceQuestions(@PathVariable("id") Long id,
                                                  @Valid @RequestBody YayaTopicQuestionsSaveReqVO reqVO) {
        contentService.replaceQuestions(id, reqVO.toSaveReqs());
        return success(true);
    }

    @PatchMapping("/topics/{id}/publish-status")
    @PreAuthorize("@ss.hasPermission('yaya:topic:publish')")
    public CommonResult<Boolean> updatePublishStatus(@PathVariable("id") Long id,
                                                     @Valid @RequestBody YayaTopicPublishStatusReqVO reqVO) {
        contentService.updateTopicPublishStatus(id, reqVO.getPublishStatus());
        return success(true);
    }

}
