package cn.iocoder.yudao.module.system.controller.sms;

import cn.iocoder.yudao.module.system.controller.sms.vo.log.SysSmsLogExcelVO;
import cn.iocoder.yudao.module.system.controller.sms.vo.log.SysSmsLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.sms.vo.log.SysSmsLogPageReqVO;
import cn.iocoder.yudao.module.system.controller.sms.vo.log.SysSmsLogRespVO;
import cn.iocoder.yudao.module.system.convert.sms.SysSmsLogConvert;
import cn.iocoder.yudao.module.system.service.sms.SysSmsLogService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsLogDO;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "短信日志")
@RestController
@RequestMapping("/system/sms-log")
@Validated
public class SysSmsLogController {

    @Resource
    private SysSmsLogService smsLogService;

    @GetMapping("/page")
    @ApiOperation("获得短信日志分页")
    @PreAuthorize("@ss.hasPermission('system:sms-log:query')")
    public CommonResult<PageResult<SysSmsLogRespVO>> getSmsLogPage(@Valid SysSmsLogPageReqVO pageVO) {
        PageResult<SysSmsLogDO> pageResult = smsLogService.getSmsLogPage(pageVO);
        return success(SysSmsLogConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出短信日志 Excel")
    @PreAuthorize("@ss.hasPermission('system:sms-log:export')")
    @OperateLog(type = EXPORT)
    public void exportSmsLogExcel(@Valid SysSmsLogExportReqVO exportReqVO,
                                  HttpServletResponse response) throws IOException {
        List<SysSmsLogDO> list = smsLogService.getSmsLogList(exportReqVO);
        // 导出 Excel
        List<SysSmsLogExcelVO> datas = SysSmsLogConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "短信日志.xls", "数据", SysSmsLogExcelVO.class, datas);
    }

}
