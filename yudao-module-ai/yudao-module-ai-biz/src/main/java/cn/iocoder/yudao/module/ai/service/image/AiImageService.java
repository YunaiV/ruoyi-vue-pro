package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.MidjourneyNotifyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDrawReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageMidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;

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
    PageResult<AiImageDO> getImagePageMy(Long userId, PageParam pageReqVO);

    /**
     * 获得绘图记录
     *
     * @param id 绘图编号
     * @return 绘图记录
     */
    AiImageDO getImage(Long id);

    /**
     * 绘制图片
     *
     * @param userId 用户编号
     * @param drawReqVO 绘制请求
     * @return 绘画编号
     */
    Long drawImage(Long userId, AiImageDrawReqVO drawReqVO);

    /**
     * Midjourney imagine（绘画）
     *
     * @param userId 用户编号
     * @param imagineReqVO 绘制请求
     * @return 绘画编号
     */
    Long midjourneyImagine(Long userId, AiImageMidjourneyImagineReqVO imagineReqVO);

    /**
     * 删除【我的】绘画记录
     *
     * @param id 绘画编号
     * @param userId 用户编号
     */
    void deleteImageMy(Long id, Long userId);

    /**
     * midjourney proxy - 回调通知
     *
     * @param notifyReqVO
     * @return
     */
    void midjourneyNotify(MidjourneyNotifyReqVO notifyReqVO);

    /**
     * 构建 midjourney - 更新对象
     *
     * @param imageId
     * @param notifyReqVO
     * @return
     */
    AiImageDO buildUpdateImage(Long imageId, MidjourneyNotifyReqVO notifyReqVO);

    /**
     * midjourney - action(放大、缩小、U1、U2...)
     *
     * @param loginUserId
     * @param imageId
     * @param customId
     * @return
     */
    void midjourneyAction(Long loginUserId, Long imageId, String customId);
}
