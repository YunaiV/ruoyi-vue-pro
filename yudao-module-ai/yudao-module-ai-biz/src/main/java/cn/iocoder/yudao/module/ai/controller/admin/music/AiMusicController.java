package cn.iocoder.yudao.module.ai.controller.admin.music;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiMusicPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiMusicRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiMusicUpdatePublicStatusReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiSunoGenerateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.service.music.AiMusicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI 音乐")
@RestController
@RequestMapping("/ai/music")
public class AiMusicController {

    @Resource
    private AiMusicService musicService;

    // TODO @xin：一个接口，获得【我的】音乐分页，参考 获得【我的】聊天角色分页 来写；用于我自己生成的列表，和音乐广场

    // TODO @xin：一个接口，删除【我的】音乐

    // TODO @xin：一个接口，获得【我的】音乐

    // TODO @xin：一个接口，修改【我的】音乐，目前只支持修改标题

    @PostMapping("/generate")
    @Operation(summary = "音乐生成")
    public CommonResult<List<Long>> generateMusic(@RequestBody @Valid AiSunoGenerateReqVO reqVO) {
        return success(musicService.generateMusic(getLoginUserId(), reqVO));
    }

    // ================ 绘图管理 ================

    @GetMapping("/page")
    @Operation(summary = "获得音乐分页")
    @PreAuthorize("@ss.hasPermission('ai:music:query')")
    public CommonResult<PageResult<AiMusicRespVO>> getMusicPage(@Valid AiMusicPageReqVO pageReqVO) {
        PageResult<AiMusicDO> pageResult = musicService.getMusicPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiMusicRespVO.class));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除音乐")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:music:delete')")
    public CommonResult<Boolean> deleteMusic(@RequestParam("id") Long id) {
        musicService.deleteMusic(id);
        return success(true);
    }

    @PutMapping("/update-public-status")
    @Operation(summary = "更新音乐发布状态")
    @PreAuthorize("@ss.hasPermission('ai:music:update')")
    public CommonResult<Boolean> updateMusicPublicStatus(@Valid @RequestBody AiMusicUpdatePublicStatusReqVO updateReqVO) {
        musicService.updateMusicPublicStatus(updateReqVO);
        return success(true);
    }

}
