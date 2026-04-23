package cn.iocoder.yudao.module.product.job;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.product.service.decision.ProductDecisionEngine;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 商品决策引擎定时任务
 *
 * 每小时执行一次，遍历所有上架商品，自动决策：放大（BOOST）/ 停售（STOP）/ 改款（REDESIGN）
 */
@Slf4j
@Component
public class ProductDecisionJob implements JobHandler {

    private static final int PAGE_SIZE = 100;

    @Resource
    private ProductSpuService productSpuService;

    @Resource
    private ProductDecisionEngine productDecisionEngine;

    @Override
    public String execute(String param) {
        int processed = 0;
        int pageNo = 1;
        while (true) {
            ProductSpuPageReqVO pageReq = new ProductSpuPageReqVO();
            pageReq.setTabType(ProductSpuPageReqVO.FOR_SALE);
            pageReq.setPageNo(pageNo);
            pageReq.setPageSize(PAGE_SIZE);
            PageResult<ProductSpuDO> page = productSpuService.getSpuPage(pageReq);
            if (page.getList().isEmpty()) {
                break;
            }
            log.info("[ProductDecisionJob] 处理第 {} 页，共 {} 条上架商品", pageNo, page.getList().size());
            for (ProductSpuDO spu : page.getList()) {
                try {
                    productDecisionEngine.decide(spu);
                    processed++;
                } catch (Exception e) {
                    log.error("[ProductDecisionJob] spuId={} 决策异常", spu.getId(), e);
                }
            }
            if (page.getList().size() < PAGE_SIZE) {
                break;
            }
            pageNo++;
        }
        return String.format("决策完成，共处理 %d 个上架商品", processed);
    }

}
