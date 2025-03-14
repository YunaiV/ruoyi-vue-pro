package cn.iocoder.yudao.module.wms.dal.dataobject.external.storage;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 外部存储库 DO
 * @author 李方捷
 * @table-fields : api_parameters,code,deleted,name,id,type,status
 */
@TableName("wms_external_storage")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_external_storage_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsExternalStorageDO extends BaseDO {

    /**
     * id
     */
    @TableId
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
     * 外部仓类型:1-三方,2-平台；
     */
    private Integer type;

    /**
     * JSON格式的对接需要的参数
     */
    private String apiParameters;

    private Boolean deleted;

    /**
     * 状态：0-不可用；1-可用
     */
    private Integer status;
}
