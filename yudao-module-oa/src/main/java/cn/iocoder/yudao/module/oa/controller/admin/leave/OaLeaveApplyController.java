package cn.iocoder.yudao.module.oa.controller.admin.leave;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.leave.vo.OaLeaveApplySaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.leave.vo.OaLeaveApplySubmitReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.leave.vo.OaLeaveApplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.leave.vo.OaLeaveApplyRespVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.leave.OaLeaveApplyDO;
import cn.iocoder.yudao.module.oa.service.leave.OaLeaveApplyService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 请假申请")
@RestController
@RequestMapping("/oa/leave-apply")
@Validated
public class OaLeaveApplyController {

    @Resource
    private OaLeaveApplyService leaveApplyService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建请假申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:leave-apply:create')")
    public CommonResult<Long> createLeaveApply(@Valid @RequestBody OaLeaveApplySaveReqVO createReqVO) {
        return success(leaveApplyService.createLeaveApply(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新请假申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:leave-apply:create')")
    public CommonResult<Boolean> updateLeaveApply(@Valid @RequestBody OaLeaveApplySaveReqVO saveReqVO) {
        leaveApplyService.updateLeaveApply(saveReqVO, getLoginUserId());
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交请假申请")
    @PreAuthorize("@ss.hasPermission('oa:leave-apply:create')")
    public CommonResult<Boolean> submitLeaveApply(@Valid @RequestBody OaLeaveApplySubmitReqVO submitReqVO) {
        leaveApplyService.submitLeaveApply(submitReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得本人请假申请分页")
    @PreAuthorize("@ss.hasPermission('oa:leave-apply:query')")
    public CommonResult<PageResult<OaLeaveApplyRespVO>> getLeaveApplyPage(@Valid OaLeaveApplyPageReqVO pageReqVO) {
        PageResult<OaLeaveApplyDO> page = leaveApplyService.getLeaveApplyPage(getLoginUserId(), pageReqVO);
        return success(new PageResult<>(buildLeaveApplyRespVOList(page.getList()), page.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得请假申请详情")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:leave-apply:query')")
    public CommonResult<OaLeaveApplyRespVO> getLeaveApply(@RequestParam("id") Long id) {
        OaLeaveApplyDO application = leaveApplyService.getLeaveApply(id);
        return success(buildLeaveApplyRespVO(application));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接请假申请详情
     *
     * @param application 申请
     * @return 请假申请响应
     */
    private OaLeaveApplyRespVO buildLeaveApplyRespVO(OaLeaveApplyDO application) {
        if (application == null) {
            return null;
        }
        return CollUtil.getFirst(buildLeaveApplyRespVOList(Collections.singletonList(application)));
    }

    /**
     * 批量拼接申请人展示信息
     *
     * @param applications 申请列表
     * @return 响应列表
     */
    private List<OaLeaveApplyRespVO> buildLeaveApplyRespVOList(List<OaLeaveApplyDO> applications) {
        if (CollUtil.isEmpty(applications)) {
            return Collections.emptyList();
        }
        // 1. 批量查询申请人
        Map<Long, AdminUserRespDTO> users = adminUserApi.getUserMap(
                convertSet(applications, application -> Long.valueOf(application.getCreator())));
        // 2. 转换并拼接 VO
        return convertList(applications, application -> {
            OaLeaveApplyRespVO vo = BeanUtils.toBean(application, OaLeaveApplyRespVO.class);
            MapUtils.findAndThen(users, Long.valueOf(application.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            return vo;
        });
    }

}
