package cn.iocoder.yudao.module.report.dal.dataobject.statisticsdata;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Date;

/**
 * 通用统计数据 DO
 *
 * @author 芋道源码
 */
@TableName("report_ekyc_face_check")
@KeySequence("report_ekyc_face_check_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EkycFaceCheckDO extends BaseDO {

    @TableId
    private Long id; // 主键

    private String customerId; // 用户ID

    private String loginName; // 用户名

    private String firstName; // 首名

    private String middleName = ""; // 中间名

    private String lastName; // 尾名

    private String birthday = ""; // 生日

    private String idFrontImg = ""; // 证件证明照

    private String ekycFaceImg = ""; // ekyc自拍照

    private String faceImg = ""; // 自拍照

    private String billNo = ""; // 业务编号

    private String captureTransactionId = ""; // zoloz人脸识别业务编号

    private String compareTransactionId = ""; // zoloz人脸对比业务编号

    private Byte status = 0; // 状态

    private String compareFaceScore = ""; // 人脸对比分数

    private Date createDate; // 创建时间

    private String createBy = ""; // 创建人

    private Date updateDate; // 更新时间

    private String updateBy = ""; // 更新人


    private String channel = ""; // 渠道

    private String domainName = ""; // 域名

    private String remark = ""; // 备注

    private String lineageName = ""; // 新四级血缘名称


    private String updater = ""; // 更新者


}
