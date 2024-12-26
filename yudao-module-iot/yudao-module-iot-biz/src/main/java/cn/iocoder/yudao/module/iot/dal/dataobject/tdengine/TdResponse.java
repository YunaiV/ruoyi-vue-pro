package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TdResponse 类用于处理 TDengine 的响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class TdResponse {

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_TB_NOT_EXIST = 866;

    /**
     * 状态
     */
    private String status;

    /**
     * 错误码
     */
    private int code;

    /**
     * 错误信息
     */
    private String desc;

    /**
     * 列信息
     * [["time","TIMESTAMP",8,""],["powerstate","TINYINT",1,""],["brightness","INT",4,""],["deviceid","NCHAR",32,"TAG"]]
     */
    private List data;

}