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

    // TODO @xin：使用 @Resource 注入，整个项目保持统一哈；
    private final SunoApi sunoApi;
    private final AiMusicMapper musicMapper;

    private final Queue<String> taskQueue = new ConcurrentLinkedQueue<>();

    // TODO @xin：要不把 descriptionMode、lyricMode 合并，同一个 generateMusic 方法，然后根据传入的 mode 模式：歌词、描述来区分？

    @Override
    public List<Long> descriptionMode(SunoReqVO reqVO) {
        // 1. 异步生成
        SunoApi.SunoRequest sunoReq = new SunoApi.SunoRequest(reqVO.getPrompt(), reqVO.getMv(), reqVO.isMakeInstrumental());
        List<SunoApi.MusicData> musicDataList = sunoApi.generate(sunoReq);
        // 2. 插入数据库
        return insertMusicData(musicDataList);
    }

    @Override
    public List<Long> lyricMode(SunoLyricModeVO reqVO) {
        // 1. 异步生成
        SunoApi.SunoRequest sunoReq = new SunoApi.SunoRequest(reqVO.getPrompt(), reqVO.getMv(), reqVO.getTags(), reqVO.getTitle());
        List<SunoApi.MusicData> musicDataList = sunoApi.customGenerate(sunoReq);
        // 2. 插入数据库
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
        // TODO @xin：建议使用 insertBatch 方法，批量插入
        return AiMusicDO.convertFrom(musicDataList).stream()
                .peek(musicDO -> musicMapper.insert(musicDO.setUserId(getLoginUserId())))
                .peek(e -> Optional.of(e.getTaskId()).ifPresent(taskQueue::add))
                .map(AiMusicDO::getId)
                .collect(Collectors.toList());
    }

    // TODO @xin：这个，改成标准的 job 来实现哈。从数据库加载任务，然后执行。
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
