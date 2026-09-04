package cn.iocoder.yudao.module.pms.controller.admin.kb.content;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.document.PmsKnowledgeDocumentRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.label.PmsKnowledgeDocumentLabelPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.label.PmsKnowledgeDocumentLabelRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.content.vo.label.PmsKnowledgeDocumentLabelSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.service.kb.content.PmsKnowledgeDocumentLabelService;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 知识库文档标签")
@RestController
@RequestMapping("/pms/kb/document-label")
@Validated
public class PmsKnowledgeDocumentLabelController {

    @Resource
    private PmsKnowledgeDocumentLabelService documentLabelService;
    @Resource
    private PmsKnowledgeLibraryService libraryService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建文档标签")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Long> createDocumentLabel(
            @Valid @RequestBody PmsKnowledgeDocumentLabelSaveReqVO saveReqVO) {
        return success(documentLabelService.createDocumentLabel(saveReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改文档标签")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Boolean> updateDocumentLabel(
            @Valid @RequestBody PmsKnowledgeDocumentLabelSaveReqVO saveReqVO) {
        documentLabelService.updateDocumentLabel(saveReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文档标签")
    @Parameter(name = "id", description = "文档标签编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:delete')")
    public CommonResult<Boolean> deleteDocumentLabel(@RequestParam("id") Long id) {
        documentLabelService.deleteDocumentLabel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文档标签详情")
    @Parameter(name = "id", description = "文档标签编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<PmsKnowledgeDocumentLabelRespVO> getDocumentLabel(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(documentLabelService.getDocumentLabel(id),
                PmsKnowledgeDocumentLabelRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得文档标签列表")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<List<PmsKnowledgeDocumentLabelRespVO>> getDocumentLabelList() {
        return success(BeanUtils.toBean(documentLabelService.getDocumentLabelList(),
                PmsKnowledgeDocumentLabelRespVO.class));
    }

    @GetMapping("/document-page")
    @Operation(summary = "获得指定标签的文档分页")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<PageResult<PmsKnowledgeDocumentRespVO>> getDocumentPageByLabel(
            @Valid PmsKnowledgeDocumentLabelPageReqVO pageReqVO) {
        PageResult<PmsKnowledgeDocumentDO> pageResult = documentLabelService.getDocumentPageByLabel(
                pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildDocumentRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private List<PmsKnowledgeDocumentRespVO> buildDocumentRespVOList(List<PmsKnowledgeDocumentDO> documents) {
        if (CollUtil.isEmpty(documents)) {
            return Collections.emptyList();
        }
        Map<Long, PmsKnowledgeLibraryDO> libraryMap = libraryService.getLibraryMap(
                convertSet(documents, PmsKnowledgeDocumentDO::getLibraryId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(documents,
                document -> NumberUtils.parseLong(document.getCreator())));
        return convertList(documents, document -> {
            PmsKnowledgeDocumentRespVO documentVO = BeanUtils.toBean(document, PmsKnowledgeDocumentRespVO.class)
                    .setContent(null).setCreatorUserId(NumberUtils.parseLong(document.getCreator()));
            findAndThen(libraryMap, document.getLibraryId(),
                    library -> documentVO.setLibraryName(library.getName()));
            findAndThen(userMap, documentVO.getCreatorUserId(),
                    user -> documentVO.setCreatorUserName(user.getNickname()));
            return documentVO;
        });
    }

}
