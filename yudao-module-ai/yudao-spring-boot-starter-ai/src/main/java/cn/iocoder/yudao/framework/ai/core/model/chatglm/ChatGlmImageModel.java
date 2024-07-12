package cn.iocoder.yudao.framework.ai.core.model.chatglm;

import cn.iocoder.yudao.framework.ai.core.model.chatglm.api.ChatGlmResponseMetadata;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.service.v4.image.CreateImageRequest;
import com.zhipu.oapi.service.v4.image.ImageApiResponse;
import org.springframework.ai.image.*;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Base64;
import java.util.stream.Collectors;

public class ChatGlmImageModel implements ImageModel {

    private ClientV4 client;

    public ChatGlmImageModel(String apiSecretKey) {
        client = new ClientV4.Builder(apiSecretKey).build();
    }

    @Override
    public ImageResponse call(ImagePrompt request) {
        CreateImageRequest imageRequest = CreateImageRequest.builder()
                .model(request.getOptions().getModel())
                .prompt(request.getInstructions().get(0).getText())
                .build();
        return convert(client.createImage(imageRequest));
    }

    private ImageResponse convert(ImageApiResponse result) {
        return new ImageResponse(
                result.getData().getData().stream().map(item -> {
                    try {
                        String url = item.getUrl();
                        String base64Image = convertImageToBase64(url);
                        Image image = new Image(url, base64Image);
                        return new ImageGeneration(image);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList()),
                new ChatGlmResponseMetadata(result)
        );
    }


    /**
     * Convert image to base64.
     * @param imageUrl the image url.
     * @return the base64 image.
     * @throws Exception the exception.
     */
    public String convertImageToBase64(String imageUrl) throws Exception {

        var url = new URL(imageUrl);
        var inputStream = url.openStream();
        var outputStream = new ByteArrayOutputStream();
        var buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        var imageBytes = outputStream.toByteArray();

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        inputStream.close();
        outputStream.close();

        return base64Image;
    }
}
