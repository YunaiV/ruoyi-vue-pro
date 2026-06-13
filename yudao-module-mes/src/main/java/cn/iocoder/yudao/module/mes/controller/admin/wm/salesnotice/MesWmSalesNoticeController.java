package cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo.MesWmSalesNoticePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo.MesWmSalesNoticeRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo.MesWmSalesNoticeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.salesnotice.MesWmSalesNoticeDO;
import cn.iocoder.yudao.module.mes.service.md.client.MesMdClientService;
import cn.iocoder.yudao.module.mes.service.wm.salesnotice.MesWmSalesNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 发货通知单")
@RestController
@RequestMapping("/mes/wm/sales-notice")
@Validated
public class MesWmSalesNoticeController {

    @Resource
    private MesWmSalesNoticeService salesNoticeService;
    @Resource
    private MesMdClientService clientService;

    @PostMapping("/create")
    @Operation(summary = "创建发货通知单")
    @PreAuthorize("@ss.hasPermission('mes:wm-sales-notice:create')")
    public CommonResult<Long> createSalesNotice(@Valid @RequestBody MesWmSalesNoticeSaveReqVO createReqVO) {
        return success(salesNoticeService.createSalesNotice(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改发货通知单")
    @PreAuthorize("@ss.hasPermission('mes:wm-sales-notice:update')")
    public CommonResult<Boolean> updateSalesNotice(@Valid @RequestBody MesWmSalesNoticeSaveReqVO updateReqVO) {
        salesNoticeService.updateSalesNotice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除发货通知单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-sales-notice:delete')")
    public CommonResult<Boolean> deleteSalesNotice(@RequestParam("id") Long id) {
        salesNoticeService.deleteSalesNotice(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得发货通知单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-sales-notice:query')")
    public CommonResult<MesWmSalesNoticeRespVO> getSalesNotice(@RequestParam("id") Long id) {
        MesWmSalesNoticeDO notice = salesNoticeService.getSalesNotice(id);
        if (notice == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(notice)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得发货通知单分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-sales-notice:query')")
    public CommonResult<PageResult<MesWmSalesNoticeRespVO>> getSalesNoticePage(
            @Valid MesWmSalesNoticePageReqVO pageReqVO) {
        PageResult<MesWmSalesNoticeDO> pageResult = salesNoticeService.getSalesNoticePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出发货通知单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wm-sales-notice:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSalesNoticeExcel(@Valid MesWmSalesNoticePageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<MesWmSalesNoticeDO> pageResult = salesNoticeService.getSalesNoticePage(pageReqVO);
        ExcelUtils.write(response, "发货通知单.xls", "数据", MesWmSalesNoticeRespVO.class,
                buildRespVOList(pageResult.getList()));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交发货通知单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-sales-notice:update')")
    public CommonResult<Boolean> submitSalesNotice(@RequestParam("id") Long id) {
        salesNoticeService.submitSalesNotice(id);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private List<MesWmSalesNoticeRespVO> buildRespVOList(List<MesWmSalesNoticeDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdClientDO> clientMap = clientService.getClientMap(
                convertSet(list, MesWmSalesNoticeDO::getClientId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmSalesNoticeRespVO.class, vo -> {
            MapUtils.findAndThen(clientMap, vo.getClientId(), client ->
                    vo.setClientCode(client.getCode()).setClientName(client.getName()));
        });
    }

}
