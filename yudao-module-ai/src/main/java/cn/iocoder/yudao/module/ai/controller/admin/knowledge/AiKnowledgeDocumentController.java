package cn.iocoder.yudao.module.ai.controller.admin.knowledge;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.document.*;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgeDocumentCreateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDocumentDO;
import cn.iocoder.yudao.module.ai.service.knowledge.AiKnowledgeDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AI 知识库文档")
@RestController
@RequestMapping("/ai/knowledge/document")
@Validated
public class AiKnowledgeDocumentController {

    @Resource
    private AiKnowledgeDocumentService documentService;

    @GetMapping("/page")
    @Operation(summary = "获取文档分页")
    @PreAuthorize("@ss.hasPermission('ai:knowledge:query')")
    public CommonResult<PageResult<AiKnowledgeDocumentRespVO>> getKnowledgeDocumentPage(
            @Valid AiKnowledgeDocumentPageReqVO pageReqVO) {
        PageResult<AiKnowledgeDocumentDO> pageResult = documentService.getKnowledgeDocumentPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiKnowledgeDocumentRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获取文档详情")
    @PreAuthorize("@ss.hasPermission('ai:knowledge:query')")
    public CommonResult<AiKnowledgeDocumentRespVO> getKnowledgeDocument(@RequestParam("id") Long id) {
        AiKnowledgeDocumentDO document = documentService.getKnowledgeDocument(id);
        return success(BeanUtils.toBean(document, AiKnowledgeDocumentRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "新建文档（单个）")
    @PreAuthorize("@ss.hasPermission('ai:knowledge:create')")
    public CommonResult<Long> createKnowledgeDocument(@RequestBody @Valid AiKnowledgeDocumentCreateReqVO reqVO) {
        Long id = documentService.createKnowledgeDocument(reqVO);
        return success(id);
    }

    @PostMapping("/create-list")
    @Operation(summary = "新建文档（多个）")
    @PreAuthorize("@ss.hasPermission('ai:knowledge:create')")
    public CommonResult<List<Long>> createKnowledgeDocumentList(
            @RequestBody @Valid AiKnowledgeDocumentCreateListReqVO reqVO) {
        List<Long> ids = documentService.createKnowledgeDocumentList(reqVO);
        return success(ids);
    }

    @PutMapping("/update")
    @Operation(summary = "更新文档")
    @PreAuthorize("@ss.hasPermission('ai:knowledge:update')")
    public CommonResult<Boolean> updateKnowledgeDocument(@Valid @RequestBody AiKnowledgeDocumentUpdateReqVO reqVO) {
        documentService.updateKnowledgeDocument(reqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新文档状态")
    @PreAuthorize("@ss.hasPermission('ai:knowledge:update')")
    public CommonResult<Boolean> updateKnowledgeDocumentStatus(
            @Valid @RequestBody AiKnowledgeDocumentUpdateStatusReqVO reqVO) {
        documentService.updateKnowledgeDocumentStatus(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文档")
    @PreAuthorize("@ss.hasPermission('ai:knowledge:delete')")
    public CommonResult<Boolean> deleteKnowledgeDocument(@RequestParam("id") Long id) {
        documentService.deleteKnowledgeDocument(id);
        return success(true);
    }

}
