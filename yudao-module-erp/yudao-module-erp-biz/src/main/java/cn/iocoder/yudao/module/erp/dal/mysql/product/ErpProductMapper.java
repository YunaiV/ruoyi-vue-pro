package cn.iocoder.yudao.module.erp.dal.mysql.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

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
            .betweenIfPresent(ErpProductDO::getCreateTime, reqVO.getCreateTime()) // 添加创建时间查询
            .betweenIfPresent(ErpProductDO::getUpdateTime, reqVO.getUpdateTime())  // 添加修改时间查询
            .likeIfExists(ErpProductDO::getName, reqVO.getName())
            .eqIfExists(ErpProductDO::getCategoryId, reqVO.getCategoryId())
            .likeIfExists(ErpProductDO::getBarCode, reqVO.getBarCode())
            .likeIfExists(ErpProductDO::getBrand, reqVO.getBrand())
            .eqIfExists(ErpProductDO::getDeptId, reqVO.getDeptId())
            .likeIfExists(ErpProductDO::getSeries, reqVO.getSeries())
            .eqIfExists(ErpProductDO::getStatus, reqVO.getStatus())
            .likeIfExists(ErpProductDO::getPackageWidth, reqVO.getPackageWidth())  // 包装宽度查询
            .likeIfExists(ErpProductDO::getPackageLength, reqVO.getPackageLength())  // 包装长度查询
            .likeIfExists(ErpProductDO::getPackageHeight, reqVO.getPackageHeight())  // 包装高度查询
            .likeIfExists(ErpProductDO::getPackageWeight, reqVO.getPackageWeight())  // 包装重量查询
            .likeIfExists(ErpProductDO::getWidth, reqVO.getWidth())  // 宽度查询
            .likeIfExists(ErpProductDO::getLength, reqVO.getLength()) // 长度查询
            .likeIfExists(ErpProductDO::getHeight, reqVO.getHeight())  // 高度查询
            .likeIfExists(ErpProductDO::getWeight, reqVO.getWeight())  // 重量查询
            // 创建人 模糊
            .eqIfExists(ErpProductDO::getCreator, reqVO.getCreator())
            // 更新人 模糊
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
        return selectOne(ErpProductDO::getBarCode, code);
    }

    default List<ErpProductDO> selectListByStatus(Boolean status) {
        return selectList(ErpProductDO::getStatus, status);
    }

    /**
     * @return java.lang.Integer
     * @Author Wqh
     * @Description 根据颜色，系列，型号查询出最大的流水号
     * @Date 13:36 2024/10/21
     * @Param [barCode]
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
     * 根据条码(barCode)查询出id列表
     *
     * @param barCode SKU（编码
     * @return id集合
     */
    default List<Long> selectIdListByBarCode(String barCode) {
        return selectList(new LambdaQueryWrapperX<ErpProductDO>()
            .like(ErpProductDO::getBarCode, barCode)
            .select(ErpProductDO::getId))
            .stream().map(ErpProductDO::getId).toList();
    }
}