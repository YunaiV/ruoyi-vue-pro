package cn.iocoder.dashboard.modules.system.controller.logger;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.excel.core.util.ExcelUtils;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogExcelVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogRespVO;
import cn.iocoder.dashboard.modules.system.convert.logger.SysLoginLogConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.logger.SysLoginLogDO;
import cn.iocoder.dashboard.modules.system.service.logger.SysLoginLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Api(tags = "登陆日志 API")
@RestController
@RequestMapping("/system/login-log")
public class SysLoginLogController {

    @Resource
    private SysLoginLogService loginLogService;

    @ApiOperation("获得登陆日志分页列表")
    @GetMapping("/page")
//    @PreAuthorize("@ss.hasPermi('system:login-log:query')")
    public CommonResult<PageResult<SysLoginLogRespVO>> getLoginLogPage(@Validated SysLoginLogPageReqVO reqVO) {
        PageResult<SysLoginLogDO> page = loginLogService.getLoginLogPage(reqVO);
        return CommonResult.success(SysLoginLogConvert.INSTANCE.convertPage(page));
    }

    @ApiOperation("导出登陆日志 Excel")
    @GetMapping("/export")
//    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
//    @PreAuthorize("@ss.hasPermi('monitor:logininfor:export')")
    public void exportLoginLog(HttpServletResponse response, @Validated SysLoginLogExportReqVO reqVO) throws IOException {
        List<SysLoginLogDO> list = loginLogService.getLoginLogList(reqVO);
        // 拼接数据
        List<SysLoginLogExcelVO> excelDataList = SysLoginLogConvert.INSTANCE.convertList(list);
        // 输出
        ExcelUtils.write(response, "登陆日志.xls", "数据列表",
                SysLoginLogExcelVO.class, excelDataList);
    }

}
