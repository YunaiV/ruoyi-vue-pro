package cn.iocoder.yudao.module.infra.controller.admin.demo02.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo02.InfraDemoStudentContactDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo02.InfraDemoStudentAddressDO;

/**
 * 学生 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class InfraDemoStudentBaseVO {

    private List<InfraDemoStudentContactDO> demoStudentContacts;

    private InfraDemoStudentAddressDO demoStudentAddress;

}