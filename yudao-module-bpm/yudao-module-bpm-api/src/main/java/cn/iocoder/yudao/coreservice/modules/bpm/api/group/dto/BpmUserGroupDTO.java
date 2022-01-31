package cn.iocoder.yudao.coreservice.modules.bpm.api.group.dto;

import lombok.Data;

import java.util.Set;
/**
 * Bpm 用户组 DTO
 *
 * @author jason
 */
@Data
public class BpmUserGroupDTO {
    /**
     * 成员用户编号数组
     */
    private Set<Long> memberUserIds;
}
