package com.somle.esb.job;

import com.somle.eccang.model.req.EccangReceivingDetailReqVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

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

        eccangService.getWarehouseList().forEach(eccangWarehouse -> {
            //过滤掉仓库编码前缀是AMAZON或者Amazon的第三方仓库
            String[] warehouseCodeArr = eccangWarehouse.getWarehouseCode().split("_");
            String warehouseCodePrefix = warehouseCodeArr[0];
            if (warehouseCodePrefix.equals("AMAZON") || warehouseCodePrefix.equals("Amazon")) {
                return;
            }

            var vo = EccangReceivingDetailReqVO.builder()
                .dateFor(beforeYesterdayFirstSecond)
                .dateTo(beforeYesterdayLastSecond)
                .warehouseIds(List.of(eccangWarehouse.getWarehouseId()))
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
        });
        return "data upload success";
    }

}
