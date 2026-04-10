package cn.iocoder.yudao.module.mes.controller.admin.pro.route;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.MesProRoutePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.MesProRouteRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.MesProRouteSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteDO;
import cn.iocoder.yudao.module.mes.service.pro.route.MesProRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - MES 工艺路线")
@RestController
@RequestMapping("/mes/pro/route")
@Validated
public class MesProRouteController {

    @Resource
    private MesProRouteService routeService;

    @PostMapping("/create")
    @Operation(summary = "创建工艺路线")
    @PreAuthorize("@ss.hasPermission('mes:pro-route:create')")
    public CommonResult<Long> createRoute(@Valid @RequestBody MesProRouteSaveReqVO createReqVO) {
        return success(routeService.createRoute(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工艺路线")
    @PreAuthorize("@ss.hasPermission('mes:pro-route:update')")
    public CommonResult<Boolean> updateRoute(@Valid @RequestBody MesProRouteSaveReqVO updateReqVO) {
        routeService.updateRoute(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新工艺路线状态")
    @Parameters({
            @Parameter(name = "id", description = "编号", required = true),
            @Parameter(name = "status", description = "状态", required = true)
    })
    @PreAuthorize("@ss.hasPermission('mes:pro-route:update')")
    public CommonResult<Boolean> updateRouteStatus(@RequestParam("id") Long id,
                                                    @RequestParam("status") Integer status) {
        routeService.updateRouteStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工艺路线")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-route:delete')")
    public CommonResult<Boolean> deleteRoute(@RequestParam("id") Long id) {
        routeService.deleteRoute(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工艺路线")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:pro-route:query')")
    public CommonResult<MesProRouteRespVO> getRoute(@RequestParam("id") Long id) {
        MesProRouteDO route = routeService.getRoute(id);
        return success(BeanUtils.toBean(route, MesProRouteRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工艺路线分页")
    @PreAuthorize("@ss.hasPermission('mes:pro-route:query')")
    public CommonResult<PageResult<MesProRouteRespVO>> getRoutePage(@Valid MesProRoutePageReqVO pageReqVO) {
        PageResult<MesProRouteDO> pageResult = routeService.getRoutePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MesProRouteRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得工艺路线精简列表", description = "只包含被开启的工艺路线，主要用于前端的下拉选项")
    public CommonResult<List<MesProRouteRespVO>> getRouteSimpleList() {
        List<MesProRouteDO> list = routeService.getRouteListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, route -> new MesProRouteRespVO()
                .setId(route.getId()).setName(route.getName()).setCode(route.getCode())));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出工艺路线 Excel")
    @PreAuthorize("@ss.hasPermission('mes:pro-route:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRouteExcel(@Valid MesProRoutePageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesProRouteDO> list = routeService.getRoutePage(pageReqVO).getList();
        List<MesProRouteRespVO> data = BeanUtils.toBean(list, MesProRouteRespVO.class);
        ExcelUtils.write(response, "工艺路线.xls", "数据", MesProRouteRespVO.class, data);
    }

}
