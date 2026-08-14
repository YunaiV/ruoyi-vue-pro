package cn.iocoder.yudao.module.fms.controller.admin.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountuser.FmsAccountUserRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountuser.FmsAccountUserUpdateReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountUserDO;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountUserService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - FMS 账套用户")
@RestController
@RequestMapping("/fms/config/account-user")
@Validated
public class FmsAccountUserController {

    @Resource
    private FmsAccountUserService accountUserService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @GetMapping("/list")
    @Operation(summary = "获得账套用户列表")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:config:account-set:authorize')")
    public CommonResult<List<FmsAccountUserRespVO>> getAccountUserList(
            @RequestParam("accountSetId") Long accountSetId) {
        List<FmsAccountUserDO> accountUsers = accountUserService.getAccountUserList(
                accountSetId, getLoginUserId());
        return success(buildAccountUserRespVOList(accountUsers));
    }

    @PutMapping("/update")
    @Operation(summary = "更新账套用户列表")
    @PreAuthorize("@ss.hasPermission('fms:config:account-set:authorize')")
    public CommonResult<Boolean> updateAccountUserList(
            @Valid @RequestBody FmsAccountUserUpdateReqVO updateReqVO) {
        accountUserService.updateAccountUserList(updateReqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/update-default-status")
    @Operation(summary = "更新默认账套状态")
    @PreAuthorize("@ss.hasPermission('fms:config:account-set:query')")
    public CommonResult<Boolean> updateAccountSetDefaultStatus(@RequestParam("accountSetId") Long accountSetId) {
        accountUserService.updateAccountSetDefaultStatus(accountSetId, getLoginUserId());
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private List<FmsAccountUserRespVO> buildAccountUserRespVOList(List<FmsAccountUserDO> accountUsers) {
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(accountUsers, FmsAccountUserDO::getUserId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        return convertList(accountUsers, accountUser -> {
            FmsAccountUserRespVO respVO = BeanUtils.toBean(accountUser, FmsAccountUserRespVO.class);
            findAndThen(userMap, accountUser.getUserId(), user -> {
                respVO.setNickname(user.getNickname()).setMobile(user.getMobile())
                        .setEmail(user.getEmail()).setStatus(user.getStatus());
                findAndThen(deptMap, user.getDeptId(), dept -> respVO.setDeptName(dept.getName()));
            });
            return respVO;
        });
    }

}
