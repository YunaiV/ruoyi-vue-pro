package cn.iocoder.yudao.module.srm.dal.mysql.purchase.payment.term;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo.SrmPaymentTermPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.payment.term.SrmPaymentTermDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 付款条款 Mapper
 *
 * @author wdy
 */
@Mapper
public interface SrmPaymentTermMapper extends BaseMapperX<SrmPaymentTermDO> {

    default PageResult<SrmPaymentTermDO> selectPage(SrmPaymentTermPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SrmPaymentTermDO>()
            .betweenIfPresent(SrmPaymentTermDO::getCreateTime, reqVO.getCreateTime())
            .likeIfPresent(SrmPaymentTermDO::getPaymentTermZh, reqVO.getPaymentTermZh())
            .likeIfPresent(SrmPaymentTermDO::getPaymentTermZhForeign, reqVO.getPaymentTermZhForeign())
            .likeIfPresent(SrmPaymentTermDO::getPaymentTermEnForeign, reqVO.getPaymentTermEnForeign())
            .likeIfPresent(SrmPaymentTermDO::getRemark, reqVO.getRemark())
            .orderByDesc(SrmPaymentTermDO::getId));
    }

}