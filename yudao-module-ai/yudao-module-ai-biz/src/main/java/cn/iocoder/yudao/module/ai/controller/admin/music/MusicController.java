package cn.iocoder.yudao.module.ai.controller.admin.music;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.SunoLyricModeVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.SunoReqVO;
import cn.iocoder.yudao.module.ai.service.music.MusicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AI 音乐生成")
@RestController
@RequestMapping("/ai/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    @PostMapping("generate/description-mode")
    @Operation(summary = "音乐生成-描述模式")
    public CommonResult<List<Long>> descriptionMode(@RequestBody @Valid SunoReqVO sunoReqVO) {
        return success(musicService.descriptionMode(sunoReqVO));
    }

    @PostMapping("generate/lyric-mode")
    @Operation(summary = "音乐生成-歌词模式")
    public CommonResult<List<Long>> lyricMode(@RequestBody @Valid SunoLyricModeVO sunoLyricModeVO) {
        return success(musicService.lyricMode(sunoLyricModeVO));
    }

}
