package cn.iocoder.yudao.module.system.controller.admin.oauth2;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.client.OAuth2ClientPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.client.OAuth2ClientRespVO;
import cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.client.OAuth2ClientSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import cn.iocoder.yudao.module.system.service.oauth2.OAuth2ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - OAuth2 客户端")
@RestController
@RequestMapping("/system/oauth2-client")
@Validated
public class OAuth2ClientController {

    @Resource
    private OAuth2ClientService oAuth2ClientService;

    @PostMapping("/create")
    @Operation(summary = "创建 OAuth2 客户端")
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:create')")
    public CommonResult<Long> createOAuth2Client(@Valid @RequestBody OAuth2ClientSaveReqVO createReqVO) {
        return success(oAuth2ClientService.createOAuth2Client(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 OAuth2 客户端")
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:update')")
    public CommonResult<Boolean> updateOAuth2Client(@Valid @RequestBody OAuth2ClientSaveReqVO updateReqVO) {
        oAuth2ClientService.updateOAuth2Client(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 OAuth2 客户端")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:delete')")
    public CommonResult<Boolean> deleteOAuth2Client(@RequestParam("id") Long id) {
        oAuth2ClientService.deleteOAuth2Client(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 OAuth2 客户端")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:query')")
    public CommonResult<OAuth2ClientRespVO> getOAuth2Client(@RequestParam("id") Long id) {
        OAuth2ClientDO client = oAuth2ClientService.getOAuth2Client(id);
        return success(BeanUtils.toBean(client, OAuth2ClientRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 OAuth2 客户端分页")
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:query')")
    public CommonResult<PageResult<OAuth2ClientRespVO>> getOAuth2ClientPage(@Valid OAuth2ClientPageReqVO pageVO) {
        PageResult<OAuth2ClientDO> pageResult = oAuth2ClientService.getOAuth2ClientPage(pageVO);
        return success(BeanUtils.toBean(pageResult, OAuth2ClientRespVO.class));
    }

}
