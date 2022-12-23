package cn.iocoder.yudao.module.product.service.property;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValuePageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueRespVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueUpdateReqVO;

import java.util.List;

/**
 * <p>
 * 规格值 Service 接口
 * </p>
 *
 * @author LuoWenFeng
 */
public interface ProductPropertyValueService {

    /**
     * 创建规格值
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPropertyValue(ProductPropertyValueCreateReqVO createReqVO);

    /**
     * 更新规格值
     *
     * @param updateReqVO 更新信息
     */
    void updatePropertyValue(ProductPropertyValueUpdateReqVO updateReqVO);

    /**
     * 删除规格值
     *
     * @param id 编号
     */
    void deletePropertyValue(Long id);

    /**
     * 获得规格值
     *
     * @param id 编号
     * @return 规格名称
     */
    ProductPropertyValueRespVO getPropertyValue(Long id);

    /**
     * 获得规格值
     *
     * @param id 编号
     * @return 规格名称
     */
    List<ProductPropertyValueRespVO> getPropertyValueListByPropertyId(List<Long> id);

    /**
     * 获取规格值 分页
     *
     * @param pageReqVO 查询条件
     * @return
     */
    PageResult<ProductPropertyValueRespVO> getPropertyValueListPage(ProductPropertyValuePageReqVO pageReqVO);
}
