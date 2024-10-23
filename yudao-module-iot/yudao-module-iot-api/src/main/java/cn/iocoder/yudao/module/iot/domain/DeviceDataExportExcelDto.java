package cn.iocoder.yudao.module.iot.domain;

import lombok.Data;

/**
 * 设备数据导出 excel DTO
 */
@Data
public class DeviceDataExportExcelDto {

    /**
     * 设备标识
     */
    private String deviceKey;

    /**
     * 导出形式 1 单个参数导出 2 全部参数导出
     */
    private String exportType;

    /**
     * 导出开始时间
     */
    private String exportBeginTime;

    /**
     * 导出结束时间
     */
    private String exportEndTime;

    /**
     * 导出参数，空则导出全部
     */
    private String exportParameter;
}
