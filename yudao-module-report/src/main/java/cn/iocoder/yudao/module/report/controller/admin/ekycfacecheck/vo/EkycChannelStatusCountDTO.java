package cn.iocoder.yudao.module.report.controller.admin.ekycfacecheck.vo;

import lombok.Data;

@Data
public class EkycChannelStatusCountDTO {
    private String date;
    private Byte status;
    private Integer count;
}
