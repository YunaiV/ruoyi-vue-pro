package cn.iocoder.yudao.module.member.service.point;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointConfigDO;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointRecordDO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.mysql.point.MemberPointRecordMapper;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;


/**
 * 积分记录 Service 实现类
 *
 * @author QingX
 */
@Slf4j
@Service
@Validated
public class MemberPointRecordServiceImpl implements MemberPointRecordService {

    @Resource
    private MemberPointRecordMapper recordMapper;
    @Resource
    private MemberPointConfigService memberPointConfigService;

    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private MemberUserService memberUserService;

    @Override
    public PageResult<MemberPointRecordDO> getPointRecordPage(MemberPointRecordPageReqVO pageReqVO) {
        // 根据用户昵称查询出用户 ids
        Set<Long> userIds = null;
        if (StringUtils.isNotBlank(pageReqVO.getNickname())) {
            List<MemberUserRespDTO> users = memberUserApi.getUserListByNickname(pageReqVO.getNickname());
            // 如果查询用户结果为空直接返回无需继续查询
            if (CollectionUtils.isEmpty(users)) {
                return PageResult.empty();
            }
            userIds = convertSet(users, MemberUserRespDTO::getId);
        }
        // 执行查询
        return recordMapper.selectPage(pageReqVO, userIds);
    }

    @Override
    public PageResult<MemberPointRecordDO> getPointRecordPage(Long userId, PageParam pageVO) {
        return recordMapper.selectPage(userId, pageVO);
    }

    @Override
    public void createPointRecord(Long userId, Integer point, MemberPointBizTypeEnum bizType, String bizId) {
        MemberPointConfigDO pointConfig = memberPointConfigService.getPointConfig();
        if (pointConfig == null || pointConfig.getTradeGivePoint() == null) {
            log.error("[createPointRecord][增加积分失败：tradeGivePoint 未配置，userId({}) point({}) bizType({}) bizId({})]",
                    userId, point, bizType.getType(), bizId);
            return;
        }

        // 1. 根据配置的比例，换算实际的积分
        point = point * pointConfig.getTradeGivePoint();
        if (bizType.isReduce() && point > 0) {
            point = -point;
        }

        // 2. 增加积分记录
        MemberUserDO user = memberUserService.getUser(userId);
        Integer userPoint = ObjectUtil.defaultIfNull(user.getPoint(), 0);
        // 用户变动后的积分，防止扣出负数 TODO 疯狂：积分是不是允许扣到负数。因为它是跟有钱有关的东西，不能让商家出现资金损失
        Integer totalPoint = NumberUtil.max(userPoint + point, 0);
        MemberPointRecordDO recordDO = new MemberPointRecordDO()
                .setUserId(userId)
                .setBizId(bizId)
                .setBizType(bizType.getType())
                .setTitle(bizType.getName())
                .setDescription(StrUtil.format(bizType.getDescription(), point))
                .setPoint(point)
                .setTotalPoint(totalPoint);
        recordMapper.insert(recordDO);

        // 3. 更新用户积分
        memberUserService.updateUserPoint(userId, totalPoint);
    }

}
