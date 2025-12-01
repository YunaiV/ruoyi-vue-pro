import type { RequestClient } from '../request-client';
import type { RequestClientConfig } from '../types';

type DownloadRequestConfig = {
  /**
   * 定义期望获得的数据类型。
   * raw: 原始的AxiosResponse，包括headers、status等。
   * body: 只返回响应数据的BODY部分(Blob)
   */
  responseReturn?: 'body' | 'raw';
} & Omit<RequestClientConfig, 'responseReturn'>;

class FileDownloader {
  private client: RequestClient;

  constructor(client: RequestClient) {
    this.client = client;
  }
  /**
   * 下载文件
   * @param url 文件的完整链接
   * @param config 配置信息，可选。
   * @returns 如果config.responseReturn为'body'，则返回Blob(默认)，否则返回RequestResponse<Blob>
   */
  public async download<T = Blob>(
    url: string,
    config?: DownloadRequestConfig,
  ): Promise<T> {
    const finalConfig: DownloadRequestConfig = {
      responseReturn: 'body',
      method: 'GET',
      ...config,
      responseType: 'blob',
    };

    // Prefer a generic request if available; otherwise, dispatch to method-specific calls.
    const method = (finalConfig.method || 'GET').toUpperCase();
    const clientAny = this.client as any;

    if (typeof clientAny.request === 'function') {
      return await clientAny.request(url, finalConfig);
    }
    const lower = method.toLowerCase();

    if (typeof clientAny[lower] === 'function') {
      if (['POST', 'PUT'].includes(method)) {
        const { data, ...rest } = finalConfig as Record<string, any>;
        return await clientAny[lower](url, data, rest);
      }

      return await clientAny[lower](url, finalConfig);
    }

    throw new Error(
      `RequestClient does not support method "${method}". Please ensure the method is properly implemented in your RequestClient instance.`,
    );
  }
}

export { FileDownloader };
