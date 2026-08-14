package cn.iocoder.yudao.module.hrm.service.config;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.hrm.dal.dataobject.config.HrmConfigDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.config.HrmConfigMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * HRM 配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmConfigServiceImpl implements HrmConfigService {

    @Resource
    private HrmConfigMapper hrmConfigMapper;

    @Override
    public List<String> getConfigValueList(Integer type) {
        return convertList(hrmConfigMapper.selectListByType(type), HrmConfigDO::getValue);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceConfigValueList(Integer type, List<String> values) {
        // 1. 删除原配置
        hrmConfigMapper.deleteByType(type);
        if (CollUtil.isEmpty(values)) {
            return;
        }

        // 2. 保存新配置
        List<HrmConfigDO> configs = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            configs.add(HrmConfigDO.builder().type(type).value(values.get(i)).sort(i + 1).build());
        }
        hrmConfigMapper.insertBatch(configs);
    }

}
