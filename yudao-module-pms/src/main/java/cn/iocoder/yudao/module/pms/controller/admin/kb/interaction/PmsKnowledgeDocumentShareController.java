package cn.iocoder.yudao.module.pms.controller.admin.kb.interaction;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.share.PmsKnowledgeDocumentSharePublicRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.share.PmsKnowledgeDocumentShareRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.share.PmsKnowledgeDocumentShareSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.share.PmsKnowledgeDocumentShareUpdateMemberListReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeDocumentShareDO;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentTypeEnum;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeDocumentShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 知识库文档分享")
@RestController
@RequestMapping("/pms/kb/document-share")
@Validated
public class PmsKnowledgeDocumentShareController {

    @Resource
    private PmsKnowledgeDocumentShareService documentShareService;

    @Resource
    private FileApi fileApi;

    @PostMapping("/open")
    @Operation(summary = "开启文档分享")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<PmsKnowledgeDocumentShareRespVO> openShare(
            @Valid @RequestBody PmsKnowledgeDocumentShareSaveReqVO reqVO) {
        PmsKnowledgeDocumentShareDO share = documentShareService.openShare(reqVO.getDocumentId(),
                reqVO.getShareUserIds(), getLoginUserId());
        return success(BeanUtils.toBean(share, PmsKnowledgeDocumentShareRespVO.class));
    }

    @PutMapping("/update-member-list")
    @Operation(summary = "更新文档内部分享成员")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Boolean> updateShareMemberList(
            @Valid @RequestBody PmsKnowledgeDocumentShareUpdateMemberListReqVO reqVO) {
        documentShareService.updateShareMemberList(reqVO.getDocumentId(), reqVO.getShareUserIds(), getLoginUserId());
        return success(true);
    }

    @PutMapping("/close")
    @Operation(summary = "关闭文档分享")
    @Parameter(name = "documentId", description = "文档编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Boolean> closeShare(@RequestParam("documentId") Long documentId) {
        documentShareService.closeShare(documentId, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文档分享配置")
    @Parameter(name = "documentId", description = "文档编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<PmsKnowledgeDocumentShareRespVO> getDocumentShare(@RequestParam("documentId") Long documentId) {
        PmsKnowledgeDocumentShareDO share = documentShareService.getActiveDocumentShare(documentId, getLoginUserId());
        return success(BeanUtils.toBean(share, PmsKnowledgeDocumentShareRespVO.class));
    }

    @GetMapping("/get-by-token")
    @Operation(summary = "通过分享令牌获得文档")
    @Parameter(name = "token", description = "分享令牌", required = true)
    @PermitAll
    @TenantIgnore
    public CommonResult<PmsKnowledgeDocumentSharePublicRespVO> getDocumentByToken(
            @RequestParam("token") @NotBlank(message = "分享令牌不能为空") String token) {
        PmsKnowledgeDocumentDO document = documentShareService.getDocumentByShareToken(token);
        PmsKnowledgeDocumentSharePublicRespVO documentVO = BeanUtils.toBean(
                document, PmsKnowledgeDocumentSharePublicRespVO.class);
        if (PmsKnowledgeDocumentTypeEnum.FILE.getType().equals(document.getType())) {
            documentVO.setContent(null).setPreviewUrl(
                    fileApi.presignGetUrl(document.getContent(), null));
        }
        return success(documentVO);
    }

}
