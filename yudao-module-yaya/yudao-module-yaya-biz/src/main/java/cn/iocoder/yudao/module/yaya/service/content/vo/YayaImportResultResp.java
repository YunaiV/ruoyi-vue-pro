package cn.iocoder.yudao.module.yaya.service.content.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class YayaImportResultResp {

    private String seasonKey;
    private Integer topics;
    private Integer questions;
    private List<String> errors = new ArrayList<>();

}
