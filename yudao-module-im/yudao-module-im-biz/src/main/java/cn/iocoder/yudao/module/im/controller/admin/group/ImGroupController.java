package cn.iocoder.yudao.module.im.controller.admin.group;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupCreateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupRespVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 群")
@RestController
@RequestMapping("/im/group")
@Validated
public class ImGroupController {

    @Resource
    private ImGroupService imGroupService;

    @PostMapping("/create")
    @Operation(summary = "创建群")
    @PreAuthorize("@ss.hasPermission('im:group:create')")
    public CommonResult<ImGroupRespVO> createGroup(@Valid @RequestBody ImGroupCreateReqVO createReqVO) {
        ImGroupDO group = imGroupService.createGroup(createReqVO, getLoginUserId());
        return success(BeanUtils.toBean(group, ImGroupRespVO.class));
    }

    @PutMapping("/update")
    @Operation(summary = "更新群")
    @PreAuthorize("@ss.hasPermission('im:group:update')")
    public CommonResult<ImGroupRespVO> updateGroup(@Valid @RequestBody ImGroupUpdateReqVO updateReqVO) {
        ImGroupDO group = imGroupService.updateGroup(updateReqVO, getLoginUserId());
        return success(BeanUtils.toBean(group, ImGroupRespVO.class));
    }

    @DeleteMapping("/dissolve")
    @Operation(summary = "解散群")
    @Parameter(name = "id", description = "群编号", required = true)
    @PreAuthorize("@ss.hasPermission('im:group:delete')")
    public CommonResult<Boolean> dissolveGroup(@RequestParam("id") Long id) {
        imGroupService.dissolveGroup(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得群")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:group:query')")
    public CommonResult<ImGroupRespVO> getGroup(@RequestParam("id") Long id) {
        ImGroupDO group = imGroupService.getGroup(id);
        return success(BeanUtils.toBean(group, ImGroupRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得当前登录用户的群列表")
    public CommonResult<List<ImGroupRespVO>> getMyGroupList() {
        List<ImGroupDO> groups = imGroupService.getActiveGroupList(getLoginUserId());
        return success(BeanUtils.toBean(groups, ImGroupRespVO.class));
    }

}
