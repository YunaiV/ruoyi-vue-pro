package cn.iocoder.yudao.module.ai.service.asr;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.asr.vo.AiAsrPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.asr.vo.AiAsrRecognizeReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.asr.AiAsrDO;

/**
 * AI 语音识别 Service 接口
 *
 * @author 芋道源码
 */
public interface AiAsrService {

    /**
     * 同步识别音频中的语音
     *
     * 音频先由前端上传到文件服务，这里按地址下载后识别；成功、失败都会新增一条记录；
     * 模型校验不通过时，直接抛出业务异常，不记录
     *
     * @param userId 用户编号
     * @param reqVO 识别信息
     * @return 识别记录
     */
    AiAsrDO recognizeAsr(Long userId, AiAsrRecognizeReqVO reqVO);

    /**
     * 获得【我的】识别记录分页
     *
     * @param userId 用户编号
     * @param pageReqVO 分页查询
     * @return 识别记录分页
     */
    PageResult<AiAsrDO> getAsrMyPage(Long userId, PageParam pageReqVO);

    /**
     * 获得【我的】识别记录
     *
     * @param id 编号
     * @param userId 用户编号
     * @return 识别记录；不存在、或不属于该用户时抛出业务异常
     */
    AiAsrDO getAsrMy(Long id, Long userId);

    /**
     * 删除【我的】识别记录（逻辑删除，音频文件保留）
     *
     * @param id 编号
     * @param userId 用户编号
     */
    void deleteAsrMy(Long id, Long userId);

    // ========== 语音识别管理 ==========

    /**
     * 获得识别记录分页
     *
     * @param pageReqVO 分页查询
     * @return 识别记录分页
     */
    PageResult<AiAsrDO> getAsrPage(AiAsrPageReqVO pageReqVO);

    /**
     * 获得识别记录
     *
     * @param id 编号
     * @return 识别记录
     */
    AiAsrDO getAsr(Long id);

    /**
     * 删除识别记录（逻辑删除，音频文件保留）
     *
     * @param id 编号
     */
    void deleteAsr(Long id);

}
