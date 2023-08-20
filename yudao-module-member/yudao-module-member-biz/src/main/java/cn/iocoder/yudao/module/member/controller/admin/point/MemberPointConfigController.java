package cn.iocoder.yudao.module.member.controller.admin.point;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.config.MemberPointConfigRespVO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.config.MemberPointConfigSaveReqVO;
import cn.iocoder.yudao.module.member.convert.point.MemberPointConfigConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointConfigDO;
import cn.iocoder.yudao.module.member.service.point.MemberPointConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会员积分设置")
@RestController
@RequestMapping("/member/point/config")
@Validated
public class MemberPointConfigController {

    @Resource
    private MemberPointConfigService memberPointConfigService;

    @PutMapping("/save")
    @Operation(summary = "保存会员积分配置")
    @PreAuthorize("@ss.hasPermission('point:config:save')")
    public CommonResult<Boolean> updateConfig(@Valid @RequestBody MemberPointConfigSaveReqVO saveReqVO) {
        memberPointConfigService.saveConfig(saveReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员积分配置")
    @PreAuthorize("@ss.hasPermission('point:config:query')")
    public CommonResult<MemberPointConfigRespVO> getConfig() {
        MemberPointConfigDO config = memberPointConfigService.getConfig();
        return success(MemberPointConfigConvert.INSTANCE.convert(config));
    }

}
