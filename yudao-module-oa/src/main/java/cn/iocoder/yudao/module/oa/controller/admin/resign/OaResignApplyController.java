package cn.iocoder.yudao.module.oa.controller.admin.resign;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.resign.vo.OaResignApplySaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.resign.vo.OaResignApplySubmitReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.resign.vo.OaResignApplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.resign.vo.OaResignApplyRespVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.resign.OaResignApplyDO;
import cn.iocoder.yudao.module.oa.service.resign.OaResignApplyService;
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

@Tag(name = "管理后台 - 离职申请")
@RestController
@RequestMapping("/oa/resign-apply")
@Validated
public class OaResignApplyController {

    @Resource
    private OaResignApplyService resignApplyService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建离职申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:resign-apply:create')")
    public CommonResult<Long> createResignApply(@Valid @RequestBody OaResignApplySaveReqVO createReqVO) {
        return success(resignApplyService.createResignApply(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新离职申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:resign-apply:create')")
    public CommonResult<Boolean> updateResignApply(@Valid @RequestBody OaResignApplySaveReqVO saveReqVO) {
        resignApplyService.updateResignApply(saveReqVO, getLoginUserId());
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交离职申请")
    @PreAuthorize("@ss.hasPermission('oa:resign-apply:create')")
    public CommonResult<Boolean> submitResignApply(@Valid @RequestBody OaResignApplySubmitReqVO submitReqVO) {
        resignApplyService.submitResignApply(submitReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得本人离职申请分页")
    @PreAuthorize("@ss.hasPermission('oa:resign-apply:query')")
    public CommonResult<PageResult<OaResignApplyRespVO>> getResignApplyPage(@Valid OaResignApplyPageReqVO pageReqVO) {
        PageResult<OaResignApplyDO> page = resignApplyService.getResignApplyPage(getLoginUserId(), pageReqVO);
        return success(new PageResult<>(buildResignApplyRespVOList(page.getList()), page.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得离职申请详情")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:resign-apply:query')")
    public CommonResult<OaResignApplyRespVO> getResignApply(@RequestParam("id") Long id) {
        OaResignApplyDO application = resignApplyService.getResignApply(id);
        return success(buildResignApplyRespVO(application));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接离职申请详情
     *
     * @param application 离职申请
     * @return 离职申请响应
     */
    private OaResignApplyRespVO buildResignApplyRespVO(OaResignApplyDO application) {
        if (application == null) {
            return null;
        }
        return CollUtil.getFirst(buildResignApplyRespVOList(Collections.singletonList(application)));
    }

    /**
     * 批量拼接申请人展示信息
     *
     * @param applications 申请列表
     * @return 响应列表
     */
    private List<OaResignApplyRespVO> buildResignApplyRespVOList(List<OaResignApplyDO> applications) {
        if (CollUtil.isEmpty(applications)) {
            return Collections.emptyList();
        }
        // 1. 批量查询申请人
        Map<Long, AdminUserRespDTO> users = adminUserApi.getUserMap(
                convertSet(applications, application -> Long.valueOf(application.getCreator())));
        // 2. 转换并拼接 VO
        return convertList(applications, application -> {
            OaResignApplyRespVO vo = BeanUtils.toBean(application, OaResignApplyRespVO.class);
            MapUtils.findAndThen(users, Long.valueOf(application.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            return vo;
        });
    }

}
