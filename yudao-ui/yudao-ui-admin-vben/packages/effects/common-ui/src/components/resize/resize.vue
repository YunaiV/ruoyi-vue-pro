<script lang="ts" setup>
/**
 * This components is refactored from vue-drag-resize: https://github.com/kirillmurashov/vue-drag-resize
 */

import {
  computed,
  getCurrentInstance,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  toRefs,
  watch,
} from 'vue';

const props = defineProps({
  stickSize: {
    type: Number,
    default: 8,
  },
  parentScaleX: {
    type: Number,
    default: 1,
  },
  parentScaleY: {
    type: Number,
    default: 1,
  },
  isActive: {
    type: Boolean,
    default: false,
  },
  preventActiveBehavior: {
    type: Boolean,
    default: false,
  },
  isDraggable: {
    type: Boolean,
    default: true,
  },
  isResizable: {
    type: Boolean,
    default: true,
  },
  aspectRatio: {
    type: Boolean,
    default: false,
  },
  parentLimitation: {
    type: Boolean,
    default: false,
  },
  snapToGrid: {
    type: Boolean,
    default: false,
  },
  gridX: {
    type: Number,
    default: 50,
    validator(val: number) {
      return val >= 0;
    },
  },
  gridY: {
    type: Number,
    default: 50,
    validator(val: number) {
      return val >= 0;
    },
  },
  parentW: {
    type: Number,
    default: 0,
    validator(val: number) {
      return val >= 0;
    },
  },
  parentH: {
    type: Number,
    default: 0,
    validator(val: number) {
      return val >= 0;
    },
  },
  w: {
    type: [String, Number],
    default: 200,
    validator(val: number) {
      return typeof val === 'string' ? val === 'auto' : val >= 0;
    },
  },
  h: {
    type: [String, Number],
    default: 200,
    validator(val: number) {
      return typeof val === 'string' ? val === 'auto' : val >= 0;
    },
  },
  minw: {
    type: Number,
    default: 50,
    validator(val: number) {
      return val >= 0;
    },
  },
  minh: {
    type: Number,
    default: 50,
    validator(val: number) {
      return val >= 0;
    },
  },
  x: {
    type: Number,
    default: 0,
    validator(val: number) {
      return typeof val === 'number';
    },
  },
  y: {
    type: Number,
    default: 0,
    validator(val: number) {
      return typeof val === 'number';
    },
  },
  z: {
    type: [String, Number],
    default: 'auto',
    validator(val: number) {
      return typeof val === 'string' ? val === 'auto' : val >= 0;
    },
  },
  dragHandle: {
    type: String,
    default: null,
  },
  dragCancel: {
    type: String,
    default: null,
  },
  sticks: {
    type: Array<'bl' | 'bm' | 'br' | 'ml' | 'mr' | 'tl' | 'tm' | 'tr'>,
    default() {
      return ['tl', 'tm', 'tr', 'mr', 'br', 'bm', 'bl', 'ml'];
    },
  },
  axis: {
    type: String,
    default: 'both',
    validator(val: string) {
      return ['both', 'none', 'x', 'y'].includes(val);
    },
  },
  contentClass: {
    type: String,
    required: false,
    default: '',
  },
});

const emit = defineEmits([
  'clicked',
  'dragging',
  'dragstop',
  'resizing',
  'resizestop',
  'activated',
  'deactivated',
]);

const styleMapping = {
  y: {
    t: 'top',
    m: 'marginTop',
    b: 'bottom',
  },
  x: {
    l: 'left',
    m: 'marginLeft',
    r: 'right',
  },
};

function addEvents(events: Map<string, (...args: any[]) => void>) {
  events.forEach((cb, eventName) => {
    document.documentElement.addEventListener(eventName, cb);
  });
}

function removeEvents(events: Map<string, (...args: any[]) => void>) {
  events.forEach((cb, eventName) => {
    document.documentElement.removeEventListener(eventName, cb);
  });
}

