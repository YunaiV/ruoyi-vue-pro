package cn.iocoder.yudao.module.jl.entity.laboratory;

import cn.iocoder.yudao.module.jl.entity.BaseEntity;
import cn.iocoder.yudao.module.jl.entity.user.User;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 实验名目 Entity
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "Category")
@Table(name = "jl_laboratory_category")
public class Category extends BaseEntity {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false )
    private Long id;

    /**
     * 名字
     */
    @Column(name = "name", nullable = false )
    private String name;

    /**
     * 技术难度
     */
    @Column(name = "difficulty_level")
    private String difficultyLevel;

    /**
     * 重要备注说明
     */
    @Column(name = "mark")
    private String mark;

    /**
     * 类型
     */
    @Column(name = "type", nullable = false )
    private String type;

    /**
     * 历史操作次数
     */
    @Column(name = "action_count" )
    private Integer actionCount = 0;

    /**
     * 实验名目的擅长人员
     */
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinTable(
            name = "jl_laboratory_category_skilluser",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> skillUsers = new HashSet<>();

}
