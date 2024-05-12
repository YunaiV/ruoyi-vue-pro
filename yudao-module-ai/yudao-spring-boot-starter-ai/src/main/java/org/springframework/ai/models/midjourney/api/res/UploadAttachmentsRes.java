package org.springframework.ai.models.midjourney.api.res;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 上传附件 - res
 *
 * author: fansili
 * time: 2024/4/8 13:32
 */
@Data
@Accessors(chain = true)
public class UploadAttachmentsRes {

    private List<Attachment> attachments;

    @Data
    @Accessors(chain = true)
    public static class Attachment {
        /**
         * 附件的ID。
         */
        private int id;
        /**
         * 附件的上传URL。
         */
        private String uploadUrl;
        /**
         * 上传到服务器的文件名。
         */
        private String uploadFilename;
    }
}