const {
  stickSize,
  parentScaleX,
  parentScaleY,
  isActive,
  preventActiveBehavior,
  isDraggable,
  isResizable,
  aspectRatio,
  parentLimitation,
  snapToGrid,
  gridX,
  gridY,
  parentW,
  parentH,
  w,
  h,
  minw,
  minh,
  x,
  y,
  z,
  dragHandle,
  dragCancel,
  sticks,
  axis,
  contentClass,
} = toRefs(props);

// states
const active = ref(false);
const zIndex = ref<null | number>(null);
const parentWidth = ref<null | number>(null);
const parentHeight = ref<null | number>(null);
const left = ref<null | number>(null);
const top = ref<null | number>(null);
const right = ref<null | number>(null);
const bottom = ref<null | number>(null);

const aspectFactor = ref<null | number>(null);

// state end

const stickDrag = ref(false);
const bodyDrag = ref(false);
const dimensionsBeforeMove = ref({
  pointerX: 0,
  pointerY: 0,
  x: 0,
  y: 0,
  w: 0,
  h: 0,
  top: 0,
  right: 0,
  bottom: 0,
  left: 0,
  width: 0,
  height: 0,
});
const limits = ref({
  left: { min: null as null | number, max: null as null | number },
  right: { min: null as null | number, max: null as null | number },
  top: { min: null as null | number, max: null as null | number },
  bottom: { min: null as null | number, max: null as null | number },
});
const currentStick = ref<null | string>(null);

const parentElement = ref<HTMLElement | null>(null);

const width = computed(() => parentWidth.value! - left.value! - right.value!);

const height = computed(() => parentHeight.value! - top.value! - bottom.value!);

const rect = computed(() => ({
  left: Math.round(left.value!),
  top: Math.round(top.value!),
  width: Math.round(width.value),
  height: Math.round(height.value),
}));

const saveDimensionsBeforeMove = ({
  pointerX,
  pointerY,
}: {
  pointerX: number;
  pointerY: number;
}) => {
  dimensionsBeforeMove.value.pointerX = pointerX;
  dimensionsBeforeMove.value.pointerY = pointerY;

  dimensionsBeforeMove.value.left = left.value as number;
  dimensionsBeforeMove.value.right = right.value as number;
  dimensionsBeforeMove.value.top = top.value as number;
  dimensionsBeforeMove.value.bottom = bottom.value as number;

  dimensionsBeforeMove.value.width = width.value as number;
  dimensionsBeforeMove.value.height = height.value as number;

  aspectFactor.value = width.value / height.value;
};

const sideCorrectionByLimit = (
  limit: { max: number; min: number },
  current: number,
) => {
  let value = current;

  if (limit.min !== null && current < limit.min) {
    value = limit.min;
  } else if (limit.max !== null && limit.max < current) {
    value = limit.max;
  }

  return value;
};

const rectCorrectionByLimit = (rect: {
  newBottom: number;
  newLeft: number;
  newRight: number;
  newTop: number;
}) => {
  // const { limits } = this;
  let { newRight, newLeft, newBottom, newTop } = rect;

  type RectRange = {
    max: number;
    min: number;
  };

  newLeft = sideCorrectionByLimit(limits.value.left as RectRange, newLeft);
  newRight = sideCorrectionByLimit(limits.value.right as RectRange, newRight);
  newTop = sideCorrectionByLimit(limits.value.top as RectRange, newTop);
  newBottom = sideCorrectionByLimit(
    limits.value.bottom as RectRange,
    newBottom,
  );

  return {
    newLeft,
    newRight,
    newTop,
    newBottom,
  };
};

