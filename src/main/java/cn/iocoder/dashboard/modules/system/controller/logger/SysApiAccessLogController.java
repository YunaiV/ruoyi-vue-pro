package cn.iocoder.dashboard.modules.system.controller.logger;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.excel.core.util.ExcelUtils;
import cn.iocoder.dashboard.framework.logger.operatelog.core.annotations.OperateLog;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.apiaccesslog.SysApiAccessLogExcelVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.apiaccesslog.SysApiAccessLogExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.apiaccesslog.SysApiAccessLogPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.apiaccesslog.SysApiAccessLogRespVO;
import cn.iocoder.dashboard.modules.system.convert.logger.SysApiAccessLogConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.logger.SysApiAccessLogDO;
import cn.iocoder.dashboard.modules.system.service.logger.SysApiAccessLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;
import static cn.iocoder.dashboard.framework.logger.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "API 访问日志")
@RestController
@RequestMapping("/system/api-access-log")
@Validated
public class SysApiAccessLogController {

    @Resource
    private SysApiAccessLogService apiAccessLogService;

    @GetMapping("/get")
    @ApiOperation("获得API 访问日志")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:api-access-log:query')")
    public CommonResult<SysApiAccessLogRespVO> getApiAccessLog(@RequestParam("id") Long id) {
        SysApiAccessLogDO apiAccessLog = apiAccessLogService.getApiAccessLog(id);
        return success(SysApiAccessLogConvert.INSTANCE.convert(apiAccessLog));
    }

    @GetMapping("/page")
    @ApiOperation("获得API 访问日志分页")
    @PreAuthorize("@ss.hasPermission('system:api-access-log:query')")
    public CommonResult<PageResult<SysApiAccessLogRespVO>> getApiAccessLogPage(@Valid SysApiAccessLogPageReqVO pageVO) {
        PageResult<SysApiAccessLogDO> pageResult = apiAccessLogService.getApiAccessLogPage(pageVO);
        return success(SysApiAccessLogConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出API 访问日志 Excel")
    @PreAuthorize("@ss.hasPermission('system:api-access-log:export')")
    @OperateLog(type = EXPORT)
    public void exportApiAccessLogExcel(@Valid SysApiAccessLogExportReqVO exportReqVO,
                                        HttpServletResponse response) throws IOException {
        List<SysApiAccessLogDO> list = apiAccessLogService.getApiAccessLogList(exportReqVO);
        // 导出 Excel
        List<SysApiAccessLogExcelVO> datas = SysApiAccessLogConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "API 访问日志.xls", "数据", SysApiAccessLogExcelVO.class, datas);
    }

}
