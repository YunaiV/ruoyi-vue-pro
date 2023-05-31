package cn.iocoder.yudao.module.jl.entity.crm;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import lombok.*;
import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 销售跟进 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "Followup")
@Table(name = "jl_crm_followup")
public class Followup extends BaseEntity {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false )
    private Long id;

    /**
     * 内容
     */
    @Column(name = "content", nullable = false )
    private String content;

    /**
     * 客户id
     */
    @Column(name = "customer_id", nullable = false )
    private Long customerId;

    /**
     * 跟进实体的 id，项目、线索、款项，客户等
     */
    @Column(name = "ref_id")
    private Long refId;

    /**
     * 跟进类型：日常联系、销售线索、催款等
     */
    @Column(name = "type", nullable = false )
    private String type;

}