const rectCorrectionByAspectRatio = (rect: {
  newBottom: number;
  newLeft: number;
  newRight: number;
  newTop: number;
}) => {
  let { newLeft, newRight, newTop, newBottom } = rect;
  // const { parentWidth, parentHeight, currentStick, aspectFactor, dimensionsBeforeMove } = this;

  let newWidth = parentWidth.value! - newLeft - newRight;
  let newHeight = parentHeight.value! - newTop - newBottom;

  if (currentStick.value![1] === 'm') {
    const deltaHeight = newHeight - dimensionsBeforeMove.value.height;

    newLeft -= (deltaHeight * aspectFactor.value!) / 2;
    newRight -= (deltaHeight * aspectFactor.value!) / 2;
  } else if (currentStick.value![0] === 'm') {
    const deltaWidth = newWidth - dimensionsBeforeMove.value.width;

    newTop -= deltaWidth / aspectFactor.value! / 2;
    newBottom -= deltaWidth / aspectFactor.value! / 2;
  } else if (newWidth / newHeight > aspectFactor.value!) {
    newWidth = aspectFactor.value! * newHeight;

    if (currentStick.value![1] === 'l') {
      newLeft = parentWidth.value! - newRight - newWidth;
    } else {
      newRight = parentWidth.value! - newLeft - newWidth;
    }
  } else {
    newHeight = newWidth / aspectFactor.value!;

    if (currentStick.value![0] === 't') {
      newTop = parentHeight.value! - newBottom - newHeight;
    } else {
      newBottom = parentHeight.value! - newTop - newHeight;
    }
  }

  return { newLeft, newRight, newTop, newBottom };
};

const stickMove = (delta: { x: number; y: number }) => {
  let newTop = dimensionsBeforeMove.value.top;
  let newBottom = dimensionsBeforeMove.value.bottom;
  let newLeft = dimensionsBeforeMove.value.left;
  let newRight = dimensionsBeforeMove.value.right;
  switch (currentStick.value![0]) {
    case 'b': {
      newBottom = dimensionsBeforeMove.value.bottom + delta.y;

      if (snapToGrid.value) {
        newBottom =
          (parentHeight.value as number) -
          Math.round(
            ((parentHeight.value as number) - newBottom) / gridY.value,
          ) *
            gridY.value;
      }

      break;
    }

    case 't': {
      newTop = dimensionsBeforeMove.value.top - delta.y;

      if (snapToGrid.value) {
        newTop = Math.round(newTop / gridY.value) * gridY.value;
      }

      break;
    }
    default: {
      break;
    }
  }

  switch (currentStick.value![1]) {
    case 'l': {
      newLeft = dimensionsBeforeMove.value.left - delta.x;

      if (snapToGrid.value) {
        newLeft = Math.round(newLeft / gridX.value) * gridX.value;
      }

      break;
    }

    case 'r': {
      newRight = dimensionsBeforeMove.value.right + delta.x;

      if (snapToGrid.value) {
        newRight =
          (parentWidth.value as number) -
          Math.round(((parentWidth.value as number) - newRight) / gridX.value) *
            gridX.value;
      }

      break;
    }
    default: {
      break;
    }
  }

  ({ newLeft, newRight, newTop, newBottom } = rectCorrectionByLimit({
    newLeft,
    newRight,
    newTop,
    newBottom,
  }));

  if (aspectRatio.value) {
    ({ newLeft, newRight, newTop, newBottom } = rectCorrectionByAspectRatio({
      newLeft,
      newRight,
      newTop,
      newBottom,
    }));
  }

  left.value = newLeft;
  right.value = newRight;
  top.value = newTop;
  bottom.value = newBottom;

  emit('resizing', rect.value);
};

const stickUp = () => {
  stickDrag.value = false;
  // dimensionsBeforeMove.value = {
  //   pointerX: 0,
  //   pointerY: 0,
  //   x: 0,
  //   y: 0,
  //   w: 0,
  //   h: 0,
  // };

  Object.assign(dimensionsBeforeMove.value, {
    pointerX: 0,
    pointerY: 0,
    x: 0,
    y: 0,
    w: 0,
    h: 0,
  });

  limits.value = {
    left: { min: null, max: null },
    right: { min: null, max: null },
    top: { min: null, max: null },
    bottom: { min: null, max: null },
  };

  emit('resizing', rect.value);
  emit('resizestop', rect.value);
};

