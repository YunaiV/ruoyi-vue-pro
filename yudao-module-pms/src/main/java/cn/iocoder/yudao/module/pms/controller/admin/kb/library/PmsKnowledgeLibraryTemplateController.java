package cn.iocoder.yudao.module.pms.controller.admin.kb.library;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.template.PmsKnowledgeLibraryTemplatePageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.template.PmsKnowledgeLibraryTemplateRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.template.PmsKnowledgeLibraryTemplateSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryTemplateDO;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryTemplateService;
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

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 管理后台 - PMS 知识库模板 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - PMS 知识库模板")
@RestController
@RequestMapping("/pms/kb/library-template")
@Validated
public class PmsKnowledgeLibraryTemplateController {

    @Resource
    private PmsKnowledgeLibraryTemplateService libraryTemplateService;

    @PostMapping("/create")
    @Operation(summary = "创建知识库模板")
    @PreAuthorize("@ss.hasPermission('pms:kb:library-template:create')")
    public CommonResult<Long> createLibraryTemplate(
            @Valid @RequestBody PmsKnowledgeLibraryTemplateSaveReqVO createReqVO) {
        return success(libraryTemplateService.createLibraryTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新知识库模板")
    @PreAuthorize("@ss.hasPermission('pms:kb:library-template:update')")
    public CommonResult<Boolean> updateLibraryTemplate(
            @Valid @RequestBody PmsKnowledgeLibraryTemplateSaveReqVO updateReqVO) {
        libraryTemplateService.updateLibraryTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除知识库模板")
    @Parameter(name = "id", description = "模板编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library-template:delete')")
    public CommonResult<Boolean> deleteLibraryTemplate(@RequestParam("id") Long id) {
        libraryTemplateService.deleteLibraryTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得知识库模板")
    @Parameter(name = "id", description = "模板编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library-template:query')")
    public CommonResult<PmsKnowledgeLibraryTemplateRespVO> getLibraryTemplate(@RequestParam("id") Long id) {
        PmsKnowledgeLibraryTemplateDO template = libraryTemplateService.getLibraryTemplate(id);
        return success(BeanUtils.toBean(template, PmsKnowledgeLibraryTemplateRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取知识库模板全列表", description = "只包含开启状态模板，返回模板选择所需的基本信息和文档标题")
    public CommonResult<List<PmsKnowledgeLibraryTemplateRespVO>> getSimpleLibraryTemplateList() {
        return success(convertList(libraryTemplateService.getLibraryTemplateList(CommonStatusEnum.ENABLE.getStatus()),
                template -> new PmsKnowledgeLibraryTemplateRespVO().setId(template.getId())
                        .setName(template.getName()).setCoverUrl(template.getCoverUrl()).setDescription(template.getDescription())
                        .setDocuments(convertList(template.getDocuments(), document ->
                                new PmsKnowledgeLibraryTemplateSaveReqVO.Document().setTitle(document.getTitle())))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得知识库模板分页")
    @PreAuthorize("@ss.hasPermission('pms:kb:library-template:query')")
    public CommonResult<PageResult<PmsKnowledgeLibraryTemplateRespVO>> getLibraryTemplatePage(
            @Valid PmsKnowledgeLibraryTemplatePageReqVO pageReqVO) {
        PageResult<PmsKnowledgeLibraryTemplateDO> pageResult = libraryTemplateService
                .getLibraryTemplatePage(pageReqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), PmsKnowledgeLibraryTemplateRespVO.class),
                pageResult.getTotal()));
    }

}
