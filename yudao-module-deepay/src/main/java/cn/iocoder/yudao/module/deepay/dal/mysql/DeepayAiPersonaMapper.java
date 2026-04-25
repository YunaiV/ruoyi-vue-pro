package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayAiPersonaDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI Persona 配置 Mapper。
 *
 * <p>查询优先级：tenantId+module → tenantId=0+module → null（代码默认）</p>
 */
@Mapper
public interface DeepayAiPersonaMapper extends BaseMapperX<DeepayAiPersonaDO> {

    /**
     * 查询指定租户 + 板块的 persona（启用的、未删除的第一条）。
     */
    default DeepayAiPersonaDO selectByTenantAndModule(Long tenantId, String module) {
        return selectOne(new LambdaQueryWrapper<DeepayAiPersonaDO>()
                .eq(DeepayAiPersonaDO::getTenantId, tenantId)
                .eq(DeepayAiPersonaDO::getModule, module)
                .eq(DeepayAiPersonaDO::getEnabled, 1)
                .eq(DeepayAiPersonaDO::getDeleted, 0)
                .orderByDesc(DeepayAiPersonaDO::getUpdateTime)
                .last("LIMIT 1"));
    }

    /**
     * 查询全部启用 persona（分页或全量展示用）。
     */
    default List<DeepayAiPersonaDO> selectAllEnabled() {
        return selectList(new LambdaQueryWrapper<DeepayAiPersonaDO>()
                .eq(DeepayAiPersonaDO::getEnabled, 1)
                .eq(DeepayAiPersonaDO::getDeleted, 0)
                .orderByAsc(DeepayAiPersonaDO::getTenantId)
                .orderByAsc(DeepayAiPersonaDO::getModule));
    }

}
