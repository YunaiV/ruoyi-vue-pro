package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductionDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductionMapper;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayStyleChainDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayStyleChainMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * PatternAgent — 生成打版文件路径和技术包 URL，落库 deepay_production。
 *
 * <p>输出：
 * <ul>
 *   <li>{@link Context#patternFile} — 版型文件路径（.dxf）</li>
 *   <li>{@link Context#techPackUrl} — 技术包下载地址（PDF）</li>
 * </ul>
 * 同时将结果回写到 {@code deepay_style_chain.pattern_file}。
 * </p>
 */
@Component
public class PatternAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(PatternAgent.class);

    @Resource
    private DeepayProductionMapper deepayProductionMapper;

    @Resource
    private DeepayStyleChainMapper deepayStyleChainMapper;

    @Override
    public Context run(Context ctx) {
        String code = ctx.chainCode != null ? ctx.chainCode : "UNKNOWN";

        ctx.patternFile  = "/pattern/" + code + ".dxf";
        ctx.techPackUrl  = "/techpack/" + code + ".pdf";

        // 落库 deepay_production
        DeepayProductionDO prod = new DeepayProductionDO();
        prod.setChainCode(code);
        prod.setPatternFile(ctx.patternFile);
        prod.setTechPack(ctx.techPackUrl);
        prod.setCreatedAt(LocalDateTime.now());
        deepayProductionMapper.insert(prod);

        // 回写 deepay_style_chain.pattern_file
        DeepayStyleChainDO chain = deepayStyleChainMapper.selectByChainCode(code);
        if (chain != null) {
            chain.setPatternFile(ctx.patternFile);
            deepayStyleChainMapper.updateById(chain);
        }

        log.info("PatternAgent: 打版完成，chainCode={} patternFile={}", code, ctx.patternFile);
        return ctx;
    }

}
