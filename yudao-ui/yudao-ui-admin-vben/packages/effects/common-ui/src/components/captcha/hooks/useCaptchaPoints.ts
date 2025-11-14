import type { CaptchaPoint } from '../types';

import { reactive } from 'vue';

export function useCaptchaPoints() {
  const points = reactive<CaptchaPoint[]>([]);
  function addPoint(point: CaptchaPoint) {
    points.push(point);
  }

  function clearPoints() {
    points.splice(0);
  }
  return {
    addPoint,
    clearPoints,
    points,
  };
}