const calcDragLimitation = () => {
  return {
    left: { min: 0, max: (parentWidth.value as number) - width.value },
    right: { min: 0, max: (parentWidth.value as number) - width.value },
    top: { min: 0, max: (parentHeight.value as number) - height.value },
    bottom: { min: 0, max: (parentHeight.value as number) - height.value },
  };
};

const calcResizeLimits = () => {
  // const { aspectFactor, width, height, bottom, top, left, right } = this;

  const parentLim = parentLimitation.value ? 0 : null;

  if (aspectRatio.value) {
    if (minw.value / minh.value > (aspectFactor.value as number)) {
      minh.value = minw.value / (aspectFactor.value as number);
    } else {
      minw.value = ((aspectFactor.value as number) * minh.value) as number;
    }
  }

  const limits = {
    left: {
      min: parentLim,
      max: (left.value as number) + (width.value - minw.value),
    },
    right: {
      min: parentLim,
      max: (right.value as number) + (width.value - minw.value),
    },
    top: {
      min: parentLim,
      max: (top.value as number) + (height.value - minh.value),
    },
    bottom: {
      min: parentLim,
      max: (bottom.value as number) + (height.value - minh.value),
    },
  };

  if (aspectRatio.value) {
    const aspectLimits = {
      left: {
        min:
          left.value! -
          Math.min(top.value!, bottom.value!) * aspectFactor.value! * 2,
        max:
          left.value! +
          ((height.value - minh.value!) / 2) * aspectFactor.value! * 2,
      },
      right: {
        min:
          right.value! -
          Math.min(top.value!, bottom.value!) * aspectFactor.value! * 2,
        max:
          right.value! +
          ((height.value - minh.value!) / 2) * aspectFactor.value! * 2,
      },
      top: {
        min:
          top.value! -
          (Math.min(left.value!, right.value!) / aspectFactor.value!) * 2,
        max:
          top.value! +
          ((width.value - minw.value) / 2 / aspectFactor.value!) * 2,
      },
      bottom: {
        min:
          bottom.value! -
          (Math.min(left.value!, right.value!) / aspectFactor.value!) * 2,
        max:
          bottom.value! +
          ((width.value - minw.value) / 2 / aspectFactor.value!) * 2,
      },
    };

    if (currentStick.value![0] === 'm') {
      limits.left = {
        min: Math.max(limits.left.min!, aspectLimits.left.min),
        max: Math.min(limits.left.max, aspectLimits.left.max),
      };
      limits.right = {
        min: Math.max(limits.right.min!, aspectLimits.right.min),
        max: Math.min(limits.right.max, aspectLimits.right.max),
      };
    } else if (currentStick.value![1] === 'm') {
      limits.top = {
        min: Math.max(limits.top.min!, aspectLimits.top.min),
        max: Math.min(limits.top.max, aspectLimits.top.max),
      };
      limits.bottom = {
        min: Math.max(limits.bottom.min!, aspectLimits.bottom.min),
        max: Math.min(limits.bottom.max, aspectLimits.bottom.max),
      };
    }
  }

  return limits;
};

const positionStyle = computed(() => ({
  top: `${top.value}px`,
  left: `${left.value}px`,
  zIndex: zIndex.value!,
}));

const sizeStyle = computed(() => ({
  width: w.value === 'auto' ? 'auto' : `${width.value}px`,
  height: h.value === 'auto' ? 'auto' : `${height.value}px`,
}));

const stickStyles = computed(() => (stick: string) => {
  const stickStyle = {
    width: `${stickSize.value / parentScaleX.value}px`,
    height: `${stickSize.value / parentScaleY.value}px`,
  };
  stickStyle[
    styleMapping.y[stick[0] as 'b' | 'm' | 't'] as 'height' | 'width'
  ] = `${stickSize.value / parentScaleX.value / -2}px`;
  stickStyle[
    styleMapping.x[stick[1] as 'l' | 'm' | 'r'] as 'height' | 'width'
  ] = `${stickSize.value / parentScaleX.value / -2}px`;
  return stickStyle;
});

