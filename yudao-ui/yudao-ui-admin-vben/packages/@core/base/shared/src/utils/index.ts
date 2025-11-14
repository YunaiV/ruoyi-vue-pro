export * from './cn';
export * from './date';
export * from './diff';
export * from './dom';
export * from './download';
export * from './encrypt';
export * from './formatNumber';
export * from './inference';
export * from './letter';
export * from './merge';
export * from './nprogress';
export * from './resources';
export * from './state-handler';
export * from './time';
export * from './to';
export * from './tree';
export * from './unique';
export * from './update-css-variables';
export * from './upload';
export * from './util';
export * from './uuid'; // add by 芋艿：从 vben2.0 复制
export * from './window';
export { default as cloneDeep } from 'lodash.clonedeep';
export { default as get } from 'lodash.get';
export { default as isEqual } from 'lodash.isequal';
export { default as set } from 'lodash.set';

/**
 * 构建排序字段
 * @param prop 字段名称
 * @param order 顺序
 */
export const buildSortingField = ({
  prop,
  order,
}: {
  order: 'ascending' | 'descending';
  prop: string;
}) => {
  return { field: prop, order: order === 'ascending' ? 'asc' : 'desc' };
};
