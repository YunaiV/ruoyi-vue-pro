package cn.iocoder.yudao.module.mp.service.mediaupload;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.mp.controller.admin.mediaupload.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.mediaupload.WxMediaUploadDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 微信素材上传表  Service 接口
 *
 * @author 芋道源码
 */
public interface WxMediaUploadService {

    /**
     * 创建微信素材上传表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createWxMediaUpload(@Valid WxMediaUploadCreateReqVO createReqVO);

    /**
     * 更新微信素材上传表
     *
     * @param updateReqVO 更新信息
     */
    void updateWxMediaUpload(@Valid WxMediaUploadUpdateReqVO updateReqVO);

    /**
     * 删除微信素材上传表
     *
     * @param id 编号
     */
    void deleteWxMediaUpload(Integer id);

    /**
     * 获得微信素材上传表
     *
     * @param id 编号
     * @return 微信素材上传表
     */
    WxMediaUploadDO getWxMediaUpload(Integer id);

    /**
     * 获得微信素材上传表 列表
     *
     * @param ids 编号
     * @return 微信素材上传表 列表
     */
    List<WxMediaUploadDO> getWxMediaUploadList(Collection<Integer> ids);

    /**
     * 获得微信素材上传表 分页
     *
     * @param pageReqVO 分页查询
     * @return 微信素材上传表 分页
     */
    PageResult<WxMediaUploadDO> getWxMediaUploadPage(WxMediaUploadPageReqVO pageReqVO);

    /**
     * 获得微信素材上传表 列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 微信素材上传表 列表
     */
    List<WxMediaUploadDO> getWxMediaUploadList(WxMediaUploadExportReqVO exportReqVO);

}
