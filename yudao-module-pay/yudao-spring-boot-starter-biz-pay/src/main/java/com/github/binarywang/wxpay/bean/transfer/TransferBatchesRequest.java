package com.github.binarywang.wxpay.bean.transfer;

import com.github.binarywang.wxpay.v3.SpecEncrypt;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 发起商家转账API参数
 *
 * @author zhongjun
 * created on  2022/6/17
 **/
@Data
@Builder(builderMethodName = "newBuilder")
@NoArgsConstructor
@AllArgsConstructor
public class TransferBatchesRequest implements Serializable {
    private static final long serialVersionUID = -2175582517588397426L;

    /**
     * 直连商户的appid
     */
    @SerializedName("appid")
    private String appid;

    /**
     * 商家批次单号
     */
    @SerializedName("out_batch_no")
    private String outBatchNo;

    /**
     * 批次名称
     */
    @SerializedName("batch_name")
    private String batchName;

    /**
     * 批次备注
     */
    @SerializedName("batch_remark")
    private String batchRemark;

    /**
     * 转账总金额
     */
    @SerializedName("total_amount")
    private Integer totalAmount;

    /**
     * 转账总笔数
     */
    @SerializedName("total_num")
    private Integer totalNum;

    /**
     * 转账明细列表
     */
    @SpecEncrypt
    @SerializedName("transfer_detail_list")
    private List<TransferDetail> transferDetailList;

    /**
     * 转账场景ID
     */
    @SerializedName("transfer_scene_id")
    private String transferSceneId;

    /**
     * 通知地址 说明：异步接收微信支付结果通知的回调地址，通知url必须为公网可访问的url，必须为https，不能携带参数。
     */
    @SerializedName("notify_url")
    private String notifyUrl;

    @Data
    @Builder(builderMethodName = "newBuilder")
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransferDetail {

        /**
         * 商家明细单号
         */
        @SerializedName("out_detail_no")
        private String outDetailNo;

        /**
         * 转账金额
         */
        @SerializedName("transfer_amount")
        private Integer transferAmount;

        /**
         * 转账备注
         */
        @SerializedName("transfer_remark")
        private String transferRemark;

        /**
         * 用户在直连商户应用下的用户标示
         */
        @SerializedName("openid")
        private String openid;

        /**
         * 收款用户姓名
         */
        @SpecEncrypt
        @SerializedName("user_name")
        private String userName;
    }
}
