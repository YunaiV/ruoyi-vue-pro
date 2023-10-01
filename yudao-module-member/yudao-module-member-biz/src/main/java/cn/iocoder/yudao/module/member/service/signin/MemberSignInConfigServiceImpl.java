package cn.iocoder.yudao.module.member.service.signin;

import cn.iocoder.yudao.module.member.controller.admin.signin.vo.config.MemberSignInConfigCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.config.MemberSignInConfigUpdateReqVO;
import cn.iocoder.yudao.module.member.convert.signin.MemberSignInConfigConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import cn.iocoder.yudao.module.member.dal.mysql.signin.MemberSignInConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.*;

/**
 * 签到规则 Service 实现类
 *
 * @author QingX
 */
@Service
@Validated
public class MemberSignInConfigServiceImpl implements MemberSignInConfigService {

    @Resource
    private MemberSignInConfigMapper memberSignInConfigMapper;

    @Override
    public Long createSignInConfig(MemberSignInConfigCreateReqVO createReqVO) {
        // 校验奖励积分、奖励经验
        validatePointAndExperience(createReqVO.getPoint(), createReqVO.getExperience());
        // 判断是否重复插入签到天数
        validateSignInConfigDayDuplicate(createReqVO.getDay(), null);

        // 插入
        MemberSignInConfigDO signInConfig = MemberSignInConfigConvert.INSTANCE.convert(createReqVO);
        memberSignInConfigMapper.insert(signInConfig);
        // 返回
        return signInConfig.getId();
    }

    @Override
    public void updateSignInConfig(MemberSignInConfigUpdateReqVO updateReqVO) {
        // 校验奖励积分、奖励经验
        validatePointAndExperience(updateReqVO.getPoint(), updateReqVO.getExperience());
        // 校验存在
        validateSignInConfigExists(updateReqVO.getId());
        // 判断是否重复插入签到天数
        validateSignInConfigDayDuplicate(updateReqVO.getDay(), updateReqVO.getId());

        // 判断更新
        MemberSignInConfigDO updateObj = MemberSignInConfigConvert.INSTANCE.convert(updateReqVO);
        memberSignInConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteSignInConfig(Long id) {
        // 校验存在
        validateSignInConfigExists(id);
        // 删除
        memberSignInConfigMapper.deleteById(id);
    }

    private void validateSignInConfigExists(Long id) {
        if (memberSignInConfigMapper.selectById(id) == null) {
            throw exception(SIGN_IN_CONFIG_NOT_EXISTS);
        }
    }

    /**
     * 校验 day 是否重复
     *
     * @param day 天
     * @param id  编号，只有更新的时候会传递
     */
    private void validateSignInConfigDayDuplicate(Integer day, Long id) {
        MemberSignInConfigDO config = memberSignInConfigMapper.selectByDay(day);
        // 1. 新增时，config 非空，则说明重复
        if (id == null && config != null) {
            throw exception(SIGN_IN_CONFIG_EXISTS);
        }
        // 2. 更新时，如果 config 非空，且 id 不相等，则说明重复
        if (id != null && config != null && !config.getId().equals(id)) {
            throw exception(SIGN_IN_CONFIG_EXISTS);
        }
    }

    private void validatePointAndExperience(Integer point, Integer experience) {
        // 奖励积分、经验 至少要配置一个，否则没有意义
        if (Objects.equals(point, 0) && Objects.equals(experience, 0)) {
            throw exception(SIGN_IN_CONFIG_AWARD_EMPTY);
        }
    }

    @Override
    public MemberSignInConfigDO getSignInConfig(Long id) {
        return memberSignInConfigMapper.selectById(id);
    }

    @Override
    public List<MemberSignInConfigDO> getSignInConfigList() {
        List<MemberSignInConfigDO> list = memberSignInConfigMapper.selectList();
        list.sort(Comparator.comparing(MemberSignInConfigDO::getDay));
        return list;
    }

    @Override
    public List<MemberSignInConfigDO> getSignInConfigList(Integer status) {
        List<MemberSignInConfigDO> list = memberSignInConfigMapper.selectListByStatus(status);
        list.sort(Comparator.comparing(MemberSignInConfigDO::getDay));
        return list;
    }

}
