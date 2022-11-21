package cn.iocoder.yudao.module.system.controller.admin.notify;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.*;
import cn.iocoder.yudao.module.system.convert.notify.NotifyTemplateConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyTemplateDO;
import cn.iocoder.yudao.module.system.service.notify.NotifyTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

// TODO 芋艿：VO 类上的 swagger 注解完善下，例如说 swagger，枚举等等
@Api(tags = "管理后台 - 站内信模版")
@RestController
@RequestMapping("/system/notify-template")
@Validated
public class NotifyTemplateController {

    @Resource
    private NotifyTemplateService notifyTemplateService;

    @PostMapping("/create")
    @ApiOperation("创建站内信模版")
    @PreAuthorize("@ss.hasPermission('system:notify-template:create')")
    public CommonResult<Long> createNotifyTemplate(@Valid @RequestBody NotifyTemplateCreateReqVO createReqVO) {
        return success(notifyTemplateService.createNotifyTemplate(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新站内信模版")
    @PreAuthorize("@ss.hasPermission('system:notify-template:update')")
    public CommonResult<Boolean> updateNotifyTemplate(@Valid @RequestBody NotifyTemplateUpdateReqVO updateReqVO) {
        notifyTemplateService.updateNotifyTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除站内信模版")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:notify-template:delete')")
    public CommonResult<Boolean> deleteNotifyTemplate(@RequestParam("id") Long id) {
        notifyTemplateService.deleteNotifyTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得站内信模版")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:notify-template:query')")
    public CommonResult<NotifyTemplateRespVO> getNotifyTemplate(@RequestParam("id") Long id) {
        NotifyTemplateDO notifyTemplate = notifyTemplateService.getNotifyTemplate(id);
        return success(NotifyTemplateConvert.INSTANCE.convert(notifyTemplate));
    }

    @GetMapping("/page")
    @ApiOperation("获得站内信模版分页")
    @PreAuthorize("@ss.hasPermission('system:notify-template:query')")
    public CommonResult<PageResult<NotifyTemplateRespVO>> getNotifyTemplatePage(@Valid NotifyTemplatePageReqVO pageVO) {
        PageResult<NotifyTemplateDO> pageResult = notifyTemplateService.getNotifyTemplatePage(pageVO);
        return success(NotifyTemplateConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出站内信模版 Excel")
    @PreAuthorize("@ss.hasPermission('system:notify-template:export')")
    @OperateLog(type = EXPORT)
    public void exportNotifyTemplateExcel(@Valid NotifyTemplateExportReqVO exportReqVO,
                                          HttpServletResponse response) throws IOException {
        List<NotifyTemplateDO> list = notifyTemplateService.getNotifyTemplateList(exportReqVO);
        // 导出 Excel
        List<NotifyTemplateExcelVO> datas = NotifyTemplateConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "站内信模版.xls", "数据", NotifyTemplateExcelVO.class, datas);
    }

    // TODO @芋艿：参考 SmsTemplateController 的 sendNotify 写一个发送站内信的接口

    // TODO @芋艿：参考 SmsSendServiceImpl，新建一个 NotifySendServiceImpl，用于提供出来给发送消息。注意，不要考虑异步发送，直接 insert 就可以了。也不用考虑发送后的回调

}
