export class StateHandler {
  private condition: boolean = false;
  private rejectCondition: (() => void) | null = null;
  private resolveCondition: (() => void) | null = null;

  isConditionTrue(): boolean {
    return this.condition;
  }

  reset() {
    this.condition = false;
    this.clearPromises();
  }

  // 触发状态为 false 时，reject
  setConditionFalse() {
    this.condition = false;
    if (this.rejectCondition) {
      this.rejectCondition();
      this.clearPromises();
    }
  }

  // 触发状态为 true 时，resolve
  setConditionTrue() {
    this.condition = true;
    if (this.resolveCondition) {
      this.resolveCondition();
      this.clearPromises();
    }
  }

  // 返回一个 Promise，等待 condition 变为 true
  waitForCondition(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.condition) {
        resolve(); // 如果 condition 已经为 true，立即 resolve
      } else {
        this.resolveCondition = resolve;
        this.rejectCondition = reject;
      }
    });
  }

  // 清理 resolve/reject 函数
  private clearPromises() {
    this.resolveCondition = null;
    this.rejectCondition = null;
  }
}
