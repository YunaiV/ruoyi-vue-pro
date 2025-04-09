package com.somle.esb.job;

import com.somle.eccang.model.req.EccangReceivingDetailReqVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * gumaomao
 *
 * @Description: 查询易仓入库单明细上传到数仓
 */
@Component
public class EccangReceivingDetailListDataJob extends EccangDataJob {
    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        List<Integer> warehouseIds = new ArrayList<>();
        //JND-DLD 加拿大多伦多仓
        warehouseIds.add(29);
        //MG-CA 美国CA仓
        warehouseIds.add(30);
        //MG-GA-N 美国GA-N仓
        warehouseIds.add(32);
        //SM-DGC 索迈德国仓
        warehouseIds.add(33);

        var vo = EccangReceivingDetailReqVO.builder()
            .dateFor(beforeYesterdayFirstSecond)
            .dateTo(beforeYesterdayLastSecond)
            .warehouseIds(warehouseIds)
            .page(1)
            .pageSize(100)
            .build();
        eccangService.streamReceivingDetail(vo).forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("receiving_detail")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(beforeYesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            }
        );
        return "data upload success";
    }

}
