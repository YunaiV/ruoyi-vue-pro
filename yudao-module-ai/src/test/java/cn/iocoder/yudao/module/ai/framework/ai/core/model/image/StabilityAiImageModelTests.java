package cn.iocoder.yudao.module.ai.framework.ai.core.model.image;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.thread.ThreadUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.stabilityai.StabilityAiImageModel;
import org.springframework.ai.stabilityai.api.StabilityAiApi;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

/**
 * {@link StabilityAiImageModel} 集成测试类
 *
 * @author fansili
 */
public class StabilityAiImageModelTests {

    private final StabilityAiImageModel imageModel = new StabilityAiImageModel(
            new StabilityAiApi("sk-e53UqbboF8QJCscYvzJscJxJXoFcFg4iJjl1oqgE7baJETmx") // 密钥
    );

    @Test
    @Disabled
    public void testCall() {
        // 准备参数
        ImageOptions options = OpenAiImageOptions.builder()
                .model("stable-diffusion-v1-6")
                .height(320).width(320)
                .build();
        ImagePrompt prompt = new ImagePrompt("great wall", options);

        // 方法调用
        ImageResponse response = imageModel.call(prompt);
        // 打印结果
        String b64Json = response.getResult().getOutput().getB64Json();
        System.out.println(response);
        viewImage(b64Json);
    }

    public static void viewImage(String b64Json) {
        // 创建一个 JFrame
        JFrame frame = new JFrame("Byte Image Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // 创建一个 JLabel 来显示图片
        byte[] imageBytes = Base64.decode(b64Json);
        JLabel label = new JLabel(new ImageIcon(imageBytes));

        // 将 JLabel 添加到 JFrame
        frame.getContentPane().add(label, BorderLayout.CENTER);

        // 显示 JFrame
        frame.setVisible(true);
        ThreadUtil.sleep(1, TimeUnit.HOURS);
    }

}
