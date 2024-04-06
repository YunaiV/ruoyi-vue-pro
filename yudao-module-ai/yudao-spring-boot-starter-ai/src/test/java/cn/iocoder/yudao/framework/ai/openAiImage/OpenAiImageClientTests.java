package cn.iocoder.yudao.framework.ai.openAiImage;

import cn.iocoder.yudao.framework.ai.imageopenai.OpenAiImageApi;
import cn.iocoder.yudao.framework.ai.imageopenai.OpenAiImageClient;
import cn.iocoder.yudao.framework.ai.imageopenai.OpenAiImageOptions;
import cn.iocoder.yudao.framework.ai.image.ImagePrompt;
import cn.iocoder.yudao.framework.ai.image.ImageResponse;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Scanner;

/**
 * author: fansili
 * time: 2024/3/17 10:40
 */
public class OpenAiImageClientTests {


    private OpenAiImageClient openAiImageClient;

    @Before
    public void setup() {
        // 初始化 openAiImageClient
        this.openAiImageClient = new OpenAiImageClient(
                new OpenAiImageApi(""),
                new OpenAiImageOptions().setResponseFormat(OpenAiImageOptions.ResponseFormatEnum.URL.getValue())
        );
    }

    @Test
    public void callTest() {
        ImageResponse call = openAiImageClient.call(new ImagePrompt("我和我的小狗，一起在北极和企鹅玩排球。"));
        System.err.println("url: " + call.getResult().getOutput().getUrl());
        System.err.println("base64: " + call.getResult().getOutput().getB64Json());

        String base64String = call.getResult().getOutput().getB64Json();
        ImageIcon imageIcon = new ImageIcon(decodeBase64ToImage(base64String));
        JLabel label = new JLabel(imageIcon);

        JFrame frame = new JFrame("Base64 Image Display");
        frame.getContentPane().add(label);
        frame.pack();
        frame.setVisible(true);

        // 阻止退出
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }


    // 将Base64解码为BufferedImage
    private static BufferedImage decodeBase64ToImage(String base64String) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes);
            return ImageIO.read(bis);
        } catch (IOException e) {
            System.out.println("Error decoding the base64 image: " + e.getMessage());
            return null;
        }
    }
}
