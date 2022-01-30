package cn.iocoder.yudao.coreservice.modules.bpm.api.form;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.coreservice.modules.bpm.api.form.dto.BpmFormDTO;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Bpm 动态表单 Service  API 接口
 *
 * @author @风里雾里
 */
public interface BpmFormServiceApi {
    /**
     * 获得动态表单
     *
     * @param id 编号
     * @return 动态表单
     */
    BpmFormDTO getForm(Long id);

    /**
     * 获得动态表单列表
     *
     * @param ids 编号
     * @return 动态表单列表
     */
    List<BpmFormDTO> getFormList(Collection<Long> ids);

    /**
     * 获得动态表单 Map
     *
     * @param ids 编号
     * @return 动态表单 Map
     */
    default Map<Long, BpmFormDTO> getFormMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(this.getFormList(ids), BpmFormDTO::getId);
    }
}
