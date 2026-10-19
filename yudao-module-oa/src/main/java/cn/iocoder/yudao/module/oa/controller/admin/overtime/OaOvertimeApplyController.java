package cn.iocoder.yudao.module.oa.controller.admin.overtime;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.overtime.vo.OaOvertimeApplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.overtime.vo.OaOvertimeApplyRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.overtime.vo.OaOvertimeApplySaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.overtime.vo.OaOvertimeApplySubmitReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.overtime.OaOvertimeApplyDO;
import cn.iocoder.yudao.module.oa.service.overtime.OaOvertimeApplyService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 加班申请")
@RestController
@RequestMapping("/oa/overtime-apply")
@Validated
public class OaOvertimeApplyController {

    @Resource
    private OaOvertimeApplyService overtimeApplyService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建加班申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:overtime-apply:create')")
    public CommonResult<Long> createOvertimeApply(@Valid @RequestBody OaOvertimeApplySaveReqVO createReqVO) {
        return success(overtimeApplyService.createOvertimeApply(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新加班申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:overtime-apply:create')")
    public CommonResult<Boolean> updateOvertimeApply(@Valid @RequestBody OaOvertimeApplySaveReqVO saveReqVO) {
        overtimeApplyService.updateOvertimeApply(saveReqVO, getLoginUserId());
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交加班申请")
    @PreAuthorize("@ss.hasPermission('oa:overtime-apply:create')")
    public CommonResult<Boolean> submitOvertimeApply(@Valid @RequestBody OaOvertimeApplySubmitReqVO submitReqVO) {
        overtimeApplyService.submitOvertimeApply(submitReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得本人加班申请分页")
    @PreAuthorize("@ss.hasPermission('oa:overtime-apply:query')")
    public CommonResult<PageResult<OaOvertimeApplyRespVO>> getOvertimeApplyPage(@Valid OaOvertimeApplyPageReqVO pageReqVO) {
        PageResult<OaOvertimeApplyDO> page = overtimeApplyService.getOvertimeApplyPage(getLoginUserId(), pageReqVO);
        return success(new PageResult<>(buildOvertimeApplyRespVOList(page.getList()), page.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得加班申请详情")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:overtime-apply:query')")
    public CommonResult<OaOvertimeApplyRespVO> getOvertimeApply(@RequestParam("id") Long id) {
        OaOvertimeApplyDO application = overtimeApplyService.getOvertimeApply(id);
        return success(buildOvertimeApplyRespVO(application));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接加班申请详情
     *
     * @param application 申请
     * @return 加班申请响应
     */
    private OaOvertimeApplyRespVO buildOvertimeApplyRespVO(OaOvertimeApplyDO application) {
        if (application == null) {
            return null;
        }
        return CollUtil.getFirst(buildOvertimeApplyRespVOList(Collections.singletonList(application)));
    }

    /**
     * 批量拼接申请人展示信息
     *
     * @param applications 申请列表
     * @return 响应列表
     */
    private List<OaOvertimeApplyRespVO> buildOvertimeApplyRespVOList(List<OaOvertimeApplyDO> applications) {
        if (CollUtil.isEmpty(applications)) {
            return Collections.emptyList();
        }
        // 1. 批量查询申请人
        Map<Long, AdminUserRespDTO> users = adminUserApi.getUserMap(
                convertSet(applications, application -> Long.valueOf(application.getCreator())));
        // 2. 转换并拼接 VO
        return convertList(applications, application -> {
            OaOvertimeApplyRespVO vo = BeanUtils.toBean(application, OaOvertimeApplyRespVO.class);
            MapUtils.findAndThen(users, Long.valueOf(application.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            return vo;
        });
    }

}
