package cn.iocoder.yudao.framework.pay.core.client.dto.transfer;

import com.github.binarywang.wxpay.bean.notify.OriginNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayBaseNotifyV3Result;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// TODO @luchi：这个可以复用 wxjava 里的类么？
@NoArgsConstructor
public class WxPayTransferPartnerNotifyV3Result implements Serializable, WxPayBaseNotifyV3Result<WxPayTransferPartnerNotifyV3Result.TransferNotifyResult> {

    private static final long serialVersionUID = -1L;

    /**
     * 源数据
     */
    private OriginNotifyResponse rawData;

    /**
     * 解密后的数据
     */
    private TransferNotifyResult result;

    @Override
    public void setRawData(OriginNotifyResponse rawData) {
        this.rawData = rawData;
    }

    @Override
    public void setResult(TransferNotifyResult data) {
        this.result = data;
    }

    public TransferNotifyResult getResult() {
        return result;
    }

    public OriginNotifyResponse getRawData() {
        return rawData;
    }

    @Data
    @NoArgsConstructor
    public static class TransferNotifyResult implements Serializable {
        private static final long serialVersionUID = 1L;

        /*********************** 公共字段 ********************

        /**
         * 商家批次单号
         */
        @SerializedName(value = "out_batch_no")
        protected String outBatchNo;

        /**
         * 微信批次单号
         */
        @SerializedName(value = "batch_id")
        protected String batchId;

        /**
         * 批次状态
         */
        @SerializedName(value = "batch_status")
        protected String batchStatus;

        /**
         * 批次总笔数
         */
        @SerializedName(value = "total_num")
        protected Integer totalNum;

        /**
         * 批次总金额
         */
        @SerializedName(value = "total_amount")
        protected Integer totalAmount;

        /**
         * 批次更新时间
         */
        @SerializedName(value = "update_time")
        private String updateTime;

        /*********************** FINISHED ********************

        /**
         * 转账成功金额
         */
        @SerializedName(value = "success_amount")
        protected Integer successAmount;

        /**
         * 转账成功笔数
         */
        @SerializedName(value = "success_num")
        protected Integer successNum;

        /**
         * 转账失败金额
         */
        @SerializedName(value = "fail_amount")
        protected Integer failAmount;

        /**
         * 转账失败笔数
         */
        @SerializedName(value = "fail_num")
        protected Integer failNum;

        /*********************** CLOSED ********************

        /**
         * 商户号
         */
        @SerializedName(value = "mchid")
        protected String mchId;

        /**
         * 批次关闭原因
         */
        @SerializedName(value = "close_reason")
        protected String closeReason;

    }
}
