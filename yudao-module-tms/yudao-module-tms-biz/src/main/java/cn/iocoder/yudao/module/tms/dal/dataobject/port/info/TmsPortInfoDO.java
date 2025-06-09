package cn.iocoder.yudao.module.tms.dal.dataobject.port.info;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * TMS港口信息 DO
 *
 * @author wdy
 */
@TableName("tms_port")
@KeySequence("tms_port_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsPortInfoDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 乐观锁
     */
    private Integer version;
    /**
     * 港口编码
     */
    private String code;
    /**
     * 港口中文名
     */
    private String name;
    /**
     * 港口英文名
     */
    private String nameEn;
    /**
     * 国家代码(字典)
     */
    private Integer countryCode;
    /**
     * 国家描述
     */
    private String countryName;
    /**
     * 城市中文名
     */
    private String cityName;
    /**
     * 城市英文名
     */
    private String cityNameEn;
    /**
     * 备注
     */
    private String remark;
    /**
     * 启用/禁用状态
     */
    private Boolean status;

}