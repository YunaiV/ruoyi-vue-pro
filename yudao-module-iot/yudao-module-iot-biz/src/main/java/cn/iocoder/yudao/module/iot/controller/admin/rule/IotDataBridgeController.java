package cn.iocoder.yudao.module.iot.controller.admin.rule;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.IotDataBridgePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.IotDataBridgeRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.IotDataBridgeSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.service.rule.IotDataBridgeService;
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
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 数据桥梁")
@RestController
@RequestMapping("/iot/data-bridge")
@Validated
public class IotDataBridgeController {

    @Resource
    private IotDataBridgeService dataBridgeService;

    @PostMapping("/create")
    @Operation(summary = "创建IoT 数据桥梁")
    @PreAuthorize("@ss.hasPermission('iot:data-bridge:create')")
    public CommonResult<Long> createDataBridge(@Valid @RequestBody IotDataBridgeSaveReqVO createReqVO) {
        return success(dataBridgeService.createDataBridge(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新IoT 数据桥梁")
    @PreAuthorize("@ss.hasPermission('iot:data-bridge:update')")
    public CommonResult<Boolean> updateDataBridge(@Valid @RequestBody IotDataBridgeSaveReqVO updateReqVO) {
        dataBridgeService.updateDataBridge(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除IoT 数据桥梁")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:data-bridge:delete')")
    public CommonResult<Boolean> deleteDataBridge(@RequestParam("id") Long id) {
        dataBridgeService.deleteDataBridge(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得IoT 数据桥梁")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:data-bridge:query')")
    public CommonResult<IotDataBridgeRespVO> getDataBridge(@RequestParam("id") Long id) {
        IotDataBridgeDO dataBridge = dataBridgeService.getDataBridge(id);
        return success(BeanUtils.toBean(dataBridge, IotDataBridgeRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得IoT 数据桥梁分页")
    @PreAuthorize("@ss.hasPermission('iot:data-bridge:query')")
    public CommonResult<PageResult<IotDataBridgeRespVO>> getDataBridgePage(@Valid IotDataBridgePageReqVO pageReqVO) {
        PageResult<IotDataBridgeDO> pageResult = dataBridgeService.getDataBridgePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDataBridgeRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出IoT 数据桥梁 Excel")
    @PreAuthorize("@ss.hasPermission('iot:data-bridge:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDataBridgeExcel(@Valid IotDataBridgePageReqVO pageReqVO,
                                      HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotDataBridgeDO> list = dataBridgeService.getDataBridgePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "IoT 数据桥梁.xls", "数据", IotDataBridgeRespVO.class,
                BeanUtils.toBean(list, IotDataBridgeRespVO.class));
    }

}