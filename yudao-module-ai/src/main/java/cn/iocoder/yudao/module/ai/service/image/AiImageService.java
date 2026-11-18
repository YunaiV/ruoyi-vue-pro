package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.*;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import jakarta.validation.Valid;

import java.util.List;

/**
 * AI 绘图 Service 接口
 *
 * @author fansili
 * @since 2024/4/22 17:48
 */
public interface AiImageService {

    /**
     * 获取【我的】绘图分页
     *
     * @param userId 用户编号
     * @param pageReqVO 分页条件
     * @return 绘图分页
     */
    PageResult<AiImageDO> getImagePageMy(Long userId, PageParam pageReqVO);

    /**
     * 获取公开的绘图分页：只有公开且已完成的绘图
     *
     * @param pageReqVO 分页条件
     * @return 绘图分页
     */
    PageResult<AiImageDO> getImagePagePublic(AiImagePublicPageReqVO pageReqVO);

    /**
     * 获得绘图记录
     *
     * @param id 绘画编号
     * @return 绘画记录
     */
    AiImageDO getImage(Long id);

    /**
     * 获得公开的绘图记录：不存在、未公开、未完成时，抛出“作品不存在或已取消公开”
     *
     * @param id 绘画编号
     * @return 绘画记录
     */
    AiImageDO getImagePublic(Long id);

    /**
     * 获得绘图列表
     *
     * @param ids 绘图编号数组
     * @return 绘图列表
     */
    List<AiImageDO> getImageList(List<Long> ids);

    /**
     * 获得派生的绘图列表：按钮动作、局部重绘生成的子任务
     *
     * @param parentId 来源绘图编号
     * @return 绘图列表
     */
    List<AiImageDO> getImageListByParentId(Long parentId);

    /**
     * 绘制图片：通用模型（OpenAI、通义、硅基、Stability）与 Seedream
     *
     * @param userId 用户编号
     * @param drawReqVO 绘制请求
     * @return 绘画编号
     */
    Long drawImage(Long userId, AiImageDrawReqVO drawReqVO);

    /**
     * 删除【我的】绘画记录
     *
     * @param id 绘画编号
     * @param userId 用户编号
     */
    void deleteImageMy(Long id, Long userId);

    /**
     * 更新【我的】绘画的公开状态：只有已完成的绘画才能公开
     *
     * @param userId 用户编号
     * @param updateReqVO 更新信息
     */
    void updateImagePublicMy(Long userId, AiImageUpdateReqVO updateReqVO);

    /**
     * 生成专业绘画指令：使用默认的对话模型，改写提示词
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @return 改写后的提示词
     */
    String optimizePrompt(Long userId, AiImageOptimizePromptReqVO reqVO);

    /**
     * 获得绘画分页
     *
     * @param pageReqVO 分页查询
     * @return 绘画分页
     */
    PageResult<AiImageDO> getImagePage(AiImagePageReqVO pageReqVO);

    /**
     * 更新绘画（管理员）
     *
     * @param updateReqVO 更新信息
     */
    void updateImage(@Valid AiImageUpdateReqVO updateReqVO);

    /**
     * 删除绘画（管理员）
     *
     * @param id 编号
     */
    void deleteImage(Long id);

    /**
     * 将超时（超过 10 分钟仍在进行中）的绘画，更新为失败
     *
     * @return 超时数量
     */
    int timeoutImage();

    // ================ midjourney 专属 ================

    /**
     * 【Midjourney】文生图、图生图、一致性
     *
     * 中转拒绝或异常时，记录更新为失败，并返回编号
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @return 绘画编号
     */
    Long midjourneyImagine(Long userId, AiMidjourneyImagineReqVO reqVO);

    /**
     * 【Midjourney】融图
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @return 绘画编号
     */
    Long midjourneyBlend(Long userId, AiMidjourneyBlendReqVO reqVO);

    /**
     * 【Midjourney】换脸
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @return 绘画编号
     */
    Long midjourneySwapFace(Long userId, AiMidjourneySwapFaceReqVO reqVO);

    /**
     * 【Midjourney】按钮动作：U1、V2、放大、平移等，生成子任务
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @return 子任务的绘画编号
     */
    Long midjourneyAction(Long userId, AiMidjourneyActionReqVO reqVO);

    /**
     * 【Midjourney】局部重绘，生成子任务
     *
     * @param userId 用户编号
     * @param reqVO 请求
     * @return 子任务的绘画编号
     */
    Long midjourneyModal(Long userId, AiMidjourneyModalReqVO reqVO);

    /**
     * 【Midjourney】同步进行中的任务
     *
     * @return 同步数量
     */
    Integer midjourneySync();

    /**
     * 【Midjourney】处理中转回调
     *
     * @param notify 通知
     */
    void midjourneyNotify(MidjourneyApi.Notify notify);

}
