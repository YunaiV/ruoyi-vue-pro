import { faker } from '@faker-js/faker';
import { eventHandler, getQuery } from 'h3';
import { verifyAccessToken } from '~/utils/jwt-utils';
import {
  sleep,
  unAuthorizedResponse,
  usePageResponseSuccess,
} from '~/utils/response';

function generateMockDataList(count: number) {
  const dataList = [];

  for (let i = 0; i < count; i++) {
    const dataItem = {
      id: faker.string.uuid(),
      imageUrl: faker.image.avatar(),
      imageUrl2: faker.image.avatar(),
      open: faker.datatype.boolean(),
      status: faker.helpers.arrayElement(['success', 'error', 'warning']),
      productName: faker.commerce.productName(),
      price: faker.commerce.price(),
      currency: faker.finance.currencyCode(),
      quantity: faker.number.int({ min: 1, max: 100 }),
      available: faker.datatype.boolean(),
      category: faker.commerce.department(),
      releaseDate: faker.date.past(),
      rating: faker.number.float({ min: 1, max: 5 }),
      description: faker.commerce.productDescription(),
      weight: faker.number.float({ min: 0.1, max: 10 }),
      color: faker.color.human(),
      inProduction: faker.datatype.boolean(),
      tags: Array.from({ length: 3 }, () => faker.commerce.productAdjective()),
    };

    dataList.push(dataItem);
  }

  return dataList;
}

const mockData = generateMockDataList(100);

export default eventHandler(async (event) => {
  const userinfo = verifyAccessToken(event);
  if (!userinfo) {
    return unAuthorizedResponse(event);
  }

  await sleep(600);

  const { page, pageSize, sortBy, sortOrder } = getQuery(event);
  // 规范化分页参数，处理 string[]
  const pageRaw = Array.isArray(page) ? page[0] : page;
  const pageSizeRaw = Array.isArray(pageSize) ? pageSize[0] : pageSize;
  const pageNumber = Math.max(
    1,
    Number.parseInt(String(pageRaw ?? '1'), 10) || 1,
  );
  const pageSizeNumber = Math.min(
    100,
    Math.max(1, Number.parseInt(String(pageSizeRaw ?? '10'), 10) || 10),
  );
  const listData = structuredClone(mockData);

  // 规范化 query 入参，兼容 string[]
  const sortKeyRaw = Array.isArray(sortBy) ? sortBy[0] : sortBy;
  const sortOrderRaw = Array.isArray(sortOrder) ? sortOrder[0] : sortOrder;
  // 检查 sortBy 是否是 listData 元素的合法属性键
  if (
    typeof sortKeyRaw === 'string' &&
    listData[0] &&
    Object.prototype.hasOwnProperty.call(listData[0], sortKeyRaw)
  ) {
    // 定义数组元素的类型
    type ItemType = (typeof listData)[0];
    const sortKey = sortKeyRaw as keyof ItemType; // 将 sortBy 断言为合法键
    const isDesc = sortOrderRaw === 'desc';
    listData.sort((a, b) => {
      const aValue = a[sortKey] as unknown;
      const bValue = b[sortKey] as unknown;

      let result = 0;

      if (typeof aValue === 'number' && typeof bValue === 'number') {
        result = aValue - bValue;
      } else if (aValue instanceof Date && bValue instanceof Date) {
        result = aValue.getTime() - bValue.getTime();
      } else if (typeof aValue === 'boolean' && typeof bValue === 'boolean') {
        if (aValue === bValue) {
          result = 0;
        } else {
          result = aValue ? 1 : -1;
        }
      } else {
        const aStr = String(aValue);
        const bStr = String(bValue);
        const aNum = Number(aStr);
        const bNum = Number(bStr);
        result =
          Number.isFinite(aNum) && Number.isFinite(bNum)
            ? aNum - bNum
            : aStr.localeCompare(bStr, undefined, {
                numeric: true,
                sensitivity: 'base',
              });
      }

      return isDesc ? -result : result;
    });
  }

  return usePageResponseSuccess(
    String(pageNumber),
    String(pageSizeNumber),
    listData,
  );
});
