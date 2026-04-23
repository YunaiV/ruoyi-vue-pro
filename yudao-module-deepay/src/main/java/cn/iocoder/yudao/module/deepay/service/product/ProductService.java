package cn.iocoder.yudao.module.deepay.service.product;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayStyleChainDO;

/**
 * Deepay 商品查询 Service。
 *
 * <p>提供按链码查询完整商品信息的能力，供页面渲染接口和 JSON API 共用。</p>
 */
public interface ProductService {

    /**
     * 根据链码查询商品信息。
     *
     * @param chainCode 6 位链码
     * @return 对应的商品记录；若不存在则返回 {@code null}
     */
    DeepayStyleChainDO getByChainCode(String chainCode);

}
