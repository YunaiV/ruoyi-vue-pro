package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import cn.iocoder.yudao.module.jl.controller.admin.join.vo.JoinSaleslead2competitorRespVO;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.JoinSaleslead2customerplanRespVO;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.JoinSaleslead2managerRespVO;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.JoinSaleslead2reportRespVO;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2customerplanDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 销售线索 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesleadRespVO extends SalesleadBaseVO {
    @Schema(description = "岗位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26580")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "竞争对手的报价，数组", defaultValue = "[]")
    private List<JoinSaleslead2competitorRespVO> competitorQuotations;

    @Schema(description = "项目售前人员列表", defaultValue = "[]")
    private List<JoinSaleslead2managerRespVO> managers;

    @Schema(description = "上传的客户方案列表", defaultValue = "[]")
    private List<JoinSaleslead2customerplanRespVO> customerPlans;

    @Schema(description = "上传的报告列表", defaultValue = "[]")
    private List<JoinSaleslead2reportRespVO> reports;
}
