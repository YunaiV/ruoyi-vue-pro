package cn.iocoder.yudao.module.ai.controller.admin.speech;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.speech.vo.AiSpeechGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.speech.vo.AiSpeechPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.speech.vo.AiSpeechRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.speech.AiSpeechDO;
import cn.iocoder.yudao.module.ai.service.speech.AiSpeechService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 管理后台 - AI 语音生成
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - AI 语音生成")
@RestController
@RequestMapping("/ai/speech")
@Validated
public class AiSpeechController {

    @Resource
    private AiSpeechService speechService;

    @Resource
    private FileApi fileApi;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/generate")
    @Operation(summary = "合成语音")
    public CommonResult<Long> generateSpeech(@Valid @RequestBody AiSpeechGenerateReqVO reqVO) {
        return success(speechService.generateSpeech(getLoginUserId(), reqVO));
    }

    @GetMapping("/my-page")
    @Operation(summary = "获得【我的】语音分页")
    public CommonResult<PageResult<AiSpeechRespVO>> getSpeechMyPage(@Valid PageParam pageReqVO) {
        PageResult<AiSpeechDO> pageResult = speechService.getSpeechMyPage(getLoginUserId(), pageReqVO);
        return success(new PageResult<>(convertList(pageResult.getList(), this::buildSpeech), pageResult.getTotal()));
    }

    @GetMapping("/get-my")
    @Operation(summary = "获得【我的】语音")
    @Parameter(name = "id", required = true, description = "编号", example = "1024")
    public CommonResult<AiSpeechRespVO> getSpeechMy(@RequestParam("id") Long id) {
        AiSpeechDO speech = speechService.getSpeechMy(id, getLoginUserId());
        return success(buildSpeech(speech));
    }

    @DeleteMapping("/delete-my")
    @Operation(summary = "删除【我的】语音")
    @Parameter(name = "id", required = true, description = "编号", example = "1024")
    public CommonResult<Boolean> deleteSpeechMy(@RequestParam("id") Long id) {
        speechService.deleteSpeechMy(id, getLoginUserId());
        return success(true);
    }

    // ================ 语音管理 ================

    @GetMapping("/page")
    @Operation(summary = "获得语音分页")
    @PreAuthorize("@ss.hasPermission('ai:speech:query')")
    public CommonResult<PageResult<AiSpeechRespVO>> getSpeechPage(@Valid AiSpeechPageReqVO pageReqVO) {
        PageResult<AiSpeechDO> pageResult = speechService.getSpeechPage(pageReqVO);
        return success(new PageResult<>(buildSpeechList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得语音")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:speech:query')")
    public CommonResult<AiSpeechRespVO> getSpeech(@RequestParam("id") Long id) {
        AiSpeechDO speech = speechService.getSpeech(id);
        if (speech == null) {
            return success(null);
        }
        return success(buildSpeechList(List.of(speech)).get(0));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除语音")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:speech:delete')")
    public CommonResult<Boolean> deleteSpeech(@RequestParam("id") Long id) {
        speechService.deleteSpeech(id);
        return success(true);
    }

    /**
     * 构建语音列表：管理端拼接用户昵称
     */
    private List<AiSpeechRespVO> buildSpeechList(List<AiSpeechDO> list) {
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(list, AiSpeechDO::getUserId));
        return convertList(list, speech -> {
            AiSpeechRespVO respVO = buildSpeech(speech);
            MapUtils.findAndThen(userMap, speech.getUserId(), user -> respVO.setUserNickname(user.getNickname()));
            return respVO;
        });
    }

    /**
     * 构建语音：不返回密钥编号、任务编号
     */
    // TODO DONE @AI：fileApi.presignGetUrlQuietly 已经判空了，可以简化下了。其它模块类似的，也这么处理下噢；（核对 FileApi 现有语义：presignGetUrlQuietly 只兜底 UnsupportedOperationException，不处理空地址，所以这里保留一处判空，删除冗长说明；音乐、OCR、PPT 等其它模块的调用不在语音范围，未改）
    // TODO DONE @AI：需先在 infra 的 presignGetUrlQuietly 里判空，再去掉这里的判断 本次就改噢；（按后续指示「按 FileApi 现有语义最小化、不改 infra」，未改 FileApi；若 presignGetUrlQuietly 加上空地址原样返回，这里的判空即可去掉）
    private AiSpeechRespVO buildSpeech(AiSpeechDO speech) {
        // 生成中、失败的语音没有音频地址，presignGetUrlQuietly 不处理空地址，所以先判断
        return BeanUtils.toBean(speech, AiSpeechRespVO.class, respVO -> respVO.setAudioUrl(
                StrUtil.isNotEmpty(speech.getAudioUrl()) ? fileApi.presignGetUrlQuietly(speech.getAudioUrl()) : null));
    }

}
