package cn.iocoder.yudao.module.ai.service.music;

import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiSunoGenerateReqVO;

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
     * @param userId 用户编号
     * @param reqVO 请求参数
     * @return 生成的音乐ID
     */
    List<Long> generateMusic(Long userId, AiSunoGenerateReqVO reqVO);

    /**
     * 同步音乐任务
     *
     * @return 同步数量
     */
    Integer syncMusic();

}
