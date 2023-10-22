package cn.iocoder.yudao.module.member.service.signin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.record.MemberSignInRecordPageReqVO;
import cn.iocoder.yudao.module.member.controller.app.signin.vo.record.AppMemberSignInRecordSummaryRespVO;
import cn.iocoder.yudao.module.member.convert.signin.MemberSignInRecordConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import cn.iocoder.yudao.module.member.dal.mysql.signin.MemberSignInRecordMapper;
import cn.iocoder.yudao.module.member.enums.MemberExperienceBizTypeEnum;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;
import cn.iocoder.yudao.module.member.service.level.MemberLevelService;
import cn.iocoder.yudao.module.member.service.point.MemberPointRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.SIGN_IN_RECORD_TODAY_EXISTS;

/**
 * 签到记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MemberSignInRecordServiceImpl implements MemberSignInRecordService {

    @Resource
    private MemberSignInRecordMapper signInRecordMapper;
    @Resource
    private MemberSignInConfigService signInConfigService;
    @Resource
    private MemberPointRecordService pointRecordService;
    @Resource
    private MemberLevelService memberLevelService;

    @Resource
    private MemberUserApi memberUserApi;

    @Override
    public AppMemberSignInRecordSummaryRespVO getSignInRecordSummary(Long userId) {
        // 1. 初始化默认返回信息
        // TODO @puhui999：这里 vo 改成 summary 会更好理解；
        AppMemberSignInRecordSummaryRespVO vo = new AppMemberSignInRecordSummaryRespVO();
        vo.setTotalDay(0);
        vo.setContinuousDay(0);
        vo.setTodaySignIn(false);

        // 2. 获取用户签到的记录数
        Long signCount = signInRecordMapper.selectCountByUserId(userId);
        if (ObjUtil.equal(signCount, 0L)) {
            return vo;
        }
        vo.setTotalDay(signCount.intValue()); // 设置总签到天数

        // 3. 校验当天是否有签到
        // TODO @puhui999：是不是 signInRecord 可以精简成 record 哈；另外，Desc 貌似可以去掉哈；最后一条了；
        MemberSignInRecordDO signInRecord = signInRecordMapper.selectLastRecordByUserIdDesc(userId);
        if (signInRecord == null) {
            return vo;
        }
        vo.setTodaySignIn(DateUtils.isToday(signInRecord.getCreateTime()));

        // 4. 校验今天是否签到，没有签到则直接返回
        if (!vo.getTodaySignIn()) {
            return vo;
        }
        // 4.1. 判断连续签到天数
        // TODO @puhui999：连续签到，可以基于 signInRecord 的 day 和当前时间判断呀？
        List<MemberSignInRecordDO> signInRecords = signInRecordMapper.selectListByUserId(userId);
        vo.setContinuousDay(calculateConsecutiveDays(signInRecords));
        return vo;
    }

    /**
     * 计算连续签到天数
     *
     * @param signInRecords 签到记录列表
     * @return int 连续签到天数
     */
    public int calculateConsecutiveDays(List<MemberSignInRecordDO> signInRecords) {
        int consecutiveDays = 1;  // 初始连续天数为1
        LocalDate previousDate = null;

        for (MemberSignInRecordDO record : signInRecords) {
            LocalDate currentDate = record.getCreateTime().toLocalDate();

            if (previousDate != null) {
                // 检查相邻两个日期是否连续
                if (currentDate.minusDays(1).isEqual(previousDate)) {
                    consecutiveDays++;
                } else {
                    // 如果日期不连续，停止遍历
                    break;
                }
            }

            previousDate = currentDate;
        }

        return consecutiveDays;
    }

    @Override
    public PageResult<MemberSignInRecordDO> getSignInRecordPage(MemberSignInRecordPageReqVO pageReqVO) {
        // 根据用户昵称查询出用户ids
        Set<Long> userIds = null;
        if (StringUtils.isNotBlank(pageReqVO.getNickname())) {
            List<MemberUserRespDTO> users = memberUserApi.getUserListByNickname(pageReqVO.getNickname());
            // 如果查询用户结果为空直接返回无需继续查询
            if (CollUtil.isEmpty(users)) {
                return PageResult.empty();
            }
            userIds = convertSet(users, MemberUserRespDTO::getId);
        }
        // 分页查询
        return signInRecordMapper.selectPage(pageReqVO, userIds);
    }

    @Override
    public PageResult<MemberSignInRecordDO> getSignRecordPage(Long userId, PageParam pageParam) {
        return signInRecordMapper.selectPage(userId, pageParam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberSignInRecordDO createSignRecord(Long userId) {
        // 1. 获取当前用户最近的签到
        MemberSignInRecordDO lastRecord = signInRecordMapper.selectLastRecordByUserIdDesc(userId);
        // 1.1. 判断是否重复签到
        validateSigned(lastRecord);

        // 2. 获取当前用户最早的一次前端记录，用于计算今天是第几天签到
        MemberSignInRecordDO firstRecord = signInRecordMapper.selectLastRecordByUserIdAsc(userId);
        // 2.1. 获取所有的签到规则
        List<MemberSignInConfigDO> signInConfigs = signInConfigService.getSignInConfigList(CommonStatusEnum.ENABLE.getStatus());
        signInConfigs.sort(Comparator.comparing(MemberSignInConfigDO::getDay));
        // 2.2. 组合数据
        MemberSignInRecordDO record = MemberSignInRecordConvert.INSTANCE.convert(userId, firstRecord, signInConfigs);

        // 3. 插入签到记录
        signInRecordMapper.insert(record);

        // 4. 增加积分
        if (!ObjectUtils.equalsAny(record.getPoint(), null, 0)) {
            pointRecordService.createPointRecord(userId, record.getPoint(), MemberPointBizTypeEnum.SIGN, String.valueOf(record.getId()));
        }
        // 5. 增加经验
        if (!ObjectUtils.equalsAny(record.getExperience(), null, 0)) {
            memberLevelService.addExperience(userId, record.getExperience(), MemberExperienceBizTypeEnum.SIGN_IN, String.valueOf(record.getId()));
        }
        return record;
    }

    private void validateSigned(MemberSignInRecordDO signInRecordDO) {
        if (signInRecordDO == null) {
            return;
        }
        if (DateUtils.isToday(signInRecordDO.getCreateTime())) {
            throw exception(SIGN_IN_RECORD_TODAY_EXISTS);
        }
    }

}
