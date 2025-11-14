import type { RouteLocationNormalized } from 'vue-router';

export interface TabDefinition extends RouteLocationNormalized {
  /**
   * 标签页的key
   */
  key?: string;
}
