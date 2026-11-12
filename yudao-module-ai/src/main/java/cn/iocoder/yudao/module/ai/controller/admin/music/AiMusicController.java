package cn.iocoder.yudao.module.ai.controller.admin.music;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.music.AiMusicService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI 音乐")
@RestController
@RequestMapping("/ai/music")
public class AiMusicController {

    @Resource
    private AiMusicService musicService;
    @Resource
    private AiApiKeyService apiKeyService;

    @Resource
    private AdminUserApi adminUserApi;

    // ================ 创作 ================

    @PostMapping("/generate")
    @Operation(summary = "生成音乐", description = "灵感模式、自定义模式")
    public CommonResult<Long> generateMusic(@Valid @RequestBody AiMusicGenerateReqVO reqVO) {
        return success(musicService.generateMusic(getLoginUserId(), reqVO));
    }

    @PostMapping("/extend")
    @Operation(summary = "续写音乐")
    public CommonResult<Long> extendMusic(@Valid @RequestBody AiMusicExtendReqVO reqVO) {
        return success(musicService.extendMusic(getLoginUserId(), reqVO));
    }

    @PostMapping("/concat")
    @Operation(summary = "拼接音乐", description = "把续写作品与原曲拼接为完整歌曲")
    public CommonResult<Long> concatMusic(@Valid @RequestBody AiMusicConcatReqVO reqVO) {
        return success(musicService.concatMusic(getLoginUserId(), reqVO));
    }

    @PostMapping("/upload")
    @Operation(summary = "上传音乐", description = "音频先通过文件上传接口存到文件服务，再把文件地址提交中转；生成后可续写")
    public CommonResult<Long> uploadMusic(@Valid @RequestBody AiMusicUploadReqVO reqVO) {
        return success(musicService.uploadMusic(getLoginUserId(), reqVO));
    }

    @PostMapping("/generate-lyric")
    @Operation(summary = "生成歌词", description = "使用默认对话模型")
    public CommonResult<AiMusicLyricRespVO> generateLyric(@Valid @RequestBody AiMusicLyricReqVO reqVO) {
        return success(musicService.generateLyric(getLoginUserId(), reqVO));
    }

    // ================ 我的作品、音乐广场 ================

    @GetMapping("/my-page")
    @Operation(summary = "获得【我的】音乐分页")
    public CommonResult<PageResult<AiMusicRespVO>> getMusicMyPage(@Valid PageParam pageReqVO) {
        PageResult<AiMusicDO> pageResult = musicService.getMusicMyPage(getLoginUserId(), pageReqVO);
        return success(new PageResult<>(buildMusicList(pageResult.getList(), false), pageResult.getTotal()));
    }

    @GetMapping("/public-page")
    @Operation(summary = "获得音乐广场分页", description = "同租户已公开、已完成的音乐")
    public CommonResult<PageResult<AiMusicRespVO>> getMusicPublicPage(@Valid AiMusicPublicPageReqVO pageReqVO) {
        PageResult<AiMusicDO> pageResult = musicService.getMusicPublicPage(pageReqVO);
        return success(new PageResult<>(buildMusicList(pageResult.getList(), false), pageResult.getTotal()));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得音乐详情", description = "本人的音乐，或已公开、已完成的音乐")
    @Parameter(name = "id", required = true, description = "音乐编号", example = "1024")
    public CommonResult<AiMusicRespVO> getMusicDetail(@RequestParam("id") Long id) {
        AiMusicDO music = musicService.getMusicDetail(id, getLoginUserId());
        return success(CollUtil.getFirst(buildMusicList(Collections.singletonList(music), false)));
    }

    @PutMapping("/update-my")
    @Operation(summary = "修改【我的】音乐", description = "名称、封面、是否公开")
    public CommonResult<Boolean> updateMyMusic(@Valid @RequestBody AiMusicUpdateMyReqVO updateReqVO) {
        musicService.updateMyMusic(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete-my")
    @Operation(summary = "删除【我的】音乐")
    @Parameter(name = "id", required = true, description = "音乐编号", example = "1024")
    public CommonResult<Boolean> deleteMusicMy(@RequestParam("id") Long id) {
        musicService.deleteMusicMy(id, getLoginUserId());
        return success(true);
    }

    @PostMapping("/play")
    @Operation(summary = "记录播放", description = "播放次数 +1")
    @Parameter(name = "id", required = true, description = "音乐编号", example = "1024")
    public CommonResult<Boolean> playMusic(@RequestParam("id") Long id) {
        musicService.playMusic(id, getLoginUserId());
        return success(true);
    }

    // ================ 音乐管理 ================

    @GetMapping("/page")
    @Operation(summary = "获得音乐分页")
    @PreAuthorize("@ss.hasPermission('ai:music:query')")
    public CommonResult<PageResult<AiMusicRespVO>> getMusicPage(@Valid AiMusicPageReqVO pageReqVO) {
        PageResult<AiMusicDO> pageResult = musicService.getMusicPage(pageReqVO);
        return success(new PageResult<>(buildMusicList(pageResult.getList(), true), pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得音乐")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:music:query')")
    public CommonResult<AiMusicRespVO> getMusic(@RequestParam("id") Long id) {
        AiMusicDO music = musicService.getMusic(id);
        if (music == null) {
            return success(null);
        }
        AiMusicRespVO respVO = CollUtil.getFirst(buildMusicList(Collections.singletonList(music), true));
        if (music.getApiKeyId() != null) {
            AiApiKeyDO apiKey = apiKeyService.getApiKey(music.getApiKeyId());
            respVO.setApiKeyName(apiKey != null ? apiKey.getName() : null);
        }
        return success(respVO);
    }

    @PutMapping("/update")
    @Operation(summary = "更新音乐", description = "目前只支持修改是否公开")
    @PreAuthorize("@ss.hasPermission('ai:music:update')")
    public CommonResult<Boolean> updateMusic(@Valid @RequestBody AiMusicUpdateReqVO updateReqVO) {
        musicService.updateMusic(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除音乐")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:music:delete')")
    public CommonResult<Boolean> deleteMusic(@RequestParam("id") Long id) {
        musicService.deleteMusic(id);
        return success(true);
    }

    /**
     * 构建音乐列表
     *
     * @param list 音乐列表
     * @param admin 是否管理端：用户、广场接口不返回 API 密钥、中转任务与歌曲编号等排查字段
     */
    private List<AiMusicRespVO> buildMusicList(List<AiMusicDO> list, boolean admin) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(list, AiMusicDO::getUserId));
        return BeanUtils.toBean(list, AiMusicRespVO.class, music -> {
            MapUtils.findAndThen(userMap, music.getUserId(), user -> music.setUserNickname(user.getNickname()));
            if (!admin) {
                music.setApiKeyId(null).setTaskId(null).setClipId(null);
            }
        });
    }

}
