package cn.iocoder.yudao.module.jl.entity.project;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 项目款项 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "ProjectFund")
@Table(name = "jl_project_fund")
public class ProjectFund extends BaseEntity {

    /**
     * 岗位ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false )
    private Long id;

    /**
     * 名称
     */
    @Column(name = "name", nullable = false )
    private String name;

    /**
     * 资金额度
     */
    @Column(name = "price", nullable = false )
    private Long price;

    /**
     * 项目 id
     */
    @Column(name = "project_id", nullable = false )
    private Long projectId;

    /**
     * 支付状态(未支付，部分支付，完全支付)
     */
    @Column(name = "status")
    private String status;

    /**
     * 支付时间
     */
    @Column(name = "paid_time")
    private LocalDateTime paidTime;

    /**
     * 支付的截止时间
     */
    @Column(name = "deadline")
    private LocalDate deadline;

    /**
     * 支付凭证上传地址
     */
    @Column(name = "receipt_url")
    private String receiptUrl;

    /**
     * 支付凭证文件名称
     */
    @Column(name = "receipt_name")
    private String receiptName;

    /**
     * 排序
     */
    @Column(name = "sort")
    private Integer sort;

}
