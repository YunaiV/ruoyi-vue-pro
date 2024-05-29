package cn.iocoder.yudao.module.ai.controller.admin.music;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.SunoReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.SunoRespVO;
import cn.iocoder.yudao.module.ai.service.music.MusicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AI 音乐生成")
@RestController
@RequestMapping("/ai/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    @PostMapping("/suno-gen")
    @Operation(summary = "音乐生成")
    public CommonResult<SunoRespVO> musicGen(@RequestBody @Valid SunoReqVO sunoReqVO) {
        return success(musicService.musicGen(sunoReqVO));
    }
}
