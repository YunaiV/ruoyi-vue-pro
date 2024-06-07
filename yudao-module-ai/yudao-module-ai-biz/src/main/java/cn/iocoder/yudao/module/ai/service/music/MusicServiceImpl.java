package cn.iocoder.yudao.module.ai.service.music;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.SunoLyricModeVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.SunoReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.dal.mysql.music.AiMusicMapper;
import cn.iocoder.yudao.module.ai.enums.AiMusicStatusEnum;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * @Author xiaoxin
 * @Date 2024/5/29
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MusicServiceImpl implements MusicService {

    private final SunoApi sunoApi;
    private final AiMusicMapper musicMapper;

    private final Queue<String> taskQueue = new ConcurrentLinkedQueue<>();


    @Override
    public List<Long> descriptionMode(SunoReqVO reqVO) {
        SunoApi.SunoReq sunoReq = new SunoApi.SunoReq(reqVO.getPrompt(), reqVO.getMv(), reqVO.isMakeInstrumental());
        //默认异步
        List<SunoApi.MusicData> musicDataList = sunoApi.generate(sunoReq);
        return insertMusicData(musicDataList);
    }


    @Override
    public List<Long> lyricMode(SunoLyricModeVO reqVO) {
        SunoApi.SunoReq sunoReq = new SunoApi.SunoReq(reqVO.getPrompt(), reqVO.getMv(), reqVO.getTags(), reqVO.getTitle());
        //默认异步
        List<SunoApi.MusicData> musicDataList = sunoApi.customGenerate(sunoReq);
        return insertMusicData(musicDataList);
    }

    /**
     * 新增音乐数据并提交 suno任务
     *
     * @param musicDataList 音乐数据列表
     * @return 音乐id集合
     */
    private List<Long> insertMusicData(List<SunoApi.MusicData> musicDataList) {
        if (CollUtil.isEmpty(musicDataList)) {
            return Collections.emptyList();
        }
        return AiMusicDO.convertFrom(musicDataList).stream()
                .peek(musicDO -> musicMapper.insert(musicDO.setUserId(getLoginUserId())))
                .peek(e -> Optional.of(e.getTaskId()).ifPresent(taskQueue::add))
                .map(AiMusicDO::getId)
                .collect(Collectors.toList());
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void flushSunoTask() {
        if (CollUtil.isEmpty(taskQueue)) {
            return;
        }
        CollUtil.split(taskQueue, 5).
                stream().map(chunk -> CollUtil.join(chunk, StrPool.COMMA))
                .forEach(taskIds -> {
                    List<SunoApi.MusicData> musicData = sunoApi.selectById(taskIds);
                    musicData.stream()
                            .map(AiMusicDO::convertFrom)
                            .forEach(musicDO -> {
                                //更新音乐生成结果
                                musicMapper.update(musicDO, Wrappers.<AiMusicDO>lambdaUpdate().eq(AiMusicDO::getTaskId, musicDO.getTaskId()));
                                //完成后剔除任务
                                if (Objects.equals(AiMusicStatusEnum.COMPLETE.getStatus(), musicDO.getStatus())) {
                                    taskQueue.remove(musicDO.getTaskId());
                                }
                            });
                });

    }
}
