package cn.iocoder.yudao.module.oa.controller.admin.supply;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.apply.*;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue.*;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.item.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.supply.*;
import cn.iocoder.yudao.module.oa.service.supply.*;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "管理后台 - 领用发放")
@RestController
@RequestMapping("/oa/supply-issue")
@Validated
public class OaSupplyIssueController {

    @Resource
    private OaSupplyApplyService supplyApplyService;
    @Resource
    private OaSupplyIssueService supplyIssueService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @GetMapping("/page")
    @Operation(summary = "获得领用发放分页")
    @PreAuthorize("@ss.hasPermission('oa:supply-issue:query')")
    public CommonResult<PageResult<OaSupplyIssueRespVO>> getSupplyIssuePage(@Valid OaSupplyIssuePageReqVO reqVO) {
        PageResult<OaSupplyApplyItemDO> page = supplyIssueService.getSupplyIssuePage(reqVO);
        return success(new PageResult<>(buildSupplyApplyItemRespVOList(page.getList()), page.getTotal()));
    }

    @PutMapping("/issue")
    @Operation(summary = "发放用品")
    @PreAuthorize("@ss.hasPermission('oa:supply-issue:issue')")
    public CommonResult<Boolean> issueSupply(@Valid @RequestBody OaSupplyIssueReqVO reqVO) {
        supplyIssueService.issueSupply(reqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/return")
    @Operation(summary = "确认归还用品")
    @PreAuthorize("@ss.hasPermission('oa:supply-issue:return')")
    public CommonResult<Boolean> returnSupply(@Valid @RequestBody OaSupplyReturnReqVO reqVO) {
        supplyIssueService.returnSupply(reqVO);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接领用发放展示字段
     *
     * @param items 申请明细
     * @return 领用发放列表
     */
    private List<OaSupplyIssueRespVO> buildSupplyApplyItemRespVOList(List<OaSupplyApplyItemDO> items) {
        if (CollUtil.isEmpty(items)) {
            return Collections.emptyList();
        }
        // 1.1 查询申请信息
        Map<Long, OaSupplyApplyDO> applies = supplyApplyService.getSupplyApplyMap(convertSet(items, OaSupplyApplyItemDO::getApplyId));
        // 1.2 查询申请人与发放人
        Set<Long> userIds = convertSet(applies.values(), apply -> Long.valueOf(apply.getCreator()));
        userIds.addAll(convertSet(items, OaSupplyApplyItemDO::getIssueUserId));
        Map<Long, AdminUserRespDTO> users = adminUserApi.getUserMap(userIds);
        // 1.3 查询部门
        Map<Long, DeptRespDTO> depts = deptApi.getDeptMap(convertSet(applies.values(), OaSupplyApplyDO::getDeptId));
        // 2. 拼接展示字段
        return convertList(items, item -> {
            OaSupplyIssueRespVO respVO = BeanUtils.toBean(item, OaSupplyIssueRespVO.class);
            MapUtils.findAndThen(applies, item.getApplyId(), apply -> {
                respVO.setNo(apply.getNo()).setUseType(apply.getUseType()).setCreateTime(apply.getCreateTime());
                MapUtils.findAndThen(users, Long.valueOf(apply.getCreator()), user -> respVO.setCreatorName(user.getNickname()));
                MapUtils.findAndThen(depts, apply.getDeptId(), dept -> respVO.setDeptName(dept.getName()));
            });
            MapUtils.findAndThen(users, item.getIssueUserId(), user -> respVO.setIssueUserName(user.getNickname()));
            return respVO;
        });
    }

}
