package cn.iocoder.yudao.module.product.service.attrvalue;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.product.controller.admin.attrvalue.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.attrvalue.AttrValueDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 规格值 Service 接口
 *
 * @author 芋道源码
 */
public interface AttrValueService {

    /**
     * 创建规格值
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createAttrValue(@Valid AttrValueCreateReqVO createReqVO);

    /**
     * 更新规格值
     *
     * @param updateReqVO 更新信息
     */
    void updateAttrValue(@Valid AttrValueUpdateReqVO updateReqVO);

    /**
     * 删除规格值
     *
     * @param id 编号
     */
    void deleteAttrValue(Integer id);

    /**
     * 获得规格值
     *
     * @param id 编号
     * @return 规格值
     */
    AttrValueDO getAttrValue(Integer id);

    /**
     * 获得规格值列表
     *
     * @param ids 编号
     * @return 规格值列表
     */
    List<AttrValueDO> getAttrValueList(Collection<Integer> ids);

    /**
     * 获得规格值分页
     *
     * @param pageReqVO 分页查询
     * @return 规格值分页
     */
    PageResult<AttrValueDO> getAttrValuePage(AttrValuePageReqVO pageReqVO);

    /**
     * 获得规格值列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 规格值列表
     */
    List<AttrValueDO> getAttrValueList(AttrValueExportReqVO exportReqVO);

}
