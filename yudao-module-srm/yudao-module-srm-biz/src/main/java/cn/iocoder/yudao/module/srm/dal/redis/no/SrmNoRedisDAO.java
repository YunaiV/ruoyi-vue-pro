package cn.iocoder.yudao.module.srm.dal.redis.no;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.redis.SrmRedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;


/**
 * Erp 订单序号的 Redis DAO
 *
 * @author HUIHUI
 */
@Repository
@Slf4j
public class SrmNoRedisDAO {

    //    /**
    //     * 其它入库 {@link cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockInDO}
    //     */
    //    public static final String STOCK_IN_NO_PREFIX = "QTRK";
    //    /**
    //     * 其它出库 {@link cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockOutDO}
    //     */
    //    public static final String STOCK_OUT_NO_PREFIX = "QCKD";
    //
    //    /**
    //     * 库存调拨 {@link cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockMoveDO}
    //     */
    //    public static final String STOCK_MOVE_NO_PREFIX = "QCDB";
    //
    //    /**
    //     * 库存盘点 {@link cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockCheckDO}
    //     */
    //    public static final String STOCK_CHECK_NO_PREFIX = "QCPD";
    //
    //    /**
    //     * 销售订单 {@link cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO}
    //     */
    //    public static final String SALE_ORDER_NO_PREFIX = "XSDD";
    //    /**
    //     * 销售出库 {@link cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutDO}
    //     */
    //    public static final String SALE_OUT_NO_PREFIX = "XSCK";
    //    /**
    //     * 销售退货 {@link cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleReturnDO}
    //     */
    //    public static final String SALE_RETURN_NO_PREFIX = "XSTH";

    /**
     * 采购订单 {@link SrmPurchaseOrderDO}
     */
    public static final String PURCHASE_ORDER_NO_PREFIX = "CGDD";
    /**
     * 采购申请 {@link SrmPurchaseRequestDO}
     */
    public static final String PURCHASE_REQUEST_NO_PREFIX = "CGSQ";
    /**
     * 采购入库 {@link SrmPurchaseInDO}
     */
    public static final String PURCHASE_IN_NO_PREFIX = "CGRK";
    /**
     * 采购退货 {@link SrmPurchaseReturnDO}
     */
    public static final String PURCHASE_RETURN_NO_PREFIX = "CGTH";
    //
    //    /**
    //     * 付款单 {@link cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentDO}
    //     */
    //    public static final String FINANCE_PAYMENT_NO_PREFIX = "FKD";
    //    /**
    //     * 收款单 {@link cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceReceiptDO}
    //     */
    public static final String FINANCE_RECEIPT_NO_PREFIX = "SKD";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成序号，使用当前日期，格式为 {PREFIX} + "_" + yyyyMMdd + "_" + 6 位自增 例如说：QTRK-20211009-000001 （没有中间空格）
     *
     * @param prefix 前缀
     * @return 序号
     */
    public String generate(String prefix, ErrorCode message) {
        // 递增序号
        String keyPrefix = prefix + StrPool.DASHED + DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
        String key = SrmRedisKeyConstants.NO + keyPrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        //判断no是否大于6位数
        ThrowUtil.ifGreater(no, 999999L, message);
        // 设置过期时间
        stringRedisTemplate.expire(key, Duration.ofDays(1L));
        return keyPrefix + StrPool.DASHED + String.format("%06d", no);
    }

    /**
     * 手动设置序号时，同时更新 Redis 的值为最大值
     *
     * @param prefix       前缀
     * @param fullSerialNo 全部流水号，例如说 "PO-20211009-000001"
     */
    public void setManualSerial(String prefix, String fullSerialNo) {
        try {
            // 解析出日期和流水号数字部分
            String[] parts = fullSerialNo.split("-");
            String datePart = parts[1];           // 20250329
            String numberPart = parts[2];         // 000123
            long manualNo = Long.parseLong(numberPart);

            // 构造 Redis key
            String keyPrefix = prefix + "-" + datePart;
            String key = SrmRedisKeyConstants.NO + keyPrefix;

            // 获取 Redis 当前值
            String current = stringRedisTemplate.opsForValue().get(key);
            long currentNo = current != null ? Long.parseLong(current) : 0;

            // 如果手动编号比当前大，则更新 Redis 的值
            if (manualNo > currentNo) {
                stringRedisTemplate.opsForValue().set(key, String.valueOf(manualNo));
            }
        } catch (Exception e) {
            log.error("[setManualSerial][prefix({}) fullSerialNo({}) 发生异常]", prefix, fullSerialNo, e);
        }
    }

    /**
     * 获取某个前缀和当前日期下的最大可用编号（不会触发自增，只读取 Redis）
     *
     * @param prefix 编号前缀，例如 "PO"
     * @return 最大编号字符串，例如 "PO-20250329-000123"，如果没有生成过返回 null
     */
    public String getMaxSerial(String prefix, ErrorCode message) {
        // 构造 key 前缀：PO-20250329
        String dateStr = DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
        String keyPrefix = prefix + StrPool.DASHED + dateStr;
        String redisKey = SrmRedisKeyConstants.NO + keyPrefix;

        String current = stringRedisTemplate.opsForValue().get(redisKey);
        if (current == null) {
            return generate(prefix, message); // 表示该前缀今天尚未生成过编号
        }

        try {
            long no = Long.parseLong(current) + 1L;
            return keyPrefix + StrPool.DASHED + String.format("%06d", no);
        } catch (NumberFormatException e) {
            log.error("[getMaxSerial] Redis 值格式错误：key = {}, value = {}", redisKey, current, e);
            return null;
        }
    }

    /**
     * 判断某个编号是否已存在（仅基于 Redis 现有流水号判断）
     *
     * @param fullSerialNo 完整编号，如 PO-20250329-000123
     * @return true = 已存在；false = 可能未生成过
     */
    public boolean isSerialExists(String fullSerialNo) {
        try {
            // 拆分编号
            String[] parts = fullSerialNo.split(StrPool.DASHED);
            if (parts.length != 3) {
                return false;
            }

            String prefix = parts[0];         // PO
            String dateStr = parts[1];        // 20250329
            String numberStr = parts[2];      // 000123

            long checkNo = Long.parseLong(numberStr);

            // 构造 Redis key：srm:no:PO-20250329
            String keyPrefix = prefix + StrPool.DASHED + dateStr;
            String redisKey = SrmRedisKeyConstants.NO + keyPrefix;

            // 获取当前 Redis 中最大流水号
            String current = stringRedisTemplate.opsForValue().get(redisKey);
            if (current == null) {
                return false;
            }

            long currentNo = Long.parseLong(current);

            // 判断该编号是否 <= 当前最大编号
            return checkNo <= currentNo;

        } catch (Exception e) {
            log.warn("[isSerialExists] 编号解析失败: {}", fullSerialNo, e);
            return false;
        }
    }

}
