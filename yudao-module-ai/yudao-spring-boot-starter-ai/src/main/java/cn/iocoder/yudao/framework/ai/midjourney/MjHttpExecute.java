package cn.iocoder.yudao.framework.ai.midjourney;

import cn.hutool.core.io.FileUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

public class MjHttpExecute implements MjExecute {

    private static final String URL = "https://discord.com/";

    @Override
    public boolean execute(MjCommandEnum mjCommand, String prompt) {
        // 发送的 uri
        String uri = "api/v9/interactions";
        // restTemplate 发送post请求
//        String result = restTemplate.postForObject(URL + uri, prompt, String.class);
        // 加载当前目录下文件
        return false;
    }

    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();

        String token =  "NTY5MDY4NDAxNzEyOTU1Mzky.G4-Fu0.MzD-7ll-ElbXTTgDPHF-WS_UyhMAfbKN3WyyBc";
        String body = FileUtil.readString("/Users/fansili/projects/github/ruoyi-vue-pro/yudao-module-ai" +
                "/yudao-spring-boot-starter-ai/src/main/java/cn/iocoder/yudao/framework/ai/midjourney" +
                "/interactions_type2.json", Charset.forName("utf-8"));
        // 创建HTTP头部
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // 设置内容类型为JSON
        headers.set("Authorization", token); // 如果需要，设置认证信息（例如JWT令牌）
        headers.set("Referer", "https://discord.com/channels/1221445697157468200/1221445862962630706"); // 如果需要，设置认证信息（例如JWT令牌）
        headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"); // 如果需要，设置认证信息（例如JWT令牌）
        headers.set("Cookie", "__dcfduid=6ca536c0e3fa11eeb7cbe34c31b49caf; __sdcfduid=6ca536c1e3fa11eeb7cbe34c31b49caf52cce5ffd8983d2a052cf6aba75fe5fe566f2c265902e283ce30dbf98b8c9c93; _gcl_au=1.1.245923998.1710853617; _ga=GA1.1.111061823.1710853617; __cfruid=6385bb3f48345a006b25992db7dcf984e395736d-1712124666; _cfuvid=O09la5ms0ypNptiG0iD8A6BKWlTxz1LG0WR7qRStD7o-1712124666575-0.0.1.1-604800000; locale=zh-CN; cf_clearance=l_YGod1_SUtYxpDVeZXiX7DLLPl1DYrquZe8WVltvYs-1712124668-1.0.1.1-Hl2.fToel23EpF2HCu9J20rB4D7OhhCzoajPSdo.9Up.wPxhvq22DP9RHzEBKuIUlKyH.kJLxXJfAt2N.LD5WQ; OptanonConsent=isIABGlobal=false&datestamp=Wed+Apr+03+2024+14%3A11%3A15+GMT%2B0800+(%E4%B8%AD%E5%9B%BD%E6%A0%87%E5%87%86%E6%97%B6%E9%97%B4)&version=6.33.0&hosts=&landingPath=https%3A%2F%2Fdiscord.com%2F&groups=C0001%3A1%2CC0002%3A1%2CC0003%3A1; _ga_Q149DFWHT7=GS1.1.1712124668.4.1.1712124679.0.0.0"); // 如果需要，设置认证信息（例如JWT令牌）
        // 封装请求体和头部信息
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        // 定义请求URL和返回类型
        String uri = "api/v9/interactions";
        String res = restTemplate.postForObject(URL + uri, requestEntity, String.class);
        System.err.println("11");

        //
//        MjHttpExecute mjHttpExecute = new MjHttpExecute();
//        mjHttpExecute.execute(null, "童话世界应该是什么样？");

    }
}
