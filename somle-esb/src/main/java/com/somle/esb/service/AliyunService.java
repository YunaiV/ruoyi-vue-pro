package com.somle.esb.service;

import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
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
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import static cn.iocoder.yudao.module.system.enums.esb.EsbChannels.DATA_CHANNEL;

@Service
public class AliyunService {

    @Autowired
    AliyunTokenRepository tokenRepository;

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.bucketName}")
    private String bucketName;


    private OSS ossClient ;

        
    @PostConstruct
    private void init() {
        AliyunToken token = tokenRepository.findAll().get(0);
        ossClient = new OSSClientBuilder().build(endpoint, token.getAccessKeyId(), token.getAccessKeySecret());
    }

    @ServiceActivator(inputChannel = DATA_CHANNEL)
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
}
