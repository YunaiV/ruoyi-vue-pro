package com.somle.esb.service;

import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsV2Result;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.somle.esb.model.AliyunToken;
import com.somle.esb.model.OssData;
import com.somle.esb.repository.AliyunTokenRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

@Service
public class AliyunService {

    @Autowired
    AliyunTokenRepository tokenRepository;

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.bucketName}")
    private String bucketName;

    private OSS ossClient;

    @PostConstruct
    private void init() {
        AliyunToken token = tokenRepository.findAll().get(0);
        ossClient = new OSSClientBuilder().build(endpoint, token.getAccessKeyId(), token.getAccessKeySecret());
    }

    @ServiceActivator(inputChannel = "dataChannel")
    @Order(2)
    public void storeOss(Message<OssData> message) throws IOException {

        OssData data = message.getPayload();
        String jsonString = JsonUtilsX.toJsonString(data);
        byte[] compressedContent = compressString(jsonString);

        String database = data.getDatabase();
        String tableNameFull = data.getTableName() + "_" + data.getSyncType();
        String folderDate = data.getFolderDate().toString();
        // String folderDate = data.getTimestamp().toLocalDateTime().atZone(ZoneId.of("UTC+8")).toLocalDate().format(DateTimeFormatter.ISO_DATE);
        String fileDir = String.format("%s/api/%s/%s/", database, tableNameFull, folderDate);
        String fileName = tableNameFull + "_" + UUID.randomUUID() + ".txt.gz";

        String ossFilePath = "dataworks/origin_data/" + fileDir + fileName;

        ossClient.putObject(bucketName, ossFilePath, new ByteArrayInputStream(compressedContent));
    }

    private byte[] compressString(String str) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(str.getBytes(StandardCharsets.UTF_8));
        }
        return byteArrayOutputStream.toByteArray();
    }

    // 下载 OSS 文件并返回输入流
    public InputStream downloadOssFileToStream(String filePath) {
        // 获取 OSS 文件对象
        OSSObject ossObject = ossClient.getObject(bucketName, filePath);
        // 返回文件内容的输入流
        return ossObject.getObjectContent();
    }

    //根据文件路径获取文件夹下的oss文件,返回文件路径集合
    public List<String> getOssFileByPath(String keyPrefix) {
        // 列举文件。如果不设置keyPrefix，则列举存储空间下的所有文件。如果设置keyPrefix，则列举包含指定前缀的文件。
        ListObjectsV2Result result = ossClient.listObjectsV2(bucketName, keyPrefix);
        List<OSSObjectSummary> objectSummaries = result.getObjectSummaries();
        //过滤size==0的
        return objectSummaries.stream().filter(ossObjectSummary -> ossObjectSummary.getSize() != 0)
            .map(OSSObjectSummary::getKey).toList();
    }
}
