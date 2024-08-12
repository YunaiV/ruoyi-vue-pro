package cn.iocoder.yudao.module.ai.service.music;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.*;
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
     * @param reqVO  请求参数
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
    void updateMusic(@Valid AiMusicUpdateReqVO updateReqVO);

    /**
     * 更新我的音乐
     *
     * @param updateReqVO 更新信息
     */
    void updateMyMusic(@Valid AiMusicUpdateMyReqVO updateReqVO, Long userId);

    /**
     * 删除AI 音乐
     *
     * @param id 编号
     */
    void deleteMusic(Long id);

    /**
     * 删除【我的】音乐记录
     *
     * @param id     音乐编号
     * @param userId 用户编号
     */
    void deleteMusicMy(Long id, Long userId);

    /**
     * 获得AI 音乐
     *
     * @param id 音乐编号
     * @return 音乐内容
     */
    AiMusicDO getMusic(Long id);

    /**
     * 获得音乐分页
     *
     * @param pageReqVO 分页查询
     * @return 音乐分页
     */
    PageResult<AiMusicDO> getMusicPage(AiMusicPageReqVO pageReqVO);

    /**
     * 获得【我的】音乐分页
     *
     * @param pageReqVO 分页查询
     * @param userId    用户编号
     * @return 音乐分页
     */
    PageResult<AiMusicDO> getMusicMyPage(AiMusicPageReqVO pageReqVO, Long userId);

}
