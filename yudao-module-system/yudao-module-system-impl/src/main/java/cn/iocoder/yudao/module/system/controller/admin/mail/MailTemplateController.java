package cn.iocoder.yudao.module.system.controller.admin.mail;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateBaseVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplatePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.mail.MailTemplateConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.service.mail.MailTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.Comparator;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 邮件模版")
@RestController
@RequestMapping("/system/mail-template")
public class MailTemplateController {
    @Autowired
    MailTemplateService mailTempleService; // TODO @wangjingyi：private；和上面要空一行；

    @PostMapping("/create")
    @ApiOperation("创建邮箱模版")
    @PreAuthorize("@ss.hasPermission('system:mail-template:create')")
    public CommonResult<Long> createMailTemplate(@Valid @RequestBody MailTemplateCreateReqVO createReqVO){
        return success(mailTempleService.create(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("修改邮箱模版")
    @PreAuthorize("@ss.hasPermission('system:mail-template:update')")
    public CommonResult<Boolean> updateMailTemplate(@Valid @RequestBody MailTemplateUpdateReqVO updateReqVO){
        mailTempleService.update(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除邮箱模版")
    @PreAuthorize("@ss.hasPermission('system:mail-template:delete')")
    public CommonResult<Boolean> deleteMailTemplate(@Valid @RequestBody Long id) {
        mailTempleService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得邮箱模版")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:mail-template:get')")
    public CommonResult<MailTemplateBaseVO> getMailTemplate(@RequestParam("id") Long id) {
        MailTemplateDO mailTemplateDO = mailTempleService.getMailTemplate(id);
        return success(MailTemplateConvert.INSTANCE.convert(mailTemplateDO));
    }

    @GetMapping("/page")
    @ApiOperation("获得邮箱模版分页")
    @PreAuthorize("@ss.hasPermission('system:mail-account:query')")
    public CommonResult<PageResult<MailTemplateBaseVO>> getMailTemplatePage(@Valid MailTemplatePageReqVO pageReqVO) {
        PageResult<MailTemplateDO> pageResult = mailTempleService.getMailTemplatePage(pageReqVO);
        return success(MailTemplateConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list-all-simple")
    @ApiOperation(value = "获得邮箱模版精简列表")
    public CommonResult<List<MailTemplateBaseVO>> getSimpleTemplateList() {
        List<MailTemplateDO> list = mailTempleService.getMailTemplateList();
        // 排序后，返回给前端
        list.sort(Comparator.comparing(MailTemplateDO::getId));
        return success(MailTemplateConvert.INSTANCE.convertList02(list));
    }
}
