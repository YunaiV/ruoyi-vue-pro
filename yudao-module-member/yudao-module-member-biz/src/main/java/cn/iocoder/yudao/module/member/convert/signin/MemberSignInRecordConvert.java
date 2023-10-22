package cn.iocoder.yudao.module.member.convert.signin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.record.MemberSignInRecordRespVO;
import cn.iocoder.yudao.module.member.controller.app.signin.vo.record.AppMemberSignInRecordRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 签到记录 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface MemberSignInRecordConvert {

    MemberSignInRecordConvert INSTANCE = Mappers.getMapper(MemberSignInRecordConvert.class);

    default PageResult<MemberSignInRecordRespVO> convertPage(PageResult<MemberSignInRecordDO> pageResult, List<MemberUserRespDTO> users) {
        PageResult<MemberSignInRecordRespVO> voPageResult = convertPage(pageResult);
        // user 拼接
        Map<Long, MemberUserRespDTO> userMap = convertMap(users, MemberUserRespDTO::getId);
        voPageResult.getList().forEach(record -> MapUtils.findAndThen(userMap, record.getUserId(),
                memberUserRespDTO -> record.setNickname(memberUserRespDTO.getNickname())));
        return voPageResult;
    }

    PageResult<MemberSignInRecordRespVO> convertPage(PageResult<MemberSignInRecordDO> pageResult);

    PageResult<AppMemberSignInRecordRespVO> convertPage02(PageResult<MemberSignInRecordDO> pageResult);

    AppMemberSignInRecordRespVO coverRecordToAppRecordVo(MemberSignInRecordDO memberSignInRecordDO);

    default MemberSignInRecordDO convert(Long userId, MemberSignInRecordDO firstRecord, List<MemberSignInConfigDO> signInConfigs) {
        // 1. 计算今天是第几天签到
        long day = ChronoUnit.DAYS.between(firstRecord.getCreateTime(), LocalDateTime.now());
        // 2. 初始化签到信息
        // TODO @puhui999：signInRecord=》record
        MemberSignInRecordDO signInRecord = new MemberSignInRecordDO().setUserId(userId)
                .setDay(Integer.parseInt(Long.toString(day))) // 设置签到天数 TODO @puhui999：day 应该跟着第几天签到走；不是累加哈；另外 long 转 int，应该 (int) day 就可以了。。。
                .setPoint(0)  // 设置签到积分默认为
                .setExperience(0);  // 设置签到经验默认为 0
        // 3. 获取签到对应的积分数
        MemberSignInConfigDO lastConfig = signInConfigs.get(signInConfigs.size() - 1); // 最大签到天数
        if (day > lastConfig.getDay()) { // 超出范围按第一天的经验计算
            // TODO @puhui999：不能直接取 0，万一它 day 不匹配哈。就是第一天没奖励。。。
            signInRecord.setPoint(signInConfigs.get(0).getPoint());
            signInRecord.setExperience(signInConfigs.get(0).getExperience());
            return signInRecord;
        }
        // TODO @puhui999：signInConfig 可以改成 config；
        MemberSignInConfigDO signInConfig = CollUtil.findOne(signInConfigs, config -> ObjUtil.equal(config.getDay(), day));
        if (signInConfig == null) {
            return signInRecord;
        }
        signInRecord.setPoint(signInConfig.getPoint());
        signInRecord.setExperience(signInConfig.getExperience());
        return signInRecord;
    }

}
