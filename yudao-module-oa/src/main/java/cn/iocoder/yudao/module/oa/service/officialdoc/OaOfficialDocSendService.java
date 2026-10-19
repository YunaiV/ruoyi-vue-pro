package cn.iocoder.yudao.module.oa.service.officialdoc;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.send.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公文发文 Service 接口
 *
 * @author 芋道源码
 */
public interface OaOfficialDocSendService {

    /**
     * 创建公文发文草稿
     *
     * @param reqVO 公文发文信息
     * @param userId 操作用户编号
     * @return 公文发文编号
     */
    Long createOfficialDocSend(OaOfficialDocSendSaveReqVO reqVO, Long userId);

    /**
     * 更新公文发文
     *
     * @param reqVO 公文发文信息
     * @param userId 操作用户编号
     */
    void updateOfficialDocSend(OaOfficialDocSendSaveReqVO reqVO, Long userId);

    /**
     * 删除公文发文
     *
     * @param id 公文发文编号
     * @param userId 操作用户编号
     */
    void deleteOfficialDocSend(Long id, Long userId);

    /**
     * 获得公文发文
     *
     * @param id 公文发文编号
     * @return 公文发文
     */
    OaOfficialDocSendDO getOfficialDocSend(Long id);

    /**
     * 获得公文发文列表
     *
     * @param ids 公文发文编号集合
     * @return 公文发文列表
     */
    List<OaOfficialDocSendDO> getOfficialDocSendList(Collection<Long> ids);

    /**
     * 获得公文发文 Map
     *
     * @param ids 公文发文编号集合
     * @return 公文发文 Map
     */
    default Map<Long, OaOfficialDocSendDO> getOfficialDocSendMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return new HashMap<>();
        }
        return CollectionUtils.convertMap(getOfficialDocSendList(ids), OaOfficialDocSendDO::getId);
    }

    /**
     * 获得公文发文分页
     *
     * @param reqVO 分页条件
     * @param userId 操作用户编号
     * @return 公文发文分页
     */
    PageResult<OaOfficialDocSendDO> getOfficialDocSendPage(OaOfficialDocSendPageReqVO reqVO, Long userId);

    /**
     * 提交公文发文审批
     *
     * @param id 单据编号
     * @param userId 操作用户编号
     * @return 流程实例编号
     */
    String submitOfficialDocSend(Long id, Long userId);

    /**
     * 撤销公文发文审批
     *
     * @param id 单据编号
     * @param userId 操作用户编号
     */
    void cancelOfficialDocSend(Long id, Long userId);

    /**
     * 更新公文发文审批结果
     *
     * @param id 单据编号
     * @param processInstanceId 流程实例编号
     * @param status 审批状态
     */
    void updateOfficialDocSendStatus(Long id, String processInstanceId, Integer status);

    /**
     * 获得引用套红模板的单据数量
     *
     * @param id 套红模板编号
     * @return 单据数量
     */
    Long getOfficialDocSendCountByTemplateId(Long id);

}
