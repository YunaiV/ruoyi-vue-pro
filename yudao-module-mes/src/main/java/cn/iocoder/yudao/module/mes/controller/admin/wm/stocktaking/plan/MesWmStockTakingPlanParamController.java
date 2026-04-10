package cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.plan;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.plan.vo.param.MesWmStockTakingPlanParamPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.plan.vo.param.MesWmStockTakingPlanParamRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.plan.vo.param.MesWmStockTakingPlanParamSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.plan.MesWmStockTakingPlanParamDO;
import cn.iocoder.yudao.module.mes.service.wm.stocktaking.plan.MesWmStockTakingPlanParamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 盘点方案参数")
@RestController
@RequestMapping("/mes/wm/stocktaking-plan-param")
@Validated
public class MesWmStockTakingPlanParamController {

    @Resource
    private MesWmStockTakingPlanParamService stockTakingPlanParamService;

    @PostMapping("/create")
    @Operation(summary = "创建盘点方案参数")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-plan:update')")
    public CommonResult<Long> createStockTakingPlanParam(@Valid @RequestBody MesWmStockTakingPlanParamSaveReqVO createReqVO) {
        return success(stockTakingPlanParamService.createStockTakingPlanParam(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改盘点方案参数")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-plan:update')")
    public CommonResult<Boolean> updateStockTakingPlanParam(@Valid @RequestBody MesWmStockTakingPlanParamSaveReqVO updateReqVO) {
        stockTakingPlanParamService.updateStockTakingPlanParam(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除盘点方案参数")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-plan:update')")
    public CommonResult<Boolean> deleteStockTakingPlanParam(@RequestParam("id") Long id) {
        stockTakingPlanParamService.deleteStockTakingPlanParam(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得盘点方案参数")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-plan:query')")
    public CommonResult<MesWmStockTakingPlanParamRespVO> getStockTakingPlanParam(@RequestParam("id") Long id) {
        MesWmStockTakingPlanParamDO param = stockTakingPlanParamService.getStockTakingPlanParam(id);
        return success(param == null ? null : BeanUtils.toBean(param, MesWmStockTakingPlanParamRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得盘点方案参数分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-stock-taking-plan:query')")
    public CommonResult<PageResult<MesWmStockTakingPlanParamRespVO>> getStockTakingPlanParamPage(
            @Valid MesWmStockTakingPlanParamPageReqVO pageReqVO) {
        PageResult<MesWmStockTakingPlanParamDO> pageResult = stockTakingPlanParamService.getStockTakingPlanParamPage(pageReqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), MesWmStockTakingPlanParamRespVO.class), pageResult.getTotal()));
    }


}
