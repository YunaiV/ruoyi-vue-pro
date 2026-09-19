package cn.iocoder.yudao.module.oa.controller.admin.supply;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.apply.*;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.supply.*;
import cn.iocoder.yudao.module.oa.service.supply.*;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
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

import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 用品领用申请")
@RestController
@RequestMapping("/oa/supply-apply")
@Validated
public class OaSupplyApplyController {

    @Resource
    private OaSupplyApplyService supplyApplyService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建用品领用申请")
    @PreAuthorize("@ss.hasPermission('oa:supply-apply:create')")
    public CommonResult<Long> createSupplyApply(@Valid @RequestBody OaSupplyApplySaveReqVO reqVO) {
        return success(supplyApplyService.createSupplyApply(reqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用品领用申请")
    @PreAuthorize("@ss.hasPermission('oa:supply-apply:update')")
    public CommonResult<Boolean> updateSupplyApply(@Valid @RequestBody OaSupplyApplySaveReqVO reqVO) {
        supplyApplyService.updateSupplyApply(reqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用品领用申请")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:supply-apply:delete')")
    public CommonResult<Boolean> deleteSupplyApply(@RequestParam("id") Long id) {
        supplyApplyService.deleteSupplyApply(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交用品领用申请")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:supply-apply:create')")
    public CommonResult<Boolean> submitSupplyApply(@RequestParam("id") Long id) {
        supplyApplyService.submitSupplyApply(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消用品领用申请")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:supply-apply:update')")
    public CommonResult<Boolean> cancelSupplyApply(@RequestParam("id") Long id) {
        supplyApplyService.cancelSupplyApply(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用品领用申请")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:supply-apply:query')")
    public CommonResult<OaSupplyApplyRespVO> getSupplyApply(@RequestParam("id") Long id) {
        OaSupplyApplyDO apply = supplyApplyService.getSupplyApply(id);
        return success(buildSupplyApplyRespVO(apply));
    }

    @GetMapping("/page")
    @Operation(summary = "获得本人的用品领用申请分页")
    @PreAuthorize("@ss.hasPermission('oa:supply-apply:query')")
    public CommonResult<PageResult<OaSupplyApplyRespVO>> getSupplyApplyPage(@Valid OaSupplyApplyPageReqVO reqVO) {
        PageResult<OaSupplyApplyDO> page = supplyApplyService.getSupplyApplyPage(reqVO, getLoginUserId());
        return success(new PageResult<>(buildSupplyApplyRespVOList(page.getList()), page.getTotal()));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接用品领用申请详情
     *
     * @param apply 领用申请
     * @return 领用申请详情
     */
    private OaSupplyApplyRespVO buildSupplyApplyRespVO(OaSupplyApplyDO apply) {
        if (apply == null) {
            return null;
        }
        List<OaSupplyApplyItemDO> items = supplyApplyService.getSupplyApplyItemList(apply.getId());
        OaSupplyApplyRespVO respVO = CollUtil.getFirst(buildSupplyApplyRespVOList(Collections.singletonList(apply)));
        respVO.setItems(BeanUtils.toBean(items, OaSupplyIssueRespVO.class));
        return respVO;
    }

    /**
     * 拼接申请人及申请部门
     *
     * @param applies 申请列表
     * @return 申请响应列表
     */
    private List<OaSupplyApplyRespVO> buildSupplyApplyRespVOList(List<OaSupplyApplyDO> applies) {
        if (CollUtil.isEmpty(applies)) {
            return Collections.emptyList();
        }
        // 1. 查询申请人及部门
        Map<Long, AdminUserRespDTO> users = adminUserApi.getUserMap(convertSet(applies, apply -> Long.valueOf(apply.getCreator())));
        Map<Long, DeptRespDTO> depts = deptApi.getDeptMap(convertSet(applies, OaSupplyApplyDO::getDeptId));
        // 2. 拼接展示字段
        return convertList(applies, apply -> {
            OaSupplyApplyRespVO respVO = BeanUtils.toBean(apply, OaSupplyApplyRespVO.class);
            MapUtils.findAndThen(users, Long.valueOf(apply.getCreator()), user -> respVO.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(depts, apply.getDeptId(), dept -> respVO.setDeptName(dept.getName()));
            return respVO;
        });
    }

}
