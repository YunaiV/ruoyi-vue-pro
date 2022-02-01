package cn.iocoder.yudao.adminserver.modules.bpm.service.task;

import cn.iocoder.yudao.module.bpm.controller.task.vo.activity.BpmActivityRespVO;

import java.util.List;

/**
 * BPM 活动实例 Service 接口
 *
 * @author 芋道源码
 */
public interface BpmActivityService {

    /**
     * 获得指定流程实例的活动实例列表
     *
     * @param processInstanceId 流程实例的编号
     * @return 活动实例列表
     */
    List<BpmActivityRespVO> getActivityListByProcessInstanceId(String processInstanceId);

    /**
     * 生成指定流程实例的高亮流程图，只高亮进行中的任务
     *
     * 友情提示，非该方法的注释。如果想实现更高级的高亮流程图（当前节点红色 + 完成节点为绿色），可参考如下内容：
     *      博客一：https://blog.csdn.net/qq_40109075/article/details/110939639
     *      博客二：https://gitee.com/tony2y/RuoYi-flowable/blob/master/ruoyi-flowable/src/main/java/com/ruoyi/flowable/flow/CustomProcessDiagramGenerator.java
     * 这里不实现的原理，需要自定义实现 ProcessDiagramGenerator 和 ProcessDiagramCanvas，代码量有点大
     *
     * 如果你想实现高亮已完成的任务，可参考 https://blog.csdn.net/qiuxinfa123/article/details/119579863 博客。不过测试下来，貌似不太对~
     *
     * @param processInstanceId 流程实例的编号
     * @return 图的字节数组
     */
    byte[] generateHighlightDiagram(String processInstanceId);

}
