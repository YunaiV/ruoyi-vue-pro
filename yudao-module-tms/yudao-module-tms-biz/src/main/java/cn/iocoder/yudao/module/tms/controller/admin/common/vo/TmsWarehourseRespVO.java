package cn.iocoder.yudao.module.tms.controller.admin.common.vo;

import lombok.Data;

@Data
public class TmsWarehourseRespVO {
    private Long id;


    /**
     * 代码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 外部存储ID
     */
    private Long externalStorageId;
}
