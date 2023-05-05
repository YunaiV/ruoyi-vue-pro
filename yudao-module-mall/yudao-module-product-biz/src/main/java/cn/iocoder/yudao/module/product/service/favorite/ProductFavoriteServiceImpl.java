package cn.iocoder.yudao.module.product.service.favorite;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoritePageReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteReqVO;
import cn.iocoder.yudao.module.product.convert.favorite.ProductFavoriteConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDO;
import cn.iocoder.yudao.module.product.dal.mysql.favorite.ProductFavoriteMapper;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteRespVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.COLLECTION_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.COLLECTION_NOT_EXISTS;

/**
 * 商品收藏 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
public class ProductFavoriteServiceImpl implements ProductFavoriteService {

    @Resource
    private ProductFavoriteMapper mapper;

    @Override
    public Boolean collect(@Valid AppFavoriteReqVO reqVO) {
        // TODO @jason：userId 要从 Controller 传递过来，Service 不能有转台
        Long userId = getLoginUserId();
       // TODO @jason：代码缩进不对；
        ProductFavoriteDO favoriteDO = mapper.selectByUserAndSpuAndType(userId, reqVO.getSpuId(), reqVO.getType());
        if (Objects.nonNull(favoriteDO)) {
            throw exception(COLLECTION_EXISTS);
        }

        // TODO @jason：插入只有成功，不用判断 1
        ProductFavoriteDO entity = ProductFavoriteConvert.INSTANCE.convert(userId, reqVO);
        int count = mapper.insert(entity);
        return count == 1;
    }

    @Override
    public Boolean cancelCollect(@Valid AppFavoriteReqVO reqVO) {
        // TODO @jason：代码缩进不对；
        Long loginUserId = getLoginUserId();
        ProductFavoriteDO favoriteDO = mapper.selectByUserAndSpuAndType(loginUserId, reqVO.getSpuId(), reqVO.getType());
        if (Objects.isNull(favoriteDO)) {
            throw exception(COLLECTION_NOT_EXISTS);
        }
        // TODO @jason：插入只有成功，不用判断 1
        int count = mapper.deleteById(favoriteDO.getId());
        return count == 1;
    }

    @Override
    public PageResult<AppFavoriteRespVO> pageCollectList(@Valid AppFavoritePageReqVO reqVO) {
        Long userId = getLoginUserId();
        return mapper.selectPageByUserAndType(userId, reqVO.getType(), reqVO);
    }

}
