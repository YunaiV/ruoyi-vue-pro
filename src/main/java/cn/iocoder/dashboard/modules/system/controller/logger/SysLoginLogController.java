package cn.iocoder.dashboard.modules.system.controller.logger;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
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

//    @GetMapping("/export")
////    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
////    @PreAuthorize("@ss.hasPermi('monitor:logininfor:export')")
//    public void exportLoginLog(SysLogininfor logininfor) {
//        List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
//        ExcelUtil<SysLogininfor> util = new ExcelUtil<SysLogininfor>(SysLogininfor.class);
//        return util.exportExcel(list, "登录日志");
//    }

}
