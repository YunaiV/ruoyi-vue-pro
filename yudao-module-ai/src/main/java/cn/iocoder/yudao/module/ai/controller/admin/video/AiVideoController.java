package cn.iocoder.yudao.module.ai.controller.admin.video;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.video.vo.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.video.AiVideoDO;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.video.AiVideoService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * @author 芋道源码
 */
@Tag(name = "管理后台 - AI 视频生成")
@RestController
@RequestMapping("/ai/video")
public class AiVideoController {

    @Resource
    private AiVideoService videoService;
    @Resource
    private AiApiKeyService apiKeyService;

    @Resource
    private AdminUserApi adminUserApi;

    // ================ 创作 ================

    @PostMapping("/generate")
    @Operation(summary = "生成视频", description = "视频生成、数字人口播、动作模仿")
    public CommonResult<Long> generateVideo(@Valid @RequestBody AiVideoGenerateReqVO reqVO) {
        return success(videoService.generateVideo(getLoginUserId(), reqVO));
    }

    // ================ 我的作品、公开作品 ================

    @GetMapping("/my-page")
    @Operation(summary = "获得【我的】视频分页")
    public CommonResult<PageResult<AiVideoRespVO>> getVideoMyPage(@Valid AiVideoMyPageReqVO pageReqVO) {
        PageResult<AiVideoDO> pageResult = videoService.getVideoMyPage(getLoginUserId(), pageReqVO);
        return success(new PageResult<>(buildVideoList(pageResult.getList(), false), pageResult.getTotal()));
    }

    @GetMapping("/public-page")
    @Operation(summary = "获得公开作品分页", description = "同租户已公开、已完成的视频")
    public CommonResult<PageResult<AiVideoRespVO>> getVideoPublicPage(@Valid AiVideoPublicPageReqVO pageReqVO) {
        PageResult<AiVideoDO> pageResult = videoService.getVideoPublicPage(pageReqVO);
        return success(new PageResult<>(buildVideoList(pageResult.getList(), false), pageResult.getTotal()));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得视频详情", description = "本人的视频，或已公开、已完成的视频")
    @Parameter(name = "id", required = true, description = "视频编号", example = "1024")
    public CommonResult<AiVideoRespVO> getVideoDetail(@RequestParam("id") Long id) {
        AiVideoDO video = videoService.getVideoDetail(id, getLoginUserId());
        return success(CollUtil.getFirst(buildVideoList(Collections.singletonList(video), false)));
    }

    @DeleteMapping("/delete-my")
    @Operation(summary = "删除【我的】视频")
    @Parameter(name = "id", required = true, description = "视频编号", example = "1024")
    public CommonResult<Boolean> deleteVideoMy(@RequestParam("id") Long id) {
        videoService.deleteVideoMy(id, getLoginUserId());
        return success(true);
    }

    // ================ 视频生成管理 ================

    @GetMapping("/page")
    @Operation(summary = "获得视频分页")
    @PreAuthorize("@ss.hasPermission('ai:video:query')")
    public CommonResult<PageResult<AiVideoRespVO>> getVideoPage(@Valid AiVideoPageReqVO pageReqVO) {
        PageResult<AiVideoDO> pageResult = videoService.getVideoPage(pageReqVO);
        return success(new PageResult<>(buildVideoList(pageResult.getList(), true), pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得视频")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:video:query')")
    public CommonResult<AiVideoRespVO> getVideo(@RequestParam("id") Long id) {
        AiVideoDO video = videoService.getVideo(id);
        if (video == null) {
            return success(null);
        }
        AiVideoRespVO respVO = CollUtil.getFirst(buildVideoList(Collections.singletonList(video), true));
        if (video.getApiKeyId() != null) {
            AiApiKeyDO apiKey = apiKeyService.getApiKey(video.getApiKeyId());
            respVO.setApiKeyName(apiKey != null ? apiKey.getName() : null);
        }
        return success(respVO);
    }

    @PutMapping("/update")
    @Operation(summary = "更新视频", description = "目前只支持修改是否公开")
    @PreAuthorize("@ss.hasPermission('ai:video:update')")
    public CommonResult<Boolean> updateVideo(@Valid @RequestBody AiVideoUpdateReqVO updateReqVO) {
        videoService.updateVideo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除视频")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:video:delete')")
    public CommonResult<Boolean> deleteVideo(@RequestParam("id") Long id) {
        videoService.deleteVideo(id);
        return success(true);
    }

    /**
     * 构建视频列表
     *
     * @param list 视频列表
     * @param admin 是否管理端：用户、公开接口中，他人的作品不返回输入文件
     */
    private List<AiVideoRespVO> buildVideoList(List<AiVideoDO> list, boolean admin) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Long loginUserId = getLoginUserId();
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(list, AiVideoDO::getUserId));
        return BeanUtils.toBean(list, AiVideoRespVO.class, video -> {
            MapUtils.findAndThen(userMap, video.getUserId(), user -> video.setUserNickname(user.getNickname()));
            if (!admin && ObjUtil.notEqual(video.getUserId(), loginUserId)) {
                video.setInputUrls(null);
            }
        });
    }

}
