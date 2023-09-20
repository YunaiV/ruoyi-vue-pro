package cn.iocoder.yudao.module.promotion.service.bargain;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.BargainActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.BargainActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.BargainActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.bargain.BargainActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.bargain.BargainActivityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.anyMatch;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;

/**
 * 砍价活动 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class BargainActivityServiceImpl implements BargainActivityService {

    @Resource
    private BargainActivityMapper bargainActivityMapper;

    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBargainActivity(BargainActivityCreateReqVO createReqVO) {
        // 校验商品 SPU 是否存在是否参加的别的活动
        validateBargainConflict(createReqVO.getSpuId(), null);
        // 校验商品 sku 是否存在
        validateSku(createReqVO.getSkuId());

        // 插入砍价活动
        BargainActivityDO activityDO = BargainActivityConvert.INSTANCE.convert(createReqVO)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setSuccessCount(0);
        bargainActivityMapper.insert(activityDO);
        return activityDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBargainActivity(BargainActivityUpdateReqVO updateReqVO) {
        // 校验存在
        BargainActivityDO activityDO = validateBargainActivityExists(updateReqVO.getId());
        // 校验状态
        if (ObjectUtil.equal(activityDO.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(BARGAIN_ACTIVITY_STATUS_DISABLE);
        }
        // 校验商品冲突
        validateBargainConflict(updateReqVO.getSpuId(), updateReqVO.getId());
        // 校验商品 sku 是否存在
        validateSku(updateReqVO.getSkuId());

        // 更新
        BargainActivityDO updateObj = BargainActivityConvert.INSTANCE.convert(updateReqVO);
        bargainActivityMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBargainActivityStock(Long id, Integer count) {
        // 查询砍价活动
        BargainActivityDO activity = getBargainActivity(id);
        if (activity == null) {
            throw exception(BARGAIN_ACTIVITY_NOT_EXISTS);
        }

        // 更新砍价库存
        int updateCount = bargainActivityMapper.updateActivityStock(id, count);
        if (updateCount == 0) {
            throw exception(BARGAIN_ACTIVITY_UPDATE_STOCK_FAIL);
        }
    }

    private void validateBargainConflict(Long spuId, Long activityId) {
        // 查询所有开启的砍价活动
        List<BargainActivityDO> activityList = bargainActivityMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (activityId != null) { // 更新时排除自己
            activityList.removeIf(item -> ObjectUtil.equal(item.getId(), activityId));
        }
        // 校验商品 spu 是否参加了其它活动
        if (anyMatch(activityList, activity -> ObjectUtil.equal(activity.getSpuId(), spuId))) {
            throw exception(BARGAIN_ACTIVITY_SPU_CONFLICTS);
        }
    }

    private void validateSku(Long skuId) {
        ProductSkuRespDTO sku = productSkuApi.getSku(skuId);
        if (sku == null) {
            throw exception(SKU_NOT_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBargainActivity(Long id) {
        // 校验存在
        BargainActivityDO activityDO = validateBargainActivityExists(id);
        // 校验状态
        //if (ObjectUtil.equal(activityDO.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
        //    throw exception(BARGAIN_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END);
        //}

        // 删除
        bargainActivityMapper.deleteById(id);
    }

    private BargainActivityDO validateBargainActivityExists(Long id) {
        BargainActivityDO activityDO = bargainActivityMapper.selectById(id);
        if (activityDO == null) {
            throw exception(BARGAIN_ACTIVITY_NOT_EXISTS);
        }
        return activityDO;
    }

    @Override
    public BargainActivityDO getBargainActivity(Long id) {
        return bargainActivityMapper.selectById(id);
    }

    @Override
    public PageResult<BargainActivityDO> getBargainActivityPage(BargainActivityPageReqVO pageReqVO) {
        return bargainActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<BargainActivityDO> getBargainActivityPageForApp(PageParam pageReqVO) {
        // 只查询进行中，且在时间范围内的
        return bargainActivityMapper.selectAppPage(pageReqVO, CommonStatusEnum.ENABLE.getStatus(), LocalDateTime.now());
    }

    @Override
    public List<BargainActivityDO> getBargainActivityListForApp(Integer count) {
        // TODO @puhui999：这种 default count 的逻辑，可以放到 controller 哈；然后可以使用 ObjectUtils.default 方法
        if (count == null) {
            count = 6;
        }
        // TODO @puhui999：这种不要用 page；会浪费一次 count；
        PageResult<BargainActivityDO> result = bargainActivityMapper.selectAppPage(new PageParam().setPageSize(count),
                CommonStatusEnum.ENABLE.getStatus(), LocalDateTime.now());
        return result.getList();
    }

}
