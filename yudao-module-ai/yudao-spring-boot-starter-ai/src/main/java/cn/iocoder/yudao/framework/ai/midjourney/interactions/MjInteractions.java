package cn.iocoder.yudao.framework.ai.midjourney.interactions;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.ai.midjourney.constants.MjConstants;
import cn.iocoder.yudao.framework.ai.midjourney.util.MjUtil;
import cn.iocoder.yudao.framework.ai.midjourney.vo.Attachments;
import cn.iocoder.yudao.framework.ai.midjourney.vo.Describe;
import cn.iocoder.yudao.framework.ai.midjourney.vo.ReRoll;
import cn.iocoder.yudao.framework.ai.midjourney.vo.UploadAttachmentsRes;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;

/**
 * 图片生成
 *
 * author: fansili
 * time: 2024/4/3 17:36
 */
@Slf4j
public class MjInteractions {


    private final String url;
    private final MidjourneyConfig midjourneyConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String HEADER_REFERER = "https://discord.com/channels/%s/%s";


    public MjInteractions(MidjourneyConfig midjourneyConfig) {
        this.midjourneyConfig = midjourneyConfig;
        this.url = midjourneyConfig.getServerUrl().concat(midjourneyConfig.getApiInteractions());
    }

    public Boolean imagine(String prompt) {
        // 获取请求模板
        String requestTemplate = midjourneyConfig.getRequestTemplates().get("imagine");
        // 设置参数
        HashMap<String, String> requestParams = Maps.newHashMap();
        requestParams.put("guild_id", midjourneyConfig.getGuildId());
        requestParams.put("channel_id", midjourneyConfig.getChannelId());
        requestParams.put("session_id", midjourneyConfig.getSessionId());
        requestParams.put("nonce", String.valueOf(IdUtil.getSnowflakeNextId()));
        requestParams.put("prompt", prompt);
        // 解析 template 参数占位符
        String requestBody = MjUtil.parseTemplate(requestTemplate, requestParams);
        // 获取 header
        HttpHeaders httpHeaders = getHttpHeaders();
        // 发送请求
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, httpHeaders);
        String res = restTemplate.postForObject(url, requestEntity, String.class);
        // 这个 res 只要不返回值，就是成功!
        boolean isSuccess = StrUtil.isBlank(res);
        if (isSuccess) {
            return true;
        }
        log.error("请求失败! 请求参数：{} 返回结果! {}", requestBody, res);
        return isSuccess;
    }



    public Boolean reRoll(ReRoll reRoll) {
        // 获取请求模板
        String requestTemplate = midjourneyConfig.getRequestTemplates().get("reroll");
        // 设置参数
        HashMap<String, String> requestParams = Maps.newHashMap();
        requestParams.put("guild_id", midjourneyConfig.getGuildId());
        requestParams.put("channel_id", midjourneyConfig.getChannelId());
        requestParams.put("session_id", midjourneyConfig.getSessionId());
        requestParams.put("nonce", String.valueOf(IdUtil.getSnowflakeNextId()));
        requestParams.put("custom_id", reRoll.getCustomId());
        requestParams.put("message_id", reRoll.getMessageId());
        // 获取 header
        HttpHeaders httpHeaders = getHttpHeaders();
        // 设置参数
        String requestBody = MjUtil.parseTemplate(requestTemplate, requestParams);
        // 发送请求
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, httpHeaders);
        String res = restTemplate.postForObject(url, requestEntity, String.class);
        // 这个 res 只要不返回值，就是成功!
        boolean isSuccess = StrUtil.isBlank(res);
        if (isSuccess) {
            return true;
        }
        log.error("请求失败! 请求参数：{} 返回结果! {}", requestBody, res);
        return isSuccess;
    }


    public UploadAttachmentsRes uploadAttachments(Attachments attachments) {
        // file
        JSONObject fileObj = new JSONObject();
        fileObj.put("id", "0");
        fileObj.put("filename", attachments.getFileSystemResource().getFilename());
        try {
            fileObj.put("file_size", attachments.getFileSystemResource().contentLength());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 创建用于存放表单数据的MultiValueMap
        MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();
        multipartRequest.put("files", Lists.newArrayList(fileObj));
        // 设置header值
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Authorization", midjourneyConfig.getToken());
        httpHeaders.set("User-Agent", midjourneyConfig.getUserAage());
        httpHeaders.set("Cookie", MjConstants.HTTP_COOKIE);
        httpHeaders.set("Referer", String.format(HEADER_REFERER, midjourneyConfig.getGuildId(), midjourneyConfig.getChannelId()));
        // 创建HttpEntity对象，包含表单数据和头部信息
        HttpEntity<MultiValueMap<String, Object>> multiValueMapHttpEntity = new HttpEntity<>(multipartRequest, httpHeaders);
        // 发送POST请求并接收响应
        String uri = String.format(midjourneyConfig.getApiAttachments(), midjourneyConfig.getChannelId());
        String response = restTemplate.postForObject(midjourneyConfig.getServerUrl().concat(uri), multiValueMapHttpEntity, String.class);
        UploadAttachmentsRes uploadAttachmentsRes = JSON.parseObject(response, UploadAttachmentsRes.class);


        //
        // 上传文件
        String uploadUrl = uploadAttachmentsRes.getAttachments().getFirst().getUploadUrl();
        String uploadAttachmentsUrl = midjourneyConfig.getApiAttachmentsUpload().concat(uploadUrl);
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<FileSystemResource> fileSystemResourceHttpEntity = new HttpEntity<>(attachments.getFileSystemResource(), httpHeaders);
        ResponseEntity<String> exchange = restTemplate.exchange(uploadUrl, HttpMethod.PUT, fileSystemResourceHttpEntity, String.class);
        String uploadRes = exchange.getBody();

        return uploadAttachmentsRes;
    }

    public Boolean describe(Describe describe) {
        // 获取请求模板
        String requestTemplate = midjourneyConfig.getRequestTemplates().get("describe");
        // 设置参数
        HashMap<String, String> requestParams = Maps.newHashMap();
        requestParams.put("guild_id", midjourneyConfig.getGuildId());
        requestParams.put("channel_id", midjourneyConfig.getChannelId());
        requestParams.put("session_id", midjourneyConfig.getSessionId());
        requestParams.put("nonce", String.valueOf(IdUtil.getSnowflakeNextId()));
        requestParams.put("file_name", describe.getFileName());
        requestParams.put("final_file_name", describe.getFinalFileName());
        // 设置 header
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA); // 设置内容类型为JSON
        httpHeaders.set("Authorization", midjourneyConfig.getToken());
        httpHeaders.set("User-Agent", midjourneyConfig.getUserAage());
        httpHeaders.set("Cookie", MjConstants.HTTP_COOKIE);
        httpHeaders.set("Referer", String.format(HEADER_REFERER, midjourneyConfig.getGuildId(), midjourneyConfig.getChannelId()));
        String requestBody = MjUtil.parseTemplate(requestTemplate, requestParams);
        // 创建表单数据
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("payload_json", requestBody);
        // 发送请求
        HttpEntity<MultiValueMap<String, String>> multiValueMapHttpEntity = new HttpEntity<>(formData, httpHeaders);
        String res = restTemplate.postForObject(url, multiValueMapHttpEntity, String.class);
        // 这个 res 只要不返回值，就是成功!
        boolean isSuccess = StrUtil.isBlank(res);
        if (isSuccess) {
            return true;
        }
        log.error("请求失败! 请求参数：{} 返回结果! {}", requestBody, res);
        return isSuccess;
    }

    @NotNull
    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON); // 设置内容类型为JSON
        httpHeaders.set("Authorization", midjourneyConfig.getToken());
        httpHeaders.set("User-Agent", midjourneyConfig.getUserAage());
        httpHeaders.set("Cookie", MjConstants.HTTP_COOKIE);
        httpHeaders.set("Referer", String.format(HEADER_REFERER, midjourneyConfig.getGuildId(), midjourneyConfig.getChannelId()));
        return httpHeaders;
    }
}
