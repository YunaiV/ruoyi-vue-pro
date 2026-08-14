package cn.iocoder.yudao.module.hrm.controller.admin.performance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate.HrmPerformanceAssessmentTemplatePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate.HrmPerformanceAssessmentTemplateRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate.HrmPerformanceAssessmentTemplateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceAssessmentTemplateDO;
import cn.iocoder.yudao.module.hrm.service.performance.config.HrmPerformanceAssessmentTemplateService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Tag(name = "管理后台 - HRM 绩效考核模板")
@RestController
@RequestMapping("/hrm/performance/assessment-template")
@Validated
public class HrmPerformanceAssessmentTemplateController {

    @Resource
    private HrmPerformanceAssessmentTemplateService assessmentTemplateService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建绩效考核模板")
    @PreAuthorize("@ss.hasPermission('hrm:performance:assessment-template:create')")
    public CommonResult<Long> createPerformanceAssessmentTemplate(
            @Valid @RequestBody HrmPerformanceAssessmentTemplateSaveReqVO createReqVO) {
        return success(assessmentTemplateService.createPerformanceAssessmentTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新绩效考核模板")
    @PreAuthorize("@ss.hasPermission('hrm:performance:assessment-template:update')")
    public CommonResult<Boolean> updatePerformanceAssessmentTemplate(
            @Valid @RequestBody HrmPerformanceAssessmentTemplateSaveReqVO updateReqVO) {
        assessmentTemplateService.updatePerformanceAssessmentTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除绩效考核模板")
    @Parameter(name = "id", description = "模板编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:performance:assessment-template:delete')")
    public CommonResult<Boolean> deletePerformanceAssessmentTemplate(@RequestParam("id") Long id) {
        assessmentTemplateService.deletePerformanceAssessmentTemplate(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除绩效考核模板")
    @Parameter(name = "ids", description = "模板编号列表", required = true, example = "1024,1025")
    @PreAuthorize("@ss.hasPermission('hrm:performance:assessment-template:delete')")
    public CommonResult<Boolean> deletePerformanceAssessmentTemplateList(@RequestParam("ids") List<Long> ids) {
        assessmentTemplateService.deletePerformanceAssessmentTemplateList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得绩效考核模板")
    @Parameter(name = "id", description = "模板编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:performance:assessment-template:query')")
    public CommonResult<HrmPerformanceAssessmentTemplateRespVO> getPerformanceAssessmentTemplate(
            @RequestParam("id") Long id) {
        // 1. 获得考核模板
        HrmPerformanceAssessmentTemplateDO template =
                assessmentTemplateService.getPerformanceAssessmentTemplate(id);
        // 2. 拼接数据
        return success(buildAssessmentTemplateRespVO(template));
    }

    @GetMapping("/page")
    @Operation(summary = "获得绩效考核模板分页")
    @PreAuthorize("@ss.hasPermission('hrm:performance:assessment-template:query')")
    public CommonResult<PageResult<HrmPerformanceAssessmentTemplateRespVO>> getPerformanceAssessmentTemplatePage(
            @Validated HrmPerformanceAssessmentTemplatePageReqVO pageReqVO) {
        PageResult<HrmPerformanceAssessmentTemplateDO> pageResult =
                assessmentTemplateService.getPerformanceAssessmentTemplatePage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 2. 拼接数据
        return success(new PageResult<>(buildAssessmentTemplateRespVOList(pageResult.getList()),
                pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得绩效考核模板精简列表")
    @PreAuthorize("@ss.hasPermission('hrm:performance:assessment-template:query')")
    public CommonResult<List<HrmPerformanceAssessmentTemplateRespVO>> getPerformanceAssessmentTemplateSimpleList() {
        List<HrmPerformanceAssessmentTemplateDO> templates = assessmentTemplateService
                .getPerformanceAssessmentTemplateListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(BeanUtils.toBean(templates, HrmPerformanceAssessmentTemplateRespVO.class));
    }

    // ==================== 拼接 VO ====================

    private HrmPerformanceAssessmentTemplateRespVO buildAssessmentTemplateRespVO(
            HrmPerformanceAssessmentTemplateDO template) {
        if (template == null) {
            return null;
        }
        return CollUtil.getFirst(buildAssessmentTemplateRespVOList(singletonList(template)));
    }

    private List<HrmPerformanceAssessmentTemplateRespVO> buildAssessmentTemplateRespVOList(
            List<HrmPerformanceAssessmentTemplateDO> templates) {
        if (CollUtil.isEmpty(templates)) {
            return emptyList();
        }
        // 1. 获得创建人信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(templates, template -> NumberUtils.parseLong(template.getCreator())));
        // 2. 拼接响应
        return BeanUtils.toBean(templates, HrmPerformanceAssessmentTemplateRespVO.class, template ->
                MapUtils.findAndThen(userMap, NumberUtils.parseLong(template.getCreator()),
                        user -> template.setCreatorName(user.getNickname())));
    }

}
