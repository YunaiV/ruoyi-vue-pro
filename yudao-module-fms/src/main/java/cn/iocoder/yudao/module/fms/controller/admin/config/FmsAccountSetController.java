package cn.iocoder.yudao.module.fms.controller.admin.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset.FmsAccountSetInitializeReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset.FmsAccountSetRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset.FmsAccountSetSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountUserDO;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - FMS 账套")
@RestController
@RequestMapping("/fms/config/account-set")
@Validated
public class FmsAccountSetController {

    @Resource
    private FmsAccountSetService accountSetService;
    @Resource
    private FmsAccountUserService accountUserService;

    @PostMapping("/create")
    @Operation(summary = "创建账套")
    @PreAuthorize("@ss.hasPermission('fms:config:account-set:create')")
    public CommonResult<Long> createAccountSet(@Valid @RequestBody FmsAccountSetSaveReqVO createReqVO) {
        return success(accountSetService.createAccountSet(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新账套")
    @PreAuthorize("@ss.hasPermission('fms:config:account-set:update')")
    public CommonResult<Boolean> updateAccountSet(@Valid @RequestBody FmsAccountSetSaveReqVO updateReqVO) {
        accountSetService.updateAccountSet(updateReqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/initialize")
    @Operation(summary = "初始化账套")
    @PreAuthorize("@ss.hasPermission('fms:config:account-set:initialize')")
    public CommonResult<Boolean> initializeAccountSet(
            @Valid @RequestBody FmsAccountSetInitializeReqVO initializeReqVO) {
        accountSetService.initializeAccountSet(initializeReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得账套")
    @Parameter(name = "id", description = "账套编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:config:account-set:query')")
    public CommonResult<FmsAccountSetRespVO> getAccountSet(@RequestParam("id") Long id) {
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetReadPermission(id, getLoginUserId());
        FmsAccountUserDO accountUser = accountUserService.getAccountUser(id, getLoginUserId());
        return success(buildAccountSetRespVO(accountSet, accountUser));
    }

    @GetMapping("/list")
    @Operation(summary = "获得当前用户账套列表")
    @PreAuthorize("@ss.hasPermission('fms:config:account-set:query')")
    public CommonResult<List<FmsAccountSetRespVO>> getAccountSetList() {
        List<FmsAccountUserDO> accountUsers = accountUserService.getAccountUserList(getLoginUserId());
        Map<Long, FmsAccountSetDO> accountSetMap = accountSetService.getAccountSetMap(
                convertSet(accountUsers, FmsAccountUserDO::getAccountSetId));
        return success(buildAccountSetRespVOList(accountSetMap, accountUsers));
    }

    // ==================== 拼接 VO ====================

    private FmsAccountSetRespVO buildAccountSetRespVO(
            FmsAccountSetDO accountSet, FmsAccountUserDO accountUser) {
        return BeanUtils.toBean(accountSet, FmsAccountSetRespVO.class)
                .setDefaultStatus(accountUser.getDefaultStatus()).setFounder(accountUser.getFounder())
                .setLevel(accountUser.getLevel());
    }

    private List<FmsAccountSetRespVO> buildAccountSetRespVOList(
            Map<Long, FmsAccountSetDO> accountSetMap, List<FmsAccountUserDO> accountUsers) {
        return convertList(accountUsers, accountUser -> buildAccountSetRespVO(
                accountSetMap.get(accountUser.getAccountSetId()), accountUser));
    }

}
