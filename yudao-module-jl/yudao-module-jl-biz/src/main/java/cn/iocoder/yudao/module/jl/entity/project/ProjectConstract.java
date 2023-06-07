package cn.iocoder.yudao.module.jl.entity.project;

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
 * 项目合同 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "ProjectConstract")
@Table(name = "jl_project_constract")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class ProjectConstract extends BaseEntity {

    /**
     * 岗位ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false )
    private Long id;

    /**
     * 项目 id
     */
    @Column(name = "project_id", nullable = false )
    private Long projectId;

    /**
     * 合同名字
     */
    @Column(name = "name")
    private String name;

    /**
     * 合同文件 URL
     */
    @Column(name = "file_url", nullable = false )
    private String fileUrl;

    /**
     * 合同状态：起效、失效、其它
     */
    @Column(name = "status")
    private String status;

    /**
     * 合同类型
     */
    @Column(name = "type")
    private String type;

    /**
     * 合同金额
     */
    @Column(name = "price")
    private Long price;

    /**
     * 签订销售人员
     */
    @Column(name = "sales_id")
    private Long salesId;

    /**
     * 合同编号
     */
    @Column(name = "sn")
    private String sn;

    /**
     * 合同文件名
     */
    @Column(name = "file_name")
    private String fileName;

}
