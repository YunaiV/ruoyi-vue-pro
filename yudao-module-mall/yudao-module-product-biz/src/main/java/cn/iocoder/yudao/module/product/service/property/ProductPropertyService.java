package cn.iocoder.yudao.module.product.service.property;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.property.ProductPropertyRespVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.property.*;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

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
     * 获得规格名称列表
     * @param listReqVO 集合查询
     * @return 规格名称集合
     */
    List<ProductPropertyRespVO> getPropertyList(ProductPropertyListReqVO listReqVO);

    /**
     * 获取属性名称分页
     *
     * @param pageReqVO 分页条件
     * @return 规格名称分页
     */
    PageResult<ProductPropertyRespVO> getPropertyPage(ProductPropertyPageReqVO pageReqVO);

    /**
     * 获得指定编号的规格名称
     *
     * @param id 编号
     * @return 规格名称
     */
    ProductPropertyRespVO getProperty(Long id);

    /**
     * 根据规格属性编号的集合，获得对应的规格 + 规格值的集合
     *
     * @param ids 规格编号的集合
     * @return 对应的规格
     */
    List<ProductPropertyRespVO> getPropertyList(Collection<Long> ids);

    /**
     * 获得规格名称 + 值的列表
     *
     * @param listReqVO 列表查询
     * @return 规格名称 + 值的列表
     */
    List<ProductPropertyAndValueRespVO> getPropertyAndValueList(ProductPropertyListReqVO listReqVO);

}
