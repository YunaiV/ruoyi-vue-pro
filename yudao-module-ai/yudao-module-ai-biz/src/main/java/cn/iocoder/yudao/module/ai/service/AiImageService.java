package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallDrawingReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallDrawingRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageMidjourneyReqVO;

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
    AiImageDallDrawingRespVO dallDrawing(AiImageDallDrawingReqVO req);

    /**
     * midjourney 图片生成
     *
     * @param req
     * @return
     */
    void midjourney(AiImageMidjourneyReqVO req);
}
