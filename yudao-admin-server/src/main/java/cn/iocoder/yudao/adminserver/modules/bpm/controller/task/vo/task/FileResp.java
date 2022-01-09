package cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.task;

import lombok.Data;

// TODO @Li：1）改成 HighlightImgRespVO 吧。2）swagger 注解要补充；3）fileByte => fileContent
/**
 * 文件输出类
 *
 * @author yunlongn
 */
@Data
public class FileResp {

    /**
     * 文件名字
     */
    private String fileName;

    /**
     * 文件输出流
     */
    private byte[] fileByte;

}
