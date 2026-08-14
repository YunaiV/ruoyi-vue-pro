package cn.iocoder.yudao.module.hrm.service.recruit.post;

import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post.HrmRecruitPostTypeDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 招聘职位类型 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmRecruitPostTypeService {

    /**
     * 校验招聘职位类型是否存在
     *
     * @param id 招聘职位类型编号
     */
    void validateRecruitPostTypeExists(Long id);

    /**
     * 获得招聘职位类型列表
     *
     * @param status 状态
     * @return 招聘职位类型列表
     */
    List<HrmRecruitPostTypeDO> getRecruitPostTypeList(Integer status);

    /**
     * 获得指定编号的招聘职位类型列表
     *
     * @param ids 招聘职位类型编号集合
     * @return 招聘职位类型列表
     */
    List<HrmRecruitPostTypeDO> getRecruitPostTypeListByIds(Collection<Long> ids);

    /**
     * 获得指定编号的招聘职位类型 Map
     *
     * @param ids 招聘职位类型编号集合
     * @return 招聘职位类型 Map
     */
    default Map<Long, HrmRecruitPostTypeDO> getRecruitPostTypeMap(Collection<Long> ids) {
        return convertMap(getRecruitPostTypeListByIds(ids), HrmRecruitPostTypeDO::getId);
    }

}
