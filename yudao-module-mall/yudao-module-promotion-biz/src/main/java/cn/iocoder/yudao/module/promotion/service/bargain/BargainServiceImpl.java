package cn.iocoder.yudao.module.promotion.service.bargain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.BargainActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.BargainActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.BargainActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.bargain.BargainActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.bargain.BargainActivityMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.bargain.BargainRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.anyMatch;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;

/**
 * 砍价活动 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class BargainServiceImpl implements BargainActivityService, BargainRecordService {

    @Resource
    private BargainActivityMapper bargainActivityMapper;
    @Resource
    private BargainRecordMapper recordMapper;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;

    private static void validateSku(Long skuId, List<ProductSkuRespDTO> skus) {
        // 校验商品 sku 是否存在
        if (!CollectionUtils.convertSet(skus, ProductSkuRespDTO::getId).contains(skuId)) {
            throw exception(SKU_NOT_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBargainActivity(BargainActivityCreateReqVO createReqVO) {
        // 校验商品 SPU 是否存在是否参加的别的活动
        validateBargainConflict(createReqVO.getSpuId(), null);
        // 获取所选 spu下的所有 sku
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuListBySpuId(CollectionUtil.newArrayList(createReqVO.getSpuId()));
        validateSku(createReqVO.getSkuId(), skus);

        // 插入砍价活动
        BargainActivityDO activityDO = BargainActivityConvert.INSTANCE.convert(createReqVO);
        // TODO 营销相关属性初始化 砍价成功更新相关属性
        activityDO.setSuccessCount(0);
        activityDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        bargainActivityMapper.insert(activityDO);
        // 返回
        return activityDO.getId();
    }

    private void validateBargainConflict(Long spuId, Long activityId) {
        // 校验商品 spu 是否存在
        List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(CollUtil.newArrayList(spuId));
        if (CollUtil.isEmpty(spuList)) {
            throw exception(SPU_NOT_EXISTS);
        }
        // 查询所有开启的砍价活动
        List<BargainActivityDO> activityDOs = bargainActivityMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        // 更新时排除自己
        if (activityId != null) {
            activityDOs.removeIf(item -> ObjectUtil.equal(item.getId(), activityId));
        }
        // 校验商品 spu 是否参加了其它活动
        if (anyMatch(activityDOs, s -> ObjectUtil.equal(s.getId(), spuId))) {
            throw exception(BARGAIN_ACTIVITY_SPU_CONFLICTS);
        }
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
        // 获取所选 spu下的所有 sku
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuListBySpuId(CollectionUtil.newArrayList(updateReqVO.getSpuId()));
        // 校验商品 sku 是否存在
        validateSku(updateReqVO.getSkuId(), skus);

        // 更新
        BargainActivityDO updateObj = BargainActivityConvert.INSTANCE.convert(updateReqVO);
        bargainActivityMapper.updateById(updateObj);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBargainActivity(Long id) {
        // 校验存在
        BargainActivityDO activityDO = validateBargainActivityExists(id);
        // 校验状态
        if (ObjectUtil.equal(activityDO.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            throw exception(BARGAIN_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END);
        }

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
        return validateBargainActivityExists(id);
    }

    @Override
    public PageResult<BargainActivityDO> getBargainActivityPage(BargainActivityPageReqVO pageReqVO) {
        return bargainActivityMapper.selectPage(pageReqVO);
    }

}
