package cn.iocoder.yudao.module.system.controller.admin.mail;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.SystemMailAccountBaseVO;
import cn.iocoder.yudao.module.system.convert.mail.SystemMailAccountConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.SystemMailAccountDO;
import cn.iocoder.yudao.module.system.service.mail.SystemMailAccountService;
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

// TODO @ジョイイ：使用 Swagger 注解，不用写这个注释啦
/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Api(tags = "管理后台 - 邮件模板")
@RestController
@RequestMapping("/system-mail-account") // TODO @ジョイイ：/system/mail-account
public class SystemMailAccountController {
    @Resource
    private SystemMailAccountService systemMailAccountService;

    // TODO @ジョイイ：最好，VO 分拆下，参考下别的模块

    @PostMapping("/create")
    @ApiOperation("创建邮箱账号")
    @PreAuthorize("@ss.hasPermission('system:system-mail-account:create')")
    public CommonResult<Long> createMailAccount(@Valid @RequestBody SystemMailAccountBaseVO baseVO) {
        return success(systemMailAccountService.create(baseVO));
    }

    @PutMapping("/update")
    @ApiOperation("修改邮箱账号")
    @PreAuthorize("@ss.hasPermission('system:system-mail-account:update')")
    public CommonResult<Boolean> updateMailAccount(@Valid @RequestBody SystemMailAccountBaseVO baseVO) {
        systemMailAccountService.update(baseVO);
        return success(true);
    }

    // TODO @ジョイイ：删除，编号即可

    @DeleteMapping("/delete")
    @ApiOperation("删除邮箱账号")
    @PreAuthorize("@ss.hasPermission('system:system-mail-account:delete')")
    public CommonResult<Boolean> deleteMailAccount(@Valid @RequestBody SystemMailAccountBaseVO baseVO) {
        systemMailAccountService.delete(baseVO);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得邮箱账号")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:system-mail-account:get')")
    public CommonResult<SystemMailAccountBaseVO> getMailAccount(@RequestParam("id") Long id) {
        SystemMailAccountDO systemMailAccountDO = systemMailAccountService.getMailAccount(id);
        return success(SystemMailAccountConvert.INSTANCE.convert(systemMailAccountDO));
    }

    // TODO @ジョイイ：分页的查询条件

    @GetMapping("/page")
    @ApiOperation("获得邮箱账号分页")
    @PreAuthorize("@ss.hasPermission('system:system-mail-account:query')")
    public CommonResult<PageResult<SystemMailAccountBaseVO>> getSmsChannelPage(@Valid PageParam pageParam) {
        PageResult<SystemMailAccountDO> pageResult = systemMailAccountService.getMailAccountPage(pageParam);
        return success(SystemMailAccountConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list-all-simple")
    @ApiOperation(value = "获得邮箱账号精简列表")
    public CommonResult<List<SystemMailAccountBaseVO>> getSimpleSmsChannels() {
        List<SystemMailAccountDO> list = systemMailAccountService.getMailAccountList();
        // 排序后，返回给前端
        list.sort(Comparator.comparing(SystemMailAccountDO::getId));
        return success(SystemMailAccountConvert.INSTANCE.convertList02(list));
    }
}
