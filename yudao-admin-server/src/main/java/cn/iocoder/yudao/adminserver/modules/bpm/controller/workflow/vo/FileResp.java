package cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo;

import lombok.Data;

/**
 * 文件输出类
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
