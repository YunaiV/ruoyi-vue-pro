package cn.iocoder.yudao.module.pms.controller.admin.kb.library;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.member.PmsKnowledgeLibraryMemberRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.member.PmsKnowledgeLibraryUpdateMemberListReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryMemberDO;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 知识库成员")
@RestController
@RequestMapping("/pms/kb/library-member")
@Validated
public class PmsKnowledgeLibraryMemberController {

    @Resource
    private PmsKnowledgeLibraryMemberService memberService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @GetMapping("/list")
    @Operation(summary = "获得知识库成员列表")
    @Parameter(name = "libraryId", description = "知识库编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<List<PmsKnowledgeLibraryMemberRespVO>> getLibraryMemberList(@RequestParam("libraryId") Long libraryId) {
        List<PmsKnowledgeLibraryMemberDO> members = memberService.getLibraryMemberList(libraryId, getLoginUserId());
        return success(buildLibraryMemberRespVOList(members));
    }

    @PutMapping("/update-list")
    @Operation(summary = "更新知识库成员")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:update')")
    public CommonResult<Boolean> updateLibraryMemberList(@Valid @RequestBody PmsKnowledgeLibraryUpdateMemberListReqVO updateReqVO) {
        memberService.updateLibraryMemberList(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/exit")
    @Operation(summary = "主动退出知识库")
    @Parameter(name = "libraryId", description = "知识库编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:kb:library:query')")
    public CommonResult<Boolean> exitLibrary(@RequestParam("libraryId") Long libraryId) {
        memberService.exitLibrary(libraryId, getLoginUserId());
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private List<PmsKnowledgeLibraryMemberRespVO> buildLibraryMemberRespVOList(
            List<PmsKnowledgeLibraryMemberDO> members) {
        if (CollUtil.isEmpty(members)) {
            return Collections.emptyList();
        }

        // 1. 批量查询用户和部门
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(members, PmsKnowledgeLibraryMemberDO::getUserId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(members, PmsKnowledgeLibraryMemberDO::getDeptId));
        Map<Long, DeptRespDTO> parentDeptMap = deptApi.getDeptMap(convertSet(deptMap.values(), DeptRespDTO::getParentId));
        // 2. 转换成员响应，并拼接用户和部门名称
        return convertList(members, member -> {
            PmsKnowledgeLibraryMemberRespVO memberVO = BeanUtils.toBean(member, PmsKnowledgeLibraryMemberRespVO.class);
            findAndThen(userMap, member.getUserId(), user ->
                    memberVO.setNickname(user.getNickname()).setAvatar(user.getAvatar()));
            findAndThen(deptMap, member.getDeptId(), dept -> {
                memberVO.setDeptName(dept.getName()).setParentDeptId(dept.getParentId());
                findAndThen(parentDeptMap, dept.getParentId(), parent -> memberVO.setParentDeptName(parent.getName()));
            });
            return memberVO;
        });
    }

}
