package cn.iocoder.yudao.module.promotion.service.seckillactivity;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.promotion.controller.admin.seckillactivity.vo.SeckillActivityBaseVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckillactivity.vo.SeckillActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckillactivity.vo.SeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckillactivity.vo.SeckillActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.seckillactivity.SeckillActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckillactivity.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckillactivity.SeckillProductDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckillactivity.SeckillActivityMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckillactivity.SeckillProductMapper;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionActivityStatusEnum;
import cn.iocoder.yudao.module.promotion.util.PromotionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;
import static java.util.Arrays.asList;

/**
 * 秒杀活动 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SeckillActivityServiceImpl implements SeckillActivityService {

    @Resource
    private SeckillActivityMapper seckillActivityMapper;

    @Resource
    private SeckillProductMapper seckillProductMapper;

    @Override
    public Long createSeckillActivity(SeckillActivityCreateReqVO createReqVO) {
//        validateSeckillActivityProductConflicts(null,createReqVO.getProducts());
        List<Integer> statuses = asList(PromotionActivityStatusEnum.WAIT.getStatus(), PromotionActivityStatusEnum.RUN.getStatus());
        // 校验商品是否冲突
        validateSeckillActivityProductConflicts(null, createReqVO.getProducts());
        // 插入秒杀活动
        SeckillActivityDO seckillActivity = SeckillActivityConvert.INSTANCE.convert(createReqVO)
                .setStatus(PromotionUtils.calculateActivityStatus(createReqVO.getStartTime(), createReqVO.getEndTime()));;
        seckillActivityMapper.insert(seckillActivity);

        // 插入商品
        List<SeckillProductDO> productDOS = CollectionUtils.convertList(createReqVO.getProducts(),
                product -> SeckillActivityConvert.INSTANCE.convert(product).setActivityId(seckillActivity.getId()));
        seckillProductMapper.insertBatch(productDOS);
        // 返回
        return seckillActivity.getId();
    }

    @Override
    public void updateSeckillActivity(SeckillActivityUpdateReqVO updateReqVO) {
        // 校验存在
        SeckillActivityDO seckillActivity = this.validateSeckillActivityExists(updateReqVO.getId());
        if (PromotionActivityStatusEnum.CLOSE.getStatus().equals(seckillActivity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED);
        }
        List<Integer> statuses = asList(PromotionActivityStatusEnum.WAIT.getStatus(), PromotionActivityStatusEnum.RUN.getStatus());
        // 校验商品是否冲突
        validateSeckillActivityProductConflicts(updateReqVO.getId(), updateReqVO.getProducts());
        // 更新活动
        SeckillActivityDO updateObj = SeckillActivityConvert.INSTANCE.convert(updateReqVO)
                .setStatus(PromotionUtils.calculateActivityStatus(updateReqVO.getStartTime(), updateReqVO.getEndTime()));
        seckillActivityMapper.updateById(updateObj);
        // 更新商品
        updateSeckillProduct(updateReqVO);
    }

    /**
     * 更新秒杀商品
     */
    private void updateSeckillProduct(SeckillActivityUpdateReqVO updateReqVO) {
        List<SeckillProductDO> seckillProductDOS = seckillProductMapper.selectListByActivityId(updateReqVO.getId());
        List<SeckillActivityBaseVO.Product> products = updateReqVO.getProducts();
        //对后台查出的数据和前台查出的数据进行遍历，
        //1.对前台数据进行遍历：如果不存在于后台的sku中需要新增
        //2.对后台数据进行遍历：如果不存在于前台的sku中需要删除
        //计算需要删除的数据
        List<Long> deleteIds = CollectionUtils.convertList(seckillProductDOS, SeckillProductDO::getId,
                seckillProductDO -> products.stream()
                        .noneMatch(product -> SeckillActivityConvert.INSTANCE.isEquals(seckillProductDO, product)));
        if (CollUtil.isNotEmpty(deleteIds)) {
            seckillProductMapper.deleteBatchIds(deleteIds);
        }
        //计算需要新增的数据
        List<SeckillProductDO> newSeckillProductDOs = CollectionUtils.convertList(products,
                product -> SeckillActivityConvert.INSTANCE.convert(product).setActivityId(updateReqVO.getId()));
        newSeckillProductDOs.removeIf(product -> seckillProductDOS.stream()
                .anyMatch(seckillProduct -> SeckillActivityConvert.INSTANCE.isEquals(seckillProduct, product)));
        if (CollUtil.isNotEmpty(newSeckillProductDOs)) {
            seckillProductMapper.insertBatch(newSeckillProductDOs);
        }
    }

    /**
     * 校验商品是否冲突
     *
     * @param id       秒杀活动编号
     * @param products 商品列表
     */
    public void validateSeckillActivityProductConflicts(Long id, List<SeckillActivityBaseVO.Product> products) {
        if (CollUtil.isEmpty(products)) {
            return;
        }
        List<SeckillProductDO> seckillProductDOS = seckillProductMapper
                .selectListBySkuIds(CollectionUtils.convertSet(products, SeckillActivityBaseVO.Product::getSkuId));
        if (CollUtil.isEmpty(seckillProductDOS)) {
            return;
        }
        List<SeckillActivityDO> seckillActivityDOS = seckillActivityMapper
                .selectBatchIds(CollectionUtils.convertSet(seckillProductDOS, SeckillProductDO::getActivityId));
        if (id != null) {// 排除自己这个活动
            seckillActivityDOS.removeIf(item -> id.equals(item.getId()));
        }
        // 排除不满足status的活动
        List<Integer> statuses = asList(PromotionActivityStatusEnum.WAIT.getStatus(), PromotionActivityStatusEnum.RUN.getStatus());
        seckillActivityDOS.removeIf(item -> !statuses.contains(item.getStatus()));
        //如果非空，则说明冲突
        if (CollUtil.isNotEmpty(seckillActivityDOS)) {
            throw exception(SECKILL_ACTIVITY_SPU_CONFLICTS);
        }

    }

    @Override
    public void closeSeckillActivity(Long id) {
        // 校验存在
        SeckillActivityDO seckillActivity = this.validateSeckillActivityExists(id);
        if (PromotionActivityStatusEnum.CLOSE.getStatus().equals(seckillActivity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED);
        }
        if (PromotionActivityStatusEnum.END.getStatus().equals(seckillActivity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_CLOSE_FAIL_STATUS_END);
        }
        // 更新
        SeckillActivityDO updateObj = new SeckillActivityDO().setId(id).setStatus(PromotionActivityStatusEnum.CLOSE.getStatus());
        seckillActivityMapper.updateById(updateObj);

    }

    @Override
    public void deleteSeckillActivity(Long id) {
        // 校验存在
        SeckillActivityDO seckillActivity = this.validateSeckillActivityExists(id);
        if (!PromotionActivityStatusEnum.CLOSE.getStatus().equals(seckillActivity.getStatus())) {
            throw exception(SECKILL_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED);
        }
        // 删除
        seckillActivityMapper.deleteById(id);
    }

    private SeckillActivityDO validateSeckillActivityExists(Long id) {
        SeckillActivityDO seckillActivity = seckillActivityMapper.selectById(id);
        if (seckillActivity == null) {
            throw exception(SECKILL_ACTIVITY_NOT_EXISTS);
        }
        return seckillActivity;
    }

    @Override
    public SeckillActivityDO getSeckillActivity(Long id) {
        return seckillActivityMapper.selectById(id);
    }

    @Override
    public List<SeckillActivityDO> getSeckillActivityList(Collection<Long> ids) {
        return seckillActivityMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SeckillActivityDO> getSeckillActivityPage(SeckillActivityPageReqVO pageReqVO) {
        return seckillActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SeckillProductDO> getSeckillProductListByActivityId(Long id) {
        return seckillProductMapper.selectListByActivityId(id);
    }

}
