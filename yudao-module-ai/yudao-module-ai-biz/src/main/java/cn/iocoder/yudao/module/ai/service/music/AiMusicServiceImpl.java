package cn.iocoder.yudao.module.ai.service.music;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiMusicPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiMusicUpdateMyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiMusicUpdateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiSunoGenerateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.dal.mysql.music.AiMusicMapper;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicGenerateModeEnum;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicStatusEnum;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.IMAGE_NOT_EXISTS;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.MUSIC_NOT_EXISTS;

/**
 * AI 音乐 Service 实现类
 *
 * @author xiaoxin
 */
@Service
@Slf4j
public class AiMusicServiceImpl implements AiMusicService {

    @Resource
    private AiApiKeyService apiKeyService;

    @Resource
    private AiMusicMapper musicMapper;

    @Resource
    private FileApi fileApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> generateMusic(Long userId, AiSunoGenerateReqVO reqVO) {
        // 1. 调用 Suno 生成音乐
        SunoApi sunoApi = apiKeyService.getSunoApi();
        List<SunoApi.MusicData> musicDataList;
        if (Objects.equals(AiMusicGenerateModeEnum.DESCRIPTION.getMode(), reqVO.getGenerateMode())) {
            // 1.1 描述模式
            SunoApi.MusicGenerateRequest generateRequest = new SunoApi.MusicGenerateRequest(
                    reqVO.getPrompt(), reqVO.getModel(), reqVO.getMakeInstrumental());
            musicDataList = sunoApi.generate(generateRequest);
        } else if (Objects.equals(AiMusicGenerateModeEnum.LYRIC.getMode(), reqVO.getGenerateMode())) {
            // 1.2 歌词模式
            SunoApi.MusicGenerateRequest generateRequest = new SunoApi.MusicGenerateRequest(
                    reqVO.getPrompt(), reqVO.getModel(), CollUtil.join(reqVO.getTags(), StrPool.COMMA), reqVO.getTitle());
            musicDataList = sunoApi.customGenerate(generateRequest);
        } else {
            throw new IllegalArgumentException(StrUtil.format("未知生成模式({})", reqVO));
        }

        // 2. 插入数据库
        if (CollUtil.isEmpty(musicDataList)) {
            return Collections.emptyList();
        }
        List<AiMusicDO> musicList = buildMusicDOList(musicDataList);
        musicList.forEach(music -> music.setUserId(userId).setPlatform(reqVO.getPlatform()).setGenerateMode(reqVO.getGenerateMode()));
        musicMapper.insertBatch(musicList);
        return convertList(musicList, AiMusicDO::getId);
    }

    @Override
    public Integer syncMusic() {
        List<AiMusicDO> streamingTask = musicMapper.selectListByStatus(AiMusicStatusEnum.IN_PROGRESS.getStatus());
        if (CollUtil.isEmpty(streamingTask)) {
            return 0;
        }
        log.info("[syncMusic][Suno 开始同步, 共 ({}) 个任务]", streamingTask.size());

        // GET 请求，为避免参数过长，分批次处理
        SunoApi sunoApi = apiKeyService.getSunoApi();
        CollUtil.split(streamingTask, 36).forEach(chunkList -> {
            Map<String, Long> taskIdMap = convertMap(chunkList, AiMusicDO::getTaskId, AiMusicDO::getId);
            List<SunoApi.MusicData> musicTaskList = sunoApi.getMusicList(new ArrayList<>(taskIdMap.keySet()));
            if (CollUtil.isEmpty(musicTaskList)) {
                log.warn("Suno 任务同步失败, 任务ID: [{}]", taskIdMap.keySet());
                return;
            }
            // 更新进度
            List<AiMusicDO> updateMusicList = buildMusicDOList(musicTaskList);
            updateMusicList.forEach(music -> music.setId(taskIdMap.get(music.getTaskId())));
            musicMapper.updateBatch(updateMusicList);
        });
        return streamingTask.size();
    }

