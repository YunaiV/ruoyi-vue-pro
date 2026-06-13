package cn.iocoder.yudao.module.mes.service.dv.subject;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.dv.subject.vo.MesDvSubjectPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.subject.vo.MesDvSubjectSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.subject.MesDvSubjectDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 点检保养项目 Service 接口
 *
 * @author 芋道源码
 */
public interface MesDvSubjectService {

    /**
     * 创建点检保养项目
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSubject(@Valid MesDvSubjectSaveReqVO createReqVO);

    /**
     * 更新点检保养项目
     *
     * @param updateReqVO 更新信息
     */
    void updateSubject(@Valid MesDvSubjectSaveReqVO updateReqVO);

    /**
     * 删除点检保养项目
     *
     * @param id 编号
     */
    void deleteSubject(Long id);

    /**
     * 获得点检保养项目
     *
     * @param id 编号
     * @return 点检保养项目
     */
    MesDvSubjectDO getSubject(Long id);

    /**
     * 获得点检保养项目分页
     *
     * @param pageReqVO 分页查询
     * @return 点检保养项目分页
     */
    PageResult<MesDvSubjectDO> getSubjectPage(MesDvSubjectPageReqVO pageReqVO);

    /**
     * 校验点检保养项目存在
     *
     * @param id 编号
     */
    void validateSubjectExists(Long id);

    /**
     * 校验点检保养项目存在且启用
     *
     * @param id 编号
     */
    void validateSubjectExistsAndEnable(Long id);

    /**
     * 获得点检保养项目列表
     *
     * @return 项目列表
     */
    List<MesDvSubjectDO> getSubjectList();

    /**
     * 获得点检保养项目列表
     *
     * @param ids 编号数组
     * @return 点检保养项目列表
     */
    List<MesDvSubjectDO> getSubjectList(Collection<Long> ids);

    /**
     * 获得点检保养项目 Map
     *
     * @param ids 编号数组
     * @return 点检保养项目 Map
     */
    default Map<Long, MesDvSubjectDO> getSubjectMap(Collection<Long> ids) {
        return convertMap(getSubjectList(ids), MesDvSubjectDO::getId);
    }

}
