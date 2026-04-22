package cn.iocoder.yudao.module.im.controller.admin.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.im.controller.admin.user.vo.ImUserRespVO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IM 用户查询")
@RestController
@RequestMapping("/im/user")
@Validated
public class ImUserController {

    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/get-self")
    @Operation(summary = "获取当前登录用户信息")
    public CommonResult<ImUserRespVO> getSelfInfo() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        return success(BeanUtils.toBean(user, ImUserRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "按用户编号查询用户（用于点头像弹名片）")
    @Parameter(name = "id", description = "用户编号", required = true, example = "2048")
    public CommonResult<ImUserRespVO> getUser(@RequestParam("id") Long id) {
        AdminUserRespDTO user = adminUserApi.getUser(id);
        if (user == null) {
            return success(null);
        }
        return success(BeanUtils.toBean(user, ImUserRespVO.class));
    }

    @GetMapping("/list-by-name")
    @Operation(summary = "按昵称模糊搜索用户（用于加好友场景）")
    @Parameter(name = "name", description = "昵称关键词", required = true, example = "芋道")
    public CommonResult<List<ImUserRespVO>> getUserListByName(@RequestParam("name") String name) {
        if (name == null || name.trim().isEmpty()) {
            return success(Collections.emptyList());
        }
        List<AdminUserRespDTO> users = adminUserApi.getUserListByNickname(name.trim());
        // TODO @芋艿：【对齐】这里需要支持按「用户名 or 昵称」搜索；
        //  AdminUser 按用户名模糊的接口暂未暴露，先仅按昵称。后续可扩 AdminUserApi 加一个
        //  `getUserListByUsernameOrNickname(keyword)` 或让前端输入时选搜索模式。
        return success(CollectionUtils.convertList(users, u -> BeanUtils.toBean(u, ImUserRespVO.class)));
    }

}
