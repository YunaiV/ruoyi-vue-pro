package cn.iocoder.yudao.module.ai.framework.ai.core.video;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 视频异步任务的引用：创建任务后返回，调用方应立即持久化，之后只凭它查询，不重新提交
 *
 * 除任务编号外，还保存继续查询所需的元数据，例如：
 * 1. 可灵旧版接口：route 为 text2video / image2video（查询路径依赖创建时的路由）
 * 2. Google Veo：taskId 为完整的 operation name，model 为模型（查询走模型的 fetchPredictOperation）
 * 3. 即梦 Visual：model 为 req_key，route 为查询的 Action（CVGetResult / CVSync2AsyncGetResult）
 *
 * 不含任何凭证，可以安全地序列化为 JSON 保存
 *
 * @author 芋道源码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiVideoTaskRef {

    /**
     * 平台标识，例如 doubao、kling、minimax-v1、minimax-v2、tongyi-wan、happyhorse、vertex-veo、jimeng
     */
    private String platform;
    /**
     * 模型（或即梦的 req_key）
     */
    private String model;
    /**
     * 上游任务编号（Veo 为 operation name）
     */
    private String taskId;
    /**
     * 查询路由，部分平台需要；不需要时为 null
     */
    private String route;
    /**
     * 创建请求的服务商请求编号，便于排查
     */
    private String requestId;

}
