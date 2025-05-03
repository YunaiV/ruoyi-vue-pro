package cn.iocoder.yudao.module.report.framework.jmreport.core.service;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.jeecg.modules.drag.service.IOnlDragExternalService;
import org.jeecg.modules.drag.vo.DragDictModel;
import org.jeecg.modules.drag.vo.DragLogDTO;

import java.util.List;
import java.util.Map;

/**
 * {@link IOnlDragExternalService} 实现类，提供积木仪表盘的查询等功能
 *
 * 实现可参考：
 * 1. <a href="https://github.com/jeecgboot/jimureport/blob/master/jimureport-example/src/main/java/com/jeecg/modules/jmreport/extend/JimuDragExternalServiceImpl.java">jimureport-example</a>
 * 2. <a href="https://gitee.com/jeecg/JeecgBoot/blob/master/jeecg-boot/jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/config/jimureport/JimuDragExternalServiceImpl.java">JeecgBoot 集成</a>
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
public class JmOnlDragExternalServiceImpl implements IOnlDragExternalService {

    // ========== DictItem 相关 ==========

    @Override
    public Map<String, List<DragDictModel>> getManyDictItems(List<String> codeList, List<JSONObject> tableDictList) {
        return IOnlDragExternalService.super.getManyDictItems(codeList, tableDictList);
    }

    @Override
    public List<DragDictModel> getDictItems(String dictCode) {
        return IOnlDragExternalService.super.getDictItems(dictCode);
    }

    @Override
    public List<DragDictModel> getTableDictItems(String dictTable, String dictText, String dictCode) {
        return IOnlDragExternalService.super.getTableDictItems(dictTable, dictText, dictCode);
    }

    @Override
    public List<DragDictModel> getCategoryTreeDictItems(List<String> ids) {
        return IOnlDragExternalService.super.getCategoryTreeDictItems(ids);
    }

    @Override
    public List<DragDictModel> getUserDictItems(List<String> ids) {
        return IOnlDragExternalService.super.getUserDictItems(ids);
    }

    @Override
    public List<DragDictModel> getDeptsDictItems(List<String> ids) {
        return IOnlDragExternalService.super.getDeptsDictItems(ids);
    }

    // ========== Log 相关 ==========

    @Override
    public void addLog(DragLogDTO dto) {
        IOnlDragExternalService.super.addLog(dto);
    }

    @Override
    public void addLog(String logMsg, int logType, int operateType) {
        IOnlDragExternalService.super.addLog(logMsg, logType, operateType);
    }

}
