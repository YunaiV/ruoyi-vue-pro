package cn.iocoder.yudao.module.oa.dal.dataobject.file;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.file.OaFileCategoryEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFileNodeStatusEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFileNodeTypeEnum;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OA 云盘文件节点 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_file_node", autoResultMap = true)
@KeySequence("oa_file_node_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaFileNodeDO extends BaseDO {

    /**
     * 根目录的父节点编号
     */
    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 父目录编号，根目录为 {@link #PARENT_ID_ROOT}
     *
     * 关联 {@link OaFileNodeDO#getId()}
     */
    private Long parentId;
    /**
     * 节点类型
     *
     * 枚举 {@link OaFileNodeTypeEnum}
     */
    private Integer type;
    /**
     * 节点名称
     */
    private String name;
    /**
     * 扩展名
     */
    private String extension;
    /**
     * 文件分类
     *
     * 枚举 {@link OaFileCategoryEnum}，文件夹为空
     * 文件上传时通过 {@link OaFileCategoryEnum#getByExtension(String)} 计算
     */
    private Integer category;
    /**
     * 文件大小，单位字节
     */
    private Long size;
    /**
     * 文件访问地址
     *
     * 由服务端文件上传结果设置，文件夹为空
     */
    private String url;
    /**
     * 所属部门编号
     *
     * 关联 {@link DeptRespDTO#getId()}
     */
    private Long deptId;
    /**
     * 节点状态，区分正常节点与回收站节点
     *
     * 枚举 {@link OaFileNodeStatusEnum}
     * 1、移入回收站及恢复通过本字段表达，不使用 BaseDO.deleted
     * 2、彻底删除使用 BaseDO.deleted，不物理删除数据库记录或底层文件
     * 3、后续访问节点需校验祖先状态，恢复目录不改变此前单独回收的子节点状态
     */
    private Integer status;

}
