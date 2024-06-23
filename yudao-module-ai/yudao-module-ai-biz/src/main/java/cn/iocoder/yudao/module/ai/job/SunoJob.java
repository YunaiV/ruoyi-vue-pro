package cn.iocoder.yudao.module.ai.job;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.service.music.AiMusicConvert;
import cn.iocoder.yudao.module.ai.service.music.AiMusicService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理 Suno Job
 * @author xiaoxin
 */
@Component
@Slf4j
public class SunoJob implements JobHandler {

    @Resource
    private SunoApi sunoApi;
    @Resource
    private AiMusicService musicService;

    @Override
    public String execute(String param) {
        List<AiMusicDO> unCompletedTask = musicService.getUnCompletedTask();

        if (CollUtil.isEmpty(unCompletedTask)) {
            log.info("Suno 无进行中任务需要更新!");
            return "Suno 无进行中任务需要更新!";
        }


        log.info("Suno 开始同步, 共 [{}] 个任务!", unCompletedTask.size());
        //GET 请求，为避免参数过长，分批次处理
        CollUtil.split(unCompletedTask, 4)
                .forEach(chunk -> {
                    Map<String, Long> taskIdMap = CollUtil.toMap(chunk, new HashMap<>(), AiMusicDO::getTaskId, AiMusicDO::getId);
                    List<SunoApi.MusicData> musicTaskList = sunoApi.getMusicList(new ArrayList<>(taskIdMap.keySet()));
                    if (CollUtil.isNotEmpty(musicTaskList)) {
                        List<AiMusicDO> aiMusicDOS = AiMusicConvert.convertFrom(musicTaskList);
                        //回填id
                        aiMusicDOS.forEach(aiMusicDO -> aiMusicDO.setId(taskIdMap.get(aiMusicDO.getTaskId())));
                        musicService.updateBatch(aiMusicDOS);
                    } else {
                        log.warn("Suno 任务同步失败, 任务ID: [{}]", taskIdMap.keySet());
                    }
                });

        return "Suno 同步 - ".concat(String.valueOf(unCompletedTask.size())).concat(" 个任务!");
    }
}
