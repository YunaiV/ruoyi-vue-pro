package cn.iocoder.yudao.module.ai.job;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.ai.client.MidjourneyProxyClient;
import cn.iocoder.yudao.module.ai.client.vo.MidjourneyNotifyReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiImageMapper;
import cn.iocoder.yudao.module.ai.enums.image.AiImageStatusEnum;
import cn.iocoder.yudao.module.ai.service.image.AiImageService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * midjourney job 定时拉去 midjourney 绘制状态
 *
 * @author fansili
 * @time 2024/6/5 14:55
 * @since 1.0
 */
@Component
@Slf4j
public class MidjourneyJob implements JobHandler {

    @Autowired
    private MidjourneyProxyClient midjourneyProxyClient;
    @Autowired
    private AiImageMapper imageMapper;
    @Autowired
    private AiImageService imageService;

    @Override
    public String execute(String param) throws Exception {
        // 1、获取 midjourney 平台，状态在 “进行中” 的 image
        log.info("Midjourney 同步 - 开始...");
        List<AiImageDO> imageList = imageMapper.selectList(
                new LambdaUpdateWrapper<AiImageDO>()
                        .eq(AiImageDO::getStatus, AiImageStatusEnum.IN_PROGRESS.getStatus())
                        .eq(AiImageDO::getPlatform, AiPlatformEnum.MIDJOURNEY.getPlatform())
        );
        log.info("Midjourney 同步 - 任务数量 {}!", imageList.size());
        if (CollUtil.isEmpty(imageList)) {
            return "Midjourney 同步 - 数量为空!";
        }
        // 2、批量拉去 task 信息
        List<MidjourneyNotifyReqVO> taskList = midjourneyProxyClient
                .listByCondition(imageList.stream().map(AiImageDO::getTaskId).collect(Collectors.toSet()));
        Map<String, MidjourneyNotifyReqVO> taskIdMap = taskList.stream().collect(Collectors.toMap(MidjourneyNotifyReqVO::getId, o -> o));
        // 3、更新 image 状态
        List<AiImageDO> updateImageList = new ArrayList<>();
        for (AiImageDO aiImageDO : imageList) {
            // 3.1 排除掉空的情况
            if (!taskIdMap.containsKey(aiImageDO.getTaskId())) {
                log.warn("Midjourney 同步 - {} 任务为空!", aiImageDO.getTaskId());
                continue;
            }
            // 3.2 获取通知对象
            MidjourneyNotifyReqVO notifyReqVO = taskIdMap.get(aiImageDO.getTaskId());
            // 3.2 构建更新对象
            updateImageList.add(imageService.buildUpdateImage(aiImageDO.getId(), notifyReqVO));
        }
        // 4、批了更新 updateImageList
        imageMapper.updateBatch(updateImageList);
        return "Midjourney 同步 - ".concat(String.valueOf(updateImageList.size())).concat(" 任务!");
    }
}
