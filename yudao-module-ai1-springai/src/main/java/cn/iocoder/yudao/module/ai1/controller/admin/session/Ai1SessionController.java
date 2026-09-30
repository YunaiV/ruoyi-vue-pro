package cn.iocoder.yudao.module.ai1.controller.admin.session;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session.Ai1SessionCreateMyReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session.Ai1SessionPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session.Ai1SessionRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session.Ai1SessionUpdateMyReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionDO;
import cn.iocoder.yudao.module.ai1.service.session.Ai1SessionService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI1 会话")
@RestController
@RequestMapping("/ai1/session")
@Validated
public class Ai1SessionController {

    @Resource
    private Ai1SessionService sessionService;

    @Resource
    private AdminUserApi adminUserApi;

    // ==================== 我的 ====================

    @PostMapping("/create-my")
    @Operation(summary = "创建【我的】对话")
    @PreAuthorize("@ss.hasPermission('ai1:session:chat')")
    public CommonResult<Long> createSessionMy(@Valid @RequestBody Ai1SessionCreateMyReqVO createReqVO) {
        return success(sessionService.createSessionMy(getLoginUserId(), createReqVO));
    }

    @PutMapping("/update-my")
    @Operation(summary = "修改【我的】对话标题")
    @PreAuthorize("@ss.hasPermission('ai1:session:chat')")
    public CommonResult<Boolean> updateSessionMy(@Valid @RequestBody Ai1SessionUpdateMyReqVO updateReqVO) {
        sessionService.updateSessionMy(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete-my")
    @Operation(summary = "删除【我的】对话", description = "连带删除消息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:session:chat')")
    public CommonResult<Boolean> deleteSessionMy(@RequestParam("id") Long id) {
        sessionService.deleteSessionMy(getLoginUserId(), id);
        return success(true);
    }

    @GetMapping("/my-list")
    @Operation(summary = "获得【我的】对话列表", description = "指定 Agent 下，按创建时间倒序")
    @Parameter(name = "agentId", description = "Agent 编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('ai1:session:chat')")
    public CommonResult<List<Ai1SessionRespVO>> getSessionMyList(@RequestParam("agentId") Long agentId) {
        List<Ai1SessionDO> list = sessionService.getSessionListByAgentIdAndUserId(agentId, getLoginUserId());
        return success(BeanUtils.toBean(list, Ai1SessionRespVO.class));
    }

    // ==================== 管理 ====================

    @GetMapping("/page")
    @Operation(summary = "获得对话分页", description = "管理端查看 Agent 下全部用户的对话")
    @PreAuthorize("@ss.hasPermission('ai1:session:query')")
    public CommonResult<PageResult<Ai1SessionRespVO>> getSessionPage(@Valid Ai1SessionPageReqVO pageReqVO) {
        PageResult<Ai1SessionDO> pageResult = sessionService.getSessionPage(pageReqVO);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(pageResult.getList(), Ai1SessionDO::getUserId));
        return success(BeanUtils.toBean(pageResult, Ai1SessionRespVO.class, respVO ->
                MapUtils.findAndThen(userMap, respVO.getUserId(), user -> respVO.setUserNickname(user.getNickname()))));
    }

}
