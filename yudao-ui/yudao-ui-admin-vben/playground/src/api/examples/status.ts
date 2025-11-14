import { requestClient } from '#/api/request';

/**
 * 模拟任意状态码
 */
async function getMockStatusApi(status: string) {
  return requestClient.get('/status', { params: { status } });
}

export { getMockStatusApi };
