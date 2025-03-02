package cn.iocoder.yudao.module.ai.controller.admin.knowledge;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.segment.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDocumentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeSegmentDO;
import cn.iocoder.yudao.module.ai.service.knowledge.AiKnowledgeDocumentService;
import cn.iocoder.yudao.module.ai.service.knowledge.AiKnowledgeSegmentService;
import cn.iocoder.yudao.module.ai.service.knowledge.bo.AiKnowledgeSegmentSearchReqBO;
import cn.iocoder.yudao.module.ai.service.knowledge.bo.AiKnowledgeSegmentSearchRespBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.URL;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

// TODO @芋艿：增加权限标识
@Tag(name = "管理后台 - AI 知识库段落")
@RestController
@RequestMapping("/ai/knowledge/segment")
@Validated
public class AiKnowledgeSegmentController {

    @Resource
    private AiKnowledgeSegmentService segmentService;

    @Resource
    private AiKnowledgeDocumentService documentService;

    @GetMapping("/page")
    @Operation(summary = "获取段落分页")
    public CommonResult<PageResult<AiKnowledgeSegmentRespVO>> getKnowledgeSegmentPage(
            @Valid AiKnowledgeSegmentPageReqVO pageReqVO) {
        PageResult<AiKnowledgeSegmentDO> pageResult = segmentService.getKnowledgeSegmentPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiKnowledgeSegmentRespVO.class));
    }

    @PutMapping("/update")
    @Operation(summary = "更新段落内容")
    public CommonResult<Boolean> updateKnowledgeSegment(@Valid @RequestBody AiKnowledgeSegmentUpdateReqVO reqVO) {
        segmentService.updateKnowledgeSegment(reqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "启禁用段落内容")
    public CommonResult<Boolean> updateKnowledgeSegmentStatus(
            @Valid @RequestBody AiKnowledgeSegmentUpdateStatusReqVO reqVO) {
        segmentService.updateKnowledgeSegmentStatus(reqVO);
        return success(true);
    }

    @GetMapping("/split")
    @Operation(summary = "切片内容")
    @Parameters({
            @Parameter(name = "url", description = "文档 URL", required = true),
            @Parameter(name = "segmentMaxTokens", description = "分段的最大 Token 数", required = true)
    })
    public CommonResult<List<AiKnowledgeSegmentRespVO>> splitContent(
            @RequestParam("url") @URL String url,
            @RequestParam(value = "segmentMaxTokens") Integer segmentMaxTokens) {
        List<AiKnowledgeSegmentDO> segments = segmentService.splitContent(url, segmentMaxTokens);
        return success(BeanUtils.toBean(segments, AiKnowledgeSegmentRespVO.class));
    }

    @GetMapping("/get-process-list")
    @Operation(summary = "获取文档处理列表")
    @Parameter(name = "documentIds", description = "文档编号列表", required = true, example = "1,2,3")
    public CommonResult<List<AiKnowledgeSegmentProcessRespVO>> getKnowledgeSegmentProcessList(
            @RequestParam("documentIds") List<Long> documentIds) {
        List<AiKnowledgeSegmentProcessRespVO> list = segmentService.getKnowledgeSegmentProcessList(documentIds);
        return success(list);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索段落内容")
    public CommonResult<List<AiKnowledgeSegmentSearchRespVO>> searchKnowledgeSegment(
            @Valid AiKnowledgeSegmentSearchReqVO reqVO) {
        // 1. 搜索段落
        List<AiKnowledgeSegmentSearchRespBO> segments = segmentService
                .searchKnowledgeSegment(BeanUtils.toBean(reqVO, AiKnowledgeSegmentSearchReqBO.class));
        if (CollUtil.isEmpty(segments)) {
            return success(Collections.emptyList());
        }

        // 2. 拼接 VO
        Map<Long, AiKnowledgeDocumentDO> documentMap = documentService.getKnowledgeDocumentMap(convertSet(
                segments, AiKnowledgeSegmentSearchRespBO::getDocumentId));
        return success(BeanUtils.toBean(segments, AiKnowledgeSegmentSearchRespVO.class,
                segment -> MapUtils.findAndThen(documentMap, segment.getDocumentId(),
                        document -> segment.setDocumentName(document.getName()))));
    }

}