    @Override
    public void updateMusic(AiMusicUpdateReqVO updateReqVO) {
        // 校验存在
        validateMusicExists(updateReqVO.getId());
        // 更新
        musicMapper.updateById(new AiMusicDO().setId(updateReqVO.getId()).setPublicStatus(updateReqVO.getPublicStatus()));
    }

    @Override
    public void updateMyMusic(AiMusicUpdateMyReqVO updateReqVO, Long userId) {
        // 校验音乐是否存在
        AiMusicDO musicDO = validateMusicExists(updateReqVO.getId());
        if (ObjUtil.notEqual(musicDO.getUserId(), userId)) {
            throw exception(MUSIC_NOT_EXISTS);
        }
        // 更新
        musicMapper.updateById(new AiMusicDO().setId(updateReqVO.getId()).setTitle(updateReqVO.getTitle()));
    }

    @Override
    public void deleteMusic(Long id) {
        // 校验存在
        validateMusicExists(id);
        // 删除
        musicMapper.deleteById(id);
    }

    @Override
    public void deleteMusicMy(Long id, Long userId) {
        // 1. 校验是否存在
        AiMusicDO music = validateMusicExists(id);
        if (ObjUtil.notEqual(music.getUserId(), userId)) {
            throw exception(IMAGE_NOT_EXISTS);
        }
        // 2. 删除记录
        musicMapper.deleteById(id);
    }

    @Override
    public AiMusicDO getMusic(Long id) {
        return musicMapper.selectById(id);
    }

    @Override
    public PageResult<AiMusicDO> getMusicPage(AiMusicPageReqVO pageReqVO) {
        return musicMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<AiMusicDO> getMusicMyPage(AiMusicPageReqVO pageReqVO, Long userId) {
        return musicMapper.selectPageByMy(pageReqVO, userId);
    }

    /**
     * 构建 AiMusicDO 集合
     *
     * @param musicList suno 音乐任务列表
     * @return AiMusicDO 集合
     */
    private List<AiMusicDO> buildMusicDOList(List<SunoApi.MusicData> musicList) {
        return convertList(musicList, musicData -> {
            Integer status = Objects.equals("complete", musicData.status()) ? AiMusicStatusEnum.SUCCESS.getStatus()
                    : Objects.equals("error", musicData.status()) ? AiMusicStatusEnum.FAIL.getStatus()
                    : AiMusicStatusEnum.IN_PROGRESS.getStatus();
            return new AiMusicDO()
                    .setTaskId(musicData.id()).setModel(musicData.modelName())
                    .setDescription(musicData.gptDescriptionPrompt())
                    .setAudioUrl(downloadFile(status, musicData.audioUrl()))
                    .setVideoUrl(downloadFile(status, musicData.videoUrl()))
                    .setImageUrl(downloadFile(status, musicData.imageUrl()))
                    .setTitle(musicData.title()).setDuration(musicData.duration())
                    .setLyric(musicData.lyric()).setTags(StrUtil.split(musicData.tags(), StrPool.COMMA))
                    .setErrorMessage(musicData.errorMessage())
                    .setStatus(status);
        });
    }

    /**
     * 音乐生成好后，将音频文件上传到文件服务器
     *
     * @param status 音乐状态
     * @param url    音频文件地址
     * @return 内部文件地址
     */
    private String downloadFile(Integer status, String url) {
        if (StrUtil.isBlank(url) || ObjectUtil.notEqual(status, AiMusicStatusEnum.SUCCESS.getStatus())) {
            return url;
        }
        try {
            byte[] bytes = HttpUtil.downloadBytes(url);
            return fileApi.createFile(bytes);
        } catch (Exception e) {
            log.error("[downloadFile][url({}) 下载失败]", url, e);
            return url;
        }
    }

    /**
     * 校验音乐是否存在
     *
     * @param id 音乐编号
     * @return 音乐信息
     */
    private AiMusicDO validateMusicExists(Long id) {
        AiMusicDO music = musicMapper.selectById(id);
        if (music == null) {
            throw exception(MUSIC_NOT_EXISTS);
        }
        return music;
    }

}
