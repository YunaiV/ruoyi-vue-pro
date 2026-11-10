package cn.iocoder.yudao.module.ai.service.music;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;

// DONE @AI：注释风格，对齐 ai service 其它模块；不用过度冗余；
/**
 * AI 音乐 Service 接口
 *
 * @author xiaoxin
 */
public interface AiMusicService {

    /**
     * 生成音乐（灵感模式、自定义模式）
     *
     * @param userId 用户编号
     * @param reqVO 生成请求
     * @return 音乐编号
     */
    Long generateMusic(Long userId, AiMusicGenerateReqVO reqVO);

    /**
     * 续写音乐
     *
     * @param userId 用户编号
     * @param reqVO 续写请求
     * @return 音乐编号
     */
    Long extendMusic(Long userId, AiMusicExtendReqVO reqVO);

    /**
     * 合并为完整歌曲
     *
     * @param userId 用户编号
     * @param reqVO 合并请求
     * @return 音乐编号
     */
    Long concatMusic(Long userId, AiMusicConcatReqVO reqVO);

    /**
     * 上传音频
     *
     * @param userId 用户编号
     * @param reqVO 上传请求
     * @return 音乐编号
     */
    Long uploadMusic(Long userId, AiMusicUploadReqVO reqVO);

    /**
     * 生成歌词
     *
     * @param userId 用户编号
     * @param reqVO 生成请求
     * @return 歌曲名称与歌词
     */
    AiMusicLyricRespVO generateLyric(Long userId, AiMusicLyricReqVO reqVO);

    /**
     * 获得【我的】音乐分页
     *
     * @param userId 用户编号
     * @param pageReqVO 分页查询
     * @return 音乐分页
     */
    PageResult<AiMusicDO> getMusicMyPage(Long userId, PageParam pageReqVO);

    /**
     * 获得公开的音乐分页
     *
     * @param pageReqVO 分页查询
     * @return 音乐分页
     */
    PageResult<AiMusicDO> getMusicPublicPage(AiMusicPublicPageReqVO pageReqVO);

    /**
     * 获得音乐详情：本人，或已公开的
     *
     * @param id 编号
     * @param userId 用户编号
     * @return 音乐
     */
    AiMusicDO getMusicDetail(Long id, Long userId);

    /**
     * 更新【我的】音乐
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateMyMusic(AiMusicUpdateMyReqVO updateReqVO, Long userId);

    /**
     * 删除【我的】音乐
     *
     * @param id 编号
     * @param userId 用户编号
     */
    void deleteMusicMy(Long id, Long userId);

    /**
     * 播放音乐：播放次数 +1
     *
     * @param id 编号
     * @param userId 用户编号
     */
    void playMusic(Long id, Long userId);

    /**
     * 获得音乐分页
     *
     * @param pageReqVO 分页查询
     * @return 音乐分页
     */
    PageResult<AiMusicDO> getMusicPage(AiMusicPageReqVO pageReqVO);

    /**
     * 获得音乐
     *
     * @param id 编号
     * @return 音乐
     */
    AiMusicDO getMusic(Long id);

    /**
     * 更新音乐
     *
     * @param updateReqVO 更新信息
     */
    void updateMusic(AiMusicUpdateReqVO updateReqVO);

    /**
     * 删除音乐
     *
     * @param id 编号
     */
    void deleteMusic(Long id);

    /**
     * 同步音乐：查询中转进度、转存文件、超时处理
     *
     * @return 处理的数量
     */
    int syncMusic();

}
