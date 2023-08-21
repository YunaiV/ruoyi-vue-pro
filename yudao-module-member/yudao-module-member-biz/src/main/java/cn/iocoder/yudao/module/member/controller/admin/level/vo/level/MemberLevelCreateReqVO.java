package cn.iocoder.yudao.module.member.controller.admin.level.vo.level;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

// TODO @疯狂：项目的 vo 和 controller 不写 author 信息哈，只写 swagger 注解
/**
 * @author owen
 */
@Schema(description = "管理后台 - 会员等级创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberLevelCreateReqVO extends MemberLevelBaseVO {

}