const bodyMove = (delta: { x: number; y: number }) => {
  let newTop = dimensionsBeforeMove.value.top - delta.y;
  let newBottom = dimensionsBeforeMove.value.bottom + delta.y;
  let newLeft = dimensionsBeforeMove.value.left - delta.x;
  let newRight = dimensionsBeforeMove.value.right + delta.x;

  if (snapToGrid.value) {
    let alignTop = true;
    let alignLeft = true;

    let diffT = newTop - Math.floor(newTop / gridY.value) * gridY.value;
    let diffB =
      (parentHeight.value as number) -
      newBottom -
      Math.floor(((parentHeight.value as number) - newBottom) / gridY.value) *
        gridY.value;
    let diffL = newLeft - Math.floor(newLeft / gridX.value) * gridX.value;
    let diffR =
      (parentWidth.value as number) -
      newRight -
      Math.floor(((parentWidth.value as number) - newRight) / gridX.value) *
        gridX.value;

    if (diffT > gridY.value / 2) {
      diffT -= gridY.value;
    }
    if (diffB > gridY.value / 2) {
      diffB -= gridY.value;
    }
    if (diffL > gridX.value / 2) {
      diffL -= gridX.value;
    }
    if (diffR > gridX.value / 2) {
      diffR -= gridX.value;
    }

    if (Math.abs(diffB) < Math.abs(diffT)) {
      alignTop = false;
    }
    if (Math.abs(diffR) < Math.abs(diffL)) {
      alignLeft = false;
    }

    newTop -= alignTop ? diffT : diffB;
    newBottom = (parentHeight.value as number) - height.value - newTop;
    newLeft -= alignLeft ? diffL : diffR;
    newRight = (parentWidth.value as number) - width.value - newLeft;
  }

  ({
    newLeft: left.value,
    newRight: right.value,
    newTop: top.value,
    newBottom: bottom.value,
  } = rectCorrectionByLimit({ newLeft, newRight, newTop, newBottom }));

  emit('dragging', rect.value);
};

const bodyUp = () => {
  bodyDrag.value = false;
  emit('dragging', rect.value);
  emit('dragstop', rect.value);

  // dimensionsBeforeMove.value = { pointerX: 0, pointerY: 0, x: 0, y: 0, w: 0, h: 0 };
  Object.assign(dimensionsBeforeMove.value, {
    pointerX: 0,
    pointerY: 0,
    x: 0,
    y: 0,
    w: 0,
    h: 0,
  });

  limits.value = {
    left: { min: null, max: null },
    right: { min: null, max: null },
    top: { min: null, max: null },
    bottom: { min: null, max: null },
  };
};

const stickDown = (
  stick: string,
  ev: { pageX: any; pageY: any; touches?: any },
  force = false,
) => {
  if ((!isResizable.value || !active.value) && !force) {
    return;
  }

  stickDrag.value = true;

  const pointerX = ev.pageX === undefined ? ev.touches[0].pageX : ev.pageX;
  const pointerY = ev.pageY === undefined ? ev.touches[0].pageY : ev.pageY;

  saveDimensionsBeforeMove({ pointerX, pointerY });

  currentStick.value = stick;

  limits.value = calcResizeLimits();
};

const move = (ev: MouseEvent & TouchEvent) => {
  if (!stickDrag.value && !bodyDrag.value) {
    return;
  }

  ev.stopPropagation();

  // touches 兼容性代码
  const pageX = ev.pageX === undefined ? ev.touches![0]!.pageX : ev.pageX;
  const pageY = ev.pageY === undefined ? ev.touches![0]!.pageY : ev.pageY;

  const delta = {
    x: (dimensionsBeforeMove.value.pointerX - pageX) / parentScaleX.value,
    y: (dimensionsBeforeMove.value.pointerY - pageY) / parentScaleY.value,
  };

  if (stickDrag.value) {
    stickMove(delta);
  }

  if (bodyDrag.value) {
    switch (axis.value) {
      case 'none': {
        return;
      }
      case 'x': {
        delta.y = 0;

        break;
      }
      case 'y': {
        delta.x = 0;

        break;
      }
      // No default
    }
    bodyMove(delta);
  }
};

