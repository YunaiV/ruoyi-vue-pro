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
public class TdResponse {

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_TB_NOT_EXIST = 866;

    private String status;

    private int code;

    private String desc;

    //[["time","TIMESTAMP",8,""],["powerstate","TINYINT",1,""],["brightness","INT",4,""],["deviceid","NCHAR",32,"TAG"]]
    private List<Object[]> data;

}
