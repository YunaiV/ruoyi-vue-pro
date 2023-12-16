package cn.iocoder.yudao.module.statistics.service.product;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.SortablePageParam;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.statistics.controller.admin.common.vo.DataComparisonRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.product.vo.ProductStatisticsReqVO;
import cn.iocoder.yudao.module.statistics.controller.admin.product.vo.ProductStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.dal.dataobject.product.ProductStatisticsDO;
import cn.iocoder.yudao.module.statistics.dal.mysql.product.ProductStatisticsMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 商品统计 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class ProductStatisticsServiceImpl implements ProductStatisticsService {

    @Resource
    private ProductStatisticsMapper productStatisticsMapper;

    @Override
    public Long createProductStatistics(ProductStatisticsDO entity) {
        // 计算 访客支付转化率（百分比）
        if (entity.getBrowseUserCount() != null && ObjUtil.notEqual(entity.getBrowseUserCount(), 0)) {
            entity.setBrowseConvertPercent(100 * entity.getOrderPayCount() / entity.getBrowseUserCount());
        }
        // 插入
        productStatisticsMapper.insert(entity);
        // 返回
        return entity.getId();
    }

    @Override
    public PageResult<ProductStatisticsDO> getProductStatisticsRankPage(ProductStatisticsReqVO reqVO, SortablePageParam pageParam) {
        // 默认浏览量倒序
        MyBatisUtils.buildDefaultSortingField(pageParam, ProductStatisticsDO::getBrowseCount);
        return productStatisticsMapper.selectPageGroupBySpuId(reqVO, pageParam);
    }

    @Override
    public DataComparisonRespVO<ProductStatisticsRespVO> getProductStatisticsAnalyse(ProductStatisticsReqVO reqVO) {
        LocalDateTime beginTime = ArrayUtil.get(reqVO.getTimes(), 0);
        LocalDateTime endTime = ArrayUtil.get(reqVO.getTimes(), 1);

        // 统计数据
        ProductStatisticsRespVO value = productStatisticsMapper.selectVoByTimeBetween(reqVO);
        // 对照数据
        LocalDateTime referenceBeginTime = beginTime.minus(Duration.between(beginTime, endTime));
        ProductStatisticsReqVO referenceReqVO = new ProductStatisticsReqVO(new LocalDateTime[]{referenceBeginTime, beginTime});
        ProductStatisticsRespVO reference = productStatisticsMapper.selectVoByTimeBetween(referenceReqVO);
        return new DataComparisonRespVO<>(value, reference);
    }

    @Override
    public List<ProductStatisticsDO> getProductStatisticsList(ProductStatisticsReqVO reqVO) {
        return productStatisticsMapper.selectListByTimeBetween(reqVO);
    }

}