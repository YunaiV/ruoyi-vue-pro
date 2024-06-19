package cn.iocoder.yudao.module.ai.service.music;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.SunoReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.dal.mysql.music.AiMusicMapper;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicGenerateEnum;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * AI 音乐 Service 实现类
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
    public List<Long> generateMusic(SunoReqVO reqVO) {
        AiMusicGenerateEnum generateEnum = AiMusicGenerateEnum.valueOfMode(reqVO.getGenerateMode());
        return switch (generateEnum) {
            case DESCRIPTION -> descriptionMode(reqVO);
            case LYRIC -> lyricMode(reqVO);
        };
    }

    @Override
    public List<AiMusicDO> getUnCompletedTask() {
        return musicMapper.selectList(new LambdaQueryWrapper<AiMusicDO>().ne(AiMusicDO::getStatus, AiMusicStatusEnum.COMPLETE.getStatus()));
    }

    @Override
    public Boolean updateBatch(List<AiMusicDO> aiMusicDOList) {
        return musicMapper.updateBatch(aiMusicDOList);
    }

    /**
     * 描述模式生成音乐
     *
     * @param reqVO 请求参数
     * @return 生成的音乐ID集合
     */
    public List<Long> descriptionMode(SunoReqVO reqVO) {
        // 1. 异步生成
        SunoApi.MusicGenerateRequest sunoReq = new SunoApi.MusicGenerateRequest(reqVO.getPrompt(), reqVO.getMv(), reqVO.getMakeInstrumental());
        List<SunoApi.MusicData> musicDataList = sunoApi.generate(sunoReq);
        // 2. 插入数据库
        return insertMusicData(musicDataList, reqVO.getGenerateMode(), reqVO.getPlatform());
    }

    /**
     * 歌词模式生成音乐
     *
     * @param reqVO 请求参数
     * @return 生成的音乐ID集合
     */
    public List<Long> lyricMode(SunoReqVO reqVO) {
        // 1. 异步生成
        SunoApi.MusicGenerateRequest sunoReq = new SunoApi.MusicGenerateRequest(reqVO.getPrompt(), reqVO.getMv(), CollUtil.join(reqVO.getTags(), StrPool.COMMA), reqVO.getTitle());
        List<SunoApi.MusicData> musicDataList = sunoApi.customGenerate(sunoReq);
        // 2. 插入数据库
        return insertMusicData(musicDataList, reqVO.getGenerateMode(), reqVO.getPlatform());
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
        List<AiMusicDO> aiMusicDOList = AiMusicConvert.convertFrom(musicDataList).stream()
                .map(musicDO -> musicDO.setUserId(getLoginUserId())
                        .setGenerateMode(generateMode)
                        .setPlatform(platform))
                .toList();
        musicMapper.insertBatch(aiMusicDOList);
        return aiMusicDOList.stream()
                .map(AiMusicDO::getId)
                .collect(Collectors.toList());
    }

}
