package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.user.BrokerageUserPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserChildSummaryPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserChildSummaryRespVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankByUserCountRespVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankPageReqVO;
import cn.iocoder.yudao.module.trade.convert.brokerage.BrokerageUserConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.BrokerageUserMapper;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageBindModeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageEnabledConditionEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;

/**
 * 分销用户 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class BrokerageUserServiceImpl implements BrokerageUserService {

    @Resource
    private BrokerageUserMapper brokerageUserMapper;

    @Resource
    private TradeConfigService tradeConfigService;

    @Resource
    private MemberUserApi memberUserApi;

    @Override
    public BrokerageUserDO getBrokerageUser(Long id) {
        return brokerageUserMapper.selectById(id);
    }

    @Override
    public List<BrokerageUserDO> getBrokerageUserList(Collection<Long> ids) {
        return brokerageUserMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<BrokerageUserDO> getBrokerageUserPage(BrokerageUserPageReqVO pageReqVO) {
        List<Long> bindUserIds = buildBindUserIdsByLevel(pageReqVO.getBindUserId(), pageReqVO.getLevel());
        return brokerageUserMapper.selectPage(pageReqVO, bindUserIds);
    }

    @Override
    public void updateBrokerageUserId(Long id, Long bindUserId) {
        // 校验存在
        BrokerageUserDO brokerageUser = validateBrokerageUserExists(id);
        // 绑定关系未发生变化
        if (Objects.equals(brokerageUser.getBindUserId(), bindUserId)) {
            return;
        }

        // 情况一：清除推广员
        if (bindUserId == null) {
            // 清除推广员
            brokerageUserMapper.updateBindUserIdAndBindUserTimeToNull(id);
            return;
        }

        // 情况二：修改推广员
        validateCanBindUser(brokerageUser, bindUserId);
        brokerageUserMapper.updateById(fillBindUserData(bindUserId, new BrokerageUserDO().setId(id)));
    }

    @Override
    public void updateBrokerageUserEnabled(Long id, Boolean enabled) {
        // 校验存在
        validateBrokerageUserExists(id);
        if (BooleanUtil.isTrue(enabled)) {
            // 开通推广资格
            brokerageUserMapper.updateById(new BrokerageUserDO().setId(id)
                    .setBrokerageEnabled(true).setBrokerageTime(LocalDateTime.now()));
        } else {
            // 取消推广资格
            brokerageUserMapper.updateEnabledFalseAndBrokerageTimeToNull(id);
        }
    }

    private BrokerageUserDO validateBrokerageUserExists(Long id) {
        BrokerageUserDO brokerageUserDO = brokerageUserMapper.selectById(id);
        if (brokerageUserDO == null) {
            throw exception(BROKERAGE_USER_NOT_EXISTS);
        }

        return brokerageUserDO;
    }

    @Override
    public BrokerageUserDO getBindBrokerageUser(Long id) {
        return Optional.ofNullable(id)
                .map(this::getBrokerageUser)
                .map(BrokerageUserDO::getBindUserId)
                .map(this::getBrokerageUser)
                .orElse(null);
    }

    @Override
    public boolean updateUserPrice(Long id, Integer price) {
        if (price > 0) {
            brokerageUserMapper.updatePriceIncr(id, price);
        } else if (price < 0) {
            return brokerageUserMapper.updatePriceDecr(id, price) > 0;
        }
        return true;
    }

    @Override
    public void updateUserFrozenPrice(Long id, Integer frozenPrice) {
        if (frozenPrice > 0) {
            brokerageUserMapper.updateFrozenPriceIncr(id, frozenPrice);
        } else if (frozenPrice < 0) {
            brokerageUserMapper.updateFrozenPriceDecr(id, frozenPrice);
        }
    }

    @Override
    public void updateFrozenPriceDecrAndPriceIncr(Long id, Integer frozenPrice) {
        Assert.isTrue(frozenPrice < 0);
        int updateRows = brokerageUserMapper.updateFrozenPriceDecrAndPriceIncr(id, frozenPrice);
        if (updateRows == 0) {
            throw exception(BROKERAGE_USER_FROZEN_PRICE_NOT_ENOUGH);
        }
    }

    @Override
    public Long getBrokerageUserCountByBindUserId(Long bindUserId, Integer level) {
        List<Long> bindUserIds = buildBindUserIdsByLevel(bindUserId, level);
        if (CollUtil.isEmpty(bindUserIds)) {
            return 0L;
        }
        return brokerageUserMapper.selectCountByBindUserIdIn(bindUserIds);
    }

    @Override
    public boolean bindBrokerageUser(Long userId, Long bindUserId) {
        // 1. 获得分销用户
        boolean isNewBrokerageUser = false;
        BrokerageUserDO brokerageUser = brokerageUserMapper.selectById(userId);
        if (brokerageUser == null) { // 分销用户不存在的情况：1. 新注册；2. 旧数据；3. 分销功能关闭后又打开
            isNewBrokerageUser = true;
            brokerageUser = new BrokerageUserDO().setId(userId).setBrokerageEnabled(false).setBrokeragePrice(0).setFrozenPrice(0);
        }

        // 2.1 校验是否能绑定用户
        boolean validated = isUserCanBind(brokerageUser);
        if (!validated) {
            return false;
        }
        // 2.3 校验能否绑定
        validateCanBindUser(brokerageUser, bindUserId);
        // 2.3 绑定用户
        if (isNewBrokerageUser) {
            Integer enabledCondition = tradeConfigService.getTradeConfig().getBrokerageEnabledCondition();
            if (BrokerageEnabledConditionEnum.ALL.getCondition().equals(enabledCondition)) { // 人人分销：用户默认就有分销资格
                brokerageUser.setBrokerageEnabled(true).setBrokerageTime(LocalDateTime.now());
            }
            brokerageUser.setBindUserId(bindUserId).setBindUserTime(LocalDateTime.now());
            brokerageUserMapper.insert(fillBindUserData(bindUserId, brokerageUser));
        } else {
            brokerageUserMapper.updateById(fillBindUserData(bindUserId, new BrokerageUserDO().setId(userId)));
        }
        return true;
    }

    /**
     * 补全绑定用户的字段
     *
     * @param bindUserId    绑定的用户编号
     * @param brokerageUser update 对象
     * @return 补全后的 update 对象
     */
    private BrokerageUserDO fillBindUserData(Long bindUserId, BrokerageUserDO brokerageUser) {
        return brokerageUser.setBindUserId(bindUserId).setBindUserTime(LocalDateTime.now());
    }

    @Override
    public Boolean getUserBrokerageEnabled(Long userId) {
        // 全局分销功能是否开启
        TradeConfigDO tradeConfig = tradeConfigService.getTradeConfig();
        if (tradeConfig == null || BooleanUtil.isFalse(tradeConfig.getBrokerageEnabled())) {
            return false;
        }

        // 用户是否有分销资格
        return Optional.ofNullable(getBrokerageUser(userId))
                .map(BrokerageUserDO::getBrokerageEnabled)
                .orElse(false);
    }

    @Override
    public PageResult<AppBrokerageUserRankByUserCountRespVO> getBrokerageUserRankPageByUserCount(AppBrokerageUserRankPageReqVO pageReqVO) {
        IPage<AppBrokerageUserRankByUserCountRespVO> pageResult = brokerageUserMapper.selectCountPageGroupByBindUserId(MyBatisUtils.buildPage(pageReqVO),
                ArrayUtil.get(pageReqVO.getTimes(), 0), ArrayUtil.get(pageReqVO.getTimes(), 1));
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }

    @Override
    public PageResult<AppBrokerageUserChildSummaryRespVO> getBrokerageUserChildSummaryPage(AppBrokerageUserChildSummaryPageReqVO pageReqVO, Long userId) {
        // 生成推广员编号列表
        List<Long> bindUserIds = buildBindUserIdsByLevel(userId, pageReqVO.getLevel());

        // 情况一：没有昵称过滤条件时，直接使用数据库的分页查询
        if (StrUtil.isBlank(pageReqVO.getNickname())) {
            // 1.1 分页查询
            IPage<AppBrokerageUserChildSummaryRespVO> pageResult = brokerageUserMapper.selectSummaryPageByUserId(
                    MyBatisUtils.buildPage(pageReqVO), BrokerageRecordBizTypeEnum.ORDER.getType(),
                    BrokerageRecordStatusEnum.SETTLEMENT.getStatus(), bindUserIds, pageReqVO.getSortingField()
            );

            // 1.2 拼接数据并返回
            List<Long> userIds = convertList(pageResult.getRecords(), AppBrokerageUserChildSummaryRespVO::getId);
            Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(userIds);
            BrokerageUserConvert.INSTANCE.copyTo(pageResult.getRecords(), userMap);
            return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
        }

        // 情况二：有昵称过滤条件时，需要跨模块（Member）过滤
        // 2.1 查询所有匹配的分销用户
        List<AppBrokerageUserChildSummaryRespVO> list = brokerageUserMapper.selectSummaryListByUserId(
                BrokerageRecordBizTypeEnum.ORDER.getType(), BrokerageRecordStatusEnum.SETTLEMENT.getStatus(),
                bindUserIds, pageReqVO.getSortingField()
        );
        if (CollUtil.isEmpty(list)) {
            return PageResult.empty();
        }

        // 2.2 查出对应的用户信息
        List<MemberUserRespDTO> users = memberUserApi.getUserList(convertList(list, AppBrokerageUserChildSummaryRespVO::getId));
        if (CollUtil.isEmpty(users)) {
            return PageResult.empty();
        }

        // 2.3 根据昵称过滤出用户编号
        Map<Long, MemberUserRespDTO> userMap = users.stream()
                .filter(user -> StrUtil.contains(user.getNickname(), pageReqVO.getNickname()))
                .collect(Collectors.toMap(MemberUserRespDTO::getId, dto -> dto));
        if (CollUtil.isEmpty(userMap)) {
            return PageResult.empty();
        }

        // 2.4 根据用户编号过滤结果
        list.removeIf(vo -> !userMap.containsKey(vo.getId()));
        if (CollUtil.isEmpty(list)) {
            return PageResult.empty();
        }

        // 2.5 处理分页
        List<AppBrokerageUserChildSummaryRespVO> result = list.stream()
                .skip((long) (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize())
                .limit(pageReqVO.getPageSize())
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(result)) {
            return PageResult.empty();
        }

        // 2.6 拼接数据并返回
        BrokerageUserConvert.INSTANCE.copyTo(result, userMap);
        return new PageResult<>(result, (long) list.size());
    }

    private boolean isUserCanBind(BrokerageUserDO user) {
        // 校验分销功能是否启用
        TradeConfigDO tradeConfig = tradeConfigService.getTradeConfig();
        if (tradeConfig == null || !BooleanUtil.isTrue(tradeConfig.getBrokerageEnabled())) {
            return false;
        }

        // 校验分佣模式：仅可后台手动设置推广员
        if (BrokerageEnabledConditionEnum.ADMIN.getCondition().equals(tradeConfig.getBrokerageEnabledCondition())) {
            throw exception(BROKERAGE_BIND_CONDITION_ADMIN);
        }

        // 校验分销关系绑定模式
        if (BrokerageBindModeEnum.REGISTER.getMode().equals(tradeConfig.getBrokerageBindMode())) {
            // 判断是否为新用户：注册时间在 30 秒内的，都算新用户
            if (!isNewRegisterUser(user.getId())) {
                throw exception(BROKERAGE_BIND_MODE_REGISTER); // 只有在注册时可以绑定
            }
        } else if (BrokerageBindModeEnum.ANYTIME.getMode().equals(tradeConfig.getBrokerageBindMode())) {
            if (user.getBindUserId() != null) {
                throw exception(BROKERAGE_BIND_OVERRIDE); // 已绑定了推广人
            }
        }
        return true;
    }

    /**
     * 判断是否为新用户
     * <p>
     * 标准：注册时间在 30 秒内的，都算新用户
     * <p>
     * 疑问：为什么通过这样的方式实现？
     * 回答：因为注册在 member 模块，希望它和 trade 模块解耦，所以只能用这种约定的逻辑。
     *
     * @param userId 用户编号
     * @return 是否新用户
     */
    private boolean isNewRegisterUser(Long userId) {
        MemberUserRespDTO user = memberUserApi.getUser(userId);
        return user != null && LocalDateTimeUtils.beforeNow(user.getCreateTime().plusSeconds(30));
    }

    private void validateCanBindUser(BrokerageUserDO user, Long bindUserId) {
        // 校验要绑定的用户有无推广资格
        BrokerageUserDO bindUser = brokerageUserMapper.selectById(bindUserId);
        if (bindUser == null || BooleanUtil.isFalse(bindUser.getBrokerageEnabled())) {
            throw exception(BROKERAGE_BIND_USER_NOT_ENABLED);
        }

        // 校验绑定自己
        if (Objects.equals(user.getId(), bindUserId)) {
            throw exception(BROKERAGE_BIND_SELF);
        }

        // 下级不能绑定自己的上级
        for (int i = 0; i <= Short.MAX_VALUE; i++) {
            if (Objects.equals(bindUser.getBindUserId(), user.getId())) {
                throw exception(BROKERAGE_BIND_LOOP);
            }
            bindUser = getBrokerageUser(bindUser.getBindUserId());
            // 找到根节点，结束循环
            if (bindUser == null || bindUser.getBindUserId() == null) {
                break;
            }
        }
    }

    /**
     * 根据绑定用户编号，获得绑定用户编号列表
     *
     * @param bindUserId 绑定用户编号
     * @param level      绑定用户的层级。
     *                   如果 level 为空，则查询 1+2 两个层级
     * @return 绑定用户编号列表
     */
    private List<Long> buildBindUserIdsByLevel(Long bindUserId, Integer level) {
        if (bindUserId == null) {
            return Collections.emptyList();
        }
        Assert.isTrue(level == null || level <= 2, "目前只支持 level 小于等于 2");
        List<Long> bindUserIds = CollUtil.newArrayList();
        if (level == null || level == 1) {
            bindUserIds.add(bindUserId);
        }
        if (level == null || level == 2) {
            bindUserIds.addAll(convertList(brokerageUserMapper.selectListByBindUserId(bindUserId), BrokerageUserDO::getId));
        }
        return bindUserIds;
    }

}
