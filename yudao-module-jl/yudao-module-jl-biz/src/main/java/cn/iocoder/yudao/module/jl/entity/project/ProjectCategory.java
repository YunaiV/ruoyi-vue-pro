package cn.iocoder.yudao.module.jl.entity.project;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 项目的实验名目 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "ProjectCategory")
@Table(name = "jl_project_category")
public class ProjectCategory extends BaseEntity {

    /**
     * 岗位ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false )
    private Long id;

    /**
     * 报价 id
     */
    @Column(name = "quote_id")
    private Long quoteId;

    /**
     * 安排单 id
     */
    @Column(name = "schedule_id")
    private Long scheduleId;

    /**
     * 类型，报价/安排单
     */
    @Column(name = "type", nullable = false )
    private String type;

    /**
     * 名目的实验类型，动物/细胞/分子等
     */
    @Column(name = "category_type")
    private String categoryType;

    /**
     * 实验名目库的名目 id
     */
    @Column(name = "category_id", nullable = false )
    private Long categoryId;

    /**
     * 实验人员
     */
    @Column(name = "operator_id")
    private Long operatorId;

    /**
     * 客户需求
     */
    @Column(name = "demand")
    private String demand;

    /**
     * 干扰项
     */
    @Column(name = "interference")
    private String interference;

    /**
     * 依赖项(json数组多个)
     */
    @Column(name = "depend_ids")
    private String dependIds;

    /**
     * 实验名目名字
     */
    @Column(name = "name", nullable = false )
    private String name;

    /**
     * 备注
     */
    @Column(name = "mark")
    private String mark;

    @ManyToOne
    @JoinColumn(name="schedule_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonBackReference
    private ProjectSchedule schedule;

    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonBackReference
    @JoinColumn(name="quote_id", insertable = false, updatable = false)
    private ProjectQuote quote;

    /**
     * 实验物资
     */
    @OneToMany(mappedBy="category", fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    @Fetch(FetchMode.SUBSELECT)
    @JsonManagedReference
    private List<ProjectSupply> supplyList;

    /**
     * 实验收费项
     */
    @OneToMany(mappedBy="category", fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    @Fetch(FetchMode.JOIN)
    @JsonManagedReference
    private List<ProjectChargeitem> chargeList;

    /**
     * 实验SOP
     */
    @OneToMany(mappedBy="category", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JsonManagedReference
    private List<ProjectSop> sopList;

}
