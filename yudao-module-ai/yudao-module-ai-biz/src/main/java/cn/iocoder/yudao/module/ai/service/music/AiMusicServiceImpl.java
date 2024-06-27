package cn.iocoder.yudao.module.ai.service.music;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiSunoGenerateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.dal.mysql.music.AiMusicMapper;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicGenerateModeEnum;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * AI 音乐 Service 实现类
 *
 * @author xiaoxin
 */
@Service
@Slf4j
public class AiMusicServiceImpl implements AiMusicService {

    @Resource
    private SunoApi sunoApi;

    @Resource
    private AiMusicMapper musicMapper;

    @Override
    public List<Long> generateMusic(Long userId, AiSunoGenerateReqVO reqVO) {
        // 1. 调用 Suno 生成音乐
        List<SunoApi.MusicData> musicDataList;
        if (Objects.equals(AiMusicGenerateModeEnum.LYRIC.getMode(), reqVO.getGenerateMode())) {
            // 1.1 歌词模式
            SunoApi.MusicGenerateRequest generateRequest = new SunoApi.MusicGenerateRequest(
                    reqVO.getPrompt(), reqVO.getModelVersion(), CollUtil.join(reqVO.getTags(), StrPool.COMMA), reqVO.getTitle());
            musicDataList = sunoApi.customGenerate(generateRequest);
        } else if (Objects.equals(AiMusicGenerateModeEnum.DESCRIPTION.getMode(), reqVO.getGenerateMode())) {
            // 1.2 描述模式
            SunoApi.MusicGenerateRequest generateRequest = new SunoApi.MusicGenerateRequest(
                    reqVO.getPrompt(), reqVO.getModelVersion(), reqVO.getMakeInstrumental());
            musicDataList = sunoApi.generate(generateRequest);
        } else {
            throw new IllegalArgumentException(StrUtil.format("未知生成模式({})", reqVO));
        }

        // 2. 插入数据库
        if (CollUtil.isEmpty(musicDataList)) {
            return Collections.emptyList();
        }
        List<AiMusicDO> musicList = buildMusicDOList(musicDataList);
        musicList.forEach(music -> music.setUserId(userId).setPlatform(music.getPlatform()).setGenerateMode(reqVO.getGenerateMode()));
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

    /**
     * 构建 AiMusicDO 集合
     *
     * @param musicList suno 音乐任务列表
     * @return AiMusicDO 集合
     */
    private static List<AiMusicDO> buildMusicDOList(List<SunoApi.MusicData> musicList) {
        return convertList(musicList, musicData -> new AiMusicDO()
                .setTaskId(musicData.id()).setModel(musicData.modelName())
                .setPrompt(musicData.prompt()).setGptDescriptionPrompt(musicData.gptDescriptionPrompt())
                .setAudioUrl(musicData.audioUrl()).setVideoUrl(musicData.videoUrl()).setImageUrl(musicData.imageUrl())
                .setTitle(musicData.title()).setLyric(musicData.lyric()).setTags(StrUtil.split(musicData.tags(), StrPool.COMMA))
                .setStatus(Objects.equals("complete", musicData.status()) ? AiMusicStatusEnum.SUCCESS.getStatus() : AiMusicStatusEnum.IN_PROGRESS.getStatus()));

    }
}
