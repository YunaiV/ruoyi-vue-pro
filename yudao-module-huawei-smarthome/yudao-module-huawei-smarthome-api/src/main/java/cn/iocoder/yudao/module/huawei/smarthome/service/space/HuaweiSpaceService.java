package cn.iocoder.yudao.module.huawei.smarthome.service.space;

import cn.iocoder.yudao.module.huawei.smarthome.dto.space.SpaceCreateReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.space.SpaceCreateRespDTO;

import javax.validation.Valid;
import java.io.IOException;

/**
 * 华为智能家居 - 空间管理 Service 接口
 *
 * @author Jules
 */
public interface HuaweiSpaceService {

    /**
     * 创建空间
     *
     * @param createReqDTO 创建空间请求 DTO
     * @return 创建成功后的空间信息，包含 spaceId
     * @throws IOException 当 API 调用失败时抛出
     */
    SpaceCreateRespDTO createSpace(@Valid SpaceCreateReqDTO createReqDTO) throws IOException;

    /**
     * 修改空间
     *
     * @param updateReqDTO 修改空间请求 DTO
     * @throws IOException 当 API 调用失败时抛出
     */
    void updateSpace(@Valid SpaceUpdateReqDTO updateReqDTO) throws IOException;

    /**
     * 删除空间
     *
     * @param deleteReqDTO 删除空间请求 DTO, 包含 spaceId
     * @throws IOException 当 API 调用失败时抛出
     */
    void deleteSpace(@Valid SpaceDeleteReqDTO deleteReqDTO) throws IOException;

    /**
     * 查询项目下所有空间信息
     *
     * @return 空间信息列表
     * @throws IOException 当 API 调用失败时抛出
     */
    SpaceListRespDTO listAllSpaces() throws IOException;

    /**
     * 查询指定一个或多个空间信息
     *
     * @param spaceIds 空间 ID 列表 (华为API文档中是 &spaceId=x1&spaceId=x2 形式，需要处理)
     * @return 匹配的空间信息列表
     * @throws IOException 当 API 调用失败时抛出
     */
    SpaceListRespDTO listSpacesByIds(List<String> spaceIds) throws IOException;

}
