import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { RequestClient } from './request-client';

describe('requestClient', () => {
  let mock: MockAdapter;
  let requestClient: RequestClient;

  beforeEach(() => {
    mock = new MockAdapter(axios);
    requestClient = new RequestClient();
  });

  afterEach(() => {
    mock.reset();
  });

  it('should successfully make a GET request', async () => {
    mock.onGet('test/url').reply(200, { data: 'response' });

    const response = await requestClient.get('test/url');

    expect(response.data).toEqual({ data: 'response' });
  });

  it('should successfully make a POST request', async () => {
    const postData = { key: 'value' };
    const mockData = { data: 'response' };
    mock.onPost('/test/post', postData).reply(200, mockData);
    const response = await requestClient.post('/test/post', postData);
    expect(response.data).toEqual(mockData);
  });

  it('should successfully make a PUT request', async () => {
    const putData = { key: 'updatedValue' };
    const mockData = { data: 'updated response' };
    mock.onPut('/test/put', putData).reply(200, mockData);
    const response = await requestClient.put('/test/put', putData);
    expect(response.data).toEqual(mockData);
  });

  it('should successfully make a DELETE request', async () => {
    const mockData = { data: 'delete response' };
    mock.onDelete('/test/delete').reply(200, mockData);
    const response = await requestClient.delete('/test/delete');
    expect(response.data).toEqual(mockData);
  });

  it('should handle network errors', async () => {
    mock.onGet('/test/error').networkError();
    try {
      await requestClient.get('/test/error');
      expect(true).toBe(false);
    } catch (error: any) {
      expect(error.isAxiosError).toBe(true);
      expect(error.message).toBe('Network Error');
    }
  });

  it('should handle timeout', async () => {
    mock.onGet('/test/timeout').timeout();
    try {
      await requestClient.get('/test/timeout');
      expect(true).toBe(false);
    } catch (error: any) {
      expect(error.isAxiosError).toBe(true);
      expect(error.code).toBe('ECONNABORTED');
    }
  });

  it('should successfully upload a file', async () => {
    const fileData = new Blob(['file contents'], { type: 'text/plain' });

    mock.onPost('/test/upload').reply((config) => {
      return config.data instanceof FormData && config.data.has('file')
        ? [200, { data: 'file uploaded' }]
        : [400, { error: 'Bad Request' }];
    });

    const response = await requestClient.upload('/test/upload', {
      file: fileData,
    });
    expect(response.data).toEqual({ data: 'file uploaded' });
  });

  it('should successfully download a file as a blob', async () => {
    const mockFileContent = new Blob(['mock file content'], {
      type: 'text/plain',
    });

    mock.onGet('/test/download').reply(200, mockFileContent);

    const res = await requestClient.download('/test/download');

    expect(res.data).toBeInstanceOf(Blob);
  });
});
