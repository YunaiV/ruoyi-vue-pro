package cn.iocoder.yudao.module.business.hi.task.inst.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kemengkai
 * @create 2022-01-11 15:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoricApproveTaskDTO {

    private String procInstId;
    private String name;
    private String assignee;

}
