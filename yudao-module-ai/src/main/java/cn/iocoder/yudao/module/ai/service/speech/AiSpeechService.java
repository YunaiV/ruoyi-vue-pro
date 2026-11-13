package cn.iocoder.yudao.module.ai.service.speech;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.speech.vo.AiSpeechGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.speech.vo.AiSpeechPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.speech.AiSpeechDO;

/**
 * AI 语音 Service 接口
 *
 * @author 芋道源码
 */
public interface AiSpeechService {

    /**
     * 合成语音：插入生成中的记录后异步执行，前端按返回的编号轮询
     *
     * @param userId 用户编号
     * @param reqVO 合成信息
     * @return 语音编号
     */
    Long generateSpeech(Long userId, AiSpeechGenerateReqVO reqVO);

    /**
     * 获得【我的】语音分页
     *
     * @param userId 用户编号
     * @param pageReqVO 分页查询
     * @return 语音分页
     */
    PageResult<AiSpeechDO> getSpeechMyPage(Long userId, PageParam pageReqVO);

    /**
     * 获得【我的】语音：生成中的，先同步一次结果
     *
     * @param id 编号
     * @param userId 用户编号
     * @return 语音
     */
    AiSpeechDO getSpeechMy(Long id, Long userId);

    /**
     * 删除【我的】语音
     *
     * @param id 编号
     * @param userId 用户编号
     */
    void deleteSpeechMy(Long id, Long userId);

    // ========== 语音管理 ==========

    /**
     * 获得语音分页
     *
     * @param pageReqVO 分页查询
     * @return 语音分页
     */
    PageResult<AiSpeechDO> getSpeechPage(AiSpeechPageReqVO pageReqVO);

    /**
     * 获得语音
     *
     * @param id 编号
     * @return 语音
     */
    AiSpeechDO getSpeech(Long id);

    /**
     * 删除语音：生成中的不能删除，音频文件保留
     *
     * @param id 编号
     */
    void deleteSpeech(Long id);

    /**
     * 同步生成中的语音：查询异步任务的结果，并把超时的置为已失败
     *
     * @return 处理的数量
     */
    int syncSpeech();

}
