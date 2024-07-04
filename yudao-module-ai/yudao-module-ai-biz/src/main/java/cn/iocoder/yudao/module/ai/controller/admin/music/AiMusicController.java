package cn.iocoder.yudao.module.ai.controller.admin.music;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.*;
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

    @GetMapping("/my-page")
    @Operation(summary = "获得【我的】音乐分页")
    public CommonResult<PageResult<AiMusicRespVO>> getMusicMyPage(@Valid AiMusicPageReqVO pageReqVO) {
        PageResult<AiMusicDO> pageResult = musicService.getMusicMyPage(pageReqVO, getLoginUserId());
        return success(BeanUtils.toBean(pageResult, AiMusicRespVO.class));
    }

    @PostMapping("/generate")
    @Operation(summary = "音乐生成")
    public CommonResult<List<Long>> generateMusic(@RequestBody @Valid AiSunoGenerateReqVO reqVO) {
        return success(musicService.generateMusic(getLoginUserId(), reqVO));
    }

    @Operation(summary = "删除【我的】音乐记录")
    @DeleteMapping("/delete-my")
    @Parameter(name = "id", required = true, description = "音乐编号", example = "1024")
    public CommonResult<Boolean> deleteMusicMy(@RequestParam("id") Long id) {
        musicService.deleteMusicMy(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get-my")
    @Operation(summary = "获取【我的】音乐")
    @Parameter(name = "id", required = true, description = "音乐编号", example = "1024")
    public CommonResult<AiMusicRespVO> getMusicMy(@RequestParam("id") Long id) {
        AiMusicDO music = musicService.getMusic(id);
        if (music == null || ObjUtil.notEqual(getLoginUserId(), music.getUserId())) {
            return success(null);
        }
        return success(BeanUtils.toBean(music, AiMusicRespVO.class));
    }

    @PostMapping("/update-my")
    @Operation(summary = "修改【我的】音乐 目前只支持修改标题")
    @Parameter(name = "title", required = true, description = "音乐名称", example = "夜空中最亮的星")
    public CommonResult<Boolean> updateMy(AiMusicUpdateMyReqVO updateReqVO) {
        musicService.updateMyMusic(updateReqVO, getLoginUserId());
        return success(true);
    }

    // ================ 音乐管理 ================

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

    @PutMapping("/update")
    @Operation(summary = "更新音乐")
    @PreAuthorize("@ss.hasPermission('ai:music:update')")
    public CommonResult<Boolean> updateMusic(@Valid @RequestBody AiMusicUpdateReqVO updateReqVO) {
        musicService.updateMusic(updateReqVO);
        return success(true);
    }

}
