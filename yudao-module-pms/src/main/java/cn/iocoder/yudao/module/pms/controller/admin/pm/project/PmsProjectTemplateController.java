package cn.iocoder.yudao.module.pms.controller.admin.pm.project;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.template.PmsProjectTemplatePageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.template.PmsProjectTemplateRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.template.PmsProjectTemplateSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectTemplateDO;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - PMS 项目模板")
@RestController
@RequestMapping("/pms/pm/project-template")
@Validated
public class PmsProjectTemplateController {

    @Resource
    private PmsProjectTemplateService projectTemplateService;

    @PostMapping("/create")
    @Operation(summary = "创建项目模板")
    @PreAuthorize("@ss.hasPermission('pms:pm:project-template:create')")
    public CommonResult<Long> createProjectTemplate(@Valid @RequestBody PmsProjectTemplateSaveReqVO createReqVO) {
        return success(projectTemplateService.createProjectTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目模板")
    @PreAuthorize("@ss.hasPermission('pms:pm:project-template:update')")
    public CommonResult<Boolean> updateProjectTemplate(@Valid @RequestBody PmsProjectTemplateSaveReqVO updateReqVO) {
        projectTemplateService.updateProjectTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除项目模板")
    @Parameter(name = "id", description = "模板编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project-template:delete')")
    public CommonResult<Boolean> deleteProjectTemplate(@RequestParam("id") Long id) {
        projectTemplateService.deleteProjectTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得项目模板")
    @Parameter(name = "id", description = "模板编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:project-template:query')")
    public CommonResult<PmsProjectTemplateRespVO> getProjectTemplate(@RequestParam("id") Long id) {
        PmsProjectTemplateDO template = projectTemplateService.getProjectTemplate(id);
        return success(BeanUtils.toBean(template, PmsProjectTemplateRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得项目模板分页")
    @PreAuthorize("@ss.hasPermission('pms:pm:project-template:query')")
    public CommonResult<PageResult<PmsProjectTemplateRespVO>> getProjectTemplatePage(
            @Valid PmsProjectTemplatePageReqVO pageReqVO) {
        PageResult<PmsProjectTemplateDO> pageResult = projectTemplateService.getProjectTemplatePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PmsProjectTemplateRespVO.class));
    }

}
