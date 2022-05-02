package cn.iocoder.yudao.module.system.controller.admin.mail;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountBaseVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountUpdateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.send.MailReqVO;
import cn.iocoder.yudao.module.system.convert.mail.MailAccountConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.service.mail.MailAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;


@Api(tags = "管理后台 - 邮件账号")
@RestController
@RequestMapping("/system/mail-account")
public class MailAccountController {

    @Resource
    private MailAccountService mailAccountService;

    @PostMapping("/create")
    @ApiOperation("创建邮箱账号")
    @PreAuthorize("@ss.hasPermission('system:mail-account:create')")
    public CommonResult<Long> createMailAccount(@Valid @RequestBody MailAccountCreateReqVO createReqVO) {
        return success(mailAccountService.create(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("修改邮箱账号")
    @PreAuthorize("@ss.hasPermission('system:mail-account:update')")
    public CommonResult<Boolean> updateMailAccount(@Valid @RequestBody MailAccountUpdateReqVO updateReqVO) {
        mailAccountService.update(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除邮箱账号")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:mail-account:delete')")
    public CommonResult<Boolean> deleteMailAccount(@RequestParam Long id) {
        mailAccountService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得邮箱账号")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:mail-account:get')")
    public CommonResult<MailAccountBaseVO> getMailAccount(@RequestParam("id") Long id) {
        MailAccountDO mailAccountDO = mailAccountService.getMailAccount(id);
        return success(MailAccountConvert.INSTANCE.convert(mailAccountDO));
    }

    @GetMapping("/page")
    @ApiOperation("获得邮箱账号分页")
    @PreAuthorize("@ss.hasPermission('system:mail-account:query')")
    public CommonResult<PageResult<MailAccountBaseVO>> getMailAccountPage(@Valid MailAccountPageReqVO pageReqVO) {
        PageResult<MailAccountDO> pageResult = mailAccountService.getMailAccountPage(pageReqVO);
        return success(MailAccountConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list-all-simple")
    @ApiOperation(value = "获得邮箱账号精简列表")
    public CommonResult<List<MailAccountBaseVO>> getSimpleMailAccountList() {
        List<MailAccountDO> list = mailAccountService.getMailAccountList();
        // 排序后，返回给前端
        list.sort(Comparator.comparing(MailAccountDO::getId));
        return success(MailAccountConvert.INSTANCE.convertList02(list));
    }

}
