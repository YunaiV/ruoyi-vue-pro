package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.client.vo.MidjourneyNotifyReqVO;
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
     */
    Long drawImage(Long userId, AiImageDrawReqVO drawReqVO);

    /**
     * midjourney 图片生成
     *
     * @param loginUserId
     * @param req
     * @return
     */
    Long midjourneyImagine(Long loginUserId, AiImageMidjourneyImagineReqVO req);

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
     * @param loginUserId
     * @param notifyReqVO
     * @return
     */
    Boolean midjourneyNotify(Long loginUserId, MidjourneyNotifyReqVO notifyReqVO);
}
