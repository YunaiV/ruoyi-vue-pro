package cn.iocoder.yudao.module.oa.controller.admin.mail;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.account.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.OaMailAccountDO;
import cn.iocoder.yudao.module.oa.service.mail.OaMailAccountService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 企业邮箱账号")
@RestController
@RequestMapping("/oa/mail-account")
@Validated
public class OaMailAccountController {

    @Resource
    private OaMailAccountService mailAccountService;

    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/list")
    @Operation(summary = "获得本人邮箱账号列表")
    @Parameter(name = "status", description = "状态，未传时查询全部状态")
    @PreAuthorize("@ss.hasPermission('oa:mail-account:query')")
    public CommonResult<List<OaMailAccountRespVO>> getMailAccountList(@RequestParam(value = "status", required = false) Integer status) {
        List<OaMailAccountDO> accounts = mailAccountService.getMailAccountListByUserIdAndStatus(getLoginUserId(), status);
        return success(BeanUtils.toBean(accounts, OaMailAccountRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得当前租户邮箱地址精简列表")
    public CommonResult<List<OaMailAccountRespVO>> getSimpleMailAccountList() {
        // 1.1 查询邮箱及归属人，批量获取用户展示信息
        List<OaMailAccountDO> accounts = mailAccountService.getMailAccountListByStatus(CommonStatusEnum.ENABLE.getStatus());
        Map<Long, AdminUserRespDTO> users = adminUserApi.getUserMap(
                convertSet(accounts, account -> NumberUtils.parseLong(account.getCreator())));
        // 1.2 全员选择器只返回邮箱和姓名，不暴露登录名及连接配置
        List<OaMailAccountRespVO> result = convertList(accounts, account -> {
            OaMailAccountRespVO respVO = new OaMailAccountRespVO().setMail(account.getMail());
            MapUtils.findAndThen(users, NumberUtils.parseLong(account.getCreator()),
                    user -> respVO.setUserName(user.getNickname()));
            return respVO;
        });

        // 2. 补充已配置邮箱的启用用户
        Set<String> mails = convertSet(result, account -> account.getMail().toLowerCase(java.util.Locale.ROOT));
        List<AdminUserRespDTO> systemUsers = adminUserApi.getUserListByStatus(CommonStatusEnum.ENABLE.getStatus());
        for (AdminUserRespDTO user : systemUsers) {
            if (StrUtil.isNotBlank(user.getEmail()) && mails.add(user.getEmail().toLowerCase(java.util.Locale.ROOT))) {
                result.add(new OaMailAccountRespVO().setMail(user.getEmail()).setUserName(user.getNickname()));
            }
        }
        return success(result);
    }

    @GetMapping("/get")
    @Operation(summary = "获得本人邮箱账号")
    @Parameter(name = "id", description = "邮箱账号编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:mail-account:query')")
    public CommonResult<OaMailAccountRespVO> getMailAccount(@RequestParam("id") Long id) {
        OaMailAccountDO account = mailAccountService.validateMailAccount(id, getLoginUserId());
        return success(BeanUtils.toBean(account, OaMailAccountRespVO.class));
    }

    @PostMapping("/create")
    @ApiAccessLog(requestEnable = false)  // 特殊：password 密码，避免日志记录
    @Operation(summary = "绑定本人邮箱账号")
    @PreAuthorize("@ss.hasPermission('oa:mail-account:create')")
    public CommonResult<Long> createMailAccount(@Valid @RequestBody OaMailAccountSaveReqVO reqVO) {
        return success(mailAccountService.createMailAccount(reqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @ApiAccessLog(requestEnable = false) // 特殊：password 密码，避免日志记录
    @Operation(summary = "更新本人邮箱账号")
    @PreAuthorize("@ss.hasPermission('oa:mail-account:update')")
    public CommonResult<Boolean> updateMailAccount(@Valid @RequestBody OaMailAccountSaveReqVO reqVO) {
        mailAccountService.updateMailAccount(reqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/update-default")
    @Operation(summary = "设置本人默认发件账号")
    @Parameter(name = "id", description = "邮箱账号编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:mail-account:update')")
    public CommonResult<Boolean> updateMailAccountDefault(@RequestParam("id") Long id) {
        mailAccountService.updateMailAccountDefault(id, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "移除本人邮箱绑定")
    @Parameter(name = "id", description = "邮箱账号编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:mail-account:delete')")
    public CommonResult<Boolean> deleteMailAccount(@RequestParam("id") Long id) {
        mailAccountService.deleteMailAccount(id, getLoginUserId());
        return success(true);
    }

    @PostMapping("/test-connection")
    @Operation(summary = "测试本人邮箱连接")
    @Parameter(name = "id", description = "邮箱账号编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:mail-account:query')")
    public CommonResult<Map<String, Boolean>> testMailAccountConnection(@RequestParam("id") Long id) {
        return success(mailAccountService.testMailAccountConnection(id, getLoginUserId()));
    }

}
