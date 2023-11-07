package cn.iocoder.yudao.module.member.convert.signin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.record.MemberSignInRecordRespVO;
import cn.iocoder.yudao.module.member.controller.app.signin.vo.record.AppMemberSignInRecordRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Comparator;
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

    default PageResult<MemberSignInRecordRespVO> convertPage(PageResult<MemberSignInRecordDO> pageResult, List<MemberUserDO> users) {
        PageResult<MemberSignInRecordRespVO> voPageResult = convertPage(pageResult);
        // user 拼接
        Map<Long, MemberUserDO> userMap = convertMap(users, MemberUserDO::getId);
        voPageResult.getList().forEach(record -> MapUtils.findAndThen(userMap, record.getUserId(),
                memberUserRespDTO -> record.setNickname(memberUserRespDTO.getNickname())));
        return voPageResult;
    }

    PageResult<MemberSignInRecordRespVO> convertPage(PageResult<MemberSignInRecordDO> pageResult);

    PageResult<AppMemberSignInRecordRespVO> convertPage02(PageResult<MemberSignInRecordDO> pageResult);

    AppMemberSignInRecordRespVO coverRecordToAppRecordVo(MemberSignInRecordDO memberSignInRecordDO);

    default MemberSignInRecordDO convert(Long userId, MemberSignInRecordDO lastRecord, List<MemberSignInConfigDO> configs) {
        // 1. 计算是第几天签到
        configs.sort(Comparator.comparing(MemberSignInConfigDO::getDay));
        MemberSignInConfigDO lastConfig = CollUtil.getLast(configs); // 最大签到天数配置
        // 1.2. 计算今天是第几天签到
        int day = 1;
        // TODO @puhui999：要判断是不是昨天签到的；是否是昨天的判断，可以抽个方法到 util 里
        if (lastRecord != null) {
            day = lastRecord.getDay() + 1;
        }
        // 1.3 判断是否超出了最大签到配置
        if (day > lastConfig.getDay()) {
            day = 1; // 超过最大配置的天数，重置到第一天。(也就是说开启下一轮签到)
        }

        // 2.1 初始化签到信息
        MemberSignInRecordDO record = new MemberSignInRecordDO().setUserId(userId)
                .setDay(day).setPoint(0).setExperience(0);
        // 2.2 获取签到对应的积分
        MemberSignInConfigDO config = CollUtil.findOne(configs, item -> ObjUtil.equal(item.getDay(), record.getDay()));
        if (config == null) {
            return record;
        }
        record.setPoint(config.getPoint());
        record.setExperience(config.getExperience());
        return record;
    }

}
