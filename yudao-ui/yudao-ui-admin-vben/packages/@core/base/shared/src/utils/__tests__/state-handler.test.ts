import { describe, expect, it } from 'vitest';

import { StateHandler } from '../state-handler';

describe('stateHandler', () => {
  it('should resolve when condition is set to true', async () => {
    const handler = new StateHandler();

    // 模拟异步设置 condition 为 true
    setTimeout(() => {
      handler.setConditionTrue(); // 明确触发 condition 为 true
    }, 10);

    // 等待条件被设置为 true
    await handler.waitForCondition();
    expect(handler.isConditionTrue()).toBe(true);
  });

  it('should resolve immediately if condition is already true', async () => {
    const handler = new StateHandler();
    handler.setConditionTrue(); // 提前设置为 true

    // 立即 resolve，因为 condition 已经是 true
    await handler.waitForCondition();
    expect(handler.isConditionTrue()).toBe(true);
  });

  it('should reject when condition is set to false after waiting', async () => {
    const handler = new StateHandler();

    // 模拟异步设置 condition 为 false
    setTimeout(() => {
      handler.setConditionFalse(); // 明确触发 condition 为 false
    }, 10);

    // 等待过程中，期望 Promise 被 reject
    await expect(handler.waitForCondition()).rejects.toThrow();
    expect(handler.isConditionTrue()).toBe(false);
  });

  it('should reset condition to false', () => {
    const handler = new StateHandler();
    handler.setConditionTrue(); // 设置为 true
    handler.reset(); // 重置为 false

    expect(handler.isConditionTrue()).toBe(false);
  });

  it('should resolve when condition is set to true after reset', async () => {
    const handler = new StateHandler();
    handler.reset(); // 确保初始为 false

    setTimeout(() => {
      handler.setConditionTrue(); // 重置后设置为 true
    }, 10);

    await handler.waitForCondition();
    expect(handler.isConditionTrue()).toBe(true);
  });
});
