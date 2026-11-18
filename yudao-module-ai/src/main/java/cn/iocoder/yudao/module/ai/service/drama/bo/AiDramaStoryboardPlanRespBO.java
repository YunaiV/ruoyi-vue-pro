package cn.iocoder.yudao.module.ai.service.drama.bo;

import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaStoryboardDO;
import lombok.Data;

import java.util.List;

/**
 * AI 短剧规划分镜 Response BO：规划出的一个镜头（未保存），及其规划提示
 *
 * @author 芋道源码
 */
@Data
public class AiDramaStoryboardPlanRespBO {

    /**
     * 镜头：未保存，没有编号
     */
    private AiDramaStoryboardDO storyboard;

    /**
     * 规划提示：供审阅时检查，例如角色指定的形象无法唯一匹配；没有时为 null。不保存
     */
    private List<String> warnings;

}
