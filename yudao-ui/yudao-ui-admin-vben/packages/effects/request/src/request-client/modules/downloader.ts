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
      ...config,
      responseType: 'blob',
    };

    const response = await this.client.get<T>(url, finalConfig);

    return response;
  }
}

export { FileDownloader };
