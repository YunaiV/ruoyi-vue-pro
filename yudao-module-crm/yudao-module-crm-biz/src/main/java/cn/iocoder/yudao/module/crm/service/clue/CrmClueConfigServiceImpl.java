package cn.iocoder.yudao.module.crm.service.clue;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.clueconfig.CrmClueConfigSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueConfigDO;
import cn.iocoder.yudao.module.crm.dal.mysql.clue.CrmClueConfigMapper;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Objects;

import static cn.iocoder.yudao.module.crm.enums.LogRecordConstants.*;

/**
 * 客户公海配置 Service 实现类
 *
 * @author zhaozian
 */
@Service
@Validated
public class CrmClueConfigServiceImpl implements CrmClueConfigService {

    @Resource
    private CrmClueConfigMapper clueConfigMapper;

    @Override
    public CrmClueConfigDO getCrmClueConfig() {
        return clueConfigMapper.selectOne();
    }

    @Override
    @LogRecord(type = CRM_CLUE_CONFIG_TYPE, subType = CRM_CLUE_CONFIG_SUB_TYPE, bizNo = "{{#ClueConfigId}}",
            success = CRM_CLUE_CONFIG_SUCCESS)
    public void saveClueConfig(CrmClueConfigSaveReqVO saveReqVO) {
        // 1. 存在，则进行更新
        CrmClueConfigDO dbConfig = getCrmClueConfig();
        CrmClueConfigDO clueConfig = BeanUtils.toBean(saveReqVO, CrmClueConfigDO.class);
        if (Objects.nonNull(dbConfig)) {
            clueConfigMapper.updateById(clueConfig.setId(dbConfig.getId()));
            // 记录操作日志上下文
            LogRecordContext.putVariable("isClueConfigUpdate", Boolean.TRUE);
            LogRecordContext.putVariable("ClueConfigId", clueConfig.getId());
            return;
        }

        // 2. 不存在，则进行插入
        clueConfigMapper.insert(clueConfig);
        // 记录操作日志上下文
        LogRecordContext.putVariable("isClueConfigUpdate", Boolean.FALSE);
        LogRecordContext.putVariable("ClueConfigId", clueConfig.getId());
    }

}
