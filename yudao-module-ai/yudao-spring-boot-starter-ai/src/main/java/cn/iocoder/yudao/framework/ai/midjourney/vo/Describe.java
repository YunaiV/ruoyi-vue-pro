package cn.iocoder.yudao.framework.ai.midjourney.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;

/**
 * describe
 *
 * author: fansili
 * time: 2024/4/7 12:30
 */
@Data
@Accessors(chain = true)
public class Describe {

    /**
     * 文件名字
     */
    private String fileName;
    /**
     * UploadAttachmentsRes 里面的 finalFileName
     */
    private String finalFileName;
}
