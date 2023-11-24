package cn.iocoder.yudao.module.crm.dal.mysql.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmSceneEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 客户 Mapper
 *
 * @author Wanwan
 */
@Mapper
public interface CrmCustomerMapper extends BaseMapperX<CrmCustomerDO> {

    default PageResult<CrmCustomerDO> selectPage(CrmCustomerPageReqVO pageReqVO) {
        LambdaQueryWrapperX<CrmCustomerDO> queryWrapperX = new LambdaQueryWrapperX<>();
        if (pageReqVO.getPool()) { // 情况一：公海
            queryWrapperX.isNull(CrmCustomerDO::getOwnerUserId);
        } else {// 情况一：不是公海
            queryWrapperX.isNotNull(CrmCustomerDO::getOwnerUserId);
        }
        return selectPage(pageReqVO, queryWrapperX
                .likeIfPresent(CrmCustomerDO::getName, pageReqVO.getName())
                .eqIfPresent(CrmCustomerDO::getMobile, pageReqVO.getMobile())
                .eqIfPresent(CrmCustomerDO::getIndustryId, pageReqVO.getIndustryId())
                .eqIfPresent(CrmCustomerDO::getLevel, pageReqVO.getLevel())
                .eqIfPresent(CrmCustomerDO::getSource, pageReqVO.getSource()));
    }

    default PageResult<CrmCustomerDO> selectPage1(CrmCustomerPageReqVO pageReqVO, Long userId) {
        LambdaQueryWrapperX<CrmCustomerDO> queryWrapperX = new LambdaQueryWrapperX<>();
        //queryWrapperX.sql
        if (pageReqVO.getPool()) { // 情况一：公海
            queryWrapperX.isNull(CrmCustomerDO::getOwnerUserId);
        } else {// 情况一：不是公海
            queryWrapperX.isNotNull(CrmCustomerDO::getOwnerUserId);
        }
        return selectPage(pageReqVO, queryWrapperX
                .likeIfPresent(CrmCustomerDO::getName, pageReqVO.getName())
                .eqIfPresent(CrmCustomerDO::getMobile, pageReqVO.getMobile())
                .eqIfPresent(CrmCustomerDO::getIndustryId, pageReqVO.getIndustryId())
                .eqIfPresent(CrmCustomerDO::getLevel, pageReqVO.getLevel())
                .eqIfPresent(CrmCustomerDO::getSource, pageReqVO.getSource()));
    }

    default PageResult<CrmCustomerDO> selectPage(CrmCustomerPageReqVO pageReqVO, Long userId) {
        IPage<CrmCustomerDO> mpPage = MyBatisUtils.buildPage(pageReqVO);
        MPJLambdaWrapperX<CrmCustomerDO> mpjLambdaWrapperX = new MPJLambdaWrapperX<>();
        // 构建数据权限连表条件
        //CrmPermissionUtils.builderRightJoinQuery(mpjLambdaWrapperX, CrmBizTypeEnum.CRM_CUSTOMER.getType(), userId);
        mpjLambdaWrapperX
                //.rightJoin("(SELECT t1.biz_id FROM crm_permission t1 WHERE (t1.biz_type = 1 AND t1.user_id = 1)) t2 on t.id = t2.biz_id");
                .rightJoin(CrmPermissionDO.class, CrmPermissionDO::getBizId, CrmCustomerDO::getId)
                .eq(CrmPermissionDO::getBizType, CrmBizTypeEnum.CRM_CUSTOMER.getType())
                .eq(CrmPermissionDO::getUserId, userId);
        /** TODO @芋艿：
         -- 常规连表-查询正常
         | ==>  Preparing:
         SELECT t.id, t.name, t.follow_up_status, t.lock_status, t.deal_status,
         t.industry_id, t.level, t.source, t.mobile, t.telephone, t.website,
         t.qq, t.wechat, t.email, t.description, t.remark, t.owner_user_id,
         t.area_id, t.detail_address, t.contact_last_time, t.contact_next_time,
         t.create_time, t.update_time, t.creator, t.updater, t.deleted
         FROM crm_customer t RIGHT JOIN crm_permission t1 ON (t1.biz_id = t.id) AND t.tenant_id = 1
         WHERE t.deleted = 0 AND t1.deleted = 0
         AND (t1.biz_type = ? AND t1.user_id = ?
         AND t.owner_user_id IS NOT NULL AND t.level = ?) AND t1.tenant_id = 1 LIMIT ?
         | ==> Parameters: 2(Integer), 1(Long), 3(Integer), 10(Long)

         -- 连接子查询-报错，但是复制到 navicat 是可以正常执行的
         -- 区别点：常规连表会自动拼接租户 AND t.tenant_id = 1
         SELECT
         t.id,t.name,t.follow_up_status,t.lock_status,t.deal_status,t.industry_id,t.level,
         t.source,t.mobile,t.telephone,t.website,t.qq,t.wechat,t.email,t.description,t.remark,
         t.owner_user_id,t.area_id,t.detail_address,t.contact_last_time,t.contact_next_time,
         t.create_time,t.update_time,t.creator,t.updater,t.deleted
         FROM crm_customer t
         RIGHT JOIN (SELECT t1.biz_id FROM crm_permission t1 WHERE (t1.biz_type = 2 AND t1.user_id = 1)) t2 on t.id = t2.biz_id
         WHERE  t.deleted=0
         AND (t.owner_user_id IS NOT NULL)
         */
        if (pageReqVO.getPool()) { // 情况一：公海
            mpjLambdaWrapperX.isNull(CrmCustomerDO::getOwnerUserId);
        } else {// 情况一：不是公海
            mpjLambdaWrapperX.isNotNull(CrmCustomerDO::getOwnerUserId);
        }
        // TODO 场景数据过滤
        if (CrmSceneEnum.isOwner(pageReqVO.getSceneType())) { // 场景一：我负责的数据
            mpjLambdaWrapperX.eq(CrmCustomerDO::getOwnerUserId, userId);
        }
        mpPage = selectJoinPage(mpPage, CrmCustomerDO.class, mpjLambdaWrapperX
                .selectAll(CrmCustomerDO.class)
                .likeIfPresent(CrmCustomerDO::getName, pageReqVO.getName())
                .eqIfPresent(CrmCustomerDO::getMobile, pageReqVO.getMobile())
                .eqIfPresent(CrmCustomerDO::getIndustryId, pageReqVO.getIndustryId())
                .eqIfPresent(CrmCustomerDO::getLevel, pageReqVO.getLevel())
                .eqIfPresent(CrmCustomerDO::getSource, pageReqVO.getSource()));
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    default void updateCustomerByOwnerUserIdIsNull(Long id, CrmCustomerDO updateObj) {
        update(updateObj, new LambdaUpdateWrapper<CrmCustomerDO>()
                .eq(CrmCustomerDO::getId, id)
                .isNull(CrmCustomerDO::getOwnerUserId));
    }

    default List<CrmCustomerDO> selectList(Collection<Long> ids) {
        return selectList(new LambdaQueryWrapperX<CrmCustomerDO>()
                .inIfPresent(CrmCustomerDO::getId, ids));
    }

}
