package cn.iocoder.yudao.module.product.service.spu;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuUpdateStatusReqVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import org.springframework.scheduling.annotation.Async;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 商品 SPU Service 接口
 *
 * @author 芋道源码
 */
public interface ProductSpuService {

    /**
     * 创建商品 SPU
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSpu(@Valid ProductSpuSaveReqVO createReqVO);

    /**
     * 更新商品 SPU
     *
     * @param updateReqVO 更新信息
     */
    void updateSpu(@Valid ProductSpuSaveReqVO updateReqVO);

    /**
     * 删除商品 SPU
     *
     * @param id 编号
     */
    void deleteSpu(Long id);

    /**
     * 获得商品 SPU
     *
     * @param id 编号
     * @return 商品 SPU
     */
    ProductSpuDO getSpu(Long id);

    /**
     * 获得商品 SPU 列表
     *
     * @param ids 编号数组
     * @return 商品 SPU 列表
     */
    List<ProductSpuDO> getSpuList(Collection<Long> ids);

    /**
     * 获得商品 SPU Map
     *
     * @param ids 编号数组
     * @return 商品 SPU Map
     */
    default Map<Long, ProductSpuDO> getSpuMap(Collection<Long> ids) {
        List<ProductSpuDO> list = getSpuList(ids);
        return CollectionUtils.convertMap(list, ProductSpuDO::getId);
    }

    /**
     * 获得指定状态的商品 SPU 列表
     *
     * @param status 状态
     * @return 商品 SPU 列表
     */
    List<ProductSpuDO> getSpuListByStatus(Integer status);

    /**
     * 获得商品 SPU 分页，提供给挂你兰后台使用
     *
     * @param pageReqVO 分页查询
     * @return 商品spu分页
     */
    PageResult<ProductSpuDO> getSpuPage(ProductSpuPageReqVO pageReqVO);

    /**
     * 获得商品 SPU 分页，提供给用户 App 使用
     *
     * @param pageReqVO 分页查询
     * @return 商品 SPU 分页
     */
    PageResult<ProductSpuDO> getSpuPage(AppProductSpuPageReqVO pageReqVO);

    /**
     * 更新商品 SPU 库存（增量）
     *
     * @param stockIncrCounts SPU 编号与库存变化（增量）的映射
     */
    void updateSpuStock(Map<Long, Integer> stockIncrCounts);

    /**
     * 更新 SPU 状态
     *
     * @param updateReqVO 更新请求
     */
    void updateSpuStatus(ProductSpuUpdateStatusReqVO updateReqVO);

    /**
     * 获取 SPU 列表标签对应的 Count 数量
     *
     * @return Count 数量
     */
    Map<Integer, Long> getTabsCount();

    /**
     * 通过分类 categoryId 查询 SPU 个数
     *
     * @param categoryId 分类 categoryId
     * @return SPU 数量
     */
    Long getSpuCountByCategoryId(Long categoryId);


    /**
     * 校验商品是否有效。如下情况，视为无效：
     * 1. 商品编号不存在
     * 2. 商品被禁用
     *
     * @param ids 商品编号数组
     * @return 商品 SPU 列表
     */
    List<ProductSpuDO> validateSpuList(Collection<Long> ids);

    /**
     * 更新商品 SPU 浏览量
     *
     * @param id        商品 SPU 编号
     * @param incrCount 增加的数量
     */
    @Async
    void updateBrowseCount(Long id, int incrCount);

}
