package cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.MesWmArrivalNoticePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.MesWmArrivalNoticeRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.MesWmArrivalNoticeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeDO;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import cn.iocoder.yudao.module.mes.service.wm.arrivalnotice.MesWmArrivalNoticeService;
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

@Tag(name = "管理后台 - MES 到货通知单")
@RestController
@RequestMapping("/mes/wm/arrival-notice")
@Validated
public class MesWmArrivalNoticeController {

    @Resource
    private MesWmArrivalNoticeService arrivalNoticeService;

    @Resource
    private MesMdVendorService vendorService;

    @PostMapping("/create")
    @Operation(summary = "创建到货通知单")
    @PreAuthorize("@ss.hasPermission('mes:wm-arrival-notice:create')")
    public CommonResult<Long> createArrivalNotice(@Valid @RequestBody MesWmArrivalNoticeSaveReqVO createReqVO) {
        return success(arrivalNoticeService.createArrivalNotice(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改到货通知单")
    @PreAuthorize("@ss.hasPermission('mes:wm-arrival-notice:update')")
    public CommonResult<Boolean> updateArrivalNotice(@Valid @RequestBody MesWmArrivalNoticeSaveReqVO updateReqVO) {
        arrivalNoticeService.updateArrivalNotice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除到货通知单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-arrival-notice:delete')")
    public CommonResult<Boolean> deleteArrivalNotice(@RequestParam("id") Long id) {
        arrivalNoticeService.deleteArrivalNotice(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得到货通知单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-arrival-notice:query')")
    public CommonResult<MesWmArrivalNoticeRespVO> getArrivalNotice(@RequestParam("id") Long id) {
        MesWmArrivalNoticeDO notice = arrivalNoticeService.getArrivalNotice(id);
        if (notice == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(notice)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得到货通知单分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-arrival-notice:query')")
    public CommonResult<PageResult<MesWmArrivalNoticeRespVO>> getArrivalNoticePage(
            @Valid MesWmArrivalNoticePageReqVO pageReqVO) {
        PageResult<MesWmArrivalNoticeDO> pageResult = arrivalNoticeService.getArrivalNoticePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出到货通知单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wm-arrival-notice:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportArrivalNoticeExcel(@Valid MesWmArrivalNoticePageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<MesWmArrivalNoticeDO> pageResult = arrivalNoticeService.getArrivalNoticePage(pageReqVO);
        ExcelUtils.write(response, "到货通知单.xls", "数据", MesWmArrivalNoticeRespVO.class,
                buildRespVOList(pageResult.getList()));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交到货通知单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-arrival-notice:update')")
    public CommonResult<Boolean> submitArrivalNotice(@RequestParam("id") Long id) {
        arrivalNoticeService.submitArrivalNotice(id);
        return success(true);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得到货通知单精简列表")
    @Parameter(name = "status", description = "状态", example = "2")
    public CommonResult<List<MesWmArrivalNoticeRespVO>> getArrivalNoticeSimpleList(
            @RequestParam(value = "status", required = false) Integer status) {
        List<MesWmArrivalNoticeDO> list = arrivalNoticeService.getArrivalNoticeListByStatus(status);
        return success(buildRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmArrivalNoticeRespVO> buildRespVOList(List<MesWmArrivalNoticeDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdVendorDO> vendorMap = vendorService.getVendorMap(
                convertSet(list, MesWmArrivalNoticeDO::getVendorId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmArrivalNoticeRespVO.class, vo -> {
            MapUtils.findAndThen(vendorMap, vo.getVendorId(), vendor ->
                    vo.setVendorCode(vendor.getCode()).setVendorName(vendor.getName()));
        });
    }

}
