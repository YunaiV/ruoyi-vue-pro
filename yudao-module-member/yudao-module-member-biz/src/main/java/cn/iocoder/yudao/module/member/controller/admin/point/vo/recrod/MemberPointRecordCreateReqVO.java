package cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 用户积分记录创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberPointRecordCreateReqVO extends MemberPointRecordBaseVO {

}
