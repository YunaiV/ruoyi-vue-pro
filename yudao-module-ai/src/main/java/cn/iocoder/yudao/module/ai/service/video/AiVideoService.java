package cn.iocoder.yudao.module.ai.service.video;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.video.vo.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.video.AiVideoDO;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskRef;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskResult;

import java.util.function.Supplier;

/**
 * AI 视频 Service 接口
 *
 * @author 芋道源码
 */
public interface AiVideoService {

    // ========== 创作 ==========

    /**
     * 生成视频：视频生成、数字人口播、动作模仿
     *
     * @param userId 用户编号
     * @param reqVO 生成信息
     * @return 视频编号
     */
    Long generateVideo(Long userId, AiVideoGenerateReqVO reqVO);

    /**
     * 校验视频模型：存在、启用、类型为视频
     *
     * @param modelId 模型编号
     * @return 模型
     */
    AiModelDO validateVideoModel(Long modelId);

    /**
     * 校验视频模型的密钥：存在、启用，且平台与模型一致
     *
     * @param model 模型
     * @return 密钥
     */
    AiApiKeyDO validateVideoApiKey(AiModelDO model);

    // ========== 我的作品、公开作品 ==========

    /**
     * 获得【我的】视频分页
     *
     * @param userId 用户编号
     * @param pageReqVO 分页查询
     * @return 视频分页
     */
    PageResult<AiVideoDO> getVideoMyPage(Long userId, AiVideoMyPageReqVO pageReqVO);

    /**
     * 获得公开作品分页：已公开、已完成的视频
     *
     * @param pageReqVO 分页查询
     * @return 视频分页
     */
    PageResult<AiVideoDO> getVideoPublicPage(AiVideoPublicPageReqVO pageReqVO);

    /**
     * 获得视频详情：本人的，或已公开、已完成的
     *
     * @param id 编号
     * @param userId 用户编号
     * @return 视频
     */
    AiVideoDO getVideoDetail(Long id, Long userId);

    /**
     * 删除【我的】视频
     *
     * @param id 编号
     * @param userId 用户编号
     */
    void deleteVideoMy(Long id, Long userId);

    // ========== 视频生成管理 ==========

    /**
     * 获得视频分页
     *
     * @param pageReqVO 分页查询
     * @return 视频分页
     */
    PageResult<AiVideoDO> getVideoPage(AiVideoPageReqVO pageReqVO);

    /**
     * 获得视频
     *
     * @param id 编号
     * @return 视频
     */
    AiVideoDO getVideo(Long id);

    /**
     * 修改视频：目前只支持修改是否公开
     *
     * @param updateReqVO 修改信息
     */
    void updateVideo(AiVideoUpdateReqVO updateReqVO);

    /**
     * 删除视频
     *
     * @param id 编号
     */
    void deleteVideo(Long id);

    // ========== 服务商：提交与查询 ==========

    /**
     * 构建提交服务商的动作：先在本地构建并校验请求（不发请求），不通过时抛出 VIDEO_PARAMS_INVALID 业务异常；
     * 返回的动作被调用时才真正提交，且只提交一次，不重试。AI 短剧的分镜视频也使用，传入未入库的视频即可
     *
     * @param video 视频：使用 type、mode、platform、model、prompt、resolution、ratio、duration、options、inputUrls
     * @param apiKey 提交时绑定的密钥
     * @return 提交动作
     */
    Supplier<AiVideoTaskRef> buildVideoSubmitter(AiVideoDO video, AiApiKeyDO apiKey);

    /**
     * 查询服务商的任务：只查询一次，不会重新提交。AI 短剧的分镜视频也使用
     *
     * @param video 视频：使用 type、platform、model、inputUrls、taskId
     * @param apiKey 提交时绑定的密钥
     * @return 任务结果
     */
    AiVideoTaskResult getVideoTask(AiVideoDO video, AiApiKeyDO apiKey);

    // ========== 同步 ==========

    /**
     * 同步视频：查询服务商、转存文件、处理超时
     *
     * @return 处理的数量
     */
    int syncVideo();

}
