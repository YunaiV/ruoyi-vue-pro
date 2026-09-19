package cn.iocoder.yudao.module.oa.controller.admin.vehicle;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.OaVehiclePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.OaVehicleRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.apply.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleApplyDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleDO;
import cn.iocoder.yudao.module.oa.service.vehicle.OaVehicleApplyService;
import cn.iocoder.yudao.module.oa.service.vehicle.OaVehicleService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
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
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 用车申请")
@RestController
@RequestMapping("/oa/vehicle-apply")
@Validated
public class OaVehicleApplyController {

    @Resource
    private OaVehicleApplyService vehicleApplyService;
    @Resource
    private OaVehicleService vehicleService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @GetMapping("/vehicle-page")
    @Operation(summary = "获得用车申请可选的车辆分页")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-apply:create')")
    public CommonResult<PageResult<OaVehicleRespVO>> getAvailableVehiclePage(@Valid OaVehiclePageReqVO pageReqVO) {
        PageResult<OaVehicleDO> pageResult = vehicleService.getVehiclePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, OaVehicleRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建用车申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-apply:create')")
    public CommonResult<Long> createVehicleApply(@Valid @RequestBody OaVehicleApplySaveReqVO reqVO) {
        return success(vehicleApplyService.createVehicleApply(reqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用车申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-apply:update')")
    public CommonResult<Boolean> updateVehicleApply(@Valid @RequestBody OaVehicleApplySaveReqVO reqVO) {
        vehicleApplyService.updateVehicleApply(reqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用车申请草稿")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-apply:delete')")
    public CommonResult<Boolean> deleteVehicleApply(@RequestParam("id") Long id) {
        vehicleApplyService.deleteVehicleApply(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交用车申请")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-apply:create')")
    public CommonResult<Boolean> submitVehicleApply(@RequestParam("id") Long id) {
        vehicleApplyService.submitVehicleApply(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "撤销用车申请")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-apply:update')")
    public CommonResult<Boolean> cancelVehicleApply(@RequestParam("id") Long id) {
        vehicleApplyService.cancelVehicleApply(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用车申请详情")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-apply:query')")
    public CommonResult<OaVehicleApplyRespVO> getVehicleApply(@RequestParam("id") Long id) {
        OaVehicleApplyDO apply = vehicleApplyService.getVehicleApply(id);
        return success(buildVehicleApplyRespVO(apply));
    }

    @GetMapping("/page")
    @Operation(summary = "获得本人的用车申请分页")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-apply:query')")
    public CommonResult<PageResult<OaVehicleApplyRespVO>> getVehicleApplyPage(@Valid OaVehicleApplyPageReqVO pageReqVO) {
        PageResult<OaVehicleApplyDO> pageResult = vehicleApplyService.getVehicleApplyPage(getLoginUserId(), pageReqVO);
        return success(new PageResult<>(buildVehicleApplyRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接用车申请详情
     *
     * @param apply 用车申请
     * @return 用车申请响应
     */
    private OaVehicleApplyRespVO buildVehicleApplyRespVO(OaVehicleApplyDO apply) {
        if (apply == null) {
            return null;
        }
        return CollUtil.getFirst(buildVehicleApplyRespVOList(Collections.singletonList(apply)));
    }

    /**
     * 构建用车申请响应列表，批量拼接关联名称
     *
     * @param applies 申请列表
     * @return 申请响应列表
     */
    private List<OaVehicleApplyRespVO> buildVehicleApplyRespVOList(List<OaVehicleApplyDO> applies) {
        if (CollUtil.isEmpty(applies)) {
            return Collections.emptyList();
        }
        // 1. 批量查询申请人、申请部门和关联业务信息
        Set<Long> userIds = convertSet(applies, OaVehicleApplyDO::getUserId);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(applies, OaVehicleApplyDO::getDeptId));
        // 2. 转换并拼接展示字段
        return convertList(applies, item -> {
            OaVehicleApplyRespVO vo = BeanUtils.toBean(item, OaVehicleApplyRespVO.class);
            MapUtils.findAndThen(userMap, item.getUserId(), user -> vo.setUserName(user.getNickname()));
            MapUtils.findAndThen(deptMap, item.getDeptId(), dept -> vo.setDeptName(dept.getName()));
            return vo;
        });
    }

}
