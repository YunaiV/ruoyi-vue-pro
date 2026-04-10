package cn.iocoder.yudao.module.ai.framework.ai.core.model.ppt.xunfei;

import cn.hutool.core.io.FileUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.xinghuo.api.XunFeiPptApi;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * {@link XunFeiPptApi} 集成测试
 *
 * @author xiaoxin
 */
public class XunFeiPptApiTests {

    // 讯飞 API 配置信息，实际使用时请替换为您的应用信息
    private static final String APP_ID = "6c8ac023";
    private static final String API_SECRET = "Y2RjM2Q1MWJjZTdkYmFiODc0OGE5NmRk";

    private final XunFeiPptApi xunfeiPptApi = new XunFeiPptApi(APP_ID, API_SECRET);

    /**
     * 获取 PPT 模板列表
     */
    @Test
    @Disabled
    public void testGetTemplatePage() {
        // 调用方法
        XunFeiPptApi.TemplatePageResponse response = xunfeiPptApi.getTemplatePage("商务", 10);
        // 打印结果
        System.out.println("模板列表响应：" + JsonUtils.toJsonString(response));

        if (response != null && response.data() != null && response.data().records() != null) {
            System.out.println("模板总数：" + response.data().total());
            System.out.println("当前页码：" + response.data().pageNum());
            System.out.println("模板数量：" + response.data().records().size());

            // 打印第一个模板的信息（如果存在）
            if (!response.data().records().isEmpty()) {
                XunFeiPptApi.TemplateInfo firstTemplate = response.data().records().get(0);
                System.out.println("模板ID：" + firstTemplate.templateIndexId());
                System.out.println("模板风格：" + firstTemplate.style());
                System.out.println("模板颜色：" + firstTemplate.color());
                System.out.println("模板行业：" + firstTemplate.industry());
            }
        }
    }

    /**
     * 创建大纲（通过文本）
     */
    @Test
    @Disabled
    public void testCreateOutline() {
        XunFeiPptApi.CreateResponse response = getCreateResponse();
        // 打印结果
        System.out.println("创建大纲响应：" + JsonUtils.toJsonString(response));

        // 保存 sid 和 outline 用于后续测试
        if (response != null && response.data() != null) {
            System.out.println("sid: " + response.data().sid());
            if (response.data().outline() != null) {
                // 使用 OutlineData 的 toJsonString 方法
                System.out.println("outline: " + response.data().outline().toJsonString());
                // 将 outline 对象转换为 JSON 字符串，用于后续 createPptByOutline 测试
                String outlineJson = response.data().outline().toJsonString();
                System.out.println("可用于 createPptByOutline 的 outline 字符串: " + outlineJson);
            }
        }
    }

    /**
     * 创建大纲（通过文本）
     *
     * @return 创建大纲响应
     */
    private XunFeiPptApi.CreateResponse getCreateResponse() {
        String param = "智能体平台 Dify 介绍";
        return xunfeiPptApi.createOutline(param);
    }

    /**
     * 通过大纲创建 PPT（完整参数）
     */
    @Test
    @Disabled
    public void testCreatePptByOutlineWithFullParams() {
        // 创建大纲对象
        XunFeiPptApi.CreateResponse createResponse = getCreateResponse();
        // 调用方法
        XunFeiPptApi.CreateResponse response = xunfeiPptApi.createPptByOutline(createResponse.data().outline(), "精简一些，不要超过6个章节");
        // 打印结果
        System.out.println("通过大纲创建 PPT 响应：" + JsonUtils.toJsonString(response));

        // 保存sid用于后续进度查询
        if (response != null && response.data() != null) {
            System.out.println("sid: " + response.data().sid());
            if (response.data().coverImgSrc() != null) {
                System.out.println("封面图片: " + response.data().coverImgSrc());
            }
        }
    }

    /**
     * 检查 PPT 生成进度
     */
    @Test
    @Disabled
    public void testCheckProgress() {
        // 准备参数 - 使用之前创建 PPT 时返回的 sid
        String sid = "e96dac09f2ec4ee289f029a5fb874ecd"; // 替换为实际的sid

        // 调用方法
        XunFeiPptApi.ProgressResponse response = xunfeiPptApi.checkProgress(sid);
        // 打印结果
        System.out.println("检查进度响应：" + JsonUtils.toJsonString(response));

        // 安全地访问响应数据
        if (response != null && response.data() != null) {
            XunFeiPptApi.ProgressResponseData data = response.data();

            // 打印PPT生成状态
            System.out.println("PPT 构建状态: " + data.pptStatus());
            System.out.println("AI 配图状态: " + data.aiImageStatus());
            System.out.println("演讲备注状态: " + data.cardNoteStatus());

            // 打印进度信息
            if (data.totalPages() != null && data.donePages() != null) {
                System.out.println("总页数: " + data.totalPages());
                System.out.println("已完成页数: " + data.donePages());
                System.out.println("完成进度: " + data.getProgressPercent() + "%");
            } else {
                System.out.println("进度: " + data.process() + "%");
            }

            // 检查是否完成
            if (data.isAllDone()) {
                System.out.println("PPT 生成已完成!");
                System.out.println("PPT 下载链接: " + data.pptUrl());
            }
            // 检查是否失败
            else if (data.isFailed()) {
                System.out.println("PPT 生成失败!");
                System.out.println("错误信息: " + data.errMsg());
            }
            // 正在进行中
            else {
                System.out.println("PPT 生成中，请稍后再查询...");
            }
        }
    }

