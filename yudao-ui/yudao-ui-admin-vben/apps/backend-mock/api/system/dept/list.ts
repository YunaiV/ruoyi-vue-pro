import { faker } from '@faker-js/faker';
import { eventHandler } from 'h3';
import { verifyAccessToken } from '~/utils/jwt-utils';
import { unAuthorizedResponse, useResponseSuccess } from '~/utils/response';

const formatterCN = new Intl.DateTimeFormat('zh-CN', {
  timeZone: 'Asia/Shanghai',
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
});

function generateMockDataList(count: number) {
  const dataList = [];

  for (let i = 0; i < count; i++) {
    const dataItem: Record<string, any> = {
      id: faker.string.uuid(),
      pid: 0,
      name: faker.commerce.department(),
      status: faker.helpers.arrayElement([0, 1]),
      createTime: formatterCN.format(
        faker.date.between({ from: '2021-01-01', to: '2022-12-31' }),
      ),
      remark: faker.lorem.sentence(),
    };
    if (faker.datatype.boolean()) {
      dataItem.children = Array.from(
        { length: faker.number.int({ min: 1, max: 5 }) },
        () => ({
          id: faker.string.uuid(),
          pid: dataItem.id,
          name: faker.commerce.department(),
          status: faker.helpers.arrayElement([0, 1]),
          createTime: formatterCN.format(
            faker.date.between({ from: '2023-01-01', to: '2023-12-31' }),
          ),
          remark: faker.lorem.sentence(),
        }),
      );
    }
    dataList.push(dataItem);
  }

  return dataList;
}

const mockData = generateMockDataList(10);

export default eventHandler(async (event) => {
  const userinfo = verifyAccessToken(event);
  if (!userinfo) {
    return unAuthorizedResponse(event);
  }

  const listData = structuredClone(mockData);

  return useResponseSuccess(listData);
});
