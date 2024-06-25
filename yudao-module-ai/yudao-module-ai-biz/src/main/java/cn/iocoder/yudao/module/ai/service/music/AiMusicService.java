package cn.iocoder.yudao.module.ai.service.music;

import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiSunoGenerateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;

import java.util.List;

/**
 * AI 音乐 Service 接口
 *
 * @author xiaoxin
 */
public interface AiMusicService {

    /**
     * 音乐生成
     *
     * @param reqVO 请求参数
     * @return 生成的音乐ID
     */
    List<Long> generateMusic(AiSunoGenerateReqVO reqVO);

    /**
     * 获取未完成状态的任务
     *
     * @return 未完成任务列表
     */
    List<AiMusicDO> getUnCompletedTask();

    /**
     * 同步音乐任务
     *
     * @return 同步数量
     */
    Integer syncMusic();

    /**
     * 批量更新音乐信息
     *
     * @param musicDOS 音乐信息
     * @return 是否成功
     */
    Boolean updateBatch(List<AiMusicDO> musicDOS);

}
