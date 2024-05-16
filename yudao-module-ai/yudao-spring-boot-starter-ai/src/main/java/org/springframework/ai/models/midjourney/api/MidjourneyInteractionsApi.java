package org.springframework.ai.models.midjourney.api;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.ai.models.midjourney.MidjourneyConfig;
import org.springframework.ai.models.midjourney.api.req.AttachmentsReq;
import org.springframework.ai.models.midjourney.api.req.DescribeReq;
import org.springframework.ai.models.midjourney.api.req.ReRollReq;
import org.springframework.ai.models.midjourney.api.res.UploadAttachmentsRes;
import org.springframework.ai.models.midjourney.util.MidjourneyUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;

// TODO @fansili：按照 spring ai 的封装习惯，这个类是不是 MidjourneyApi
/**
 * 图片生成
 *
 * author: fansili
 * time: 2024/4/3 17:36
 */
@Slf4j
public class MidjourneyInteractionsApi extends MidjourneyInteractions {

    private final String url;
    private final RestTemplate restTemplate = new RestTemplate(); // TODO @fansili：优先级低：后续搞到统一的管理

    public MidjourneyInteractionsApi(MidjourneyConfig midjourneyConfig) {
        super(midjourneyConfig);
        this.url = midjourneyConfig.getServerUrl().concat(midjourneyConfig.getApiInteractions());
    }

    public Boolean imagine(String nonce, String prompt) {
        // 获取请求模板
        String requestTemplate = midjourneyConfig.getRequestTemplates().get("imagine");
        // 设置参数
        HashMap<String, String> requestParams = getDefaultParams();
        requestParams.put("nonce", nonce);
        requestParams.put("prompt", prompt);
        // 解析 template 参数占位符
        String requestBody = MidjourneyUtil.parseTemplate(requestTemplate, requestParams);
        // 获取 header
        HttpHeaders httpHeaders = getHeadersOfAppJson();
        // 发送请求
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, httpHeaders);
        String res = restTemplate.postForObject(url, requestEntity, String.class);
        // 这个 res 只要不返回值，就是成功!
        // TODO @fansili：可以直接 if (StrUtil.isBlank(res))
        if (StrUtil.isBlank(res)) {
            return true;
        } else {
            log.error("请求失败! 请求参数：{} 返回结果! {}", requestBody, res);
            return false;
        }
    }
    // TODO done @fansili：方法和方法之间，空一行哈；


    public Boolean reRoll(ReRollReq reRoll) {
        // 获取请求模板
        String requestTemplate = midjourneyConfig.getRequestTemplates().get("reroll");
        // 设置参数
        HashMap<String, String> requestParams = getDefaultParams();
        requestParams.put("custom_id", reRoll.getCustomId());
        requestParams.put("message_id", reRoll.getMessageId());
        // 获取 header
        HttpHeaders httpHeaders = getHeadersOfAppJson();
        // 设置参数
        String requestBody = MidjourneyUtil.parseTemplate(requestTemplate, requestParams);
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

    // TODO @fansili：搞成私有方法，可能会好点；
    public UploadAttachmentsRes uploadAttachments(AttachmentsReq attachments) {
        // file
        JSONObject fileObj = new JSONObject();
        fileObj.put("id", "0");
        fileObj.put("filename", attachments.getFileSystemResource().getFilename());
        // TODO @fansili：这块用 lombok 哪个异常处理，简化下代码；
        try {
            fileObj.put("file_size", attachments.getFileSystemResource().contentLength());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 创建用于存放表单数据的MultiValueMap
        MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();
        multipartRequest.put("files", Lists.newArrayList(fileObj));
        // 设置header值
        HttpHeaders httpHeaders = getHeadersOfAppJson();
        // 创建HttpEntity对象，包含表单数据和头部信息
        HttpEntity<MultiValueMap<String, Object>> multiValueMapHttpEntity = new HttpEntity<>(multipartRequest, httpHeaders);
        // 发送POST请求并接收响应
        String uri = String.format(midjourneyConfig.getApiAttachments(), midjourneyConfig.getChannelId());
        String response = restTemplate.postForObject(midjourneyConfig.getServerUrl().concat(uri), multiValueMapHttpEntity, String.class);
        UploadAttachmentsRes uploadAttachmentsRes = JSON.parseObject(response, UploadAttachmentsRes.class);

        //
        // 上传文件
        String uploadUrl = uploadAttachmentsRes.getAttachments().getFirst().getUploadUrl();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<FileSystemResource> fileSystemResourceHttpEntity = new HttpEntity<>(attachments.getFileSystemResource(), httpHeaders);
        ResponseEntity<String> exchange = restTemplate.exchange(uploadUrl, HttpMethod.PUT, fileSystemResourceHttpEntity, String.class);
        String uploadRes = exchange.getBody();
        return uploadAttachmentsRes;
    }

    public Boolean describe(DescribeReq describe) {
        // 获取请求模板
        String requestTemplate = midjourneyConfig.getRequestTemplates().get("describe");
        // 设置参数
        HashMap<String, String> requestParams = getDefaultParams();
        requestParams.put("file_name", describe.getFileName());
        requestParams.put("final_file_name", describe.getFinalFileName());
        // 设置 header
        HttpHeaders httpHeaders = getHeadersOfFormData();
        String requestBody = MidjourneyUtil.parseTemplate(requestTemplate, requestParams);
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

}
