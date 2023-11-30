package cn.iocoder.yudao.module.crm.dal.mysql.customer;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmSceneEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 客户 Mapper
 *
 * @author Wanwan
 */
@Mapper
public interface CrmCustomerMapper extends BaseMapperX<CrmCustomerDO> {

    default int updateOwnerUserIdById(Long id, Long ownerUserId) {
        return update(new LambdaUpdateWrapper<CrmCustomerDO>()
                .eq(CrmCustomerDO::getId, id)
                .set(CrmCustomerDO::getOwnerUserId, ownerUserId));
    }

    default PageResult<CrmCustomerDO> selectPageWithAdmin(CrmCustomerPageReqVO pageReqVO, Long userId) {
        // 情况一：管理员查看
        LambdaQueryWrapperX<CrmCustomerDO> queryWrapperX = new LambdaQueryWrapperX<>();
        appendQueryParams(queryWrapperX, pageReqVO, userId);
        return selectPage(pageReqVO, queryWrapperX
                .likeIfPresent(CrmCustomerDO::getName, pageReqVO.getName())
                .eqIfPresent(CrmCustomerDO::getMobile, pageReqVO.getMobile())
                .eqIfPresent(CrmCustomerDO::getIndustryId, pageReqVO.getIndustryId())
                .eqIfPresent(CrmCustomerDO::getLevel, pageReqVO.getLevel())
                .eqIfPresent(CrmCustomerDO::getSource, pageReqVO.getSource()));
    }

    default PageResult<CrmCustomerDO> selectPage(CrmCustomerPageReqVO pageReqVO, Long userId) {
        // 情况二：获取当前用户能看的分页数据
        IPage<CrmCustomerDO> mpPage = MyBatisUtils.buildPage(pageReqVO);
        MPJLambdaWrapperX<CrmCustomerDO> mpjLambdaWrapperX = new MPJLambdaWrapperX<>();
        // 构建数据权限连表条件
        mpjLambdaWrapperX
                .innerJoin(CrmPermissionDO.class, CrmPermissionDO::getBizId, CrmCustomerDO::getId)
                .eq(CrmPermissionDO::getBizType, CrmBizTypeEnum.CRM_CUSTOMER.getType())
                .eq(CrmPermissionDO::getUserId, userId);
        appendQueryParams(mpjLambdaWrapperX, pageReqVO, userId);
        mpjLambdaWrapperX
                .selectAll(CrmCustomerDO.class)
                .likeIfPresent(CrmCustomerDO::getName, pageReqVO.getName())
                .eqIfPresent(CrmCustomerDO::getMobile, pageReqVO.getMobile())
                .eqIfPresent(CrmCustomerDO::getIndustryId, pageReqVO.getIndustryId())
                .eqIfPresent(CrmCustomerDO::getLevel, pageReqVO.getLevel())
                .eqIfPresent(CrmCustomerDO::getSource, pageReqVO.getSource());
        // 特殊：不分页，直接查询全部
        if (PageParam.PAGE_SIZE_NONE.equals(pageReqVO.getPageNo())) {
            List<CrmCustomerDO> list = selectJoinList(CrmCustomerDO.class, mpjLambdaWrapperX);
            return new PageResult<>(list, (long) list.size());
        }
        mpPage = selectJoinPage(mpPage, CrmCustomerDO.class, mpjLambdaWrapperX);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    static void appendQueryParams(MPJLambdaWrapperX<CrmCustomerDO> mpjLambdaWrapperX, CrmCustomerPageReqVO pageReqVO, Long userId) {
        if (pageReqVO.getPool()) { // 情况一：公海
            mpjLambdaWrapperX.isNull(CrmCustomerDO::getOwnerUserId);
        } else { // 情况二：不是公海
            mpjLambdaWrapperX.isNotNull(CrmCustomerDO::getOwnerUserId);
        }
        // TODO 场景数据过滤
        if (CrmSceneEnum.isOwner(pageReqVO.getSceneType())) { // 场景一：我负责的数据
            mpjLambdaWrapperX.eq(CrmCustomerDO::getOwnerUserId, userId);
        }
    }

    static void appendQueryParams(LambdaQueryWrapperX<CrmCustomerDO> lambdaQueryWrapperX, CrmCustomerPageReqVO pageReqVO, Long userId) {
        if (pageReqVO.getPool()) { // 情况一：公海
            lambdaQueryWrapperX.isNull(CrmCustomerDO::getOwnerUserId);
        } else { // 情况二：不是公海
            lambdaQueryWrapperX.isNotNull(CrmCustomerDO::getOwnerUserId);
        }
        // TODO 场景数据过滤
        if (CrmSceneEnum.isOwner(pageReqVO.getSceneType())) { // 场景一：我负责的数据
            lambdaQueryWrapperX.eq(CrmCustomerDO::getOwnerUserId, userId);
        }
    }

}
