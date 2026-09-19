package cn.iocoder.yudao.module.oa.controller.admin.reimbursement;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.reimbursement.vo.OaReimbursementSaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.reimbursement.vo.OaReimbursementSubmitReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.reimbursement.vo.OaReimbursementPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.reimbursement.vo.OaReimbursementRespVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.reimbursement.OaReimbursementDO;
import cn.iocoder.yudao.module.oa.service.reimbursement.OaReimbursementService;
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

@Tag(name = "管理后台 - 费用报销")
@RestController
@RequestMapping("/oa/reimbursement")
@Validated
public class OaReimbursementController {

    @Resource
    private OaReimbursementService reimbursementService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建费用报销草稿")
    @PreAuthorize("@ss.hasPermission('oa:reimbursement:create')")
    public CommonResult<Long> createReimbursement(@Valid @RequestBody OaReimbursementSaveReqVO createReqVO) {
        return success(reimbursementService.createReimbursement(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新费用报销草稿")
    @PreAuthorize("@ss.hasPermission('oa:reimbursement:create')")
    public CommonResult<Boolean> updateReimbursement(@Valid @RequestBody OaReimbursementSaveReqVO saveReqVO) {
        reimbursementService.updateReimbursement(saveReqVO, getLoginUserId());
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交费用报销")
    @PreAuthorize("@ss.hasPermission('oa:reimbursement:create')")
    public CommonResult<Boolean> submitReimbursement(@Valid @RequestBody OaReimbursementSubmitReqVO submitReqVO) {
        reimbursementService.submitReimbursement(submitReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得本人费用报销分页")
    @PreAuthorize("@ss.hasPermission('oa:reimbursement:query')")
    public CommonResult<PageResult<OaReimbursementRespVO>> getReimbursementPage(@Valid OaReimbursementPageReqVO pageReqVO) {
        PageResult<OaReimbursementDO> page = reimbursementService.getReimbursementPage(getLoginUserId(), pageReqVO);
        return success(new PageResult<>(buildReimbursementRespVOList(page.getList()), page.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得费用报销详情")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:reimbursement:query')")
    public CommonResult<OaReimbursementRespVO> getReimbursement(@RequestParam("id") Long id) {
        OaReimbursementDO application = reimbursementService.getReimbursement(id);
        return success(buildReimbursementRespVO(application));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接费用报销详情
     *
     * @param application 申请
     * @return 费用报销响应
     */
    private OaReimbursementRespVO buildReimbursementRespVO(OaReimbursementDO application) {
        if (application == null) {
            return null;
        }
        return CollUtil.getFirst(buildReimbursementRespVOList(Collections.singletonList(application)));
    }

    /**
     * 批量拼接申请人展示信息
     *
     * @param applications 申请列表
     * @return 响应列表
     */
    private List<OaReimbursementRespVO> buildReimbursementRespVOList(List<OaReimbursementDO> applications) {
        if (CollUtil.isEmpty(applications)) {
            return Collections.emptyList();
        }
        // 1. 批量查询申请人
        Map<Long, AdminUserRespDTO> users = adminUserApi.getUserMap(
                convertSet(applications, application -> Long.valueOf(application.getCreator())));
        // 2. 转换并拼接 VO
        return convertList(applications, application -> {
            OaReimbursementRespVO vo = BeanUtils.toBean(application, OaReimbursementRespVO.class);
            MapUtils.findAndThen(users, Long.valueOf(application.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            return vo;
        });
    }

}
