package cn.iocoder.yudao.module.ai.service.music;

import cn.iocoder.yudao.module.ai.controller.admin.music.vo.SunoLyricModeVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.SunoReqVO;

import java.util.List;

/**
 * @Author xiaoxin
 * @Date 2024/5/29
 */
public interface MusicService {

    /**
     * 音乐生成-描述模式
     */
    List<Long> descriptionMode(SunoReqVO reqVO);


    /**
     * 音乐生成-歌词模式
     **/
    List<Long> lyricMode(SunoLyricModeVO reqVO);
}
