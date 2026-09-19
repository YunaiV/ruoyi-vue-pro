package cn.iocoder.yudao.module.oa.controller.admin.vehicle;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.returning.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleApplyDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleReturnDO;
import cn.iocoder.yudao.module.oa.service.vehicle.OaVehicleApplyService;
import cn.iocoder.yudao.module.oa.service.vehicle.OaVehicleReturnService;
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

@Tag(name = "管理后台 - 还车申请")
@RestController
@RequestMapping("/oa/vehicle-return")
@Validated
public class OaVehicleReturnController {

    @Resource
    private OaVehicleApplyService vehicleApplyService;
    @Resource
    private OaVehicleReturnService vehicleReturnService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建还车申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-return:create')")
    public CommonResult<Long> createVehicleReturn(@Valid @RequestBody OaVehicleReturnSaveReqVO reqVO) {
        return success(vehicleReturnService.createVehicleReturn(reqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新还车申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-return:update')")
    public CommonResult<Boolean> updateVehicleReturn(@Valid @RequestBody OaVehicleReturnSaveReqVO reqVO) {
        vehicleReturnService.updateVehicleReturn(reqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除还车申请草稿")
    @Parameter(name = "id", description = "还车申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-return:delete')")
    public CommonResult<Boolean> deleteVehicleReturn(@RequestParam("id") Long id) {
        vehicleReturnService.deleteVehicleReturn(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交还车申请")
    @Parameter(name = "id", description = "还车申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-return:create')")
    public CommonResult<Boolean> submitVehicleReturn(@RequestParam("id") Long id) {
        vehicleReturnService.submitVehicleReturn(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消还车申请")
    @Parameter(name = "id", description = "还车申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-return:update')")
    public CommonResult<Boolean> cancelVehicleReturn(@RequestParam("id") Long id) {
        vehicleReturnService.cancelVehicleReturn(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得还车申请详情")
    @Parameter(name = "id", description = "还车申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-return:query')")
    public CommonResult<OaVehicleReturnRespVO> getVehicleReturn(@RequestParam("id") Long id) {
        OaVehicleReturnDO vehicleReturn = vehicleReturnService.getVehicleReturn(id);
        return success(buildVehicleReturnRespVO(vehicleReturn));
    }

    @GetMapping("/page")
    @Operation(summary = "获得本人还车申请分页")
    @PreAuthorize("@ss.hasPermission('oa:vehicle-return:query')")
    public CommonResult<PageResult<OaVehicleReturnRespVO>> getVehicleReturnPage(
            @Valid OaVehicleReturnPageReqVO pageReqVO) {
        PageResult<OaVehicleReturnDO> pageResult = vehicleReturnService.getVehicleReturnPage(getLoginUserId(), pageReqVO);
        return success(new PageResult<>(buildVehicleReturnRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接还车申请详情
     *
     * @param vehicleReturn 还车申请
     * @return 还车申请响应
     */
    private OaVehicleReturnRespVO buildVehicleReturnRespVO(OaVehicleReturnDO vehicleReturn) {
        if (vehicleReturn == null) {
            return null;
        }
        return CollUtil.getFirst(buildVehicleReturnRespVOList(Collections.singletonList(vehicleReturn)));
    }

    /**
     * 构建还车申请响应列表，批量拼接关联名称
     *
     * @param returns 申请列表
     * @return 申请响应列表
     */
    private List<OaVehicleReturnRespVO> buildVehicleReturnRespVOList(List<OaVehicleReturnDO> returns) {
        if (CollUtil.isEmpty(returns)) {
            return Collections.emptyList();
        }
        // 1. 批量查询申请人、申请部门和关联业务信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(returns, OaVehicleReturnDO::getUserId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(returns, OaVehicleReturnDO::getDeptId));
        Map<Long, OaVehicleApplyDO> applyMap = vehicleApplyService.getVehicleApplyMap(convertSet(returns, OaVehicleReturnDO::getApplyId));
        // 2. 转换并拼接展示字段
        return convertList(returns, item -> {
            OaVehicleReturnRespVO vo = BeanUtils.toBean(item, OaVehicleReturnRespVO.class);
            MapUtils.findAndThen(userMap, item.getUserId(), user -> vo.setUserName(user.getNickname()));
            MapUtils.findAndThen(deptMap, item.getDeptId(), dept -> vo.setDeptName(dept.getName()));
            MapUtils.findAndThen(applyMap, item.getApplyId(), apply ->
                    vo.setApplyNo(apply.getNo()).setVehicleNo(apply.getVehicleNo()));
            return vo;
        });
    }

}
