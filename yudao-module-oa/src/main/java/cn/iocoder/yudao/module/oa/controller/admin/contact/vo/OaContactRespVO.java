package cn.iocoder.yudao.module.oa.controller.admin.contact.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OA 外部联系人 Response VO")
@Data
public class OaContactRespVO {

    @Schema(description = "联系人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "创建人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long ownerUserId;

    @Schema(description = "创建人用户昵称", example = "芋道源码")
    private String ownerUserName;

    @Schema(description = "分类编号", example = "1024")
    private Long categoryId;

    @Schema(description = "分类名称", example = "客户")
    private String categoryName;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String name;

    @Schema(description = "姓名拼音", example = "zhangsan")
    private String pinyin;

    @Schema(description = "性别", example = "1")
    private Integer sex;

    @Schema(description = "手机号码", example = "13800138000")
    private String mobile;

    @Schema(description = "邮箱", example = "yudao@example.com")
    private String email;

    @Schema(description = "地址", example = "上海市浦东新区世纪大道 100 号")
    private String address;

    @Schema(description = "公司名称", example = "芋道科技有限公司")
    private String companyName;

    @Schema(description = "公司电话", example = "021-12345678")
    private String companyPhone;

    @Schema(description = "头像地址", example = "https://example.com/image.png")
    private String avatar;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    private String remark;

    @Schema(description = "共享记录列表")
    private List<Share> shares;

    @Schema(description = "当前行的共享关系，仅我共享的列表返回")
    private Share share;

    @Schema(description = "分享给当前用户的共享人昵称", example = "芋道源码")
    private String sharerName;

    @Schema(description = "当前接收人的处理状态", example = "false")
    private Boolean handleStatus;

    @Schema(description = "当前接收人的分类编号", example = "1024")
    private Long sharedCategoryId;

    @Schema(description = "当前接收人的分类名称", example = "客户联系人")
    private String sharedCategoryName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

    @Schema(description = "管理后台 - OA 联系人共享 Response VO")
    @Data
    public static class Share {

        @Schema(description = "共享记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "共享接收人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long userId;

        @Schema(description = "实际共享人昵称", example = "张三")
        private String creatorName;

        @Schema(description = "共享接收人用户昵称", example = "芋道源码")
        private String userName;

        @Schema(description = "共享接收人用户头像", example = "https://example.com/avatar.png")
        private String userAvatar;

        @Schema(description = "分类编号", example = "1024")
        private Long categoryId;

        @Schema(description = "接收人的分类名称", example = "客户")
        private String categoryName;

        @Schema(description = "处理状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
        private Boolean handleStatus;

        @Schema(description = "共享时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "1789347600000")
        private LocalDateTime createTime;

    }

}
