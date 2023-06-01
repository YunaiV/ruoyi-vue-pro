package cn.iocoder.yudao.module.jl.entity.project;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 项目管理 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "Project")
@Table(name = "jl_project_base")
public class Project extends BaseEntity {

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
     * 项目名字
     */
    @Column(name = "name", nullable = false )
    private String name;

    /**
     * 项目开展阶段
     */
    @Column(name = "stage")
    private String stage;

    /**
     * 项目状态
     */
    @Column(name = "status")
    private String status;

    /**
     * 项目类型
     */
    @Column(name = "type", nullable = false )
    private String type;

    /**
     * 启动时间
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * 截止时间
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * 项目负责人
     */
    @Column(name = "manager_id")
    private Long managerId;

    /**
     * 参与者 ids，数组
     */
    @Column(name = "participants")
    private String participants;

    /**
     * 销售 id
     */
    @Column(name = "sales_id")
    private Long salesId;

    /**
     * 销售 id
     */
    @Column(name = "customer_id")
    private Long customerId;

}
