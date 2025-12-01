/* eslint-disable @typescript-eslint/no-non-null-assertion */
import type { MallSpuApi } from '#/api/mall/product/spu';
import type { PropertyAndValues } from '#/views/mall/product/spu/components/type';

/** 获得商品的规格列表 - 商品相关的公共函数（被其它模块如 promotion 使用） */
const getPropertyList = (spu: MallSpuApi.Spu): PropertyAndValues[] => {
  //  直接拿返回的 skus 属性逆向生成出 propertyList
  const properties: PropertyAndValues[] = [];
  // 只有是多规格才处理
  if (spu.specType) {
    spu.skus?.forEach((sku) => {
      sku.properties?.forEach(
        ({ propertyId, propertyName, valueId, valueName }) => {
          // 添加属性
          if (!properties?.some((item) => item.id === propertyId)) {
            properties.push({
              id: propertyId!,
              name: propertyName!,
              values: [],
            });
          }
          // 添加属性值
          const index = properties?.findIndex((item) => item.id === propertyId);
          if (
            !properties[index]?.values?.some((value) => value.id === valueId)
          ) {
            properties[index]?.values?.push({ id: valueId!, name: valueName! });
          }
        },
      );
    });
  }
  return properties;
};

export { getPropertyList };
