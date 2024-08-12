package cn.iocoder.yudao.module.product.service.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.tag.vo.ProductTagCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.tag.vo.ProductTagPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.tag.vo.ProductTagUpdateReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.tag.ProductTagDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 商品标签 Service 接口
 */
public interface ProductTagService {

    /**
     * 创建商品标签
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTag(@Valid ProductTagCreateReqVO createReqVO);

    /**
     * 更新商品标签
     *
     * @param updateReqVO 更新信息
     */
    void updateTag(@Valid ProductTagUpdateReqVO updateReqVO);

    /**
     * 删除商品标签
     *
     * @param id 编号
     */
    void deleteTag(Long id);

    /**
     * 获得商品标签
     *
     * @param id 编号
     * @return 商品标签
     */
    ProductTagDO getTag(Long id);

    /**
     * 获得商品标签列表
     *
     * @param ids 编号
     * @return 商品标签列表
     */
    List<ProductTagDO> getTagList(Collection<Long> ids);

    /**
     * 获得商品标签分页
     *
     * @param pageReqVO 分页查询
     * @return 商品标签分页
     */
    PageResult<ProductTagDO> getTagPage(ProductTagPageReqVO pageReqVO);

    /**
     * 获取标签列表
     *
     * @return 标签列表
     */
    List<ProductTagDO> getTagList();

}
