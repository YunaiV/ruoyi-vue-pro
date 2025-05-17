package cn.iocoder.yudao.module.ai.framework.ai.core.model.mcp;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.doubao.DouBaoChatModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;

@Disabled
public class DouBaoMcpTests {

    private final OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
            .openAiApi(OpenAiApi.builder()
                    .baseUrl(DouBaoChatModel.BASE_URL)
                    .apiKey("5c1b5747-26d2-4ebd-a4e0-dd0e8d8b4272") // apiKey
                    .build())
            .defaultOptions(OpenAiChatOptions.builder()
                    .model("doubao-1-5-lite-32k-250115") // 模型（doubao）
                    .temperature(0.7)
                    .build())
            .build();

    private final DouBaoChatModel chatModel = new DouBaoChatModel(openAiChatModel);

    private final MethodToolCallbackProvider provider = MethodToolCallbackProvider.builder()
            .toolObjects(new UserService())
            .build();

    private final ChatClient chatClient = ChatClient.builder(chatModel)
            .defaultTools(provider)
            .build();

    @Test
    public void testMcpGetUserInfo() {

        // 打印结果
        System.out.println(chatClient.prompt()
                .user("目前有哪些工具可以使用")
                .call()
                .content());
        System.out.println("====================================");
        // 打印结果
        System.out.println(chatClient.prompt()
                .user("小新的年龄是多少")
                .call()
                .content());
        System.out.println("====================================");
        // 打印结果
        System.out.println(chatClient.prompt()
                .user("获取小新的基本信息")
                .call()
                .content());
        System.out.println("====================================");
        // 打印结果
        System.out.println(chatClient.prompt()
                .user("小新是什么职业的")
                .call()
                .content());
        System.out.println("====================================");
        // 打印结果
        System.out.println(chatClient.prompt()
                .user("小新的教育背景")
                .call()
                .content());
        System.out.println("====================================");
        // 打印结果
        System.out.println(chatClient.prompt()
                .user("小新的兴趣爱好是什么")
                .call()
                .content());
        System.out.println("====================================");

    }


    static class UserService {

        @Tool(name = "getUserAge", description = "获取用户年龄")
        public String getUserAge(String userName) {
            return "《" + userName + "》的年龄为：18";
        }

        @Tool(name = "getUserSex", description = "获取用户性别")
        public String getUserSex(String userName) {
            return "《" + userName + "》的性别为：男";
        }

        @Tool(name = "getUserBasicInfo", description = "获取用户基本信息，包括姓名、年龄、性别等")
        public String getUserBasicInfo(String userName) {
            return "《" + userName + "》的基本信息：\n姓名：" + userName + "\n年龄：18\n性别：男\n身高：175cm\n体重：65kg";
        }

        @Tool(name = "getUserContact", description = "获取用户联系方式，包括电话、邮箱等")
        public String getUserContact(String userName) {
            return "《" + userName + "》的联系方式：\n电话：138****1234\n邮箱：" + userName.toLowerCase() + "@example.com\nQQ：123456789";
        }

        @Tool(name = "getUserAddress", description = "获取用户地址信息")
        public String getUserAddress(String userName) {
            return "《" + userName + "》的地址信息：北京市朝阳区科技园区88号";
        }

        @Tool(name = "getUserJob", description = "获取用户职业信息")
        public String getUserJob(String userName) {
            return "《" + userName + "》的职业信息：软件工程师，就职于ABC科技有限公司，工作年限5年";
        }

        @Tool(name = "getUserHobbies", description = "获取用户兴趣爱好")
        public String getUserHobbies(String userName) {
            return "《" + userName + "》的兴趣爱好：编程、阅读、旅游、摄影、打篮球";
        }

        @Tool(name = "getUserEducation", description = "获取用户教育背景")
        public String getUserEducation(String userName) {
            return "《" + userName + "》的教育背景：\n本科：计算机科学与技术专业，北京大学\n硕士：软件工程专业，清华大学";
        }

    }

}