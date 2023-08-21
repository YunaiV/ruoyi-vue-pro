package cn.iocoder.yudao.module.member.controller.admin.user;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.user.vo.MemberUserPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.user.vo.MemberUserRespVO;
import cn.iocoder.yudao.module.member.controller.admin.user.vo.MemberUserUpdateReqVO;
import cn.iocoder.yudao.module.member.convert.user.MemberUserConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelDO;
import cn.iocoder.yudao.module.member.dal.dataobject.tag.MemberTagDO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.service.level.MemberLevelService;
import cn.iocoder.yudao.module.member.service.tag.MemberTagService;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会员用户")
@RestController
@RequestMapping("/member/user")
@Validated
public class MemberUserController {

    @Resource
    private MemberUserService memberUserService;
    @Resource
    private MemberTagService memberTagService;
    @Resource
    private MemberLevelService memberLevelService;

    @PutMapping("/update")
    @Operation(summary = "更新会员用户")
    @PreAuthorize("@ss.hasPermission('member:user:update')")
    public CommonResult<Boolean> updateUser(@Valid @RequestBody MemberUserUpdateReqVO updateReqVO) {
        memberUserService.updateUser(updateReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:user:query')")
    public CommonResult<MemberUserRespVO> getUser(@RequestParam("id") Long id) {
        MemberUserDO user = memberUserService.getUser(id);
        return success(MemberUserConvert.INSTANCE.convert03(user));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员用户分页")
    @PreAuthorize("@ss.hasPermission('member:user:query')")
    public CommonResult<PageResult<MemberUserRespVO>> getUserPage(@Valid MemberUserPageReqVO pageVO) {
        PageResult<MemberUserDO> pageResult = memberUserService.getUserPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 处理会员标签返显
        Set<Long> tagIds = pageResult.getList().stream()
                .map(MemberUserDO::getTagIds)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        List<MemberTagDO> tags = memberTagService.getTagList(tagIds);
        // 处理会员级别返显
        List<MemberLevelDO> levels = memberLevelService.getEnableLevelList();
        // 拼接
        return success(MemberUserConvert.INSTANCE.convertPage(pageResult, tags, levels));
    }

}
