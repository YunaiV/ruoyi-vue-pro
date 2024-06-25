package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDrawReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyActionReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyImagineReqVO;
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
     * 删除【我的】绘画记录
     *
     * @param id 绘画编号
     * @param userId 用户编号
     */
    void deleteImageMy(Long id, Long userId);

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
