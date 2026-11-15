package cn.iocoder.yudao.module.ai.controller.admin.drama;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaChatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptPolishReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptUpdateReqVO;
import cn.iocoder.yudao.module.ai.service.drama.AiDramaScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * @author 芋道源码
 */
@Tag(name = "管理后台 - AI 短剧剧本")
@RestController
@RequestMapping("/ai/drama/script")
public class AiDramaScriptController {

    @Resource
    private AiDramaScriptService scriptService;

    @PutMapping("/update")
    @Operation(summary = "保存【我的】剧本", description = "剧本不存在时创建")
    public CommonResult<Boolean> updateScript(@Valid @RequestBody AiDramaScriptUpdateReqVO updateReqVO) {
        scriptService.updateScriptMy(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @PostMapping("/generate")
    @Operation(summary = "根据故事想法生成剧本", description = "同步调用对话模型，覆盖已有的剧本内容")
    public CommonResult<AiDramaScriptRespVO> generateScript(@Valid @RequestBody AiDramaChatReqVO reqVO) {
        return success(BeanUtils.toBean(scriptService.generateScriptMy(getLoginUserId(), reqVO), AiDramaScriptRespVO.class));
    }

    @PostMapping("/polish")
    @Operation(summary = "按修改意见打磨剧本", description = "同步调用对话模型，记录修改意见")
    public CommonResult<AiDramaScriptRespVO> polishScript(@Valid @RequestBody AiDramaScriptPolishReqVO reqVO) {
        return success(BeanUtils.toBean(scriptService.polishScriptMy(getLoginUserId(), reqVO), AiDramaScriptRespVO.class));
    }

    // DONE @AI：s 改成 list 结尾？方法名，和 url 都是？？？（URL 改为 analyze-asset-list、generate-storyboard-list，Controller、Service 方法名同步；Vue3 API 已同步）
    @PostMapping("/analyze-asset-list")
    @Operation(summary = "从剧本分析角色、场景、道具", description = "只新增名称不重复的")
    public CommonResult<Boolean> analyzeAssetList(@Valid @RequestBody AiDramaChatReqVO reqVO) {
        scriptService.analyzeAssetListMy(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/generate-storyboard-list")
    @Operation(summary = "由剧本拆分分镜", description = "替换项目已有的全部分镜")
    public CommonResult<Boolean> generateStoryboardList(@Valid @RequestBody AiDramaChatReqVO reqVO) {
        scriptService.generateStoryboardListMy(getLoginUserId(), reqVO);
        return success(true);
    }

}