const up = () => {
  if (stickDrag.value) {
    stickUp();
  } else if (bodyDrag.value) {
    bodyUp();
  }
};

const deselect = () => {
  if (preventActiveBehavior.value) {
    return;
  }
  active.value = false;
};

const domEvents = ref(
  new Map([
    ['mousedown', deselect],
    ['mouseleave', up],
    ['mousemove', move],
    ['mouseup', up],
    ['touchcancel', up],
    ['touchend', up],
    ['touchmove', move],
    ['touchstart', up],
  ]),
);

const container = ref<HTMLDivElement>();

onMounted(() => {
  const currentInstance = getCurrentInstance();
  const $el = currentInstance?.vnode.el as HTMLElement;

  parentElement.value = $el?.parentNode as HTMLElement;
  parentWidth.value = parentW.value ?? parentElement.value?.clientWidth;
  parentHeight.value = parentH.value ?? parentElement.value?.clientHeight;

  left.value = x.value;
  top.value = y.value;
  right.value = (parentWidth.value -
    (w.value === 'auto' ? container.value!.scrollWidth : (w.value as number)) -
    left.value) as number;
  bottom.value = (parentHeight.value -
    (h.value === 'auto' ? container.value!.scrollHeight : (h.value as number)) -
    top.value) as number;

  addEvents(domEvents.value);

  if (dragHandle.value) {
    [...($el?.querySelectorAll(dragHandle.value) || [])].forEach(
      (dragHandle) => {
        (dragHandle as HTMLElement).dataset.dragHandle = String(
          currentInstance?.uid,
        );
      },
    );
  }

  if (dragCancel.value) {
    [...($el?.querySelectorAll(dragCancel.value) || [])].forEach(
      (cancelHandle) => {
        (cancelHandle as HTMLElement).dataset.dragCancel = String(
          currentInstance?.uid,
        );
      },
    );
  }
});

onBeforeUnmount(() => {
  removeEvents(domEvents.value);
});

const bodyDown = (ev: MouseEvent & TouchEvent) => {
  const { target, button } = ev;

  if (!preventActiveBehavior.value) {
    active.value = true;
  }

  if (button && button !== 0) {
    return;
  }

  emit('clicked', ev);

  if (!active.value) {
    return;
  }

  if (
    dragHandle.value &&
    (target! as HTMLElement).dataset.dragHandle !==
      getCurrentInstance()?.uid.toString()
  ) {
    return;
  }

  if (
    dragCancel.value &&
    (target! as HTMLElement).dataset.dragCancel ===
      getCurrentInstance()?.uid.toString()
  ) {
    return;
  }

  if (ev.stopPropagation !== undefined) {
    ev.stopPropagation();
  }

  if (ev.preventDefault !== undefined) {
    ev.preventDefault();
  }

  if (isDraggable.value) {
    bodyDrag.value = true;
  }

  const pointerX = ev.pageX === undefined ? ev.touches[0]!.pageX : ev.pageX;
  const pointerY = ev.pageY === undefined ? ev.touches[0]!.pageY : ev.pageY;

  saveDimensionsBeforeMove({ pointerX, pointerY });

  if (parentLimitation.value) {
    limits.value = calcDragLimitation();
  }
};

watch(
  () => active.value,
  (isActive) => {
    if (isActive) {
      emit('activated');
    } else {
      emit('deactivated');
    }
  },
);

watch(
  () => isActive.value,
  (val) => {
    active.value = val;
  },
  { immediate: true },
);

watch(
  () => z.value,
  (val) => {
    if ((val as number) >= 0 || val === 'auto') {
      zIndex.value = val as number;
    }
  },
  { immediate: true },
);

