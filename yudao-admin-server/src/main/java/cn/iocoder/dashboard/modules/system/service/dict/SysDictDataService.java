package cn.iocoder.dashboard.modules.system.service.dict;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.dict.core.service.DictDataFrameworkService;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.dict.SysDictDataDO;

import java.util.List;

/**
 * 字典数据 Service 接口
 *
 * @author ruoyi
 */
public interface SysDictDataService extends DictDataFrameworkService {

    /**
     * 初始化字典数据的本地缓存
     */
    void initLocalCache();

    /**
     * 创建字典数据
     *
     * @param reqVO 字典数据信息
     * @return 字典数据编号
     */
    Long createDictData(SysDictDataCreateReqVO reqVO);

    /**
     * 更新字典数据
     *
     * @param reqVO 字典数据信息
     */
    void updateDictData(SysDictDataUpdateReqVO reqVO);

    /**
     * 删除字典数据
     *
     * @param id 字典数据编号
     */
    void deleteDictData(Long id);

    /**
     * 获得字典数据列表
     *
     * @return 字典数据全列表
     */
    List<SysDictDataDO> getDictDatas();

    /**
     * 获得字典数据分页列表
     *
     * @param reqVO 分页请求
     * @return 字典数据分页列表
     */
    PageResult<SysDictDataDO> getDictDataPage(SysDictDataPageReqVO reqVO);

    /**
     * 获得字典数据列表
     *
     * @param reqVO 列表请求
     * @return 字典数据列表
     */
    List<SysDictDataDO> getDictDatas(SysDictDataExportReqVO reqVO);

    /**
     * 获得字典数据详情
     *
     * @param id 字典数据编号
     * @return 字典数据
     */
    SysDictDataDO getDictData(Long id);

    /**
     * 获得指定字典类型的数据数量
     *
     * @param dictType 字典类型
     * @return 数据数量
     */
    int countByDictType(String dictType);

}
