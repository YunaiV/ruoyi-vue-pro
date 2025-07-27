package cn.iocoder.yudao.module.pay.api.transfer.dto;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 转账单创建 Request DTO
 *
 * @author jason
 */
@Data
public class PayTransferCreateReqDTO {

    /**
     * 应用标识
     */
    @NotNull(message = "应用标识不能为空")
    private String appKey;

    /**
     * 用户 IP
     */
    @NotEmpty(message = "用户 IP 不能为空")
    private String userIp;

    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 用户类型
     */
    @InEnum(UserTypeEnum.class)
    private Integer userType;

    // ========== 商户相关字段 ==========
    /**
     * 商户转账单编号
     */
    @NotEmpty(message = "商户转账单编号能为空")
    private String merchantTransferId;

    /**
     * 转账金额，单位：分
     */
    @Min(value = 1, message = "转账金额必须大于零")
    @NotNull(message = "转账金额不能为空")
    private Integer price;

    /**
     * 转账标题
     */
    @NotEmpty(message = "转账标题不能为空")
    private String subject;

    /**
     * 收款人账号
     *
     * 微信场景下：openid
     * 支付宝场景下：支付宝账号
     */
    @NotEmpty(message = "收款人账号不能为空")
    private String userAccount;
    /**
     * 收款人姓名
     */
    private String userName;

    /**
     * 转账渠道
     */
    @NotEmpty(message = "转账渠道不能为空")
    private String channelCode;

    /**
     * 转账渠道的额外参数
     */
    private Map<String, String> channelExtras;

    /**
     * 【微信】现金营销场景
     *
     * @param activityName 活动名称
     * @param rewardDescription 奖励说明
     * @return channelExtras
     */
    public static Map<String, String> buildWeiXinChannelExtra1000(String activityName, String rewardDescription) {
        return buildWeiXinChannelExtra(1000,
                "活动名称", activityName,
                "奖励说明", rewardDescription);
    }

    /**
     * 【微信】企业报销场景
     *
     * @param expenseType 报销类型
     * @param expenseDescription 报销说明
     * @return channelExtras
     */
    public static Map<String, String> buildWeiXinChannelExtra1006(String expenseType, String expenseDescription) {
        return buildWeiXinChannelExtra(1006,
                "报销类型", expenseType,
                "报销说明", expenseDescription);
    }

    private static Map<String, String> buildWeiXinChannelExtra(Integer sceneId, String... values) {
        Map<String, String> channelExtras = new HashMap<>();
        // 构建场景报备信息列表
        List<Map<String, String>> sceneReportInfos = new ArrayList<>();
        for (int i = 0; i < values.length; i += 2) {
            Map<String, String> info = new HashMap<>();
            info.put("infoType", values[i]);
            info.put("infoContent", values[i + 1]);
            sceneReportInfos.add(info);
        }
        // 设置场景ID和场景报备信息
        channelExtras.put("sceneId", StrUtil.toString(sceneId));
        channelExtras.put("sceneReportInfos", JsonUtils.toJsonString(sceneReportInfos));
        return channelExtras;
    }

}
