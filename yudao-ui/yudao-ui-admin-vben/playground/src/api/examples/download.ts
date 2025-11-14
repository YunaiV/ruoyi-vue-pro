import type { RequestResponse } from '@vben/request';

import { requestClient } from '../request';

/**
 * 下载文件，获取Blob
 * @returns Blob
 */
async function downloadFile1() {
  return requestClient.download<Blob>(
    'https://unpkg.com/@vbenjs/static-source@0.1.7/source/logo-v1.webp',
  );
}

/**
 * 下载文件，获取完整的Response
 * @returns RequestResponse<Blob>
 */
async function downloadFile2() {
  return requestClient.download<RequestResponse<Blob>>(
    'https://unpkg.com/@vbenjs/static-source@0.1.7/source/logo-v1.webp',
    {
      responseReturn: 'raw',
    },
  );
}

export { downloadFile1, downloadFile2 };
