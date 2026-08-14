package cn.iocoder.yudao.module.hrm.service.recruit.config;

import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.config.HrmRecruitEliminateReasonSaveReqVO;
import cn.iocoder.yudao.module.hrm.service.config.HrmConfigService;
import cn.iocoder.yudao.module.hrm.enums.config.HrmConfigTypeEnum;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertLinkedSet;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_RECRUIT_CONFIG_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_RECRUIT_ELIMINATE_REASON_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_RECRUIT_ELIMINATE_REASON_UPDATE_SUCCESS;

/**
 * HRM 招聘设置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmRecruitConfigServiceImpl implements HrmRecruitConfigService {

    @Resource
    private HrmConfigService configService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_RECRUIT_CONFIG_TYPE, subType = HRM_RECRUIT_ELIMINATE_REASON_UPDATE_SUB_TYPE,
            bizNo = "{{#configType}}", success = HRM_RECRUIT_ELIMINATE_REASON_UPDATE_SUCCESS)
    public void saveRecruitEliminateReason(HrmRecruitEliminateReasonSaveReqVO saveReqVO) {
        // 1. 过滤并去重淘汰原因
        List<String> reasons = new ArrayList<>(convertLinkedSet(saveReqVO.getReasons(), String::trim,
                StringUtils::hasText));
        HrmRecruitEliminateReasonSaveReqVO oldConfig = new HrmRecruitEliminateReasonSaveReqVO()
                .setReasons(getRecruitEliminateReasonList());
        saveReqVO.setReasons(reasons);

        // 2. 保存淘汰原因
        configService.replaceConfigValueList(HrmConfigTypeEnum.RECRUIT_ELIMINATE.getType(), reasons);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("configType", HrmConfigTypeEnum.RECRUIT_ELIMINATE.getType());
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, oldConfig);
    }

    @Override
    public List<String> getRecruitEliminateReasonList() {
        return configService.getConfigValueList(HrmConfigTypeEnum.RECRUIT_ELIMINATE.getType());
    }

}
