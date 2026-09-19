package cn.iocoder.yudao.module.oa.controller.admin.seal;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.seal.vo.OaSealPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.seal.vo.OaSealRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.seal.vo.apply.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.seal.OaSealApplyDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.seal.OaSealDO;
import cn.iocoder.yudao.module.oa.service.seal.OaSealApplyService;
import cn.iocoder.yudao.module.oa.service.seal.OaSealService;
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
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 用印申请")
@RestController
@RequestMapping("/oa/seal-apply")
@Validated
public class OaSealApplyController {

    @Resource
    private OaSealApplyService sealApplyService;
    @Resource
    private OaSealService sealService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @GetMapping("/seal-page")
    @Operation(summary = "获得可申请的印章分页")
    @PreAuthorize("@ss.hasAnyPermissions('oa:seal-apply:create', 'oa:seal-apply:query')")
    public CommonResult<PageResult<OaSealRespVO>> getSealPage(@Valid OaSealPageReqVO pageReqVO) {
        PageResult<OaSealDO> pageResult = sealService.getSealPage(pageReqVO);
        return success(new PageResult<>(convertList(pageResult.getList(), seal -> new OaSealRespVO()
                .setId(seal.getId()).setNo(seal.getNo()).setName(seal.getName())), pageResult.getTotal()));
    }

    @PostMapping("/create")
    @Operation(summary = "创建用印申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:seal-apply:create')")
    public CommonResult<Long> createSealApply(@Valid @RequestBody OaSealApplySaveReqVO reqVO) {
        return success(sealApplyService.createSealApply(reqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用印申请草稿")
    @PreAuthorize("@ss.hasPermission('oa:seal-apply:update')")
    public CommonResult<Boolean> updateSealApply(@Valid @RequestBody OaSealApplySaveReqVO reqVO) {
        sealApplyService.updateSealApply(reqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用印申请草稿")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:seal-apply:delete')")
    public CommonResult<Boolean> deleteSealApply(@RequestParam("id") Long id) {
        sealApplyService.deleteSealApply(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交用印申请")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:seal-apply:create')")
    public CommonResult<Boolean> submitSealApply(@RequestParam("id") Long id) {
        sealApplyService.submitSealApply(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "撤销用印申请")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:seal-apply:update')")
    public CommonResult<Boolean> cancelSealApply(@RequestParam("id") Long id) {
        sealApplyService.cancelSealApply(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用印申请")
    @Parameter(name = "id", description = "申请编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:seal-apply:query')")
    public CommonResult<OaSealApplyRespVO> getSealApply(@RequestParam("id") Long id) {
        OaSealApplyDO apply = sealApplyService.getSealApply(id);
        return success(buildSealApplyRespVO(apply));
    }

    @GetMapping("/page")
    @Operation(summary = "获得本人的用印申请分页")
    @PreAuthorize("@ss.hasPermission('oa:seal-apply:query')")
    public CommonResult<PageResult<OaSealApplyRespVO>> getSealApplyPage(
            @Valid OaSealApplyPageReqVO pageReqVO) {
        PageResult<OaSealApplyDO> pageResult = sealApplyService.getSealApplyPage(getLoginUserId(), pageReqVO);
        return success(new PageResult<>(buildSealApplyRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接用印申请详情
     *
     * @param apply 用印申请
     * @return 用印申请详情
     */
    private OaSealApplyRespVO buildSealApplyRespVO(OaSealApplyDO apply) {
        if (apply == null) {
            return null;
        }
        return CollUtil.getFirst(buildSealApplyRespVOList(Collections.singletonList(apply)));
    }

    /**
     * 拼接用印申请列表的用户和部门信息
     *
     * @param applies 用印申请列表
     * @return 用印申请响应列表
     */
    private List<OaSealApplyRespVO> buildSealApplyRespVOList(List<OaSealApplyDO> applies) {
        if (CollUtil.isEmpty(applies)) {
            return Collections.emptyList();
        }
        // 1. 批量查询申请人、申请部门和关联业务信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSetByFlatMap(applies, apply -> Stream.of(apply.getUserId(), apply.getKeeperUserId())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSetByFlatMap(applies, apply -> Stream.of(apply.getDeptId(), apply.getKeeperDeptId())));
        // 2. 转换并拼接展示字段
        return convertList(applies, item -> {
            OaSealApplyRespVO vo = BeanUtils.toBean(item, OaSealApplyRespVO.class);
            MapUtils.findAndThen(userMap, item.getUserId(), user -> vo.setUserName(user.getNickname()));
            MapUtils.findAndThen(deptMap, item.getDeptId(), dept -> vo.setDeptName(dept.getName()));
            MapUtils.findAndThen(userMap, item.getKeeperUserId(), user -> vo.setKeeperName(user.getNickname()));
            MapUtils.findAndThen(deptMap, item.getKeeperDeptId(), dept -> vo.setKeeperDeptName(dept.getName()));
            return vo;
        });
    }

}