watch(
  () => x.value,
  (newVal, oldVal) => {
    if (stickDrag.value || bodyDrag.value || newVal === left.value) {
      return;
    }

    const delta = oldVal - newVal;

    bodyDown({ pageX: left.value!, pageY: top.value! } as MouseEvent &
      TouchEvent);
    bodyMove({ x: delta, y: 0 });

    nextTick(() => {
      bodyUp();
    });
  },
);

watch(
  () => y.value,
  (newVal, oldVal) => {
    if (stickDrag.value || bodyDrag.value || newVal === top.value) {
      return;
    }

    const delta = oldVal - newVal;

    bodyDown({ pageX: left.value, pageY: top.value } as MouseEvent &
      TouchEvent);
    bodyMove({ x: 0, y: delta });

    nextTick(() => {
      bodyUp();
    });
  },
);

watch(
  () => w.value,
  (newVal, oldVal) => {
    if (stickDrag.value || bodyDrag.value || newVal === width.value) {
      return;
    }

    const stick = 'mr';
    const delta = (oldVal as number) - (newVal as number);

    stickDown(
      stick,
      { pageX: right.value, pageY: top.value! + height.value / 2 },
      true,
    );
    stickMove({ x: delta, y: 0 });

    nextTick(() => {
      stickUp();
    });
  },
);

watch(
  () => h.value,
  (newVal, oldVal) => {
    if (stickDrag.value || bodyDrag.value || newVal === height.value) {
      return;
    }

    const stick = 'bm';
    const delta = (oldVal as number) - (newVal as number);

    stickDown(
      stick,
      { pageX: left.value! + width.value / 2, pageY: bottom.value },
      true,
    );
    stickMove({ x: 0, y: delta });

    nextTick(() => {
      stickUp();
    });
  },
);

watch(
  () => parentW.value,
  (val) => {
    right.value = val - width.value - left.value!;
    parentWidth.value = val;
  },
);

watch(
  () => parentH.value,
  (val) => {
    bottom.value = val - height.value - top.value!;
    parentHeight.value = val;
  },
);
</script>

<template>
  <div
    :class="`${active || isActive ? 'active' : 'inactive'} ${contentClass ? contentClass : ''}`"
    :style="positionStyle"
    class="resize"
    @mousedown="bodyDown($event as TouchEvent & MouseEvent)"
    @touchend="up"
    @touchstart="bodyDown($event as TouchEvent & MouseEvent)"
  >
    <div ref="container" :style="sizeStyle" class="content-container">
      <slot></slot>
    </div>
    <div
      v-for="(stick, index) of sticks"
      :key="index"
      :class="[`resize-stick-${stick}`, isResizable ? '' : 'not-resizable']"
      :style="stickStyles(stick)"
      class="resize-stick"
      @mousedown.stop.prevent="
        stickDown(stick, $event as TouchEvent & MouseEvent)
      "
      @touchstart.stop.prevent="
        stickDown(stick, $event as TouchEvent & MouseEvent)
      "
    ></div>
  </div>
</template>

<style lang="css" scoped>
.resize {
  position: absolute;
  box-sizing: border-box;
}

.resize.active::before {
  position: absolute;
  top: 0;
  left: 0;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  outline: 1px dashed #d6d6d6;
  content: '';
}

.resize-stick {
  position: absolute;
  box-sizing: border-box;
  font-size: 1px;
  background: #fff;
  border: 1px solid #6c6c6c;
  box-shadow: 0 0 2px #bbb;
}

.inactive .resize-stick {
  display: none;
}

.resize-stick-tl,
.resize-stick-br {
  cursor: nwse-resize;
}

.resize-stick-tm,
.resize-stick-bm {
  left: 50%;
  cursor: ns-resize;
}

.resize-stick-tr,
.resize-stick-bl {
  cursor: nesw-resize;
}

.resize-stick-ml,
.resize-stick-mr {
  top: 50%;
  cursor: ew-resize;
}

.resize-stick.not-resizable {
  display: none;
}

.content-container {
  position: relative;
  display: block;
}
</style>
