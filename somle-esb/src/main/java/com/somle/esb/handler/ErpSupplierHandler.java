//package com.somle.esb.handler;
//
//import cn.hutool.core.util.ObjUtil;
//import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
//import cn.iocoder.yudao.module.erp.api.supplier.dto.ErpSupplierDTO;
//import com.somle.eccang.model.EccangProduct;
//import com.somle.eccang.service.EccangService;
//import com.somle.esb.converter.ErpToEccangConverter;
//import com.somle.esb.converter.ErpToKingdeeConverter;
//import com.somle.kingdee.model.KingdeeProductSaveReqVO;
//import com.somle.kingdee.model.supplier.KingdeeSupplier;
//import com.somle.kingdee.service.KingdeeService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.integration.annotation.ServiceActivator;
//import org.springframework.messaging.Message;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * @Description: $
// * @Author: c-tao
// * @Date: 2025/1/13$
// */
//@Slf4j
//@Component
//public class ErpSupplierHandler {
//
//    @Autowired
//    KingdeeService kingdeeService;
//
//    @Autowired
//    ErpToKingdeeConverter erpToKingdeeConverter;
//
//    /**
//     * @Author Wqh
//     * @Description 上传金蝶供应商信息
//     * @Date 9:45 2024/11/6
//     * @Param []
//     * @return void
//     **/
//    public void syncSupplierToKingdee(ErpSupplierDTO erpSupplierDTO) {
//        KingdeeSupplier kingdeeProduct = erpToKingdeeConverter.toKingdee(erpSupplierDTO);
//        kingdeeService.addSupplier(kingdeeProduct);
//    }
//}
