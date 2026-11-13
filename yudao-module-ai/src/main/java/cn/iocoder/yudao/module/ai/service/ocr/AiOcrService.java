package cn.iocoder.yudao.module.ai.service.ocr;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.ocr.vo.AiOcrPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ocr.vo.AiOcrRecognizeReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ocr.vo.AiOcrUpdateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.ocr.AiOcrDO;

/**
 * AI 文字识别 Service 接口
 *
 * @author 芋道源码
 */
public interface AiOcrService {

    /**
     * 同步识别图片中的文字
     *
     * 图片先由前端上传到文件服务，这里按地址下载后识别；成功、失败都会新增一条记录；
     * 图片、模型校验不通过时，直接抛出业务异常，不记录
     *
     * @param userId 用户编号
     * @param reqVO 识别信息
     * @return 识别记录
     */
    AiOcrDO recognizeOcr(Long userId, AiOcrRecognizeReqVO reqVO);

    /**
     * 获得【我的】识别记录分页
     *
     * @param userId 用户编号
     * @param pageReqVO 分页查询
     * @return 识别记录分页
     */
    PageResult<AiOcrDO> getOcrMyPage(Long userId, PageParam pageReqVO);

    /**
     * 获得【我的】识别记录
     *
     * @param id 编号
     * @param userId 用户编号
     * @return 识别记录；不存在、或不属于该用户时抛出业务异常
     */
    AiOcrDO getOcrMy(Long id, Long userId);

    /**
     * 保存【我的】识别修订
     *
     * @param updateReqVO 修订信息
     * @param userId 用户编号
     * @return 保存后的识别记录
     */
    AiOcrDO updateOcrMy(AiOcrUpdateReqVO updateReqVO, Long userId);

    /**
     * 删除【我的】识别记录（逻辑删除，图片文件保留）
     *
     * @param id 编号
     * @param userId 用户编号
     */
    void deleteOcrMy(Long id, Long userId);

    // ========== OCR 管理 ==========

    /**
     * 获得识别记录分页
     *
     * @param pageReqVO 分页查询
     * @return 识别记录分页
     */
    PageResult<AiOcrDO> getOcrPage(AiOcrPageReqVO pageReqVO);

    /**
     * 获得识别记录
     *
     * @param id 编号
     * @return 识别记录
     */
    AiOcrDO getOcr(Long id);

    /**
     * 删除识别记录（逻辑删除，图片文件保留）
     *
     * @param id 编号
     */
    void deleteOcr(Long id);

}
