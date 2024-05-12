package org.springframework.ai.models.midjourney.api.req;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * describe
 *
 * author: fansili
 * time: 2024/4/7 12:30
 */
@Data
@Accessors(chain = true)
public class DescribeReq {

    /**
     * 文件名字
     */
    private String fileName;
    /**
     * UploadAttachmentsRes 里面的 finalFileName
     */
    private String finalFileName;
}
