package cn.iocoder.yudao.module.bpm.dal.mysql.task;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmActivityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author kemengkai
 * @create 2022-05-09 09:26 TODO @ke：@date 哈。ps：一般这种类，可以不用谢类注释和方法注释。因为是 dao 是通用操作，所以都是通过方法名，可以指导它的用途
 */
@Mapper
public interface BpmActivityMapper extends BaseMapperX<BpmActivityDO> {

    // TODO @ke：这个方法，是不是暂时用不到呀。如果是的话，可以删除哈
    /**
     * 获取所有历史任务
     *
     * @return 返回历史任务
     */
    List<BpmActivityDO> listAll();

    // TODO @ke：可以试试，把 activiti 的表，映射成对应的实体，然后读取下。我们尽量避免 xml 操作，因为要做多 db 类型的支持，例如说 oracle 等。通过 mybatis plus 帮助我们生成不同数据库的表操作
    /**
     * 获取指定流程的历史任务
     *
     * @param procInstId 流程id
     *
     * @return 返回历史任务
     */
    List<BpmActivityDO> listAllByProcInstIdAndDelete(@Param("procInstId") String procInstId);

    /**
     * 逻辑删除hiActInst表任务
     *
     * @param taskIdList 任务列表
     *
     * @return 返回是否成功
     */
    Boolean delHiActInstByTaskId(@Param("taskIdList") List<String> taskIdList);

    /**
     * 逻辑删除hiTaskInst任务
     *
     * @param taskIdList 任务列表
     *
     * @return 返回是否成功
     */
    Boolean delHiTaskInstByTaskId(@Param("taskIdList") List<String> taskIdList);
}
