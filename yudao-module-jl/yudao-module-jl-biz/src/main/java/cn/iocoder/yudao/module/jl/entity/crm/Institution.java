package cn.iocoder.yudao.module.jl.entity.crm;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import lombok.*;
import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 机构/公司 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "Institution")
@Table(name = "jl_crm_institution")
public class Institution extends BaseEntity {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false )
    private Long id;

    /**
     * 省份
     */
    @Column(name = "province", nullable = false )
    private String province;

    /**
     * 城市
     */
    @Column(name = "city", nullable = false )
    private String city;

    /**
     * 名字
     */
    @Column(name = "name", nullable = false )
    private String name;

    /**
     * 详细地址
     */
    @Column(name = "address", nullable = false )
    private String address;

    /**
     * 备注信息
     */
    @Column(name = "mark")
    private String mark;

    /**
     * 机构类型枚举值
     */
    @Column(name = "type", nullable = false )
    private String type;

}
