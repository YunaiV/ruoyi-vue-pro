package cn.iocoder.yudao.module.member.service.signin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.record.MemberSignInRecordPageReqVO;
import cn.iocoder.yudao.module.member.controller.app.signin.vo.AppMemberSignInSummaryRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import cn.iocoder.yudao.module.member.dal.mysql.signin.MemberSignInConfigMapper;
import cn.iocoder.yudao.module.member.dal.mysql.signin.MemberSignInRecordMapper;
import cn.iocoder.yudao.module.member.enums.ErrorCodeConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 用户签到积分 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MemberSignInRecordServiceImpl implements MemberSignInRecordService {

    @Resource
    private MemberSignInRecordMapper signInRecordMapper;
    @Resource
    private MemberSignInConfigMapper signInConfigMapper;
    @Resource
    private MemberUserApi memberUserApi;


    @Override
    public AppMemberSignInSummaryRespVO getUserSummary(Long userId) {
        AppMemberSignInSummaryRespVO vo  = new AppMemberSignInSummaryRespVO();
        vo.setTotalDay(0);
        vo.setContinuousDay(0);
        vo.setTodaySignIn(false);
        //获取用户签到的记录，按照天数倒序获取
        List <MemberSignInRecordDO> signInRecordDOList = signInRecordMapper.selectListByUserId(userId);
        if(!CollectionUtils.isEmpty(signInRecordDOList)){
            //设置总签到天数
            vo.setTotalDay(signInRecordDOList.size());
            //判断当天是否有签到复用校验方法
            try {
                validSignDay(signInRecordDOList.get(0));
                vo.setTodaySignIn(false);
            }catch (Exception e){
                vo.setTodaySignIn(true);
            }
            //如果当天签到了则说明连续签到天数有意义，否则直接用默认值0
            if(vo.getTodaySignIn()){
                //下方计算连续签到从2天开始，此处直接设置一天连续签到
                vo.setContinuousDay(1);
                //判断连续签到天数
                for (int i = 1; i < signInRecordDOList.size(); i++) {
                    //前一天减1等于当前天数则说明连续，继续循环
                    LocalDate cur = signInRecordDOList.get(i).getCreateTime().toLocalDate();
                    LocalDate pre = signInRecordDOList.get(i-1).getCreateTime().toLocalDate();
                    if(1==daysBetween(cur,pre)){
                        vo.setContinuousDay(i+1);
                    }else{
                        break;
                    }
                }
            }


        }
        return vo;
    }

    private long daysBetween(LocalDate date1,LocalDate date2){
        return ChronoUnit.DAYS.between(date1, date2);
    }

    @Override
    public PageResult <MemberSignInRecordDO> getSignInRecordPage(MemberSignInRecordPageReqVO pageReqVO) {
        // 根据用户昵称查询出用户ids
        Set <Long> userIds = null;
        if (StringUtils.isNotBlank(pageReqVO.getNickname())) {
            List <MemberUserRespDTO> users = memberUserApi.getUserListByNickname(pageReqVO.getNickname());
            // 如果查询用户结果为空直接返回无需继续查询
            if (CollectionUtils.isEmpty(users)) {
                return PageResult.empty();
            }
            userIds = convertSet(users, MemberUserRespDTO::getId);
        }
        return signInRecordMapper.selectPage(pageReqVO, userIds);
    }

    @Override
    public MemberSignInRecordDO create(Long userId) {
        //获取当前用户签到的最大天数
        MemberSignInRecordDO maxSignDay = signInRecordMapper.selectOne(new LambdaQueryWrapperX <MemberSignInRecordDO>()
                .eq(MemberSignInRecordDO::getUserId, userId)
                .orderByDesc(MemberSignInRecordDO::getDay)
                .last("limit 1"));
        //判断是否重复签到
        validSignDay(maxSignDay);

        /**1.查询出当前签到的天数**/
        MemberSignInRecordDO sign = new MemberSignInRecordDO();
        sign.setUserId(userId);
        //设置签到初始化天数
        sign.setDay(1);
        //设置签到分数默认为0
        sign.setPoint(0);
        //如果不为空则修改当前签到对应的天数
        if (maxSignDay != null) {
            sign.setDay(maxSignDay.getDay() + 1);
        }
        /**2.获取签到对应的分数**/
        //获取所有的签到规则，按照天数排序，只获取启用的
        List <MemberSignInConfigDO> configDOList = signInConfigMapper.selectList(new LambdaQueryWrapperX <MemberSignInConfigDO>()
                .eq(MemberSignInConfigDO::getEnable, 1)
                .orderByAsc(MemberSignInConfigDO::getDay));
        //如果签到的天数大于最大启用的规则天数，直接给最大签到的分数
        MemberSignInConfigDO lastConfig = configDOList.get(configDOList.size() - 1);
        if (sign.getDay() > lastConfig.getDay()) {
            sign.setPoint(lastConfig.getPoint());
        } else {
            configDOList.forEach(el -> {
                //循环匹配对应天数，设置对应分数
                if (el.getDay() == sign.getDay()) {
                    sign.setPoint(el.getPoint());
                }

            });
        }

        //3.插入当前签到获取的分数
        signInRecordMapper.insert(sign);
        //4.返回给用户
        return sign;
    }

    void validSignDay(MemberSignInRecordDO signInRecordDO) {
        if (signInRecordDO == null)
            return;
        LocalDate today = LocalDate.now();
        if (today.equals(signInRecordDO.getCreateTime().toLocalDate())) {
            throw exception(ErrorCodeConstants.SIGN_IN_RECORD_EXISTS);
        }
    }
}
