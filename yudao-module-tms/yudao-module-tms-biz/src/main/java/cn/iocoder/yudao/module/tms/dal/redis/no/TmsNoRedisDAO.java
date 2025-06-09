package cn.iocoder.yudao.module.tms.dal.redis.no;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.tms.dal.redis.TmsRedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Repository
public class TmsNoRedisDAO {

    /**
     * 头程申请
     */
    public static final String FIRST_MILE_REQUEST_NO_PREFIX = "TCSQ";
    /**
     * 头程单
     */
    public static final String FIRST_MILE_NO_PREFIX = "TCDD";

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
        String key = TmsRedisKeyConstants.NO + keyPrefix;
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
     * @param fullSerialNo 全部流水号，例如说 "CGDD-20250109-000001"
     */
    public void setManualSerial(String prefix, String fullSerialNo) {
        try {
            // 解析出日期和流水号数字部分
            String[] parts = fullSerialNo.split(StrPool.DASHED);
            String datePart = parts[1];           // 20250329
            String numberPart = parts[2];         // 000123
            long manualNo = Long.parseLong(numberPart);

            // 构造 Redis key
            String keyPrefix = prefix + StrPool.DASHED + datePart;
            String key = TmsRedisKeyConstants.NO + keyPrefix;

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
        String redisKey = TmsRedisKeyConstants.NO + keyPrefix;

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
}

