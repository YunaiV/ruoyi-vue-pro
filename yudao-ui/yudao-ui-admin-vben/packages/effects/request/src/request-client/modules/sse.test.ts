import type { RequestClient } from '../request-client';

import { beforeEach, describe, expect, it, vi } from 'vitest';

import { SSE } from './sse';

// 模拟 TextDecoder
const OriginalTextDecoder = globalThis.TextDecoder;

beforeEach(() => {
  vi.stubGlobal(
    'TextDecoder',
    class {
      private decoder = new OriginalTextDecoder();
      decode(value: Uint8Array, opts?: any) {
        return this.decoder.decode(value, opts);
      }
    },
  );
});

// 创建 fetch mock
const createFetchMock = (chunks: string[], ok = true) => {
  const encoder = new TextEncoder();
  let index = 0;
  return vi.fn().mockResolvedValue({
    ok,
    status: ok ? 200 : 500,
    body: {
      getReader: () => ({
        read: async () => {
          if (index < chunks.length) {
            return { done: false, value: encoder.encode(chunks[index++]) };
          }
          return { done: true, value: undefined };
        },
      }),
    },
  });
};

describe('sSE', () => {
  let client: RequestClient;
  let sse: SSE;

  beforeEach(() => {
    vi.restoreAllMocks();
    client = {
      getBaseUrl: () => 'http://localhost',
      instance: {
        interceptors: {
          request: {
            handlers: [],
          },
        },
      },
    } as unknown as RequestClient;
    sse = new SSE(client);
  });

  it('should call requestSSE when postSSE is used', async () => {
    const spy = vi.spyOn(sse, 'requestSSE').mockResolvedValue(undefined);
    await sse.postSSE('/test', { foo: 'bar' }, { headers: { a: '1' } });
    expect(spy).toHaveBeenCalledWith(
      '/test',
      { foo: 'bar' },
      {
        headers: { a: '1' },
        method: 'POST',
      },
    );
  });

  it('should throw error if fetch response not ok', async () => {
    vi.stubGlobal('fetch', createFetchMock([], false));
    await expect(sse.requestSSE('/bad')).rejects.toThrow(
      'HTTP error! status: 500',
    );
  });

  it('should trigger onMessage and onEnd callbacks', async () => {
    const messages: string[] = [];
    const onMessage = vi.fn((msg: string) => messages.push(msg));
    const onEnd = vi.fn();

    vi.stubGlobal('fetch', createFetchMock(['hello', ' world']));

    await sse.requestSSE('/sse', undefined, { onMessage, onEnd });

    expect(onMessage).toHaveBeenCalledTimes(2);
    expect(messages.join('')).toBe('hello world');
    // onEnd 不再带参数
    expect(onEnd).toHaveBeenCalled();
  });

  it('should apply request interceptors', async () => {
    const interceptor = vi.fn(async (config) => {
      config.headers['x-test'] = 'intercepted';
      return config;
    });
    (client.instance.interceptors.request as any).handlers.push({
      fulfilled: interceptor,
    });

    // 创建 fetch mock，并挂到全局
    const fetchMock = createFetchMock(['data']);
    vi.stubGlobal('fetch', fetchMock);

    await sse.requestSSE('/sse', undefined, {});

    expect(interceptor).toHaveBeenCalled();
    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost/sse',
      expect.objectContaining({
        headers: expect.any(Headers),
      }),
    );

    const calls = fetchMock.mock?.calls;
    expect(calls).toBeDefined();
    expect(calls?.length).toBeGreaterThan(0);

    const init = calls?.[0]?.[1] as RequestInit;
    expect(init).toBeDefined();

    const headers = init?.headers as Headers;
    expect(headers?.get('x-test')).toBe('intercepted');
    expect(headers?.get('accept')).toBe('text/event-stream');
  });

  it('should throw error when no reader', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        body: null,
      }),
    );
    await expect(sse.requestSSE('/sse')).rejects.toThrow('No reader');
  });
});
