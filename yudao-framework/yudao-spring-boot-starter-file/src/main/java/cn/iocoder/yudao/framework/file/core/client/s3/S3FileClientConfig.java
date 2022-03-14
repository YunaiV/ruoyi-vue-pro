package cn.iocoder.yudao.framework.file.core.client.s3;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.file.core.client.FileClientConfig;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

/**
 * S3 文件客户端的配置类
 *
 * @author 芋道源码
 */
@Data
public class S3FileClientConfig implements FileClientConfig {

    public static final String ENDPOINT_QINIU = "qiniucs.com";

    /**
     * 节点地址
     * 1. MinIO：
     * 2. 阿里云：https://help.aliyun.com/document_detail/31837.html
     * 3. 腾讯云：
     * 4. 七牛云：https://developer.qiniu.com/kodo/4088/s3-access-domainname
     * 5. 华为云：
     */
    @NotNull(message = "endpoint 不能为空")
    private String endpoint;
    /**
     * 自定义域名
     * 1. MinIO：
     * 2. 阿里云：https://help.aliyun.com/document_detail/31836.html
     * 3. 腾讯云：https://cloud.tencent.com/document/product/436/11142
     * 4. 七牛云：https://developer.qiniu.com/kodo/8556/set-the-custom-source-domain-name
     * 5. 华为云：
     */
    @URL(message = "domain 必须是 URL 格式")
    private String domain;
    /**
     * 区域
     * 1. MinIO：
     * 2. 阿里云：https://help.aliyun.com/document_detail/31837.html
     * 3. 腾讯云：
     * 4. 七牛云：https://developer.qiniu.com/kodo/4088/s3-access-domainname
     * 5. 华为云：
     */
    @NotNull(message = "region 不能为空")
    private String region;
    /**
     * 存储 Bucket
     */
    @NotNull(message = "bucket 不能为空")
    private String bucket;

    /**
     * 访问 Key
     * 1. MinIO：
     * 2. 阿里云：
     * 3. 腾讯云：https://console.cloud.tencent.com/cam/capi
     * 4. 七牛云：https://portal.qiniu.com/user/key
     * 5. 华为云：
     */
    @NotNull(message = "accessKey 不能为空")
    private String accessKey;
    /**
     * 访问 Secret
     */
    @NotNull(message = "accessSecret 不能为空")
    private String accessSecret;

    @AssertTrue(message = "domain 不能为空")
    @SuppressWarnings("RedundantIfStatement")
    public boolean isDomainValid() {
        // 如果是七牛，必须带有 domain
        if (endpoint.contains(ENDPOINT_QINIU) && StrUtil.isEmpty(domain)) {
            return false;
        }
        return true;
    }

}
