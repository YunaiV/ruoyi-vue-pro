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

    // TODO @fan：@Resource
    @Autowired
    private MidjourneyProxyClient midjourneyProxyClient;
    @Autowired
    private AiImageMapper imageMapper;
    @Autowired
    private AiImageService imageService;

    // TODO @fan：这个方法，建议实现到 AiImageService，例如说 midjourneySync，返回 int 同步数量。
    @Override
    public String execute(String param) throws Exception {
        // 1、获取 midjourney 平台，状态在 “进行中” 的 image
        // TODO @fan：43 和 51 其实有点重叠，日志，建议只打 51
        log.info("Midjourney 同步 - 开始...");
        // TODO @fan：Job、Service 等业务层，不要直接使用 LambdaUpdateWrapper，这样会导致 mapper 穿透到逻辑层。要收敛到 mapper 里。
        List<AiImageDO> imageList = imageMapper.selectList(
                new LambdaUpdateWrapper<AiImageDO>()
                        .eq(AiImageDO::getStatus, AiImageStatusEnum.IN_PROGRESS.getStatus())
                        .eq(AiImageDO::getPlatform, AiPlatformEnum.MIDJOURNEY.getPlatform())
        );
        log.info("Midjourney 同步 - 任务数量 {}!", imageList.size());
        if (CollUtil.isEmpty(imageList)) {
            // TODO @fan：51 和 54，其实有点重叠。建议 51 挪到 55 之后打。
            return "Midjourney 同步 - 数量为空!";
        }
        // 2、批量拉去 task 信息
        // TODO @fan：imageList.stream().map(AiImageDO::getTaskId).collect(Collectors.toSet()))，可以使用 CollectionUtils.convertSet 简化
        List<MidjourneyNotifyReqVO> taskList = midjourneyProxyClient
                .listByCondition(imageList.stream().map(AiImageDO::getTaskId).collect(Collectors.toSet()));
        // TODO @fan：taskList.stream().collect(Collectors.toMap(MidjourneyNotifyReqVO::getId, o -> o))，也可以使用 CollectionUtils.convertMap；本质上，重用 set、map 转换，要 convert 简化
        Map<String, MidjourneyNotifyReqVO> taskIdMap = taskList.stream().collect(Collectors.toMap(MidjourneyNotifyReqVO::getId, o -> o));
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
            MidjourneyNotifyReqVO notifyReqVO = taskIdMap.get(aiImageDO.getTaskId());
            // 3.2 构建更新对象
            // TODO @fan：建议 List<MidjourneyNotifyReqVO> 作为 imageService 去更新；
            updateImageList.add(imageService.buildUpdateImage(aiImageDO.getId(), notifyReqVO));
        }
        // 4、批了更新 updateImageList
        imageMapper.updateBatch(updateImageList);
        return "Midjourney 同步 - ".concat(String.valueOf(updateImageList.size())).concat(" 任务!");
    }

}
