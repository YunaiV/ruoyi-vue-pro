import { faker } from '@faker-js/faker';
import { eventHandler, getQuery } from 'h3';
import { verifyAccessToken } from '~/utils/jwt-utils';
import { getMenuIds, MOCK_MENU_LIST } from '~/utils/mock-data';
import { unAuthorizedResponse, usePageResponseSuccess } from '~/utils/response';

const formatterCN = new Intl.DateTimeFormat('zh-CN', {
  timeZone: 'Asia/Shanghai',
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
});

const menuIds = getMenuIds(MOCK_MENU_LIST);

function generateMockDataList(count: number) {
  const dataList = [];

  for (let i = 0; i < count; i++) {
    const dataItem: Record<string, any> = {
      id: faker.string.uuid(),
      name: faker.commerce.product(),
      status: faker.helpers.arrayElement([0, 1]),
      createTime: formatterCN.format(
        faker.date.between({ from: '2022-01-01', to: '2025-01-01' }),
      ),
      permissions: faker.helpers.arrayElements(menuIds),
      remark: faker.lorem.sentence(),
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

  const {
    page = 1,
    pageSize = 20,
    name,
    id,
    remark,
    startTime,
    endTime,
    status,
  } = getQuery(event);
  let listData = structuredClone(mockData);
  if (name) {
    listData = listData.filter((item) =>
      item.name.toLowerCase().includes(String(name).toLowerCase()),
    );
  }
  if (id) {
    listData = listData.filter((item) =>
      item.id.toLowerCase().includes(String(id).toLowerCase()),
    );
  }
  if (remark) {
    listData = listData.filter((item) =>
      item.remark?.toLowerCase()?.includes(String(remark).toLowerCase()),
    );
  }
  if (startTime) {
    listData = listData.filter((item) => item.createTime >= startTime);
  }
  if (endTime) {
    listData = listData.filter((item) => item.createTime <= endTime);
  }
  if (['0', '1'].includes(status as string)) {
    listData = listData.filter((item) => item.status === Number(status));
  }
  return usePageResponseSuccess(page as string, pageSize as string, listData);
});
