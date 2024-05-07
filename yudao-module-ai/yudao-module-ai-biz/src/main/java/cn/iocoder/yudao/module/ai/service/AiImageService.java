package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallDrawingReq;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageMidjourneyReq;

/**
 * ai 作图
 *
 * @author fansili
 * @time 2024/4/25 15:50
 * @since 1.0
 */
public interface AiImageService {

    /**
     * ai绘画 - dall2/dall3 绘画
     *
     * @param req
     */
    void dallDrawing(AiImageDallDrawingReq req);

    /**
     * midjourney 图片生成
     *
     * @param req
     * @return
     */
    void midjourney(AiImageMidjourneyReq req);
}
