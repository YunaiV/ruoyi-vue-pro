package cn.iocoder.yudao.module.iot.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategoryPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategorySaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductCategoryDO;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * IoT 产品分类 Service 接口
 *
 * @author 芋道源码
 */
public interface IotProductCategoryService {

    /**
     * 创建产品分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductCategory(@Valid IotProductCategorySaveReqVO createReqVO);

    /**
     * 更新产品分类
     *
     * @param updateReqVO 更新信息
     */
    void updateProductCategory(@Valid IotProductCategorySaveReqVO updateReqVO);

    /**
     * 删除产品分类
     *
     * @param id 编号
     */
    void deleteProductCategory(Long id);

    /**
     * 获得产品分类
     *
     * @param id 编号
     * @return 产品分类
     */
    IotProductCategoryDO getProductCategory(Long id);

    /**
     * 获得产品分类列表
     *
     * @param ids 编号
     * @return 产品分类列表
     */
    List<IotProductCategoryDO> getProductCategoryList(Collection<Long> ids);

    /**
     * 获得产品分类 Map
     *
     * @param ids 编号
     * @return 产品分类 Map
     */
    default Map<Long, IotProductCategoryDO> getProductCategoryMap(Collection<Long> ids) {
        return convertMap(getProductCategoryList(ids), IotProductCategoryDO::getId);
    }

    /**
     * 获得产品分类分页
     *
     * @param pageReqVO 分页查询
     * @return 产品分类分页
     */
    PageResult<IotProductCategoryDO> getProductCategoryPage(IotProductCategoryPageReqVO pageReqVO);

    /**
     * 获得产品分类列表，根据状态
     *
     * @param status 状态
     * @return 产品分类列表
     */
    List<IotProductCategoryDO> getProductCategoryListByStatus(Integer status);

    /**
     * 获得产品分类数量
     *
     * @param createTime 创建时间，如果为空，则统计所有分类数量
     * @return 产品分类数量
     */
    Long getProductCategoryCount(@Nullable LocalDateTime createTime);

    /**
     * 获得各个品类下设备数量统计，其中 key 是产品分类名
     *
     * @return 品类设备统计列表
     */
    Map<String, Integer> getProductCategoryDeviceCountMap();

}