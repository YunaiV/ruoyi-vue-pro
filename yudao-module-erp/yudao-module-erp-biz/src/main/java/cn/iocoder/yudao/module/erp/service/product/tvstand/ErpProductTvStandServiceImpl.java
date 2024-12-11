package cn.iocoder.yudao.module.erp.service.product.tvstand;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import cn.iocoder.yudao.module.erp.service.product.tvstand.bo.ErpProductTvStandBO;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCT_FIELD_NOT_MATCH;

/**
 * @author: Wqh
 * @date: 2024/12/3 16:40
 */
@Service
public class ErpProductTvStandServiceImpl implements ErpProductTvStandService{
    /***
    * @Author Wqh
    * @Description 校验电视机架字段是否匹配
    * @Date 15:00 2024/12/11
    * @Param [vo]
    * @return void
    **/
    public void validationField(ErpProductSaveReqVO vo) throws IllegalAccessException {
        // 获取 ErpProductBO 的字段
        Field[] productBoFields = ErpProductBO.class.getDeclaredFields();
        Map<String, Field> productBoFieldMap = new HashMap<>();
        for (Field field : productBoFields) {
            productBoFieldMap.put(field.getName(), field);
        }
        // 获取 ErpProductTvStandBO 的字段信息
        Field[] boFields = ErpProductTvStandBO.class.getDeclaredFields();
        Map<String, Field> boFieldMap = new HashMap<>();
        for (Field field : boFields) {
            boFieldMap.put(field.getName(), field);
        }
        // 获取 ErpProductSaveReqVO 的字段信息
        Field[] voFields = ErpProductSaveReqVO.class.getDeclaredFields();
        for (Field voField : voFields) {
            voField.setAccessible(true);
            //排除共有的字段，判断voField和field是否相同
            if (productBoFieldMap.containsKey(voField.getName())){
                continue;
            }
            Field boField = boFieldMap.get(voField.getName());
            // 字段在 ErpProductTvStandBO 中不存在
            if (boField == null) {
                //获取vo中的字段值
                Object voFieldValue = voField.get(vo);
                //如果存在字段值则抛出异常
                ThrowUtil.ifThrow(ObjUtil.isNotEmpty(voFieldValue),PRODUCT_FIELD_NOT_MATCH);
            }
        }
    }
}
