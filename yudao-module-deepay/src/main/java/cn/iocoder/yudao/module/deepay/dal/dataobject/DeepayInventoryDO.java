package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存表 deepay_inventory
 */
@TableName("deepay_inventory")
@Data
public class DeepayInventoryDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联链码 */
    private String chainCode;

    /** 可用库存 */
    private Integer stock;

    /** 锁定库存（下单未支付） */
    private Integer lockedStock;

    /**
     * 库存状态。
     * <ul>
     *   <li>{@code NORMAL} —— 库存充足（stock &gt; 2）</li>
     *   <li>{@code LOW}    —— 库存不足（0 &lt; stock &le; 2）</li>
     *   <li>{@code OUT}    —— 库存耗尽（stock == 0）</li>
     * </ul>
     */
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
