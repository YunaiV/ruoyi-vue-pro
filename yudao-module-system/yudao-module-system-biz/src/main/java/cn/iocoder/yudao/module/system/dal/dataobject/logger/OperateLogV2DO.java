package cn.iocoder.yudao.module.system.dal.dataobject.logger;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志表 V2
 *
 * @author 芋道源码
 */
@TableName(value = "system_operate_log_v2", autoResultMap = true)
@KeySequence("system_operate_log_seq_v2") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
public class OperateLogV2DO extends BaseDO {

    /**
     * 日志主键
     */
    @TableId
    private Long id;
    /**
     * 链路追踪编号
     *
     * 一般来说，通过链路追踪编号，可以将访问日志，错误日志，链路追踪日志，logger 打印日志等，结合在一起，从而进行排错。
     */
    private String traceId;
    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 属性，或者 AdminUserDO 的 id 属性
     */
    private Long userId;
    /**
     * 用户类型
     *
     * 关联 {@link  UserTypeEnum}
     */
    private Integer userType;
    // TODO @puhui999：module 改成 type，name 改成 subType；
    /**
     * 操作模块
     */
    private String module;
    /**
     * 操作名
     */
    private String name;
    /**
     * 操作模块业务编号
     */
    private Long bizId;
    /**
     * 操作内容，记录整个操作的明细
     *
     * 例如说，修改编号为 1 的用户信息，将性别从男改成女，将姓名从芋道改成源码。
     */
    private String content;
    /**
     * 拓展字段，有些复杂的业务，需要记录一些字段 ( JSON 格式 )
     *
     * 例如说，记录订单编号，{ orderId: "1"}
     */
    // TODO @puhui999：看看能不能类似 exts 搞 json 格式；
    private String extra;
    /**
     * 请求方法名
     */
    private String requestMethod;
    /**
     * 请求地址
     */
    private String requestUrl;
    /**
     * 用户 IP
     */
    private String userIp;
    /**
     * 浏览器 UA
     */
    private String userAgent;

    // TODO @芋艿：requestUrl、requestMethod
    // TODO @芋艿：javaMethod、javaMethodArgs
    // TODO @芋艿：startTime、duration
    // TODO @芋艿：resultMsg、resultData

}
