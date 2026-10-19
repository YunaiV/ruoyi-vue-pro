package cn.iocoder.yudao.module.oa.controller.admin.supply;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.apply.*;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.issue.*;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.item.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.supply.*;
import cn.iocoder.yudao.module.oa.service.supply.*;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
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

@Tag(name = "管理后台 - 办公用品")
@RestController
@RequestMapping("/oa/supply-item")
@Validated
public class OaSupplyItemController {

    @Resource
    private OaSupplyItemService supplyItemService;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建办公用品")
    @PreAuthorize("@ss.hasPermission('oa:supply-item:create')")
    public CommonResult<Long> createSupplyItem(@Valid @RequestBody OaSupplyItemSaveReqVO reqVO) {
        return success(supplyItemService.createSupplyItem(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新办公用品")
    @PreAuthorize("@ss.hasPermission('oa:supply-item:update')")
    public CommonResult<Boolean> updateSupplyItem(@Valid @RequestBody OaSupplyItemSaveReqVO reqVO) {
        supplyItemService.updateSupplyItem(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除办公用品")
    @Parameter(name = "id", description = "办公用品编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:supply-item:delete')")
    public CommonResult<Boolean> deleteSupplyItem(@RequestParam("id") Long id) {
        supplyItemService.deleteSupplyItem(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得办公用品")
    @Parameter(name = "id", description = "办公用品编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:supply-item:query')")
    public CommonResult<OaSupplyItemRespVO> getSupplyItem(@RequestParam("id") Long id) {
        OaSupplyItemDO item = supplyItemService.getSupplyItem(id);
        return success(BeanUtils.toBean(item, OaSupplyItemRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得办公用品分页")
    @PreAuthorize("@ss.hasPermission('oa:supply-item:query')")
    public CommonResult<PageResult<OaSupplyItemRespVO>> getSupplyItemPage(@Valid OaSupplyItemPageReqVO reqVO) {
        // 1. 查询物品及所属部门
        PageResult<OaSupplyItemDO> page = supplyItemService.getSupplyItemPage(reqVO);
        Map<Long, DeptRespDTO> depts = deptApi.getDeptMap(convertSet(page.getList(), OaSupplyItemDO::getDeptId));
        // 2. 拼接部门名称
        PageResult<OaSupplyItemRespVO> result = BeanUtils.toBean(page, OaSupplyItemRespVO.class);
        result.getList().forEach(item -> {
            DeptRespDTO dept = depts.get(item.getDeptId());
            if (dept != null) {
                item.setDeptName(dept.getName());
            }
        });
        return success(result);
    }

    @GetMapping("/select-page")
    @Operation(summary = "获得可领用的办公用品分页")
    public CommonResult<PageResult<OaSupplyItemRespVO>> getSupplyItemSelectPage(@Valid OaSupplyItemPageReqVO reqVO) {
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        PageResult<OaSupplyItemDO> page = supplyItemService.getSupplyItemPage(reqVO);
        return success(BeanUtils.toBean(page, OaSupplyItemRespVO.class));
    }

    @PutMapping("/stock-in")
    @Operation(summary = "办公用品入库")
    @PreAuthorize("@ss.hasPermission('oa:supply-item:stock-in')")
    public CommonResult<Boolean> stockInSupplyItem(@Valid @RequestBody OaSupplyItemStockInReqVO reqVO) {
        supplyItemService.stockInSupplyItem(reqVO);
        return success(true);
    }

}
