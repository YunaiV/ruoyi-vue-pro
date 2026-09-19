package cn.iocoder.yudao.module.oa.controller.admin.mail;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.provider.OaMailProviderRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.provider.OaMailProviderSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.OaMailProviderDO;
import cn.iocoder.yudao.module.oa.service.mail.OaMailProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 企业邮箱服务配置")
@RestController
@RequestMapping("/oa/mail-provider")
@Validated
public class OaMailProviderController {

    @Resource
    private OaMailProviderService mailProviderService;

    @GetMapping("/list")
    @Operation(summary = "获得邮箱服务配置列表")
    @Parameter(name = "status", description = "状态，未传时查询全部状态")
    @PreAuthorize("@ss.hasPermission('oa:mail-provider:query')")
    public CommonResult<List<OaMailProviderRespVO>> getMailProviderList(@RequestParam(value = "status", required = false) Integer status) {
        List<OaMailProviderDO> providers = mailProviderService.getMailProviderList(status);
        return success(BeanUtils.toBean(providers, OaMailProviderRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得邮箱服务配置精简列表")
    public CommonResult<List<OaMailProviderRespVO>> getSimpleMailProviderList() {
        List<OaMailProviderDO> providers = mailProviderService.getMailProviderList(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(providers, provider -> new OaMailProviderRespVO()
                .setId(provider.getId()).setName(provider.getName()).setStatus(provider.getStatus())));
    }

    @GetMapping("/get")
    @Operation(summary = "获得邮箱服务配置")
    @Parameter(name = "id", description = "服务配置编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:mail-provider:query')")
    public CommonResult<OaMailProviderRespVO> getMailProvider(@RequestParam("id") Long id) {
        OaMailProviderDO provider = mailProviderService.validateMailProviderExists(id);
        return success(BeanUtils.toBean(provider, OaMailProviderRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建邮箱服务配置")
    @PreAuthorize("@ss.hasPermission('oa:mail-provider:create')")
    public CommonResult<Long> createMailProvider(@Valid @RequestBody OaMailProviderSaveReqVO reqVO) {
        return success(mailProviderService.createMailProvider(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新邮箱服务配置")
    @PreAuthorize("@ss.hasPermission('oa:mail-provider:update')")
    public CommonResult<Boolean> updateMailProvider(@Valid @RequestBody OaMailProviderSaveReqVO reqVO) {
        mailProviderService.updateMailProvider(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除邮箱服务配置")
    @Parameter(name = "id", description = "服务配置编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:mail-provider:delete')")
    public CommonResult<Boolean> deleteMailProvider(@RequestParam("id") Long id) {
        mailProviderService.deleteMailProvider(id);
        return success(true);
    }

}
