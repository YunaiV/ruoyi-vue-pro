package cn.iocoder.yudao.module.oa.controller.admin.mail;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.message.OaMailFolderRespVO;
import cn.iocoder.yudao.module.oa.service.mail.OaMailMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 企业邮箱文件夹")
@RestController
@RequestMapping("/oa/mail-folder")
@Validated
public class OaMailFolderController {

    @Resource
    private OaMailMessageService mailMessageService;

    @GetMapping("/list")
    @Operation(summary = "获得邮箱文件夹列表")
    @Parameter(name = "accountId", description = "邮箱账号编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:mail-account:query')")
    public CommonResult<List<OaMailFolderRespVO>> getMailFolderList(
            @RequestParam Long accountId) {
        return success(mailMessageService.getMailFolderList(accountId, getLoginUserId()));
    }

}
