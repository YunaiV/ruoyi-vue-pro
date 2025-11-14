import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace DataSinkApi {
  /** IoT 数据流转目的 VO */
  export interface Sink {
    id?: number;
    name: string;
    description?: string;
    status?: number;
    type: string;
    config?: any;
    createTime?: Date;
  }
}

/** IoT 数据流转目的 */
export interface DataSinkVO {
  id?: number;
  name?: string;
  description?: string;
  status?: number;
  type?: string;
  config?: any;
  createTime?: Date;
}

/** IoT 数据目的类型枚举 */
export enum IotDataSinkTypeEnum {
  HTTP = 'HTTP',
  KAFKA = 'KAFKA',
  MQTT = 'MQTT',
  RABBITMQ = 'RABBITMQ',
  REDIS_STREAM = 'REDIS_STREAM',
  ROCKETMQ = 'ROCKETMQ',
}

/** HTTP 配置 */
export interface HttpConfig {
  url?: string;
  method?: string;
  headers?: Record<string, string>;
  timeout?: number;
}

/** MQTT 配置 */
export interface MqttConfig {
  broker?: string;
  port?: number;
  topic?: string;
  username?: string;
  password?: string;
  clientId?: string;
  qos?: number;
}

/** Kafka 配置 */
export interface KafkaMQConfig {
  bootstrapServers?: string;
  topic?: string;
  acks?: string;
  retries?: number;
  batchSize?: number;
}

/** RabbitMQ 配置 */
export interface RabbitMQConfig {
  host?: string;
  port?: number;
  virtualHost?: string;
  username?: string;
  password?: string;
  exchange?: string;
  routingKey?: string;
  queue?: string;
}

/** RocketMQ 配置 */
export interface RocketMQConfig {
  nameServer?: string;
  topic?: string;
  tag?: string;
  producerGroup?: string;
}

/** Redis Stream 配置 */
export interface RedisStreamMQConfig {
  host?: string;
  port?: number;
  password?: string;
  database?: number;
  streamKey?: string;
  maxLen?: number;
}

/** 查询数据流转目的分页 */
export function getDataSinkPage(params: PageParam) {
  return requestClient.get<PageResult<DataSinkApi.Sink>>(
    '/iot/data-sink/page',
    { params },
  );
}

/** 查询数据流转目的详情 */
export function getDataSink(id: number) {
  return requestClient.get<DataSinkApi.Sink>(`/iot/data-sink/get?id=${id}`);
}

/** 查询所有数据流转目的列表 */
export function getDataSinkList() {
  return requestClient.get<DataSinkApi.Sink[]>('/iot/data-sink/list');
}

/** 查询数据流转目的简单列表 */
export function getDataSinkSimpleList() {
  return requestClient.get<DataSinkApi.Sink[]>('/iot/data-sink/simple-list');
}

/** 新增数据流转目的 */
export function createDataSink(data: DataSinkVO) {
  return requestClient.post('/iot/data-sink/create', data);
}

/** 修改数据流转目的 */
export function updateDataSink(data: DataSinkVO) {
  return requestClient.put('/iot/data-sink/update', data);
}

/** 删除数据流转目的 */
export function deleteDataSink(id: number) {
  return requestClient.delete(`/iot/data-sink/delete?id=${id}`);
}

/** 批量删除数据流转目的 */
export function deleteDataSinkList(ids: number[]) {
  return requestClient.delete('/iot/data-sink/delete-list', {
    params: { ids: ids.join(',') },
  });
}

/** 更新数据流转目的状态 */
export function updateDataSinkStatus(id: number, status: number) {
  return requestClient.put(`/iot/data-sink/update-status`, {
    id,
    status,
  });
}
