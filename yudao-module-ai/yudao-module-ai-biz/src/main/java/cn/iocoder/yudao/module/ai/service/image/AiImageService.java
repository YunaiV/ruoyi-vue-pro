package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.*;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyActionReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * AI 绘图 Service 接口
 *
 * @author fansili
 */
public interface AiImageService {

    /**
     * 获取【我的】绘图分页
     *
     * @param userId 用户编号
     * @param pageReqVO 分页条件
     * @return 绘图分页
     */
    PageResult<AiImageDO> getImagePageMy(Long userId, AiImagePageReqVO pageReqVO);

    /**
     * 获取公开的绘图分页
     *
     * @param pageReqVO 分页条件
     * @return 绘图分页
     */
    PageResult<AiImageDO> getImagePagePublic(AiImagePublicPageReqVO pageReqVO);

    /**
     * 获得绘图记录
     *
     * @param id 绘图编号
     * @return 绘图记录
     */
    AiImageDO getImage(Long id);

    /**
     * 获得绘图列表
     *
     * @param ids 绘图编号数组
     * @return 绘图记录列表
     */
    List<AiImageDO> getImageList(List<Long> ids);

    /**
     * 绘制图片
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
     * 获得绘画分页
     *
     * @param pageReqVO 分页查询
     * @return 绘画分页
     */
    PageResult<AiImageDO> getImagePage(AiImagePageReqVO pageReqVO);

    /**
     * 更新绘画
     *
     * @param updateReqVO 更新信息
     */
    void updateImage(@Valid AiImageUpdateReqVO updateReqVO);

    /**
     * 删除绘画
     *
     * @param id 编号
     */
    void deleteImage(Long id);

    // ================ midjourney 专属 ================

    /**
     * 【Midjourney】生成图片
     *
     * @param userId 用户编号
     * @param reqVO 绘制请求
     * @return 绘画编号
     */
    Long midjourneyImagine(Long userId, AiMidjourneyImagineReqVO reqVO);

    /**
     * 【Midjourney】同步图片进展
     *
     * @return 同步成功数量
     */
    Integer midjourneySync();

    /**
     * 【Midjourney】通知图片进展
     *
     * @param notify 通知
     */
    void midjourneyNotify(MidjourneyApi.Notify notify);

    /**
     * 【Midjourney】Action 操作(放大、缩小、U1、U2...)
     *
     * @param userId 用户编号
     * @param reqVO 绘制请求
     * @return 绘画编号
     */
    Long midjourneyAction(Long userId, AiMidjourneyActionReqVO reqVO);

}
