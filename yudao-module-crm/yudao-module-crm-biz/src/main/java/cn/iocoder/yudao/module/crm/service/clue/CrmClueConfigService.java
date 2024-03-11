package cn.iocoder.yudao.module.crm.service.clue;

import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.clueconfig.CrmClueConfigSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueConfigDO;

import javax.validation.Valid;

/**
 * 线索配置 Service 接口
 *
 * @author zhaozian
 */
public interface CrmClueConfigService {

    /**
     * 获得客户公海配置
     *
     * @return 客户公海配置
     */
    CrmClueConfigDO getCrmClueConfig();

    /**
     * 保存客户公海配置
     *
     * @param saveReqVO 更新信息
     */
    void saveClueConfig(@Valid CrmClueConfigSaveReqVO saveReqVO);

}
