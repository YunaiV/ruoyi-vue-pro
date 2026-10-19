package cn.iocoder.yudao.module.oa.dal.redis.no;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.oa.dal.redis.RedisKeyConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * OA 业务单号的 Redis DAO
 *
 * @author 芋道源码
 */
@Repository
public class OaNoRedisDAO {

    /**
     * 用品领用申请 {@link cn.iocoder.yudao.module.oa.dal.dataobject.supply.OaSupplyApplyDO}
     */
    public static final String SUPPLY_APPLY_NO_PREFIX = "YP";

    /**
     * 出差申请 {@link cn.iocoder.yudao.module.oa.dal.dataobject.travel.OaTravelApplyDO}
     */
    public static final String TRAVEL_APPLY_NO_PREFIX = "CC";
    /**
     * 出差报销 {@link cn.iocoder.yudao.module.oa.dal.dataobject.travel.OaTravelReimbursementDO}
     */
    public static final String TRAVEL_REIMBURSEMENT_NO_PREFIX = "CLBX";

    /**
     * 用车申请 {@link cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleApplyDO}
     */
    public static final String VEHICLE_APPLY_NO_PREFIX = "YC";
    /**
     * 还车申请 {@link cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleReturnDO}
     */
    public static final String VEHICLE_RETURN_NO_PREFIX = "HC";
    /**
     * 印章编号 {@link cn.iocoder.yudao.module.oa.dal.dataobject.seal.OaSealDO}
     */
    public static final String SEAL_NO_PREFIX = "YZ";
    /**
     * 用印申请 {@link cn.iocoder.yudao.module.oa.dal.dataobject.seal.OaSealApplyDO}
     */
    public static final String SEAL_APPLY_NO_PREFIX = "YY";

    /**
     * 会议室预定 {@link cn.iocoder.yudao.module.oa.dal.dataobject.meetingroom.OaMeetingRoomBookingDO}
     */
    public static final String MEETING_ROOM_BOOKING_NO_PREFIX = "HY";

    /**
     * 工作汇报 {@link cn.iocoder.yudao.module.oa.dal.dataobject.workreport.OaWorkReportDO}
     */
    public static final String WORK_REPORT_NO_PREFIX = "WR";

    /**
     * 公文发文 {@link cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.OaOfficialDocSendDO}
     */
    public static final String OFFICIAL_DOC_SEND_NO_PREFIX = "FW";
    /**
     * 公文收文 {@link cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.OaOfficialDocReceiveDO}
     */
    public static final String OFFICIAL_DOC_RECEIVE_NO_PREFIX = "SW";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成序号，使用当前日期，格式为 {PREFIX} + yyyyMMdd + 6 位自增
     * 例如说：YC20260912000001 （没有中间空格）
     *
     * @param prefix 前缀
     * @return 序号
     */
    public String generate(String prefix) {
        // 递增序号
        String noPrefix = prefix + DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
        String key = RedisKeyConstants.NO + noPrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        // 设置过期时间
        stringRedisTemplate.expire(key, Duration.ofDays(1L));
        return noPrefix + String.format("%06d", no);
    }

}
