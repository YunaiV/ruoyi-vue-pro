package cn.iocoder.yudao.module.crm.dal.mysql.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.product.CrmProductPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.util.CrmQueryWrapperUtils;
import org.apache.ibatis.annotations.Mapper;

/**
 * CRM 产品 Mapper
 *
 * @author ZanGe丶
 */
@Mapper
public interface CrmProductMapper extends BaseMapperX<CrmProductDO> {

    default PageResult<CrmProductDO> selectPage(CrmProductPageReqVO reqVO, Long userId) {
        MPJLambdaWrapperX<CrmProductDO> query = new MPJLambdaWrapperX<>();
        // 拼接数据权限的查询条件
        CrmQueryWrapperUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_PRODUCT.getType(),
                CrmProductDO::getId, userId, null, Boolean.FALSE);
        // 拼接自身的查询条件
        query.selectAll(CrmProductDO.class)
                .likeIfPresent(CrmProductDO::getName, reqVO.getName())
                .eqIfPresent(CrmProductDO::getStatus, reqVO.getStatus())
                .orderByDesc(CrmProductDO::getId);
        return selectJoinPage(reqVO, CrmProductDO.class, query);
    }

    default CrmProductDO selectByNo(String no) {
        return selectOne(CrmProductDO::getNo, no);
    }

}
