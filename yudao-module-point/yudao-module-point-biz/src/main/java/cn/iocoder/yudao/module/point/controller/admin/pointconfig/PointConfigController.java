package cn.iocoder.yudao.module.point.controller.admin.pointconfig;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.point.controller.admin.pointconfig.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.pointconfig.PointConfigDO;
import cn.iocoder.yudao.module.point.convert.pointconfig.PointConfigConvert;
import cn.iocoder.yudao.module.point.service.pointconfig.PointConfigService;

@Tag(name = "管理后台 - 积分设置")
@RestController
@RequestMapping("/point/config")
@Validated
public class PointConfigController {

    @Resource
    private PointConfigService configService;

    @PostMapping("/create")
    @Operation(summary = "创建积分设置")
    @PreAuthorize("@ss.hasPermission('point:config:create')")
    public CommonResult<Integer> createConfig(@Valid @RequestBody PointConfigCreateReqVO createReqVO) {
        return success(configService.createConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新积分设置")
    @PreAuthorize("@ss.hasPermission('point:config:update')")
    public CommonResult<Boolean> updateConfig(@Valid @RequestBody PointConfigUpdateReqVO updateReqVO) {
        configService.updateConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除积分设置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('point:config:delete')")
    public CommonResult<Boolean> deleteConfig(@RequestParam("id") Integer id) {
        configService.deleteConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得积分设置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('point:config:query')")
    public CommonResult<PointConfigRespVO> getConfig(@RequestParam("id") Integer id) {
        PointConfigDO config = configService.getConfig(id);
        return success(PointConfigConvert.INSTANCE.convert(config));
    }

    @GetMapping("/list")
    @Operation(summary = "获得积分设置列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('point:config:query')")
    public CommonResult<List<PointConfigRespVO>> getConfigList(@RequestParam("ids") Collection<Integer> ids) {
        List<PointConfigDO> list = configService.getConfigList(ids);
        return success(PointConfigConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得积分设置分页")
    @PreAuthorize("@ss.hasPermission('point:config:query')")
    public CommonResult<PageResult<PointConfigRespVO>> getConfigPage(@Valid PointConfigPageReqVO pageVO) {
        PageResult<PointConfigDO> pageResult = configService.getConfigPage(pageVO);
        return success(PointConfigConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出积分设置 Excel")
    @PreAuthorize("@ss.hasPermission('point:config:export')")
    @OperateLog(type = EXPORT)
    public void exportConfigExcel(@Valid PointConfigExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<PointConfigDO> list = configService.getConfigList(exportReqVO);
        // 导出 Excel
        List<PointConfigExcelVO> datas = PointConfigConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "积分设置.xls", "数据", PointConfigExcelVO.class, datas);
    }

}
