package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.client.vo.MidjourneyNotifyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageListReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageMidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageMidjourneyOperateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;

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
     * @param loginUserId
     * @param req
     * @return
     */
    PageResult<AiImageDO> getImagePageMy(Long loginUserId, AiImageListReqVO req);

    /**
     * 获取 - image 信息
     *
     * @param id
     * @return
     */
    AiImageDO getMy(Long id);

    /**
     * ai绘画 - dall2/dall3 绘画
     *
     * @param loginUserId
     * @param req
     */
    Long dall(Long loginUserId, AiImageDallReqVO req);

    /**
     * midjourney 图片生成
     *
     * @param loginUserId
     * @param req
     * @return
     */
    Long midjourneyImagine(Long loginUserId, AiImageMidjourneyImagineReqVO req);

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
     * @param loginUserId
     */
    Boolean deleteIdMy(Long id, Long loginUserId);

    /**
     * midjourney proxy - 回调通知
     *
     * @param loginUserId
     * @param notifyReqVO
     * @return
     */
    Boolean midjourneyNotify(Long loginUserId, MidjourneyNotifyReqVO notifyReqVO);
}
