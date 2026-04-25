package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.AiPersonaDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 角色人设配置 Mapper。
 */
@Mapper
public interface AiPersonaMapper extends BaseMapperX<AiPersonaDO> {

    /**
     * 按租户 ID + 模块名查询启用的 persona，按 sort_order 升序。
     *
     * @param tenantId 租户 ID
     * @param module   模块名
     * @return persona 列表（可能为空）
     */
    default List<AiPersonaDO> selectByTenantAndModule(Long tenantId, String module) {
        return selectList(new LambdaQueryWrapper<AiPersonaDO>()
                .eq(AiPersonaDO::getTenantId, tenantId)
                .eq(AiPersonaDO::getModule, module)
                .eq(AiPersonaDO::getEnabled, 1)
                .orderByAsc(AiPersonaDO::getSortOrder));
    }

    /**
     * 查询所有启用的 persona（管理后台列表用）。
     */
    default List<AiPersonaDO> selectAllEnabled() {
        return selectList(new LambdaQueryWrapper<AiPersonaDO>()
                .eq(AiPersonaDO::getEnabled, 1)
                .orderByAsc(AiPersonaDO::getTenantId)
                .orderByAsc(AiPersonaDO::getModule)
                .orderByAsc(AiPersonaDO::getSortOrder));
    }
}
