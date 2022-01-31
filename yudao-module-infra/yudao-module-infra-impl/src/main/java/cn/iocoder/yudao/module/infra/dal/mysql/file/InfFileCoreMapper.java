package cn.iocoder.yudao.module.infra.dal.mysql.file;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.file.InfFileDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InfFileCoreMapper extends BaseMapperX<InfFileDO> {

    default Integer selectCountById(String id) {
        return selectCount(InfFileDO::getId, id);
    }

    /**
     * 基于 Path 获取文件
     * 实际上，是基于 ID 查询
     * 由于前端使用 <img /> 的方式获取图片，所以需要忽略租户的查询
     *
     * @param path 路径
     * @return 文件
     */
    @InterceptorIgnore(tenantLine = "true")
    default InfFileDO selectByPath(String path) {
        return selectById(path);
    }

}
