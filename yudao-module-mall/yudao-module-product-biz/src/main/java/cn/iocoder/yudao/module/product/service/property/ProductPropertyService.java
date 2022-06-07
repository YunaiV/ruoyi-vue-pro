package cn.iocoder.yudao.module.product.service.property;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 规格名称 Service 接口
 *
 * @author 芋道源码
 */
public interface ProductPropertyService {

    /**
     * 创建规格名称
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProperty(@Valid ProductPropertyCreateReqVO createReqVO);

    /**
     * 更新规格名称
     *
     * @param updateReqVO 更新信息
     */
    void updateProperty(@Valid ProductPropertyUpdateReqVO updateReqVO);

    /**
     * 删除规格名称
     *
     * @param id 编号
     */
    void deleteProperty(Long id);

    /**
     * 获得规格名称
     *
     * @param id 编号
     * @return 规格名称
     */
    ProductPropertyDO getProperty(Long id);

    /**
     * 获得规格名称列表
     *
     * @param ids 编号
     * @return 规格名称列表
     */
    List<ProductPropertyDO> getPropertyList(Collection<Long> ids);

    /**
     * 获得规格名称分页
     *
     * @param pageReqVO 分页查询
     * @return 规格名称分页
     */
    PageResult<ProductPropertyDO> getPropertyPage(ProductPropertyPageReqVO pageReqVO);

    /**
     * 获得规格名称列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 规格名称列表
     */
    List<ProductPropertyDO> getPropertyList(ProductPropertyExportReqVO exportReqVO);

    /**
     * 获取属性及属性值列表 分页
     * @param pageReqVO
     * @return
     */
    PageResult<ProductPropertyRespVO> getPropertyListPage(ProductPropertyPageReqVO pageReqVO);

    ProductPropertyRespVO getPropertyResp(Long id);

    /**
     * 根据数据名id集合查询属性名以及属性值的集合
     * @param propertyIds 属性名id集合
     * @return
     */
    List<ProductPropertyRespVO> selectByIds(List<Long> propertyIds);
}
