package cn.iocoder.yudao.module.im.controller.admin.groupmember;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.groupmember.vo.ImGroupMemberPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.groupmember.vo.ImGroupMemberRespVO;
import cn.iocoder.yudao.module.im.controller.admin.groupmember.vo.ImGroupMemberSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

// TODO @芋艿：得看看 create、update、delete、get、page 这几个接口，要保留哪些
@Tag(name = "管理后台 - 群成员")
@RestController
@RequestMapping("/im/group-member")
@Validated
public class ImGroupMemberController {

    @Resource
    private ImGroupMemberService imGroupMemberService;

    @PostMapping("/create")
    @Operation(summary = "创建群成员")
    @PreAuthorize("@ss.hasPermission('im:group-member:create')")
    public CommonResult<Long> createGroupMember(@Valid @RequestBody ImGroupMemberSaveReqVO createReqVO) {
        return success(imGroupMemberService.createGroupMember(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新群成员")
    @PreAuthorize("@ss.hasPermission('im:group-member:update')")
    public CommonResult<Boolean> updateGroupMember(@Valid @RequestBody ImGroupMemberSaveReqVO updateReqVO) {
        imGroupMemberService.updateGroupMember(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除群成员")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('im:group-member:delete')")
    public CommonResult<Boolean> deleteGroupMember(@RequestParam("id") Long id) {
        imGroupMemberService.deleteGroupMember(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得群成员")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:group-member:query')")
    public CommonResult<ImGroupMemberRespVO> getGroupMember(@RequestParam("id") Long id) {
        ImGroupMemberDO groupMember = imGroupMemberService.getGroupMember(id);
        return success(BeanUtils.toBean(groupMember, ImGroupMemberRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得群成员分页")
    @PreAuthorize("@ss.hasPermission('im:group-member:query')")
    public CommonResult<PageResult<ImGroupMemberRespVO>> getGroupMemberPage(@Valid ImGroupMemberPageReqVO pageReqVO) {
        PageResult<ImGroupMemberDO> pageResult = imGroupMemberService.getGroupMemberPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ImGroupMemberRespVO.class));
    }

}