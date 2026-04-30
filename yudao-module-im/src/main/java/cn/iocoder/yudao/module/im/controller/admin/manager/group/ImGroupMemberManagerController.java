package cn.iocoder.yudao.module.im.controller.admin.manager.group;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.member.ImGroupMemberManagerRespVO;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IM 群成员管理")
@RestController
@RequestMapping("/im/manager/group/member")
@Validated
public class ImGroupMemberManagerController {

    @Resource
    private ImGroupMemberService groupMemberService;

    @GetMapping("/list")
    @Operation(summary = "获得群成员列表")
    @Parameter(name = "groupId", description = "群编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:group:query')")
    public CommonResult<List<ImGroupMemberManagerRespVO>> getGroupMemberList(@RequestParam("groupId") Long groupId) {
        return success(groupMemberService.getGroupMemberList(groupId));
    }

}
