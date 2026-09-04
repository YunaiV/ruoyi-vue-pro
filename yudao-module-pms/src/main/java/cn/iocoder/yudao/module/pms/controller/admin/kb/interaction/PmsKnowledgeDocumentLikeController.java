package cn.iocoder.yudao.module.pms.controller.admin.kb.interaction;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeDocumentLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 知识库文档点赞")
@RestController
@RequestMapping("/pms/kb/document-like")
@Validated
public class PmsKnowledgeDocumentLikeController {

    @Resource
    private PmsKnowledgeDocumentLikeService documentLikeService;

    @PostMapping("/create")
    @Operation(summary = "点赞文档")
    @Parameter(name = "documentId", description = "文档编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<Boolean> createDocumentLike(@RequestParam("documentId") Long documentId) {
        documentLikeService.createDocumentLike(documentId, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "取消点赞文档")
    @Parameter(name = "documentId", description = "文档编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<Boolean> deleteDocumentLike(@RequestParam("documentId") Long documentId) {
        documentLikeService.deleteDocumentLike(documentId, getLoginUserId());
        return success(true);
    }

}
