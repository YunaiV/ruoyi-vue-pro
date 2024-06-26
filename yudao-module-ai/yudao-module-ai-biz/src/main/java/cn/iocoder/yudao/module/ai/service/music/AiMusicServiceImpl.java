package cn.iocoder.yudao.module.ai.service.music;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiSunoGenerateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.dal.mysql.music.AiMusicMapper;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicGenerateModeEnum;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

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
    public List<Long> generateMusic(AiSunoGenerateReqVO reqVO) {
        List<SunoApi.MusicData> musicDataList;
        if (Objects.equals(AiMusicGenerateModeEnum.LYRIC.getMode(), reqVO.getGenerateMode())) {
            // 1.1 歌词模式
            SunoApi.MusicGenerateRequest sunoReq = new SunoApi.MusicGenerateRequest(
                    reqVO.getPrompt(), reqVO.getModelVersion(), CollUtil.join(reqVO.getTags(), StrPool.COMMA), reqVO.getTitle());
            musicDataList = sunoApi.customGenerate(sunoReq);
        } else if (Objects.equals(AiMusicGenerateModeEnum.DESCRIPTION.getMode(), reqVO.getGenerateMode())) {
            // 1.2 描述模式
            SunoApi.MusicGenerateRequest sunoReq = new SunoApi.MusicGenerateRequest(
                    reqVO.getPrompt(), reqVO.getModelVersion(), reqVO.getMakeInstrumental());
            musicDataList = sunoApi.generate(sunoReq);
        } else {
            throw new IllegalArgumentException(StrUtil.format("未知生成模式({})", reqVO));
        }
        // 2. 插入数据库
        if (CollUtil.isEmpty(musicDataList)) {

            return Collections.emptyList();
        }
        List<AiMusicDO> aiMusicDOList = CollectionUtils.convertList(buildMusicDOList(musicDataList), musicDO ->
                musicDO.setUserId(getLoginUserId())
                        .setGenerateMode(reqVO.getGenerateMode())
                        .setPlatform(reqVO.getPlatform()
                        ));
        musicMapper.insertBatch(aiMusicDOList);
        return CollectionUtils.convertList(aiMusicDOList, AiMusicDO::getId);
    }

    @Override
    public Integer syncMusic() {
        List<AiMusicDO> streamingTask = musicMapper.selectListByStatus(AiMusicStatusEnum.STREAMING.getStatus());
        if (CollUtil.isEmpty(streamingTask)) {
            return 0;
        }
        log.info("[syncMusic][Suno 开始同步, 共 ({}) 个任务]", streamingTask.size());
        // GET 请求，为避免参数过长，分批次处理
        CollUtil.split(streamingTask, 36).forEach(chunk -> {
            Map<String, Long> taskIdMap = CollectionUtils.convertMap(chunk, AiMusicDO::getTaskId, AiMusicDO::getId);
            List<SunoApi.MusicData> musicTaskList = sunoApi.getMusicList(new ArrayList<>(taskIdMap.keySet()));
            if (CollUtil.isEmpty(musicTaskList)) {
                log.warn("Suno 任务同步失败, 任务ID: [{}]", taskIdMap.keySet());
                return;
            }
            List<AiMusicDO> aiMusicDOS = buildMusicDOList(musicTaskList);
            //回填id
            aiMusicDOS.forEach(aiMusicDO -> aiMusicDO.setId(taskIdMap.get(aiMusicDO.getTaskId())));
            musicMapper.updateBatch(aiMusicDOS);
        });
        return streamingTask.size();
    }

    /**
     * 构建 AiMusicDO 集合
     *
     * @param musicTaskList suno 音乐任务列表
     * @return AiMusicDO 集合
     */
    private static List<AiMusicDO> buildMusicDOList(List<SunoApi.MusicData> musicTaskList) {
        // TODO @xin：想通的变量，放在同一行，避免过长。
        return CollectionUtils.convertList(musicTaskList, musicData -> new AiMusicDO()
                .setTaskId(musicData.id())
                .setPrompt(musicData.prompt())
                .setGptDescriptionPrompt(musicData.gptDescriptionPrompt())
                .setAudioUrl(musicData.audioUrl())
                .setVideoUrl(musicData.videoUrl())
                .setImageUrl(musicData.imageUrl())
                .setLyric(musicData.lyric())
                .setTitle(musicData.title())
                .setStatus(Objects.equals("complete", musicData.status()) ? AiMusicStatusEnum.COMPLETE.getStatus() : AiMusicStatusEnum.STREAMING.getStatus())
                .setModel(musicData.modelName())
                .setTags(StrUtil.split(musicData.tags(), StrPool.COMMA)));
    }

}
