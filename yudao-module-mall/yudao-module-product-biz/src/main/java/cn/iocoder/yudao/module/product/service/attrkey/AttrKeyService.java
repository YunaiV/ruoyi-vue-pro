package cn.iocoder.yudao.module.product.service.attrkey;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.product.controller.admin.attrkey.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.attrkey.AttrKeyDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 规格名称 Service 接口
 *
 * @author 芋道源码
 */
public interface AttrKeyService {

    /**
     * 创建规格名称
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createAttrKey(@Valid AttrKeyCreateReqVO createReqVO);

    /**
     * 更新规格名称
     *
     * @param updateReqVO 更新信息
     */
    void updateAttrKey(@Valid AttrKeyUpdateReqVO updateReqVO);

    /**
     * 删除规格名称
     *
     * @param id 编号
     */
    void deleteAttrKey(Integer id);

    /**
     * 获得规格名称
     *
     * @param id 编号
     * @return 规格名称
     */
    AttrKeyDO getAttrKey(Integer id);

    /**
     * 获得规格名称列表
     *
     * @param ids 编号
     * @return 规格名称列表
     */
    List<AttrKeyDO> getAttrKeyList(Collection<Integer> ids);

    /**
     * 获得规格名称分页
     *
     * @param pageReqVO 分页查询
     * @return 规格名称分页
     */
    PageResult<AttrKeyDO> getAttrKeyPage(AttrKeyPageReqVO pageReqVO);

    /**
     * 获得规格名称列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 规格名称列表
     */
    List<AttrKeyDO> getAttrKeyList(AttrKeyExportReqVO exportReqVO);

}
