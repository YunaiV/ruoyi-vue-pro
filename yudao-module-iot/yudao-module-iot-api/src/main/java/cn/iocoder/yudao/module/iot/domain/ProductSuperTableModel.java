package cn.iocoder.yudao.module.iot.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Optional;

@Data
public class ProductSuperTableModel {
    private static final long serialVersionUID = 1L;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Timestamp ts;

    private String superTableName;

    /**
     * columnsName,columnsProperty
     */
    private HashMap<Optional, Optional> columns;

    /**
     * tagsName,tagsProperty
     */
    private HashMap<Optional, Optional> tags;

}
