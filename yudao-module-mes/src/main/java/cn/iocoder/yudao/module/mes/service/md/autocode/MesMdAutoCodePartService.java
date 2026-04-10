package cn.iocoder.yudao.module.mes.service.md.autocode;

import cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.part.MesMdAutoCodePartSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 编码规则组成 Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdAutoCodePartService {

    /**
     * 创建规则组成
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAutoCodePart(@Valid MesMdAutoCodePartSaveReqVO createReqVO);

    /**
     * 更新规则组成
     *
     * @param updateReqVO 更新信息
     */
    void updateAutoCodePart(@Valid MesMdAutoCodePartSaveReqVO updateReqVO);

    /**
     * 删除规则组成
     *
     * @param id 编号
     */
    void deleteAutoCodePart(Long id);

    /**
     * 获得规则组成
     *
     * @param id 编号
     * @return 规则组成
     */
    MesMdAutoCodePartDO getAutoCodePart(Long id);

    /**
     * 获得规则组成列表（根据规则 ID）
     *
     * @param ruleId 规则 ID
     * @return 规则组成列表
     */
    List<MesMdAutoCodePartDO> getAutoCodePartListByRuleId(Long ruleId);

}
