package cn.iocoder.yudao.framework.ai.image;

import cn.iocoder.yudao.framework.ai.core.model.chatglm.ChatGlmImageModel;
import cn.iocoder.yudao.framework.ai.core.model.chatglm.ChatGlmImageOptions;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.alibaba.fastjson.JSON;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.core.httpclient.ApacheHttpClientTransport;
import com.zhipu.oapi.service.v4.image.CreateImageRequest;
import com.zhipu.oapi.service.v4.image.ImageApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.qianfan.QianFanImageModel;
import org.springframework.ai.qianfan.QianFanImageOptions;
import org.springframework.ai.qianfan.api.QianFanImageApi;

/**
 * 百度千帆 image
 */
public class ChatGlmImageModelTests {

    @Test
    public void callTest() {
        ChatGlmImageModel model = new ChatGlmImageModel("78d3228c1d9e5e342a3e1ab349e2dd7b.VXLoq5vrwK2ofboy");
        ImageResponse call = model.call(new ImagePrompt("万里长城", ChatGlmImageOptions.builder().build()));
        System.err.println(call.getResult().getOutput().getUrl());
    }

    @Test
    public void createImageTest() {
        ClientV4 client = new ClientV4.Builder("78d3228c1d9e5e342a3e1ab349e2dd7b.VXLoq5vrwK2ofboy").build();
        CreateImageRequest createImageRequest = new CreateImageRequest();
        createImageRequest.setModel("cogview-3");
        createImageRequest.setPrompt("长城!");
        ImageApiResponse image = client.createImage(createImageRequest);
        System.err.println(JSON.toJSONString(image));
    }
}
