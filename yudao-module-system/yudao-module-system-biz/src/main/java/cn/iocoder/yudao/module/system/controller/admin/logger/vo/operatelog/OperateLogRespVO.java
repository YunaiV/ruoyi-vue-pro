package cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - 操作日志 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OperateLogRespVO {

    @Schema(description = "日志编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("日志编号")
    private Long id;

    @Schema(description = "链路追踪编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "89aca178-a370-411c-ae02-3f0d672be4ab")
    private String traceId;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("操作人")
    private String userNickname;

    @Schema(description = "操作模块", requiredMode = Schema.RequiredMode.REQUIRED, example = "订单")
    @ExcelProperty("操作模块")
    private String module;

    @Schema(description = "操作名", requiredMode = Schema.RequiredMode.REQUIRED, example = "创建订单")
    @ExcelProperty("操作名")
    private String name;

    @Schema(description = "操作分类，参见 OperateLogTypeEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "操作类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.OPERATE_TYPE)
    private Integer type;

    @Schema(description = "操作明细", example = "修改编号为 1 的用户信息，将性别从男改成女，将姓名从芋道改成源码。")
    private String content;

    @Schema(description = "拓展字段", example = "{'orderId': 1}")
    private Map<String, Object> exts;

    @Schema(description = "请求方法名", requiredMode = Schema.RequiredMode.REQUIRED, example = "GET")
    @NotEmpty(message = "请求方法名不能为空")
    private String requestMethod;

    @Schema(description = "请求地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "/xxx/yyy")
    private String requestUrl;

    @Schema(description = "用户 IP", requiredMode = Schema.RequiredMode.REQUIRED, example = "127.0.0.1")
    private String userIp;

    @Schema(description = "浏览器 UserAgent", requiredMode = Schema.RequiredMode.REQUIRED, example = "Mozilla/5.0")
    private String userAgent;

    @Schema(description = "Java 方法名", requiredMode = Schema.RequiredMode.REQUIRED, example = "cn.iocoder.yudao.adminserver.UserController.save(...)")
    private String javaMethod;

    @Schema(description = "Java 方法的参数")
    private String javaMethodArgs;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("操作日志")
    private LocalDateTime startTime;

    @Schema(description = "执行时长，单位：毫秒", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("执行时长")
    private Integer duration;

    @Schema(description = "结果码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "结果码")
    private Integer resultCode;

    @Schema(description = "结果提示")
    private String resultMsg;

    @Schema(description = "结果数据")
    private String resultData;

}
