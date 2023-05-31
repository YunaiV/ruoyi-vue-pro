package cn.iocoder.yudao.module.jl.entity.crm;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import lombok.*;
import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 友商 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "Competitor")
@Table(name = "jl_crm_competitor")
public class Competitor extends BaseEntity {

    /**
     * 岗位ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false )
    private Long id;

    /**
     * 公司名
     */
    @Column(name = "name", nullable = false )
    private String name;

    /**
     * 联系人
     */
    @Column(name = "contact_name")
    private String contactName;

    /**
     * 手机号
     */
    @Column(name = "phone")
    private String phone;

    /**
     * 友商类型
     *
     * 枚举 {@link TODO competitor_type 对应的类}
     */
    @Column(name = "type")
    private String type;

    /**
     * 优势
     */
    @Column(name = "advantage")
    private String advantage;

    /**
     * 劣势
     */
    @Column(name = "disadvantage")
    private String disadvantage;

    /**
     * 备注
     */
    @Column(name = "mark")
    private String mark;

}
