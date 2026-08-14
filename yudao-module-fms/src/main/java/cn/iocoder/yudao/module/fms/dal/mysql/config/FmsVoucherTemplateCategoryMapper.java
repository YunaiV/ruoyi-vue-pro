package cn.iocoder.yudao.module.fms.dal.mysql.config;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherTemplateCategoryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * FMS 凭证模板分类 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsVoucherTemplateCategoryMapper extends BaseMapperX<FmsVoucherTemplateCategoryDO> {

    default List<FmsVoucherTemplateCategoryDO> selectListByAccountSetId(Long accountSetId) {
        return selectList(new LambdaQueryWrapperX<FmsVoucherTemplateCategoryDO>()
                .eq(FmsVoucherTemplateCategoryDO::getAccountSetId, accountSetId)
                .orderByAsc(FmsVoucherTemplateCategoryDO::getId));
    }

    default FmsVoucherTemplateCategoryDO selectByAccountSetIdAndName(Long accountSetId, String name) {
        return selectOne(FmsVoucherTemplateCategoryDO::getAccountSetId, accountSetId,
                FmsVoucherTemplateCategoryDO::getName, name);
    }

}
