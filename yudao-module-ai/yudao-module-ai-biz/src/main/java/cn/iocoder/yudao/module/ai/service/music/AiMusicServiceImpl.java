package cn.iocoder.yudao.module.ai.service.music;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.AiSunoGenerateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.dal.mysql.music.AiMusicMapper;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicGenerateEnum;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
        if (Objects.equals(AiMusicGenerateEnum.LYRIC.getMode(), reqVO.getGenerateMode())) {
            //歌词模式
            SunoApi.MusicGenerateRequest sunoReq = new SunoApi.MusicGenerateRequest(reqVO.getPrompt(), reqVO.getModelVersion(), CollUtil.join(reqVO.getTags(), StrPool.COMMA), reqVO.getTitle());
            musicDataList = sunoApi.customGenerate(sunoReq);
        } else if (Objects.equals(AiMusicGenerateEnum.DESCRIPTION.getMode(), reqVO.getGenerateMode())) {
            //描述模式
            SunoApi.MusicGenerateRequest sunoReq = new SunoApi.MusicGenerateRequest(reqVO.getPrompt(), reqVO.getModelVersion(), reqVO.getMakeInstrumental());
            musicDataList = sunoApi.generate(sunoReq);
        } else {
            log.error("未知的生成模式：{}", reqVO.getGenerateMode());
            throw new IllegalArgumentException("未知的生成模式");
        }
        // 2. 插入数据库
        return insertMusicData(musicDataList, reqVO.getGenerateMode(), reqVO.getPlatform());
    }

    @Override
    public List<AiMusicDO> getUnCompletedTask() {
        return musicMapper.selectList(new LambdaQueryWrapper<AiMusicDO>().ne(AiMusicDO::getStatus, AiMusicStatusEnum.COMPLETE.getStatus()));
    }

    @Override
    public Integer syncMusicTask() {
        List<AiMusicDO> unCompletedTask = this.getUnCompletedTask();
        if (CollUtil.isEmpty(unCompletedTask)) {
            log.info("Suno 无进行中任务需要更新!");
            return 0;
        }
        log.info("Suno 开始同步, 共 [{}] 个任务!", unCompletedTask.size());
        //GET 请求，为避免参数过长，分批次处理
        CollUtil.split(unCompletedTask, 4)
                .forEach(chunk -> {
                    Map<String, Long> taskIdMap = CollUtil.toMap(chunk, new HashMap<>(), AiMusicDO::getTaskId, AiMusicDO::getId);
                    List<SunoApi.MusicData> musicTaskList = sunoApi.getMusicList(new ArrayList<>(taskIdMap.keySet()));
                    if (CollUtil.isNotEmpty(musicTaskList)) {
                        List<AiMusicDO> aiMusicDOS = buildMusicDOList(musicTaskList);
                        //回填id
                        aiMusicDOS.forEach(aiMusicDO -> aiMusicDO.setId(taskIdMap.get(aiMusicDO.getTaskId())));
                        this.updateBatch(aiMusicDOS);
                    } else {
                        log.warn("Suno 任务同步失败, 任务ID: [{}]", taskIdMap.keySet());
                    }
                });
        return unCompletedTask.size();
    }

    @Override
    public Boolean updateBatch(List<AiMusicDO> musicDOS) {
        return musicMapper.updateBatch(musicDOS);
    }

    /**
     * 新增音乐数据并提交 suno任务
     *
     * @param musicDataList 音乐数据列表
     * @return 音乐id集合
     */
    private List<Long> insertMusicData(List<SunoApi.MusicData> musicDataList, String generateMode, String platform) {
        if (CollUtil.isEmpty(musicDataList)) {
            return Collections.emptyList();
        }
        List<AiMusicDO> aiMusicDOList = buildMusicDOList(musicDataList).stream()
                .map(musicDO -> musicDO.setUserId(getLoginUserId())
                        .setGenerateMode(generateMode)
                        .setPlatform(platform))
                .toList();
        musicMapper.insertBatch(aiMusicDOList);
        return aiMusicDOList.stream()
                .map(AiMusicDO::getId)
                .collect(Collectors.toList());
    }

    /**
     * 构建 AiMusicDO 集合
     *
     * @param musicTaskList suno 音乐任务列表
     * @return AiMusicDO 集合
     */
    private static List<AiMusicDO> buildMusicDOList(List<SunoApi.MusicData> musicTaskList) {
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
                .setTags(StrUtil.isNotBlank(musicData.tags()) ? List.of(musicData.tags().split(StrPool.COMMA)) : null));
    }
}
