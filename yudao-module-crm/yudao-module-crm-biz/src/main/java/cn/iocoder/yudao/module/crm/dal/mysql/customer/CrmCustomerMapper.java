package cn.iocoder.yudao.module.crm.dal.mysql.customer;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.util.CrmQueryPageUtils;
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

    default int updateOwnerUserIdById(Long id, Long ownerUserId) {
        return update(new LambdaUpdateWrapper<CrmCustomerDO>()
                .eq(CrmCustomerDO::getId, id)
                .set(CrmCustomerDO::getOwnerUserId, ownerUserId));
    }

    /**
     * 获取客户分页
     *
     * @param pageReqVO      请求
     * @param userId         用户编号
     * @param subordinateIds 下属用户编号
     * @param isAdmin        是否为管理
     * @return 客户分页数据
     */
    default PageResult<CrmCustomerDO> selectPage(CrmCustomerPageReqVO pageReqVO, Long userId, Collection<Long> subordinateIds, Boolean isAdmin) {
        IPage<CrmCustomerDO> mpPage = MyBatisUtils.buildPage(pageReqVO);
        MPJLambdaWrapperX<CrmCustomerDO> mpjLambdaWrapperX = new MPJLambdaWrapperX<>();
        // 构建数据权限连表条件
        CrmQueryPageUtils.builderQuery(mpjLambdaWrapperX, pageReqVO, userId,
                CrmBizTypeEnum.CRM_CUSTOMER.getType(), CrmCustomerDO::getId, subordinateIds, isAdmin);
        mpjLambdaWrapperX.selectAll(CrmCustomerDO.class)
                .likeIfPresent(CrmCustomerDO::getName, pageReqVO.getName())
                .eqIfPresent(CrmCustomerDO::getMobile, pageReqVO.getMobile())
                .eqIfPresent(CrmCustomerDO::getIndustryId, pageReqVO.getIndustryId())
                .eqIfPresent(CrmCustomerDO::getLevel, pageReqVO.getLevel())
                .eqIfPresent(CrmCustomerDO::getSource, pageReqVO.getSource());
        // 特殊：不分页，直接查询全部
        // TODO @puhui999：下面这个，封装一个方法；从 56 到 61 这里哈；
        if (PageParam.PAGE_SIZE_NONE.equals(pageReqVO.getPageNo())) {
            List<CrmCustomerDO> list = selectJoinList(CrmCustomerDO.class, mpjLambdaWrapperX);
            return new PageResult<>(list, (long) list.size());
        }
        mpPage = selectJoinPage(mpPage, CrmCustomerDO.class, mpjLambdaWrapperX);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

}
