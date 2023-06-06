package cn.iocoder.yudao.module.jl.entity.laboratory;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import lombok.*;
import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 实验收费项 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "ChargeItem")
@Table(name = "jl_laboratory_chargeitem")
public class ChargeItem extends BaseEntity {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false )
    private Long id;

    /**
     * 收费类型
     */
    @Column(name = "type", nullable = false )
    private String type;

    /**
     * 名称
     */
    @Column(name = "name", nullable = false )
    private String name;

    /**
     * 成本价
     */
    @Column(name = "cost_price", nullable = false )
    private Long costPrice;

    /**
     * 建议销售价
     */
    @Column(name = "suggested_selling_price", nullable = false )
    private Long suggestedSellingPrice;

    /**
     * 备注
     */
    @Column(name = "mark")
    private String mark;

    /**
     * 收费的标准/规则
     */
    @Column(name = "fee_standard")
    private String feeStandard;

}
