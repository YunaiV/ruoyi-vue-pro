package cn.iocoder.yudao.module.ai1.controller.admin.knowledge;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.knowledgedocument.Ai1KnowledgeDocumentPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.knowledgedocument.Ai1KnowledgeDocumentRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.knowledgedocument.Ai1KnowledgeDocumentSaveReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.knowledgedocument.Ai1KnowledgeDocumentVectorizeRespVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeDocumentDO;
import cn.iocoder.yudao.module.ai1.service.knowledge.Ai1KnowledgeDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AI1 知识文档")
@RestController
@RequestMapping("/ai1/knowledge/document")
@Validated
public class Ai1KnowledgeDocumentController {

    @Resource
    private Ai1KnowledgeDocumentService knowledgeDocumentService;

    @PostMapping("/create")
    @Operation(summary = "创建文档（粘贴文本）")
    @PreAuthorize("@ss.hasPermission('ai1:knowledge-document:create')")
    public CommonResult<Long> createKnowledgeDocument(@Valid @RequestBody Ai1KnowledgeDocumentSaveReqVO createReqVO) {
        return success(knowledgeDocumentService.createKnowledgeDocument(createReqVO));
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文档", description = "仅支持 .txt / .md 文本文件")
    @Parameter(name = "knowledgeBaseId", description = "知识库编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('ai1:knowledge-document:create')")
    public CommonResult<Long> uploadKnowledgeDocument(@RequestParam("knowledgeBaseId") Long knowledgeBaseId,
                                                      @RequestParam("file") MultipartFile file) {
        return success(knowledgeDocumentService.uploadKnowledgeDocument(knowledgeBaseId, file));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文档", description = "内容变更后需要重新向量化")
    @PreAuthorize("@ss.hasPermission('ai1:knowledge-document:update')")
    public CommonResult<Boolean> updateKnowledgeDocument(@Valid @RequestBody Ai1KnowledgeDocumentSaveReqVO updateReqVO) {
        knowledgeDocumentService.updateKnowledgeDocument(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文档")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:knowledge-document:delete')")
    public CommonResult<Boolean> deleteKnowledgeDocument(@RequestParam("id") Long id) {
        knowledgeDocumentService.deleteKnowledgeDocument(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除文档")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:knowledge-document:delete')")
    public CommonResult<Boolean> deleteKnowledgeDocumentList(@RequestParam("ids") List<Long> ids) {
        knowledgeDocumentService.deleteKnowledgeDocumentListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文档", description = "包含完整内容")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:knowledge-document:query')")
    public CommonResult<Ai1KnowledgeDocumentRespVO> getKnowledgeDocument(@RequestParam("id") Long id) {
        Ai1KnowledgeDocumentDO document = knowledgeDocumentService.getKnowledgeDocument(id);
        return success(BeanUtils.toBean(document, Ai1KnowledgeDocumentRespVO.class,
                respVO -> respVO.setContentLength(StrUtil.length(respVO.getContent()))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得文档分页", description = "不返回文档内容")
    @PreAuthorize("@ss.hasPermission('ai1:knowledge-document:query')")
    public CommonResult<PageResult<Ai1KnowledgeDocumentRespVO>> getKnowledgeDocumentPage(@Valid Ai1KnowledgeDocumentPageReqVO pageReqVO) {
        PageResult<Ai1KnowledgeDocumentDO> pageResult = knowledgeDocumentService.getKnowledgeDocumentPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, Ai1KnowledgeDocumentRespVO.class,
                respVO -> respVO.setContentLength(StrUtil.length(respVO.getContent())).setContent(null)));
    }

    @PostMapping("/vectorize")
    @Operation(summary = "文档向量化", description = "返回分片数量")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:knowledge-document:vectorize')")
    public CommonResult<Integer> vectorizeKnowledgeDocument(@RequestParam("id") Long id) {
        return success(knowledgeDocumentService.vectorizeKnowledgeDocument(id));
    }

    @PostMapping("/vectorize-all")
    @Operation(summary = "知识库批量向量化")
    @Parameter(name = "knowledgeBaseId", description = "知识库编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('ai1:knowledge-document:vectorize')")
    public CommonResult<Ai1KnowledgeDocumentVectorizeRespVO> vectorizeKnowledgeDocumentAll(
            @RequestParam("knowledgeBaseId") Long knowledgeBaseId) {
        return success(knowledgeDocumentService.vectorizeKnowledgeDocumentListByKnowledgeBaseId(knowledgeBaseId));
    }

}
