package cn.iocoder.yudao.module.oa.controller.admin.regular;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.regular.vo.OaRegularApplySaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.regular.vo.OaRegularApplySubmitReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.regular.vo.OaRegularApplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.regular.vo.OaRegularApplyRespVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.regular.OaRegularApplyDO;
import cn.iocoder.yudao.module.oa.service.regular.OaRegularApplyService;
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

@Tag(name = "管理后台 - 转正申请")
@RestController
@RequestMapping("/oa/regular-apply")
@Validated
public class OaRegularApplyController {

    @Resource
    private OaRegularApplyService regularApplyService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建转正申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:regular-apply:create')")
    public CommonResult<Long> createRegularApply(@Valid @RequestBody OaRegularApplySaveReqVO createReqVO) {
        return success(regularApplyService.createRegularApply(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新转正申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:regular-apply:create')")
    public CommonResult<Boolean> updateRegularApply(@Valid @RequestBody OaRegularApplySaveReqVO saveReqVO) {
        regularApplyService.updateRegularApply(saveReqVO, getLoginUserId());
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交转正申请")
    @PreAuthorize("@ss.hasPermission('oa:regular-apply:create')")
    public CommonResult<Boolean> submitRegularApply(@Valid @RequestBody OaRegularApplySubmitReqVO submitReqVO) {
        regularApplyService.submitRegularApply(submitReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得本人转正申请分页")
    @PreAuthorize("@ss.hasPermission('oa:regular-apply:query')")
    public CommonResult<PageResult<OaRegularApplyRespVO>> getRegularApplyPage(@Valid OaRegularApplyPageReqVO pageReqVO) {
        PageResult<OaRegularApplyDO> page = regularApplyService.getRegularApplyPage(getLoginUserId(), pageReqVO);
        return success(new PageResult<>(buildRegularApplyRespVOList(page.getList()), page.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得转正申请详情")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:regular-apply:query')")
    public CommonResult<OaRegularApplyRespVO> getRegularApply(@RequestParam("id") Long id) {
        OaRegularApplyDO application = regularApplyService.getRegularApply(id);
        return success(buildRegularApplyRespVO(application));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接转正申请详情
     *
     * @param application 申请
     * @return 转正申请响应
     */
    private OaRegularApplyRespVO buildRegularApplyRespVO(OaRegularApplyDO application) {
        if (application == null) {
            return null;
        }
        return CollUtil.getFirst(buildRegularApplyRespVOList(Collections.singletonList(application)));
    }

    /**
     * 批量拼接申请人展示信息
     *
     * @param applications 申请列表
     * @return 响应列表
     */
    private List<OaRegularApplyRespVO> buildRegularApplyRespVOList(List<OaRegularApplyDO> applications) {
        if (CollUtil.isEmpty(applications)) {
            return Collections.emptyList();
        }
        // 1. 批量查询申请人
        Map<Long, AdminUserRespDTO> users = adminUserApi.getUserMap(
                convertSet(applications, application -> Long.valueOf(application.getCreator())));
        // 2. 转换并拼接 VO
        return convertList(applications, application -> {
            OaRegularApplyRespVO vo = BeanUtils.toBean(application, OaRegularApplyRespVO.class);
            MapUtils.findAndThen(users, Long.valueOf(application.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            return vo;
        });
    }

}
