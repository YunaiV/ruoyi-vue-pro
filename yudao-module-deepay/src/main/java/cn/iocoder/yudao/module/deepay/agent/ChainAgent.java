package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayStyleChainDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayStyleChainMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * 链码生成 Agent（核心）。
 *
 * <p>职责：
 * <ol>
 *   <li>生成 6 位随机大写字母 + 数字组合的 chainCode，确保数据库唯一（最多重试 {@link #MAX_RETRY} 次）。</li>
 *   <li>将 chainCode、selectedImage 以及状态 {@code CREATED} 写入 {@code deepay_style_chain} 表。</li>
 *   <li>把 chainCode 写回 {@link Context#chainCode}。</li>
 * </ol>
 * </p>
 *
 * <p>后续接入区块链铸造等逻辑时，只需在本类中扩展，无需修改其他 Agent。</p>
 */
@Component
public class ChainAgent implements Agent {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_RETRY = 10;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Resource
    private DeepayStyleChainMapper deepayStyleChainMapper;

    @Override
    public Context run(Context ctx) {
        String code = generateUniqueCode();
        // 写库
        DeepayStyleChainDO record = new DeepayStyleChainDO();
        record.setChainCode(code);
        record.setImageUrl(ctx.selectedImage);
        record.setTitle(ctx.title);
        record.setDescription(ctx.description);
        record.setPrice(ctx.price);
        record.setStatus("CREATED");
        record.setCreatedAt(LocalDateTime.now());
        deepayStyleChainMapper.insert(record);

        ctx.chainCode = code;
        return ctx;
    }

    /**
     * 生成全局唯一的 6 位链码；若与库中已有记录冲突则重试，最多 {@link #MAX_RETRY} 次。
     *
     * @return 唯一链码
     * @throws IllegalStateException 重试超限时抛出
     */
    private String generateUniqueCode() {
        for (int i = 0; i < MAX_RETRY; i++) {
            String code = randomCode();
            Long count = deepayStyleChainMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeepayStyleChainDO>()
                            .eq(DeepayStyleChainDO::getChainCode, code));
            if (count == null || count == 0) {
                return code;
            }
        }
        throw new IllegalStateException("ChainAgent: 无法在 " + MAX_RETRY + " 次重试内生成唯一 chainCode，请稍后重试");
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

}
