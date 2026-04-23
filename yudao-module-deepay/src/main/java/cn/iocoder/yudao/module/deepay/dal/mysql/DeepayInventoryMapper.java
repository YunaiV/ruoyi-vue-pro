package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayInventoryDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeepayInventoryMapper extends BaseMapperX<DeepayInventoryDO> {

    default DeepayInventoryDO selectByChainCode(String chainCode) {
        return selectOne(new LambdaQueryWrapper<DeepayInventoryDO>()
                .eq(DeepayInventoryDO::getChainCode, chainCode));
    }

    default List<DeepayInventoryDO> selectAll() {
        return selectList(new LambdaQueryWrapper<>());
    }

    /**
     * 原子库存扣减：仅在 stock > 0 时执行 stock-- 和 locked_stock--（最小为 0）。
     *
     * <p>使用单条 UPDATE + WHERE stock &gt; 0 保证永不出现负库存，
     * 无需先 SELECT 再 UPDATE，天然防并发。</p>
     *
     * @param chainCode 链码
     * @return 受影响行数；0 表示库存不足
     */
    default int decrementStockAtomic(String chainCode) {
        return update(null, new LambdaUpdateWrapper<DeepayInventoryDO>()
                .eq(DeepayInventoryDO::getChainCode, chainCode)
                .gt(DeepayInventoryDO::getStock, 0)
                .setSql("stock = stock - 1, locked_stock = GREATEST(locked_stock - 1, 0)"));
    }

}
