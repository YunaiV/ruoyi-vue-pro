package cn.iocoder.yudao.module.jl.entity.crm;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import lombok.*;
import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 销售线索中竞争对手的报价 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "SalesleadCompetitor")
@Table(name = "jl_crm_saleslead_competitor")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class SalesleadCompetitor extends BaseEntity {

    /**
     * 岗位ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false )
    private Long id;

    /**
     * 销售线索 id
     */
    @Column(name = "saleslead_id", nullable = false )
    private Long salesleadId;

    /**
     * 竞争对手
     */
    @Column(name = "competitor_id", nullable = false )
    private Long competitorId;

    /**
     * 竞争对手的报价
     */
    @Column(name = "competitor_quotation")
    private Long competitorQuotation;

}
