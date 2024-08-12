package cn.iocoder.yudao.module.product.service.tag;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.tag.vo.ProductTagCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.tag.vo.ProductTagPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.tag.vo.ProductTagUpdateReqVO;
import cn.iocoder.yudao.module.product.convert.tag.ProductTagConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.tag.ProductTagDO;
import cn.iocoder.yudao.module.product.dal.mysql.tag.ProductTagMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ProductTagServiceImpl implements ProductTagService {

    @Resource
    private ProductTagMapper productTagMapper;

    @Override
    public Long createTag(ProductTagCreateReqVO createReqVO) {
        // 校验名称唯一
        validateTagNameUnique(null, createReqVO.getName());
        // 插入
        ProductTagDO tag = ProductTagConvert.INSTANCE.convert(createReqVO);
        productTagMapper.insert(tag);
        // 返回
        return tag.getId();
    }

    @Override
    public void updateTag(ProductTagUpdateReqVO updateReqVO) {
        // 校验存在
        validateTagExists(updateReqVO.getId());
        // 校验名称唯一
        validateTagNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 更新
        ProductTagDO updateObj = ProductTagConvert.INSTANCE.convert(updateReqVO);
        productTagMapper.updateById(updateObj);
    }

    @Override
    public void deleteTag(Long id) {
        // 校验存在
        validateTagExists(id);
        // 删除
        productTagMapper.deleteById(id);
    }

    private void validateTagExists(Long id) {
        if (productTagMapper.selectById(id) == null) {
            throw exception(TAG_NOT_EXISTS);
        }
    }

    private void validateTagNameUnique(Long id, String name) {
        if (StrUtil.isBlank(name)) {
            return;
        }
        ProductTagDO tag = productTagMapper.selectByName(name);
        if (tag == null) {
            return;
        }

        // 如果 id 为空，说明不用比较是否为相同 id 的标签
        if (id == null) {
            throw exception(TAG_NAME_EXISTS);
        }
        if (!tag.getId().equals(id)) {
            throw exception(TAG_NAME_EXISTS);
        }
    }

    @Override
    public ProductTagDO getTag(Long id) {
        return productTagMapper.selectById(id);
    }

    @Override
    public List<ProductTagDO> getTagList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return productTagMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ProductTagDO> getTagPage(ProductTagPageReqVO pageReqVO) {
        return productTagMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProductTagDO> getTagList() {
        return productTagMapper.selectList();
    }

}
