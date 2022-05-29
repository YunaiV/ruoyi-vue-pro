package cn.iocoder.yudao.module.product.service.propertyvalue;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.propertyvalue.ProductPropertyValueDO;

/**
 * 规格值 Service 接口
 *
 * @author 芋道源码
 */
public interface ProductPropertyValueService {

    /**
     * 创建规格值
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createPropertyValue(@Valid ProductPropertyValueCreateReqVO createReqVO);

    /**
     * 更新规格值
     *
     * @param updateReqVO 更新信息
     */
    void updatePropertyValue(@Valid ProductPropertyValueUpdateReqVO updateReqVO);

    /**
     * 删除规格值
     *
     * @param id 编号
     */
    void deletePropertyValue(Integer id);

    /**
     * 获得规格值
     *
     * @param id 编号
     * @return 规格值
     */
    ProductPropertyValueDO getPropertyValue(Integer id);

    /**
     * 获得规格值列表
     *
     * @param ids 编号
     * @return 规格值列表
     */
    List<ProductPropertyValueDO> getPropertyValueList(Collection<Integer> ids);

    /**
     * 批量插入属性值
     * @param propertyValues
     */
    void batchInsert(List<ProductPropertyValueDO> propertyValues);

    /**
     * 根据属性id查询
     * @param propertyIds
     * @return
     */
    List<ProductPropertyValueDO> getPropertyValueListByPropertyId(List<Long> propertyIds);

    /**
     * 根据属性id 删除
     * @param propertyId
     */
    void deletePropertyValueByPropertyId(Long propertyId);
}
