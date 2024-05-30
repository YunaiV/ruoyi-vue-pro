package cn.iocoder.yudao.module.ai.service.music;

import cn.iocoder.yudao.module.ai.controller.admin.music.vo.SunoReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.SunoRespVO;

/**
 * @Author xiaoxin
 * @Date 2024/5/29
 */
public interface MusicService {

    /**
     * 音乐生成
     *
     * @param sunoReqVO 请求实体
     * @return 响应实体
     */
    SunoRespVO musicGen(SunoReqVO sunoReqVO);
}
