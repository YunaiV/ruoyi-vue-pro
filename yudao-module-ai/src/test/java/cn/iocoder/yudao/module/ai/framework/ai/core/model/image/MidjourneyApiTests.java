package cn.iocoder.yudao.module.ai.framework.ai.core.model.image;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

/**
 * {@link MidjourneyApi} 集成测试
 *
 * @author 芋道源码
 */
public class MidjourneyApiTests {

    private final MidjourneyApi midjourneyApi = new MidjourneyApi(
            "https://api.holdai.top/mj", // 链接
            "sk-aN6nWn3fILjrgLFT0fC4Aa60B72e4253826c77B29dC94f17", // 密钥
            null);

    @Test
    @Disabled
    public void testImagine() {
        // 准备参数
        MidjourneyApi.ImagineRequest request = new MidjourneyApi.ImagineRequest(null,
                "生成一个小猫，可爱的", null,
                MidjourneyApi.ImagineRequest.buildState(512, 512, "6.0", MidjourneyApi.ModelEnum.MIDJOURNEY.getModel()));

        // 方法调用
        MidjourneyApi.SubmitResponse response = midjourneyApi.imagine(request);
        // 打印结果
        System.out.println(response);
    }

    @Test
    @Disabled
    public void testAction() {
        // 准备参数
        MidjourneyApi.ActionRequest request = new MidjourneyApi.ActionRequest("1720277033455953",
                "MJ::JOB::upsample::1::ee267661-ee52-4ced-a530-0343ba95af3b", null);

        // 方法调用
        MidjourneyApi.SubmitResponse response = midjourneyApi.action(request);
        // 打印结果
        System.out.println(response);
    }

    @Test
    @Disabled
    public void testGetTaskList() {
        // 准备参数。该参数可以通过 MidjourneyApi.SubmitResponse 的 result 获取
//        String taskId = "1720277033455953";
        String taskId = "1720277214045971";

        // 方法调用
        List<MidjourneyApi.Notify> taskList = midjourneyApi.getTaskList(Collections.singletonList(taskId));
        // 打印结果
        System.out.println(taskList);
    }

}
