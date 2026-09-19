package cn.iocoder.yudao.module.oa.controller.admin.travel;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.travel.vo.apply.*;
import cn.iocoder.yudao.module.oa.controller.admin.travel.vo.apply.OaTravelApplyRespVO;
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

@Tag(name = "管理后台 - 出差申请")
@RestController
@RequestMapping("/oa/travel-apply")
@Validated
public class OaTravelApplyController {

    @Resource
    private OaTravelApplyService travelApplyService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建出差申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:travel-apply:save')")
    public CommonResult<Long> createTravelApply(@Valid @RequestBody OaTravelApplySaveReqVO reqVO) {
        return success(travelApplyService.createTravelApply(reqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新出差申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:travel-apply:save')")
    public CommonResult<Boolean> updateTravelApply(@Valid @RequestBody OaTravelApplySaveReqVO reqVO) {
        travelApplyService.updateTravelApply(reqVO, getLoginUserId());
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交出差申请")
    @PreAuthorize("@ss.hasPermission('oa:travel-apply:save')")
    public CommonResult<Boolean> submitTravelApply(@Valid @RequestBody OaTravelApplySubmitReqVO submitReqVO) {
        travelApplyService.submitTravelApply(submitReqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "撤回出差申请")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:travel-apply:save')")
    public CommonResult<Boolean> cancelTravelApply(@RequestParam("id") Long id) {
        travelApplyService.cancelTravelApply(id, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除出差申请")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:travel-apply:delete')")
    public CommonResult<Boolean> deleteTravelApply(@RequestParam("id") Long id) {
        travelApplyService.deleteTravelApply(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得出差申请详情")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:travel-apply:query')")
    public CommonResult<OaTravelApplyRespVO> getTravelApply(@RequestParam("id") Long id) {
        OaTravelApplyDO record = travelApplyService.getTravelApply(id);
        return success(buildTravelApplyRespVO(record));
    }

    @GetMapping("/page")
    @Operation(summary = "获得本人出差申请分页")
    @PreAuthorize("@ss.hasPermission('oa:travel-apply:query')")
    public CommonResult<PageResult<OaTravelApplyRespVO>> getTravelApplyPage(@Valid OaTravelApplyPageReqVO reqVO) {
        PageResult<OaTravelApplyDO> page = travelApplyService.getTravelApplyPage(getLoginUserId(), reqVO);
        return success(new PageResult<>(buildTravelApplyRespVOList(page.getList()), page.getTotal()));
    }

    @GetMapping("/approved-list")
    @Operation(summary = "获得本人可关联的已通过出差申请")
    public CommonResult<List<OaTravelApplyRespVO>> getApprovedTravelApplyList() {
        List<OaTravelApplyDO> applies = travelApplyService.getApprovedTravelApplyList(getLoginUserId());
        return success(BeanUtils.toBean(applies, OaTravelApplyRespVO.class));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接出差申请详情
     *
     * @param record 单据
     * @return 响应信息
     */
    private OaTravelApplyRespVO buildTravelApplyRespVO(OaTravelApplyDO record) {
        if (record == null) {
            return null;
        }
        return CollUtil.getFirst(buildTravelApplyRespVOList(Collections.singletonList(record)));
    }

    /**
     * 批量拼接申请人与部门展示信息
     *
     * @param records 单据列表
     * @return 响应列表
     */
    private List<OaTravelApplyRespVO> buildTravelApplyRespVOList(List<OaTravelApplyDO> records) {
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }
        // 1. 批量查询展示信息
        Map<Long, AdminUserRespDTO> users = adminUserApi.getUserMap(
                convertSet(records, record -> Long.valueOf(record.getCreator())));
        Map<Long, DeptRespDTO> depts = deptApi.getDeptMap(convertSet(records, OaTravelApplyDO::getDeptId));
        // 2. 转换并拼接 VO
        return convertList(records, record -> {
            OaTravelApplyRespVO vo = BeanUtils.toBean(record, OaTravelApplyRespVO.class);
            MapUtils.findAndThen(users, Long.valueOf(record.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(depts, record.getDeptId(), dept -> vo.setDeptName(dept.getName()));
            return vo;
        });
    }

}
