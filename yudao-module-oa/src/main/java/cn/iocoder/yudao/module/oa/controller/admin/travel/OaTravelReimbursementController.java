package cn.iocoder.yudao.module.oa.controller.admin.travel;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.travel.vo.reimbursement.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.travel.*;
import cn.iocoder.yudao.module.oa.service.travel.*;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
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

@Tag(name = "管理后台 - 出差报销")
@RestController
@RequestMapping("/oa/travel-reimbursement")
@Validated
public class OaTravelReimbursementController {

    @Resource
    private OaTravelReimbursementService travelReimbursementService;
    @Resource
    private OaTravelApplyService travelApplyService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建出差报销草稿")
    @PreAuthorize("@ss.hasPermission('oa:travel-reimbursement:save')")
    public CommonResult<Long> createTravelReimbursement(@Valid @RequestBody OaTravelReimbursementSaveReqVO reqVO) {
        return success(travelReimbursementService.createTravelReimbursement(reqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新出差报销草稿")
    @PreAuthorize("@ss.hasPermission('oa:travel-reimbursement:save')")
    public CommonResult<Boolean> updateTravelReimbursement(@Valid @RequestBody OaTravelReimbursementSaveReqVO reqVO) {
        travelReimbursementService.updateTravelReimbursement(reqVO, getLoginUserId());
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交出差报销")
    @PreAuthorize("@ss.hasPermission('oa:travel-reimbursement:save')")
    public CommonResult<Boolean> submitTravelReimbursement(@Valid @RequestBody OaTravelReimbursementSubmitReqVO submitReqVO) {
        travelReimbursementService.submitTravelReimbursement(submitReqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "撤回出差报销")
    @Parameter(name = "id", description = "报销编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:travel-reimbursement:save')")
    public CommonResult<Boolean> cancelTravelReimbursement(@RequestParam("id") Long id) {
        travelReimbursementService.cancelTravelReimbursement(id, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除出差报销")
    @Parameter(name = "id", description = "报销编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:travel-reimbursement:delete')")
    public CommonResult<Boolean> deleteTravelReimbursement(@RequestParam("id") Long id) {
        travelReimbursementService.deleteTravelReimbursement(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得出差报销详情")
    @Parameter(name = "id", description = "报销编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:travel-reimbursement:query')")
    public CommonResult<OaTravelReimbursementRespVO> getTravelReimbursement(@RequestParam("id") Long id) {
        OaTravelReimbursementDO record = travelReimbursementService.getTravelReimbursement(id);
        return success(buildTravelReimbursementRespVO(record));
    }

    @GetMapping("/page")
    @Operation(summary = "获得本人出差报销分页")
    @PreAuthorize("@ss.hasPermission('oa:travel-reimbursement:query')")
    public CommonResult<PageResult<OaTravelReimbursementRespVO>> getTravelReimbursementPage(@Valid OaTravelReimbursementPageReqVO reqVO) {
        PageResult<OaTravelReimbursementDO> page = travelReimbursementService.getTravelReimbursementPage(getLoginUserId(), reqVO);
        return success(new PageResult<>(buildTravelReimbursementRespVOList(page.getList()), page.getTotal()));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接出差报销详情
     *
     * @param record 单据
     * @return 响应信息
     */
    private OaTravelReimbursementRespVO buildTravelReimbursementRespVO(OaTravelReimbursementDO record) {
        if (record == null) {
            return null;
        }
        return CollUtil.getFirst(buildTravelReimbursementRespVOList(Collections.singletonList(record)));
    }

    /**
     * 批量拼接申请人与部门展示信息
     *
     * @param records 单据列表
     * @return 响应列表
     */
    private List<OaTravelReimbursementRespVO> buildTravelReimbursementRespVOList(List<OaTravelReimbursementDO> records) {
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }
        // 1. 批量查询展示信息
        Map<Long, AdminUserRespDTO> users = adminUserApi.getUserMap(
                convertSet(records, record -> Long.valueOf(record.getCreator())));
        Map<Long, DeptRespDTO> depts = deptApi.getDeptMap(convertSet(records, OaTravelReimbursementDO::getDeptId));
        Map<Long, OaTravelApplyDO> applies = travelApplyService.getTravelApplyMap(
                convertSet(records, OaTravelReimbursementDO::getTravelApplyId, record -> record.getTravelApplyId() != null));
        // 2. 转换并拼接 VO
        return convertList(records, record -> {
            OaTravelReimbursementRespVO vo = BeanUtils.toBean(record, OaTravelReimbursementRespVO.class);
            MapUtils.findAndThen(users, Long.valueOf(record.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(depts, record.getDeptId(), dept -> vo.setDeptName(dept.getName()));
            MapUtils.findAndThen(applies, record.getTravelApplyId(), apply -> vo.setTravelApplyNo(apply.getNo()));
            return vo;
        });
    }

}
