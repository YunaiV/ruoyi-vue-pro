package cn.iocoder.yudao.module.ai.controller.admin.music;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.SunoReqVO;
import cn.iocoder.yudao.module.ai.service.music.AiMusicService;
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

@Tag(name = "管理后台 - AI 音乐")
@RestController
@RequestMapping("/ai/music")
@RequiredArgsConstructor // TODO @xin：通过 @Resource 注入哈
public class AiMusicController {

    // TODO @xin：变量不用有 ai 前缀
    private final AiMusicService aiMusicService;

    @PostMapping("/generate")
    @Operation(summary = "音乐生成")
    public CommonResult<List<Long>> generateMusic(@RequestBody @Valid SunoReqVO sunoReqVO) {
        return success(aiMusicService.generateMusic(sunoReqVO));
    }

}
