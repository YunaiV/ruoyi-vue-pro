package cn.iocoder.yudao.module.oa.controller.admin.officialdoc;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.template.OaOfficialDocTemplatePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.template.OaOfficialDocTemplateRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.template.OaOfficialDocTemplateSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.OaOfficialDocTemplateDO;
import cn.iocoder.yudao.module.oa.service.officialdoc.OaOfficialDocTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 套红模板")
@RestController
@RequestMapping("/oa/officialdoc-template")
@Validated
public class OaOfficialDocTemplateController {

    @Resource
    private OaOfficialDocTemplateService officialDocTemplateService;

    @PostMapping("/create")
    @Operation(summary = "创建套红模板")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-template:create')")
    public CommonResult<Long> createOfficialDocTemplate(@Valid @RequestBody OaOfficialDocTemplateSaveReqVO reqVO) {
        return success(officialDocTemplateService.createOfficialDocTemplate(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新套红模板")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-template:update')")
    public CommonResult<Boolean> updateOfficialDocTemplate(@Valid @RequestBody OaOfficialDocTemplateSaveReqVO reqVO) {
        officialDocTemplateService.updateOfficialDocTemplate(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除套红模板")
    @Parameter(name = "id", description = "模板编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-template:delete')")
    public CommonResult<Boolean> deleteOfficialDocTemplate(@RequestParam("id") Long id) {
        officialDocTemplateService.deleteOfficialDocTemplate(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得套红模板分页列表")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-template:query')")
    public CommonResult<PageResult<OaOfficialDocTemplateRespVO>> getOfficialDocTemplatePage(
            @Valid OaOfficialDocTemplatePageReqVO pageReqVO) {
        PageResult<OaOfficialDocTemplateDO> pageResult = officialDocTemplateService.getOfficialDocTemplatePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, OaOfficialDocTemplateRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得套红模板精简列表")
    public CommonResult<List<OaOfficialDocTemplateRespVO>> getSimpleOfficialDocTemplateList() {
        List<OaOfficialDocTemplateDO> templates = officialDocTemplateService.getOfficialDocTemplateList(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(templates, template -> new OaOfficialDocTemplateRespVO()
                .setId(template.getId()).setName(template.getName())));
    }

    @GetMapping("/get")
    @Operation(summary = "获得套红模板详情")
    @Parameter(name = "id", description = "模板编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-template:query')")
    public CommonResult<OaOfficialDocTemplateRespVO> getOfficialDocTemplate(@RequestParam("id") Long id) {
        OaOfficialDocTemplateDO template = officialDocTemplateService.getOfficialDocTemplate(id);
        return success(BeanUtils.toBean(template, OaOfficialDocTemplateRespVO.class));
    }

}
