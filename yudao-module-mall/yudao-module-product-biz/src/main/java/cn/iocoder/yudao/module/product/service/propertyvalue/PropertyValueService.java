package cn.iocoder.yudao.module.product.service.propertyvalue;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.propertyvalue.PropertyValueDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 规格值 Service 接口
 *
 * @author 芋道源码
 */
public interface PropertyValueService {

    /**
     * 创建规格值
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createPropertyValue(@Valid PropertyValueCreateReqVO createReqVO);

    /**
     * 更新规格值
     *
     * @param updateReqVO 更新信息
     */
    void updatePropertyValue(@Valid PropertyValueUpdateReqVO updateReqVO);

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
    PropertyValueDO getPropertyValue(Integer id);

    /**
     * 获得规格值列表
     *
     * @param ids 编号
     * @return 规格值列表
     */
    List<PropertyValueDO> getPropertyValueList(Collection<Integer> ids);

    /**
     * 获得规格值分页
     *
     * @param pageReqVO 分页查询
     * @return 规格值分页
     */
    PageResult<PropertyValueDO> getPropertyValuePage(PropertyValuePageReqVO pageReqVO);

    /**
     * 获得规格值列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 规格值列表
     */
    List<PropertyValueDO> getPropertyValueList(PropertyValueExportReqVO exportReqVO);

}
