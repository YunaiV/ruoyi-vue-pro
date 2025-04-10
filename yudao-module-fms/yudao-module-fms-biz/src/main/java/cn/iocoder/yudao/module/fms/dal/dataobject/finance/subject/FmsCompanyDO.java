package cn.iocoder.yudao.module.fms.dal.dataobject.finance.subject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Fms财务公司 DO
 *
 * @author 王岽宇
 */
@TableName("fms_company")
@KeySequence("fms_company_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsCompanyDO extends BaseDO {
    //公司名称
    private String companyName;
    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 乐观锁
     */
    private Integer revision;
    /**
     * 主体名称
     */
    private String name;
    /**
     * 联系人
     */
    private String contact;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 联系电话
     */
    private String telephone;
    /**
     * 电子邮箱
     */
    private String email;
    /**
     * 传真
     */
    private String fax;
    /**
     * 送达地址
     */
    private String deliveryAddress;
    /**
     * 公司地址
     */
    private String companyAddress;
    /**
     * 备注
     */
    private String remark;
    /**
     * 开启状态 (0-关闭, 1-开启)
     * <p>
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum common_boolean_status 对应的类}
     */
    private Boolean status;
    /**
     * 纳税人识别号
     */
    private String taxNo;
    /**
     * 开户行
     */
    private String bankName;
    /**
     * 开户账号
     */
    private String bankAccount;
    /**
     * 开户地址
     */
    private String bankAddress;

}