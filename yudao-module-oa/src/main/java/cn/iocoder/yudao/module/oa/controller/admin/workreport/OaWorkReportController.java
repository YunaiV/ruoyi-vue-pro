package cn.iocoder.yudao.module.oa.controller.admin.workreport;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportSaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportStatisticsReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.workreport.vo.OaWorkReportStatisticsRespVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.workreport.OaWorkReportDO;
import cn.iocoder.yudao.module.oa.enums.workreport.OaWorkReportTypeEnum;
import cn.iocoder.yudao.module.oa.service.workreport.OaWorkReportService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - OA 工作汇报")
@RestController
@RequestMapping("/oa/work-report")
@Validated
public class OaWorkReportController {

    @Resource
    private OaWorkReportService workReportService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    // ==================== 我的汇报 ====================

    @PostMapping("/create")
    @Operation(summary = "创建工作汇报草稿")
    @PreAuthorize("@ss.hasPermission('oa:work-report:create')")
    public CommonResult<Long> createWorkReport(@Valid @RequestBody OaWorkReportSaveReqVO createReqVO) {
        return success(workReportService.createWorkReport(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工作汇报草稿")
    @PreAuthorize("@ss.hasPermission('oa:work-report:update')")
    public CommonResult<Boolean> updateWorkReport(@Valid @RequestBody OaWorkReportSaveReqVO updateReqVO) {
        workReportService.updateWorkReport(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工作汇报草稿")
    @Parameter(name = "id", description = "汇报编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:work-report:delete')")
    public CommonResult<Boolean> deleteWorkReport(@RequestParam("id") Long id) {
        workReportService.deleteWorkReport(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交工作汇报")
    @Parameter(name = "id", description = "汇报编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:work-report:update')")
    public CommonResult<Boolean> submitWorkReport(@RequestParam("id") Long id) {
        workReportService.submitWorkReport(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消提交工作汇报")
    @Parameter(name = "id", description = "汇报编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:work-report:update')")
    public CommonResult<Boolean> cancelWorkReport(@RequestParam("id") Long id) {
        workReportService.cancelWorkReport(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工作汇报详情")
    @Parameter(name = "id", description = "汇报编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasAnyPermissions('oa:work-report:query', 'oa:work-report:statistics')")
    public CommonResult<OaWorkReportRespVO> getWorkReport(@RequestParam("id") Long id) {
        OaWorkReportDO workReport = workReportService.getWorkReport(id, getLoginUserId());
        return success(buildWorkReportRespVO(workReport));
    }

    @GetMapping("/page")
    @Operation(summary = "获得我的工作汇报分页")
    @PreAuthorize("@ss.hasPermission('oa:work-report:query')")
    public CommonResult<PageResult<OaWorkReportRespVO>> getWorkReportPage(
            @Valid OaWorkReportPageReqVO pageReqVO) {
        PageResult<OaWorkReportDO> pageResult = workReportService.getWorkReportPage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildWorkReportRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 汇报统计 ====================

    @GetMapping("/statistics")
    @Operation(summary = "获得工作汇报统计")
    @PreAuthorize("@ss.hasPermission('oa:work-report:statistics')")
    public CommonResult<OaWorkReportStatisticsRespVO> getWorkReportStatistics(
            @Valid OaWorkReportStatisticsReqVO reqVO) {
        return success(workReportService.getWorkReportStatistics(reqVO, getLoginUserId()));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接工作汇报详情
     *
     * @param workReport 工作汇报
     * @return 工作汇报响应
     */
    private OaWorkReportRespVO buildWorkReportRespVO(OaWorkReportDO workReport) {
        if (workReport == null) {
            return null;
        }
        return CollUtil.getFirst(buildWorkReportRespVOList(Collections.singletonList(workReport)));
    }

    /**
     * 拼接工作汇报响应列表
     *
     * @param workReports 工作汇报列表
     * @return 工作汇报响应列表
     */
    private List<OaWorkReportRespVO> buildWorkReportRespVOList(List<OaWorkReportDO> workReports) {
        if (CollUtil.isEmpty(workReports)) {
            return Collections.emptyList();
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(workReports, report -> NumberUtils.parseLong(report.getCreator())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(workReports, OaWorkReportDO::getDeptId));
        return convertList(workReports, workReport -> {
            Long userId = NumberUtils.parseLong(workReport.getCreator());
            OaWorkReportRespVO respVO = BeanUtils.toBean(workReport, OaWorkReportRespVO.class).setUserId(userId)
                    .setPeriodKey(OaWorkReportTypeEnum.valueOf(workReport.getType()).formatPeriod(workReport.getStartTime()));
            MapUtils.findAndThen(userMap, userId, user -> respVO.setUserName(user.getNickname()));
            MapUtils.findAndThen(deptMap, workReport.getDeptId(), dept -> respVO.setDeptName(dept.getName()));
            return respVO;
        });
    }

}