    /**
     * 轮询检查 PPT 生成进度直到完成
     */
    @Test
    @Disabled
    public void testPollCheckProgress() throws InterruptedException {
        // 准备参数 - 使用之前创建 PP T时返回的 sid
        String sid = "1690ef6ee0344e72b5c5434f403b8eaa"; // 替换为实际的sid

        // 最大轮询次数
        int maxPolls = 20;
        // 轮询间隔（毫秒）- 讯飞 API 限流为 3 秒一次
        long pollInterval = 3500;

        for (int i = 0; i < maxPolls; i++) {
            System.out.println("第" + (i + 1) + "次查询进度...");

            // 调用方法
            XunFeiPptApi.ProgressResponse response = xunfeiPptApi.checkProgress(sid);

            // 安全地访问响应数据
            if (response != null && response.data() != null) {
                XunFeiPptApi.ProgressResponseData data = response.data();

                // 打印进度信息
                System.out.println("PPT 构建状态: " + data.pptStatus());
                if (data.totalPages() != null && data.donePages() != null) {
                    System.out.println("完成进度: " + data.donePages() + "/" + data.totalPages()
                            + " (" + data.getProgressPercent() + "%)");
                }

                // 检查是否完成
                if (data.isAllDone()) {
                    System.out.println("PPT 生成已完成!");
                    System.out.println("PPT 下载链接: " + data.pptUrl());
                    break;
                }
                // 检查是否失败
                else if (data.isFailed()) {
                    System.out.println("PPT 生成失败!");
                    System.out.println("错误信息: " + data.errMsg());
                    break;
                }
                // 正在进行中，继续轮询
                else {
                    System.out.println("PPT 生成中，等待" + (pollInterval / 1000) + "秒后继续查询...");
                    Thread.sleep(pollInterval);
                }
            } else {
                System.out.println("查询失败，等待" + (pollInterval / 1000) + "秒后重试...");
                Thread.sleep(pollInterval);
            }
        }
    }

    /**
     * 直接创建 PPT（通过文本）
     */
    @Test
    @Disabled
    public void testCreatePptByText() {
        // 准备参数
        String query = "合肥天气趋势分析，包括近5年的气温变化、降水量变化、极端天气事件，以及对城市生活的影响";

        // 调用方法
        XunFeiPptApi.CreateResponse response = xunfeiPptApi.create(query);
        // 打印结果
        System.out.println("直接创建 PPT 响应：" + JsonUtils.toJsonString(response));

        // 保存 sid 用于后续进度查询
        if (response != null && response.data() != null) {
            System.out.println("sid: " + response.data().sid());
            if (response.data().coverImgSrc() != null) {
                System.out.println("封面图片: " + response.data().coverImgSrc());
            }
            System.out.println("标题: " + response.data().title());
            System.out.println("副标题: " + response.data().subTitle());
        }
    }

    /**
     * 直接创建 PPT（通过文件）
     */
    @Test
    @Disabled
    public void testCreatePptByFile() {
        // 准备参数
        File file = new File("src/test/resources/test.txt"); // 请确保此文件存在
        MultipartFile multipartFile = convertFileToMultipartFile(file);

        // 调用方法
        XunFeiPptApi.CreateResponse response = xunfeiPptApi.create(multipartFile, file.getName());
        // 打印结果
        System.out.println("通过文件创建PPT响应：" + JsonUtils.toJsonString(response));

        // 保存 sid 用于后续进度查询
        if (response != null && response.data() != null) {
            System.out.println("sid: " + response.data().sid());
            if (response.data().coverImgSrc() != null) {
                System.out.println("封面图片: " + response.data().coverImgSrc());
            }
            System.out.println("标题: " + response.data().title());
            System.out.println("副标题: " + response.data().subTitle());
        }
    }

    /**
     * 直接创建 PPT（完整参数）
     */
    @Test
    @Disabled
    public void testCreatePptWithFullParams() {
        // 准备参数
        String query = "合肥天气趋势分析，包括近 5 年的气温变化、降水量变化、极端天气事件，以及对城市生活的影响";

        // 创建请求对象
        XunFeiPptApi.CreatePptRequest request = XunFeiPptApi.CreatePptRequest.builder()
                .query(query)
                .language("cn")
                .isCardNote(true)
                .search(true)
                .isFigure(true)
                .aiImage("advanced")
                .author("测试用户")
                .build();

        // 调用方法
        XunFeiPptApi.CreateResponse response = xunfeiPptApi.create(request);
        // 打印结果
        System.out.println("使用完整参数创建 PPT 响应：" + JsonUtils.toJsonString(response));

        // 保存 sid 用于后续进度查询
        if (response != null && response.data() != null) {
            String sid = response.data().sid();
            System.out.println("sid: " + sid);
            if (response.data().coverImgSrc() != null) {
                System.out.println("封面图片: " + response.data().coverImgSrc());
            }
            System.out.println("标题: " + response.data().title());
            System.out.println("副标题: " + response.data().subTitle());

            // 立即查询一次进度
            System.out.println("立即查询进度...");
            XunFeiPptApi.ProgressResponse progressResponse = xunfeiPptApi.checkProgress(sid);
            if (progressResponse != null && progressResponse.data() != null) {
                XunFeiPptApi.ProgressResponseData progressData = progressResponse.data();
                System.out.println("PPT 构建状态: " + progressData.pptStatus());
                if (progressData.totalPages() != null && progressData.donePages() != null) {
                    System.out.println("完成进度: " + progressData.donePages() + "/" + progressData.totalPages()
                            + " (" + progressData.getProgressPercent() + "%)");
                }
            }
        }
    }

    /**
     * 将 File 转换为 MultipartFile
     */
    private MultipartFile convertFileToMultipartFile(File file) {
        return new MockMultipartFile("file", file.getName(), "text/plain", FileUtil.readBytes(file));
    }

}