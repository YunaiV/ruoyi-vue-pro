package cn.iocoder.yudao.module.ai.service.music;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiMusicPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiMusicUpdatePublicStatusReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiSunoGenerateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import jakarta.validation.Valid;

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

    /**
     * 更新音乐发布状态
     *
     * @param updateReqVO 更新信息
     */
    void updateMusicPublicStatus(@Valid AiMusicUpdatePublicStatusReqVO updateReqVO);

    /**
     * 删除AI 音乐
     *
     * @param id 编号
     */
    void deleteMusic(Long id);

    /**
     * 获得音乐分页
     *
     * @param pageReqVO 分页查询
     * @return 音乐分页
     */
    PageResult<AiMusicDO> getMusicPage(AiMusicPageReqVO pageReqVO);

}
