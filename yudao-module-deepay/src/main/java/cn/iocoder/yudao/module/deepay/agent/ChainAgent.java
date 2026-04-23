package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayStyleChainDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayStyleChainMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * ChainAgent — 在流程最开始生成全局唯一链码，作为本次生产的主键。
 *
 * <p>提前执行的好处：后续所有 Agent（JudgeAgent、AIDecisionAgent、PatternAgent 等）
 * 都能在写库时直接使用 {@link Context#chainCode}，保证数据链完整不断裂。</p>
 *
 * <p>守卫：{@code if (ctx.chainCode == null)} — 支持 REDESIGN 复用已有 chainCode 时安全跳过。</p>
 */
@Component
public class ChainAgent implements Agent {

    private static final String ALPHABET   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int    CODE_LENGTH = 6;
    private static final int    MAX_RETRY   = 10;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Resource
    private DeepayStyleChainMapper deepayStyleChainMapper;

    @Override
    public Context run(Context ctx) {
        // 守卫：已有 chainCode 时不重复生成（REDESIGN 二次进入保护）
        if (ctx.chainCode != null) {
            return ctx;
        }

        String code = generateUniqueCode();

        // 创建 style_chain 骨架记录
        DeepayStyleChainDO record = new DeepayStyleChainDO();
        record.setChainCode(code);
        record.setImageUrl(ctx.selectedImage);
        record.setKeyword(ctx.keyword);
        record.setTitle(ctx.title);
        record.setDescription(ctx.description);
        record.setPrice(ctx.price);
        record.setStatus("CREATED");
        record.setCreatedAt(LocalDateTime.now());
        deepayStyleChainMapper.insert(record);

        ctx.chainCode = code;
        return ctx;
    }

    private String generateUniqueCode() {
        for (int i = 0; i < MAX_RETRY; i++) {
            String code = randomCode();
            Long count = deepayStyleChainMapper.selectCount(
                    new LambdaQueryWrapper<DeepayStyleChainDO>()
                            .eq(DeepayStyleChainDO::getChainCode, code));
            if (count == null || count == 0) {
                return code;
            }
        }
        throw new IllegalStateException(
                "ChainAgent: 无法在 " + MAX_RETRY + " 次内生成唯一 chainCode，请稍后重试");
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

}
