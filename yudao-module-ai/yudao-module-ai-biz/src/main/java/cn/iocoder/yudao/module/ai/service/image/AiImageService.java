package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.*;

import java.util.List;

/**
 * ai 作图
 *
 * @author fansili
 * @time 2024/4/25 15:50
 * @since 1.0
 */
public interface AiImageService {

    /**
     * ai绘画 - 列表
     *
     * @param req
     * @return
     */
    PageResult<AiImageListRespVO> list(AiImageListReqVO req);

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

    /**
     * midjourney 操作(u1、u2、放大、换一批...)
     *
     * @param req
     */
    void midjourneyOperate(AiImageMidjourneyOperateReqVO req);

    /**
     * 删除 - image 记录
     *
     * @param id
     */
    void delete(Long id);

}
