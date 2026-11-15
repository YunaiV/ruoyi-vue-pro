package cn.iocoder.yudao.module.ai.controller.admin.drama;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardVideoReqVO;
import cn.iocoder.yudao.module.ai.service.drama.AiDramaStoryboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * @author 芋道源码
 */
@Tag(name = "管理后台 - AI 短剧分镜")
@RestController
@RequestMapping("/ai/drama/storyboard")
public class AiDramaStoryboardController {

    @Resource
    private AiDramaStoryboardService storyboardService;

    @PostMapping("/create")
    @Operation(summary = "创建分镜")
    public CommonResult<Long> createStoryboard(@Valid @RequestBody AiDramaStoryboardSaveReqVO createReqVO) {
        return success(storyboardService.createStoryboardMy(getLoginUserId(), createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改分镜", description = "镜头视频生成中时不允许")
    public CommonResult<Boolean> updateStoryboard(@Valid @RequestBody AiDramaStoryboardSaveReqVO updateReqVO) {
        storyboardService.updateStoryboardMy(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除分镜")
    @Parameter(name = "id", required = true, description = "分镜编号", example = "1024")
    public CommonResult<Boolean> deleteStoryboard(@RequestParam("id") Long id) {
        storyboardService.deleteStoryboardMy(getLoginUserId(), id);
        return success(true);
    }

    @PostMapping("/generate-video")
    @Operation(summary = "生成镜头视频", description = "id 为分镜编号；提交服务商后由 Job 查询结果")
    public CommonResult<Boolean> generateVideo(@Valid @RequestBody AiDramaStoryboardVideoReqVO reqVO) {
        storyboardService.generateStoryboardVideoMy(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/generate-video-batch")
    @Operation(summary = "批量生成镜头视频", description = "id 为项目编号；未生成、已失败的镜头逐个提交")
    public CommonResult<Integer> generateVideoBatch(@Valid @RequestBody AiDramaStoryboardVideoReqVO reqVO) {
        return success(storyboardService.generateStoryboardVideoBatchMy(getLoginUserId(), reqVO));
    }

}
