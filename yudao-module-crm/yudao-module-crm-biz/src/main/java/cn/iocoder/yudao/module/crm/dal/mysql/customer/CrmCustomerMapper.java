package cn.iocoder.yudao.module.crm.dal.mysql.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.message.vo.CrmTodayCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.message.CrmContactStatusEnum;
import cn.iocoder.yudao.module.crm.util.CrmQueryWrapperUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 客户 Mapper
 *
 * @author Wanwan
 */
@Mapper
public interface CrmCustomerMapper extends BaseMapperX<CrmCustomerDO> {

    default Long selectCountByLockStatusAndOwnerUserId(Boolean lockStatus, Long ownerUserId) {
        return selectCount(new LambdaUpdateWrapper<CrmCustomerDO>()
                .eq(CrmCustomerDO::getLockStatus, lockStatus)
                .eq(CrmCustomerDO::getOwnerUserId, ownerUserId));
    }

    default Long selectCountByDealStatusAndOwnerUserId(@Nullable Boolean dealStatus, Long ownerUserId) {
        return selectCount(new LambdaQueryWrapperX<CrmCustomerDO>()
                .eqIfPresent(CrmCustomerDO::getDealStatus, dealStatus)
                .eq(CrmCustomerDO::getOwnerUserId, ownerUserId));
    }

    default int updateOwnerUserIdById(Long id, Long ownerUserId) {
        return update(new LambdaUpdateWrapper<CrmCustomerDO>()
                .eq(CrmCustomerDO::getId, id)
                .set(CrmCustomerDO::getOwnerUserId, ownerUserId));
    }

    default PageResult<CrmCustomerDO> selectPage(CrmCustomerPageReqVO pageReqVO, Long userId) {
        MPJLambdaWrapperX<CrmCustomerDO> query = new MPJLambdaWrapperX<>();
        // 拼接数据权限的查询条件
        CrmQueryWrapperUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_CUSTOMER.getType(),
                CrmCustomerDO::getId, userId, pageReqVO.getSceneType(), pageReqVO.getPool());
        // 拼接自身的查询条件
        query.selectAll(CrmCustomerDO.class)
                .likeIfPresent(CrmCustomerDO::getName, pageReqVO.getName())
                .eqIfPresent(CrmCustomerDO::getMobile, pageReqVO.getMobile())
                .eqIfPresent(CrmCustomerDO::getIndustryId, pageReqVO.getIndustryId())
                .eqIfPresent(CrmCustomerDO::getLevel, pageReqVO.getLevel())
                .eqIfPresent(CrmCustomerDO::getSource, pageReqVO.getSource());
        return selectJoinPage(pageReqVO, CrmCustomerDO.class, query);
    }

    default List<CrmCustomerDO> selectBatchIds(Collection<Long> ids, Long userId) {
        MPJLambdaWrapperX<CrmCustomerDO> query = new MPJLambdaWrapperX<>();
        // 拼接数据权限的查询条件
        CrmQueryWrapperUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_CUSTOMER.getType(), ids, userId);
        return selectJoinList(CrmCustomerDO.class, query);
    }

    /**
     * 待办事项 - 今日需联系客户
     *
     * @param pageReqVO 分页请求参数
     * @param userId    当前用户ID
     * @return 分页结果
     */
    default PageResult<CrmCustomerDO> selectTodayCustomerPage(CrmTodayCustomerPageReqVO pageReqVO, Long userId) {
        MPJLambdaWrapperX<CrmCustomerDO> query = new MPJLambdaWrapperX<>();
        // 拼接数据权限的查询条件
        CrmQueryWrapperUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_CUSTOMER.getType(),
                CrmCustomerDO::getId, userId, pageReqVO.getSceneType(), null);

        query.selectAll(CrmCustomerDO.class)
                .leftJoin(CrmFollowUpRecordDO.class, CrmFollowUpRecordDO::getBizId, CrmCustomerDO::getId)
                .eq(CrmFollowUpRecordDO::getType, CrmBizTypeEnum.CRM_CUSTOMER.getType());

        // 拼接自身的查询条件
        // TODO @dbh52：这里不仅仅要获得 today、tomorrow。而是 today 要获取今天的 00:00:00 这种；
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate yesterday = today.minusDays(1);
        if (pageReqVO.getContactStatus().equals(CrmContactStatusEnum.NEEDED_TODAY.getType())) {
            // 今天需联系：
            // 1.【客户】的【下一次联系时间】 是【今天】
            // 2. 无法找到【今天】创建的【跟进】记录
            query.between(CrmCustomerDO::getContactNextTime, today, tomorrow)
                    // TODO @dbh52：是不是查询 CrmCustomerDO::contactLastTime < today？因为今天联系过，应该会更新该字段，减少链表查询；
                    .between(CrmFollowUpRecordDO::getCreateTime, today, tomorrow)
                    .isNull(CrmFollowUpRecordDO::getId);
        } else if (pageReqVO.getContactStatus().equals(CrmContactStatusEnum.EXPIRED.getType())) {
            // 已逾期：
            // 1. 【客户】的【下一次联系时间】 <= 【昨天】
            // 2. 无法找到【今天】创建的【跟进】记录
            //  TODO @dbh52：是不是 contactNextTime 在当前时间之前，且 contactLastTime < contactNextTime？说白了，就是下次联系时间超过当前时间，且最后联系时间没去联系；
            query.le(CrmCustomerDO::getContactNextTime, yesterday)
                    .between(CrmFollowUpRecordDO::getCreateTime, today, tomorrow)
                    .isNull(CrmFollowUpRecordDO::getId);
        } else if (pageReqVO.getContactStatus().equals(CrmContactStatusEnum.ALREADY_CONTACT.getType())) {
            // 已联系：
            // 1.【客户】的【下一次联系时间】 是【今天】
            // 2. 找到【今天】创建的【跟进】记录
            query.between(CrmCustomerDO::getContactNextTime, today, tomorrow)
                    // TODO @dbh52：contactLastTime 是今天
                    .between(CrmFollowUpRecordDO::getCreateTime, today, tomorrow)
                    .isNotNull(CrmFollowUpRecordDO::getId);
        } else {
            // TODO: 参数错误，是不是要兜一下底；直接抛出异常就好啦；
        }

        return selectJoinPage(pageReqVO, CrmCustomerDO.class, query);
    }

}
