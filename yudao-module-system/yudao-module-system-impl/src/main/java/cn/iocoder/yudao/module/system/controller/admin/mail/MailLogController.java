package cn.iocoder.yudao.module.system.controller.admin.mail;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.*;
import cn.iocoder.yudao.module.system.convert.mail.MailLogConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailLogDO;
import cn.iocoder.yudao.module.system.service.mail.MailLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;


@Api(tags = "管理后台 - 邮件日志")
@RestController
@RequestMapping("/system/mail-log")
public class MailLogController {
    @Autowired
    private MailLogService mailLogService;

    @GetMapping("/page")
    @ApiOperation("获得邮箱日志分页")
    @PreAuthorize("@ss.hasPermission('system:mail-log:query')")
    public CommonResult<PageResult<MailLogRespVO>> getMailLogPage(@Valid MailLogPageReqVO pageVO) {
        PageResult<MailLogDO> pageResult = mailLogService.getMailLogPage(pageVO);
        return success(MailLogConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出邮箱日志Excel")
    @PreAuthorize("@ss.hasPermission('system:mail-log:export')")
    public void exportMailLogExcel(@Valid MailLogExportReqVO exportReqVO ,
                                   HttpServletResponse response) throws IOException {
        List<MailLogDO> list = mailLogService.getMailLogList(exportReqVO);
        // 导出 Excel
        List<MailLogExcelVO> datas = MailLogConvert.INSTANCE.convertList(list);
        ExcelUtils.write(response, "邮箱日志.xls", "数据", MailLogExcelVO.class, datas);
    }
}
