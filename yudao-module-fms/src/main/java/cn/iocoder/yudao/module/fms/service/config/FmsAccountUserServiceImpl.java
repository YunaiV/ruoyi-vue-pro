package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountuser.FmsAccountUserUpdateReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountUserDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsAccountUserMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsAccountUserLevelEnum;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_ACCOUNT_SET_MEMBER_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_ACCOUNT_SET_MEMBER_UPDATE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_ACCOUNT_SET_TYPE;

/**
 * FMS 账套用户 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsAccountUserServiceImpl implements FmsAccountUserService {

    @Resource
    private FmsAccountUserMapper accountUserMapper;

    @Resource
    @Lazy // 延迟加载，避免与账套 Service 循环依赖
    private FmsAccountSetService accountSetService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public void createAccountOwner(Long accountSetId, Long userId) {
        boolean defaultStatus = accountUserMapper.selectCountByUserId(userId) == 0;
        FmsAccountUserDO accountUser = new FmsAccountUserDO().setAccountSetId(accountSetId).setUserId(userId)
                .setDefaultStatus(defaultStatus).setFounder(true)
                .setLevel(FmsAccountUserLevelEnum.OWNER.getLevel());
        accountUserMapper.insert(accountUser);
    }

    @Override
    public List<FmsAccountUserDO> getAccountUserList(Long userId) {
        return accountUserMapper.selectListByUserId(userId);
    }

    @Override
    public FmsAccountUserDO getAccountUser(Long accountSetId, Long userId) {
        return accountUserMapper.selectByAccountSetIdAndUserId(accountSetId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccountSetDefaultStatus(Long accountSetId, Long userId) {
        // 1. 校验用户属于目标账套
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 清除用户原默认账套，再设置目标账套
        accountUserMapper.updateDefaultStatusByUserId(userId);
        accountUserMapper.updateDefaultStatusByAccountSetIdAndUserId(accountSetId, userId);
    }

    @Override
    public List<FmsAccountUserDO> getAccountUserList(Long accountSetId, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询账套成员
        return accountUserMapper.selectListByAccountSetId(accountSetId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = FMS_ACCOUNT_SET_TYPE, subType = FMS_ACCOUNT_SET_MEMBER_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.accountSetId}}", success = FMS_ACCOUNT_SET_MEMBER_UPDATE_SUCCESS)
    public void updateAccountUserList(FmsAccountUserUpdateReqVO updateReqVO, Long userId) {
        // 1.1 校验账套主管权限
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetOwnerPermission(
                updateReqVO.getAccountSetId(), userId);
        // 1.2 查询现有成员并校验授权成员
        List<FmsAccountUserDO> accountUsers = accountUserMapper.selectListByAccountSetId(accountSet.getId());
        Set<Long> founderUserIds = convertSet(accountUsers, FmsAccountUserDO::getUserId,
                accountUser -> Boolean.TRUE.equals(accountUser.getFounder()));
        Map<Long, Integer> memberLevelMap = convertMap(updateReqVO.getMembers(),
                FmsAccountUserUpdateReqVO.Member::getUserId, FmsAccountUserUpdateReqVO.Member::getLevel);
        Set<Long> memberUserIds = new HashSet<>(memberLevelMap.keySet());
        memberUserIds.removeAll(founderUserIds);
        adminUserApi.validateUserList(memberUserIds);

        // 2.1 移出取消授权的成员
        Set<Long> currentMemberUserIds = convertSet(accountUsers, FmsAccountUserDO::getUserId,
                accountUser -> Boolean.FALSE.equals(accountUser.getFounder()));
        Set<Long> deleteUserIds = new HashSet<>(currentMemberUserIds);
        deleteUserIds.removeAll(memberUserIds);
        if (CollUtil.isNotEmpty(deleteUserIds)) {
            accountUserMapper.deleteByAccountSetIdAndUserIds(accountSet.getId(), deleteUserIds);
        }

        // 2.2 更新已有成员的权限级别
        List<FmsAccountUserDO> updateAccountUsers = convertList(accountUsers,
                accountUser -> new FmsAccountUserDO().setId(accountUser.getId())
                        .setLevel(memberLevelMap.get(accountUser.getUserId())),
                accountUser -> Boolean.FALSE.equals(accountUser.getFounder())
                        && memberLevelMap.containsKey(accountUser.getUserId())
                        && ObjUtil.notEqual(accountUser.getLevel(), memberLevelMap.get(accountUser.getUserId())));
        if (CollUtil.isNotEmpty(updateAccountUsers)) {
            accountUserMapper.updateBatch(updateAccountUsers);
        }

        // 2.3 加入新增授权的成员
        Set<Long> createUserIds = new HashSet<>(memberUserIds);
        createUserIds.removeAll(currentMemberUserIds);
        if (CollUtil.isNotEmpty(createUserIds)) {
            accountUserMapper.insertBatch(convertList(createUserIds,
                    memberUserId -> new FmsAccountUserDO().setAccountSetId(accountSet.getId())
                            .setUserId(memberUserId).setDefaultStatus(false).setFounder(false)
                            .setLevel(memberLevelMap.get(memberUserId))));
        }

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("accountSet", accountSet);
    }
}
