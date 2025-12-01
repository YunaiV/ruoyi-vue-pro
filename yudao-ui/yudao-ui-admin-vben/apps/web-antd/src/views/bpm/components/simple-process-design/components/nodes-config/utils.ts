import { APPROVE_TYPE, ApproveType, TimeUnitType } from '../../consts';

/** 获取条件节点默认的名称 */
export function getDefaultConditionNodeName(
  index: number,
  defaultFlow: boolean | undefined,
): string {
  if (defaultFlow) {
    return '其它情况';
  }
  return `条件${index + 1}`;
}

/** 获取包容分支条件节点默认的名称 */
export function getDefaultInclusiveConditionNodeName(
  index: number,
  defaultFlow: boolean | undefined,
): string {
  if (defaultFlow) {
    return '其它情况';
  }
  return `包容条件${index + 1}`;
}

/** 转换时间单位字符串为枚举值 */
export function convertTimeUnit(strTimeUnit: string) {
  if (strTimeUnit === 'M') {
    return TimeUnitType.MINUTE;
  }
  if (strTimeUnit === 'H') {
    return TimeUnitType.HOUR;
  }
  if (strTimeUnit === 'D') {
    return TimeUnitType.DAY;
  }
  return TimeUnitType.HOUR;
}

/** 根据审批类型获取对应的文本描述 */
export function getApproveTypeText(approveType: ApproveType): string {
  let approveTypeText = '';
  APPROVE_TYPE.forEach((item) => {
    if (item.value === approveType) {
      approveTypeText = item.label;
    }
  });
  return approveTypeText;
}
