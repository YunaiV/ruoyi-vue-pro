package cn.iocoder.yudao.module.erp.dal.mysql.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * ERP 产品 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpProductMapper extends BaseMapperX<ErpProductDO> {

    default PageResult<ErpProductDO> selectPage(ErpProductPageReqVO reqVO) {
        MPJLambdaWrapper<ErpProductDO> orderByDesc = new MPJLambdaWrapperX<ErpProductDO>()
            .betweenIfPresent(ErpProductDO::getCreateTime, reqVO.getCreateTime())
            .betweenIfPresent(ErpProductDO::getUpdateTime, reqVO.getUpdateTime())
            .eqIfExists(ErpProductDO::getCategoryId, reqVO.getCategoryId())
            .likeIfExists(ErpProductDO::getCode, reqVO.getCode())
            .likeIfExists(ErpProductDO::getBrand, reqVO.getBrand())
            .eqIfExists(ErpProductDO::getDeptId, reqVO.getDeptId())
            .likeIfExists(ErpProductDO::getSeries, reqVO.getSeries())
            .eqIfExists(ErpProductDO::getStatus, reqVO.getStatus())
            .likeIfExists(ErpProductDO::getPackageWidth, reqVO.getPackageWidth())
            .likeIfExists(ErpProductDO::getPackageLength, reqVO.getPackageLength())
            .likeIfExists(ErpProductDO::getPackageHeight, reqVO.getPackageHeight())
            .likeIfExists(ErpProductDO::getPackageWeight, reqVO.getPackageWeight())
            .likeIfExists(ErpProductDO::getWidth, reqVO.getWidth())
            .likeIfExists(ErpProductDO::getLength, reqVO.getLength())
            .likeIfExists(ErpProductDO::getHeight, reqVO.getHeight())
            .likeIfExists(ErpProductDO::getWeight, reqVO.getWeight())
            .eqIfExists(ErpProductDO::getCreator, reqVO.getCreator())
            .eqIfExists(ErpProductDO::getUpdater, reqVO.getUpdater())
            .orderByDesc(ErpProductDO::getId);
        return selectPage(reqVO, orderByDesc);
    }

    default Long selectCountByCategoryId(Long categoryId) {
        return selectCount(ErpProductDO::getCategoryId, categoryId);
    }

    default Long selectCountByUnitId(Long unitId) {
        return selectCount(ErpProductDO::getUnitId, unitId);
    }

    default ErpProductDO selectByCode(String code) {
        return selectOne(ErpProductDO::getCode, code);
    }

    default List<ErpProductDO> selectListByStatus(Boolean status) {
        return selectList(ErpProductDO::getStatus, status);
    }

    /**
     * @return java.lang.Integer
     * @Author Wqh
     * @Description 根据颜色，系列，型号查询出最大的流水号
     * @Date 13:36 2024/10/21
     * @Param [code]
     **/
    default ErpProductDO selectMaxSerialByColorAndModelAndSeries(String color, String model, String series) {
        return selectOne(new LambdaQueryWrapperX<ErpProductDO>()
            .eqIfPresent(ErpProductDO::getColor, color)
            .eqIfPresent(ErpProductDO::getModel, model)
            .eqIfPresent(ErpProductDO::getSeries, series)
            .orderByDesc(ErpProductDO::getSerial)
            .select(ErpProductDO::getSerial)
            .last("limit 1"));
    }

    default List<ErpProductDO> selectByColorAndSeriesAndModel(String color, String model, String series) {
        return selectList(new LambdaQueryWrapperX<ErpProductDO>()
            .eqIfPresent(ErpProductDO::getColor, color)
            .eqIfPresent(ErpProductDO::getModel, model)
            .eqIfPresent(ErpProductDO::getSeries, series));
    }

    default ErpProductDO selectByName(String name) {
        return selectOne(ErpProductDO::getName, name);
    }

    /**
     * 根据条码(code)查询出id列表
     *
     * @param code SKU（编码
     * @return id集合
     */
    default List<Long> selectIdListByCode(String code) {
        return selectList(new LambdaQueryWrapperX<ErpProductDO>()
            .eq(ErpProductDO::getCode, code)
            .select(ErpProductDO::getId))
            .stream().map(ErpProductDO::getId).toList();
    }

    /**
     * 根据条码(code)查询出id列表
     *
     * @param codes SKU（编码
     * @return ErpProductDO 集合
     */
    default List<ErpProductDO> selectByCodes(Collection<String> codes){
        return selectList(new LambdaQueryWrapperX<ErpProductDO>()
            .in(ErpProductDO::getCode, codes));
    }
}