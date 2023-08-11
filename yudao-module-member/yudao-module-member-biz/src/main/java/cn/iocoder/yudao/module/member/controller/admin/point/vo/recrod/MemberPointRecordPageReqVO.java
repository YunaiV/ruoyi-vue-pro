package cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

import java.util.List;

@Schema(description = "管理后台 - 用户积分记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberPointRecordPageReqVO extends PageParam {

    @Schema(description = "用户昵称", example = "张三")
    private String nickName;

    //用户id集合，用户将nickName查询转换成user_ids查询
    private List<Long> userIds;

    @Schema(description = "业务类型", example = "1")
    private String bizType;

    @Schema(description = "积分标题")
    private String title;

    @Schema(description = "状态：1-订单创建，2-冻结期，3-完成，4-失效（订单退款） ", example = "1")
    private Integer status;


}
