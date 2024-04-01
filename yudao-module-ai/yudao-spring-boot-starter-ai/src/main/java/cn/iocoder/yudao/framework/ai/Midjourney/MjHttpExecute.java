package cn.iocoder.yudao.framework.ai.Midjourney;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class MjHttpExecute implements MjExecute {

    private static final String URL = "https://discord.com/";

    private RestTemplate restTemplate = new RestTemplate();


    @Override
    public boolean execute(MjCommandEnum mjCommand, String prompt) {
        // 发送的 uri
        String uri = "api/v9/interactions";
        // restTemplate 发送post请求
        String result = restTemplate.postForObject(URL + uri, prompt, String.class);
        // 加载当前目录下文件

        return false;
    }
}
