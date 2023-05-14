package cn.iocoder.yudao.module.product.service.favorite;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoritePageReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteReqVO;
import cn.iocoder.yudao.module.product.convert.favorite.ProductFavoriteConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.favorite.ProductFavoriteMapper;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteRespVO;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.FAVORITE_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.FAVORITE_NOT_EXISTS;

/**
 * 商品收藏 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
public class ProductFavoriteServiceImpl implements ProductFavoriteService {

    @Resource
    private ProductSpuService productSpuService;

    // TODO @JASON：ProductFavoriteMapper 最好全一点哈。productFavoriteMapper
    @Resource
    private ProductFavoriteMapper mapper;

    @Override
    public Boolean createFavorite(Long userId, @Valid AppFavoriteReqVO reqVO) {
        // TODO @JASON：userId 校验可以去掉，直接在 Controller 要求登录，通过 @PreAuthenticated 注解
        Assert.notNull(userId, "the userId must not be null");
        ProductFavoriteDO favoriteDO = mapper.selectByUserAndSpuAndType(userId, reqVO.getSpuId(), reqVO.getType());
        if (Objects.nonNull(favoriteDO)) {
            throw exception(FAVORITE_EXISTS);
        }
        ProductFavoriteDO entity = ProductFavoriteConvert.INSTANCE.convert(userId, reqVO);
        mapper.insert(entity);
        // TODO @JASON：可以返回 Long id；
        return Boolean.TRUE;
    }

    @Override
    public Boolean deleteFavorite(Long userId, @Valid AppFavoriteReqVO reqVO) {
        Assert.notNull(userId, "the userId must not be null ");
        ProductFavoriteDO favoriteDO = mapper.selectByUserAndSpuAndType(userId, reqVO.getSpuId(), reqVO.getType());
        if (Objects.isNull(favoriteDO)) {
            throw exception(FAVORITE_NOT_EXISTS);
        }
        mapper.deleteById(favoriteDO.getId());
        // TODO 可以不返回
        return Boolean.TRUE;
    }

    @Override
    public PageResult<AppFavoriteRespVO> getFavoritePage(Long userId, @Valid AppFavoritePageReqVO reqVO) {
        Assert.notNull(userId, "the userId must not be null ");
        PageResult<ProductFavoriteDO> favorites = mapper.selectPageByUserAndType(userId, reqVO.getType(), reqVO);
        // TODO @Jason：if return 更好；例如说，如果判断是空，就 return 掉；
        if (favorites.getTotal() > 0) {
            PageResult<AppFavoriteRespVO> pageResult = new PageResult<>(favorites.getTotal());
            List<ProductFavoriteDO> list = favorites.getList();
            // 得到商品 spu 信息
            List<Long> spuIds = CollectionUtils.convertList(list, ProductFavoriteDO::getSpuId);
            // TODO @Jason：这里可以用默认的方法，不用 , val -> val 拉
            Map<Long, ProductSpuDO> spuMap = CollectionUtils.convertMap(productSpuService.getSpuList(spuIds), ProductSpuDO::getId, val -> val);
            List<AppFavoriteRespVO> resultList = new ArrayList<>(list.size());
            for (ProductFavoriteDO item : list) {
                ProductSpuDO spuDO = spuMap.get(item.getSpuId());
                // TODO @Jason：可以让 convert 整个 convert 放到 mapstruct 里。convert 就是用于各种数据转换，不带逻辑的那种。
                resultList.add(ProductFavoriteConvert.INSTANCE.convert(spuDO, item));
            }
            pageResult.setList(resultList);
            return pageResult;
        }else {
            return PageResult.empty();
        }
    }

    @Override
    public Boolean checkFavorite(Long userId, @Valid AppFavoriteReqVO reqVO) {
        Assert.notNull(userId, "the userId must not be null ");
        ProductFavoriteDO favoriteDO = mapper.selectByUserAndSpuAndType(userId, reqVO.getSpuId(), reqVO.getType());
        return Objects.nonNull(favoriteDO);
    }

}
