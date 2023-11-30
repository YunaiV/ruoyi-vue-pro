package cn.iocoder.yudao.module.crm.dal.mysql.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

/**
 * 商机 Mapper
 *
 * @author ljlleo
 */
@Mapper
public interface CrmBusinessMapper extends BaseMapperX<CrmBusinessDO> {

    default PageResult<CrmBusinessDO> selectPage(CrmBusinessPageReqVO reqVO, Collection<Long> ids) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmBusinessDO>()
                .in(CrmBusinessDO::getId, ids)
                .likeIfPresent(CrmBusinessDO::getName, reqVO.getName())
                .orderByDesc(CrmBusinessDO::getId));
    }

    default PageResult<CrmBusinessDO> selectPageByCustomer(CrmContractPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmBusinessDO>()
                .eq(CrmBusinessDO::getCustomerId, reqVO.getCustomerId()) // 必须传递
                .likeIfPresent(CrmBusinessDO::getName, reqVO.getName())
                .orderByDesc(CrmBusinessDO::getId));
    }

}
