package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayStyleChainMapper;
import cn.iocoder.yudao.module.deepay.service.ima.ImaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ima 知识库同步 Agent。
 *
 * <p>职责：在 {@link ChainAgent} 成功落库后，将设计图同步到 ima 知识库：
 * <ol>
 *   <li>调用 {@link ImaService#createKnowledgeBase(String)} 创建以 chainCode 命名的知识库。</li>
 *   <li>调用 {@link ImaService#uploadImage(String, String)} 上传 selectedImage。</li>
 *   <li>通过 {@link DeepayStyleChainMapper#updateImaKbId(String, String)} 将 kbId 回填数据库。</li>
 *   <li>将 kbId 写回 {@link Context#imaKbId}。</li>
 * </ol>
 * </p>
 *
 * <p><strong>容错原则：</strong>ima 是副本，不是主数据源。
 * 任何 ima 异常均被捕获并记录日志，不向上抛出，不影响主流程返回。
 * {@link ImaService} 未配置（Bean 不存在）时同样静默跳过。</p>
 */
@Component
public class ImaAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(ImaAgent.class);

    /**
     * ima 业务服务（可选）。
     * 未配置 {@code deepay.ima.enabled=true} 时该字段为 null，Agent 直接跳过。
     */
    @Autowired(required = false)
    private ImaService imaService;

    @Resource
    private DeepayStyleChainMapper deepayStyleChainMapper;

    @Override
    public Context run(Context ctx) {
        if (imaService == null) {
            log.debug("ImaAgent: ima 未启用，跳过同步（chainCode={}）", ctx.chainCode);
            return ctx;
        }
        try {
            // 1. 创建知识库
            String kbId = imaService.createKnowledgeBase(ctx.chainCode);
            // 2. 上传设计图
            imaService.uploadImage(kbId, ctx.selectedImage);
            // 3. 回填数据库（主数据已落库，此步骤为扩展信息）
            deepayStyleChainMapper.updateImaKbId(ctx.chainCode, kbId);
            // 4. 写回 Context
            ctx.imaKbId = kbId;
            log.info("ImaAgent: 同步成功，chainCode={}, kbId={}", ctx.chainCode, kbId);
        } catch (Exception e) {
            // ima 失败不影响主流程
            log.error("ImaAgent: 同步 ima 失败，chainCode={}", ctx.chainCode, e);
        }
        return ctx;
    }

}
