package cn.iocoder.yudao.module.ai.dal.dataobject.image;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

// TODO @fan：@time、@sine 注解可以不加哈
/**
 * AI 绘画 DO
 *
 * @author fansili
 * @time 2024/4/25 15:53
 * @since 1.0
 */
@Data
@Accessors(chain = true) // TODO @fan：这个不用添加哈，全局的 lombok.config 搞啦
@TableName("ai_image") // TODO @fan：建议放在 @Data 前面，因为它是关键注解，@Data 本质可悲替代
public class AiImageDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "提示词")
    private String prompt;

    // TODO @fan：要加一个平台字段；platform；因为一个平台，会有多种 model 模型；

    @Schema(description = "模型")
    private String model;

    // TODO @fan：这个记录到 db，要不改成 width、height？更明确的尺寸
    @Schema(description = "尺寸大小")
    private String size;

    // TODO @fan：style 是所有模型都有么？如果不是，是不是靠 drawRequest 记录下就好了；
    @Schema(description = "风格")
    private String style;

    @Schema(description = "图片地址(自己服务器)")
    private String picUrl;

    @Schema(description = "绘画状态：提交、排队、绘画中、绘画完成、绘画失败")
    private String status;

    @Schema(description = "绘画图片地址(绘画好的服务器)")
    private String originalPicUrl;

    @Schema(description = "绘画错误信息")
    private String errorMessage;

    @Schema(description = "是否发布")
    private String publicStatus;

    // TODO @fan：增加一个 Map<String, Object> 字段，drawRequest；用于记录请求模型的字段；对应的就是 ImageOptions
    // TODO @fan：增加一个 Map<String, Object> 字段，记录 ImageResponseMetadata？这样 mjNonceId、mjOperationId、mjOperationName、mjOperations 这些字段，貌似可以收掉

    // ============ mj 需要字段

    @Schema(description = "用户操作的Nonce编号(MJ返回)")
    private String mjNonceId;

    @Schema(description = "用户操作的操作编号(MJ返回)")
    private String mjOperationId;

    @Schema(description = "用户操作的操作名字(MJ返回)")
    private String mjOperationName;

    @Schema(description = "mj图片生产成功保存的 components json 数组")
    private String mjOperations;

}

