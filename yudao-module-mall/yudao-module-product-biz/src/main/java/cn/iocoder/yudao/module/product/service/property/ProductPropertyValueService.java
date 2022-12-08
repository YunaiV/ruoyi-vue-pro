package cn.iocoder.yudao.module.product.service.property;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValuePageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueRespVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueUpdateReqVO;
import cn.iocoder.yudao.module.product.service.property.bo.ProductPropertyValueDetailRespBO;

import java.util.Collection;
import java.util.List;

/**
 * 商品属性值 Service 接口
 *
 * @author LuoWenFeng
 */
public interface ProductPropertyValueService {

    /**
     * 创建属性值
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPropertyValue(ProductPropertyValueCreateReqVO createReqVO);

    /**
     * 更新属性值
     *
     * @param updateReqVO 更新信息
     */
    void updatePropertyValue(ProductPropertyValueUpdateReqVO updateReqVO);

    /**
     * 删除属性值
     *
     * @param id 编号
     */
    void deletePropertyValue(Long id);

    /**
     * 获得属性值
     *
     * @param id 编号
     * @return 属性值
     */
    ProductPropertyValueRespVO getPropertyValue(Long id);

    /**
     * 根据编号数组，获得属性值列表
     *
     * @param ids 编号数组
     * @return 属性值明细列表
     */
    List<ProductPropertyValueDetailRespBO> getPropertyValueDetailList(Collection<Long> ids);

    /**
     * 获得属性值
     *
     * @param id 编号
     * @return 属性值
     */
    List<ProductPropertyValueRespVO> getPropertyValueListByPropertyId(List<Long> id);

    /**
     * 获取属性值的分页
     *
     * @param pageReqVO 查询条件
     * @return 属性值的分页
     */
    PageResult<ProductPropertyValueRespVO> getPropertyValueListPage(ProductPropertyValuePageReqVO pageReqVO);
}
