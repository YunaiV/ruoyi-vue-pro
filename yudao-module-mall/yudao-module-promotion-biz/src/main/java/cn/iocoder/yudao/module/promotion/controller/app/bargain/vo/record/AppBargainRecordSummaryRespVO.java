package cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 砍价记录的简要概括 Response VO")
@Data
public class AppBargainRecordSummaryRespVO {

    @Schema(description = "砍价用户数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer successUserCount;

    @Schema(description = "成功砍价的记录", requiredMode = Schema.RequiredMode.REQUIRED) // 只返回最近的 7 个
    private List<Record> successList;

    @Schema(description = "成功砍价记录")
    @Data
    public static class Record {

        @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王**")
        private String nickname;

        @Schema(description = "用户头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xxx.jpg")
        private String avatar;

        @Schema(description = "活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "天蚕土豆")
        private String activityName;

    }

}
