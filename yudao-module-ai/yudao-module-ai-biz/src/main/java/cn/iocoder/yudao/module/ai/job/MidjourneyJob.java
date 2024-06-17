package cn.iocoder.yudao.module.ai.job;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.MidjourneyNotifyReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiImageMapper;
import cn.iocoder.yudao.module.ai.enums.image.AiImageStatusEnum;
import cn.iocoder.yudao.module.ai.service.image.AiImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    // TODO @fan：@Resource
    @Autowired(required = false)
    private MidjourneyApi midjourneyApi;
    @Autowired
    private AiImageMapper imageMapper;
    @Autowired
    private AiImageService imageService;

    // TODO @fan：这个方法，建议实现到 AiImageService，例如说 midjourneySync，返回 int 同步数量。
    @Override
    public String execute(String param) {
        // 1、获取 midjourney 平台，状态在 “进行中” 的 image
        List<AiImageDO> imageList = imageMapper.selectByStatusAndPlatform(AiImageStatusEnum.IN_PROGRESS, AiPlatformEnum.MIDJOURNEY);
        log.info("Midjourney 同步 - 任务数量 {}!", imageList.size());
        if (CollUtil.isEmpty(imageList)) {
            return "Midjourney 同步 - 数量为空!";
        }
        log.info("Midjourney 同步 - 开始...");
        // 2、批量拉去 task 信息
        List<MidjourneyApi.NotifyRequest> taskList = midjourneyApi
                .listByCondition(CollectionUtils.convertSet(imageList, AiImageDO::getTaskId));
        Map<String, MidjourneyApi.NotifyRequest> taskIdMap
                = CollectionUtils.convertMap(taskList, MidjourneyApi.NotifyRequest::id);
        // 3、更新 image 状态
        List<AiImageDO> updateImageList = new ArrayList<>();
        for (AiImageDO aiImageDO : imageList) {
            // 3.1 排除掉空的情况
            if (!taskIdMap.containsKey(aiImageDO.getTaskId())) {
                log.warn("Midjourney 同步 - {} 任务为空!", aiImageDO.getTaskId());
                continue;
            }
            // TODO @ 3.1 和 3.2 是不是融合下；get，然后判空，continue；
            // 3.2 获取通知对象
            MidjourneyApi.NotifyRequest notifyRequest = taskIdMap.get(aiImageDO.getTaskId());
            // 3.2 构建更新对象
            // TODO @fan：建议 List<MidjourneyNotifyReqVO> 作为 imageService 去更新；
            // TODO @芋艿 BeanUtils.toBean 转换为 null
            updateImageList.add(imageService.buildUpdateImage(aiImageDO.getId(),
                    JsonUtils.parseObject(JsonUtils.toJsonString(notifyRequest), MidjourneyNotifyReqVO.class)));
        }
        // 4、批了更新 updateImageList
        imageMapper.updateBatch(updateImageList);
        return "Midjourney 同步 - ".concat(String.valueOf(updateImageList.size())).concat(" 任务!");
    }

}
