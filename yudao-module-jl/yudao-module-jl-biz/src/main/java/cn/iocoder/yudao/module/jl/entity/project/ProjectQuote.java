package cn.iocoder.yudao.module.jl.entity.project;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import lombok.*;
import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 项目报价 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "ProjectQuote")
@Table(name = "jl_project_quote")
public class ProjectQuote extends BaseEntity {

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
    @Column(name = "saleslead_id")
    private Long salesleadId;

    /**
     * 项目 id
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * 报价单的名字
     */
    @Column(name = "name", nullable = false )
    private String name;

    /**
     * 方案 URL
     */
    @Column(name = "report_url", nullable = false )
    private String reportUrl;

    /**
     * 折扣(100: 无折扣, 98: 98折)
     */
    @Column(name = "discount", nullable = false )
    private Integer discount;

    /**
     * 状态, 已提交、已作废、已采用
     */
    @Column(name = "status", nullable = false )
    private String status;

}
