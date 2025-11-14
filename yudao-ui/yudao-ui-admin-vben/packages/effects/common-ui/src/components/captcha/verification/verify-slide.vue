<script lang="ts" setup>
import type { VerificationProps } from './typing';

/**
 * VerifySlide
 * @description 滑块
 */
import {
  computed,
  getCurrentInstance,
  nextTick,
  onMounted,
  reactive,
  ref,
  toRefs,
} from 'vue';

import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';

import { AES } from '@vben-core/shared/utils';

import { resetSize } from './utils/util';

const props = withDefaults(defineProps<VerificationProps>(), {
  barSize: () => ({
    height: '40px',
    width: '310px',
  }),
  blockSize: () => ({
    height: '50px',
    width: '50px',
  }),
  captchaType: 'blockPuzzle',
  explain: '',
  imgSize: () => ({
    height: '155px',
    width: '310px',
  }),
  mode: 'fixed',
  type: '1',
  space: 5,
});

const emit = defineEmits(['onSuccess', 'onError', 'onClose']);

const {
  blockSize,
  captchaType,
  explain,
  mode,
  checkCaptchaApi,
  getCaptchaApi,
} = toRefs(props);

const { proxy } = getCurrentInstance()!;
const secretKey = ref(); // 后端返回的ase加密秘钥
const passFlag = ref(); // 是否通过的标识
const backImgBase = ref(); // 验证码背景图片
const blockBackImgBase = ref(); // 验证滑块的背景图片
const backToken = ref(); // 后端返回的唯一token值
const startMoveTime = ref(); // 移动开始的时间
const endMovetime = ref(); // 移动结束的时间
const tipWords = ref();
const text = ref();
const finishText = ref();
const setSize = reactive({
  barHeight: '0px',
  barWidth: '0px',
  imgHeight: '0px',
  imgWidth: '0px',
});
const moveBlockLeft = ref();
const leftBarWidth = ref();
// 移动中样式
const moveBlockBackgroundColor = ref();
const leftBarBorderColor = ref('#ddd');
const iconColor = ref();
const iconClass = ref('icon-right');
const status = ref(false); // 鼠标状态
const isEnd = ref(false); // 是够验证完成
const showRefresh = ref(true);
const transitionLeft = ref();
const transitionWidth = ref();
const startLeft = ref(0);

const barArea = computed(() => {
  return proxy?.$el.querySelector('.verify-bar-area');
});
function init() {
  text.value =
    explain.value === '' ? $t('ui.captcha.sliderDefaultText') : explain.value;

  getPictrue();
  nextTick(() => {
    const { barHeight, barWidth, imgHeight, imgWidth } = resetSize(proxy);
    setSize.imgHeight = imgHeight;
    setSize.imgWidth = imgWidth;
    setSize.barHeight = barHeight;
    setSize.barWidth = barWidth;
    proxy?.$parent?.$emit('ready', proxy);
  });

  window.removeEventListener('touchmove', move);
  window.removeEventListener('mousemove', move);

  // 鼠标松开
  window.removeEventListener('touchend', end);
  window.removeEventListener('mouseup', end);

  window.addEventListener('touchmove', move);
  window.addEventListener('mousemove', move);

  // 鼠标松开
  window.addEventListener('touchend', end);
  window.addEventListener('mouseup', end);
}

onMounted(() => {
  // 禁止拖拽
  init();
  proxy?.$el.addEventListener('selectstart', () => {
    return false;
  });
});

// 鼠标按下
function start(e: MouseEvent | TouchEvent) {
  const x =
    ((e as TouchEvent).touches
      ? (e as TouchEvent).touches[0]?.pageX
      : (e as MouseEvent).clientX) || 0;
  startLeft.value = Math.floor(x - barArea.value.getBoundingClientRect().left);
  startMoveTime.value = Date.now(); // 开始滑动的时间
  if (isEnd.value === false) {
    text.value = '';
    moveBlockBackgroundColor.value = '#337ab7';
    leftBarBorderColor.value = '#337AB7';
    iconColor.value = '#fff';
    e.stopPropagation();
    status.value = true;
  }
}
// 鼠标移动
function move(e: MouseEvent | TouchEvent) {
  if (status.value && isEnd.value === false) {
    const x =
      ((e as TouchEvent).touches
        ? (e as TouchEvent).touches[0]?.pageX
        : (e as MouseEvent).clientX) || 0;
    const bar_area_left = barArea.value.getBoundingClientRect().left;
    let move_block_left = x - bar_area_left; // 小方块相对于父元素的left值
    if (
      move_block_left >=
      barArea.value.offsetWidth - Number.parseInt(blockSize.value.width) / 2 - 2
    )
      move_block_left =
        barArea.value.offsetWidth -
        Number.parseInt(blockSize.value.width) / 2 -
        2;

    if (move_block_left <= 0)
      move_block_left = Number.parseInt(blockSize.value.width) / 2;

    // 拖动后小方块的left值
    moveBlockLeft.value = `${move_block_left - startLeft.value}px`;
    leftBarWidth.value = `${move_block_left - startLeft.value}px`;
  }
}

// 鼠标松开
function end() {
  endMovetime.value = Date.now();
  // 判断是否重合
  if (status.value && isEnd.value === false) {
    let moveLeftDistance = Number.parseInt(
      (moveBlockLeft.value || '').replace('px', ''),
    );
    moveLeftDistance =
      (moveLeftDistance * 310) / Number.parseInt(setSize.imgWidth);
    const data = {
      captchaType: captchaType.value,
      pointJson: secretKey.value
        ? AES.encrypt(
            JSON.stringify({ x: moveLeftDistance, y: 5 }),
            secretKey.value,
          )
        : JSON.stringify({ x: moveLeftDistance, y: 5 }),
      token: backToken.value,
    };
    checkCaptchaApi?.value?.(data).then((response) => {
      const res = response.data;
      if (res.repCode === '0000') {
        moveBlockBackgroundColor.value = '#5cb85c';
        leftBarBorderColor.value = '#5cb85c';
        iconColor.value = '#fff';
        iconClass.value = 'icon-check';
        showRefresh.value = false;
        isEnd.value = true;
        if (mode.value === 'pop') {
          setTimeout(() => {
            emit('onClose');
            refresh();
          }, 1500);
        }
        passFlag.value = true;
        tipWords.value = `${((endMovetime.value - startMoveTime.value) / 1000).toFixed(2)}s
            ${$t('ui.captcha.title')}`;
        const captchaVerification = secretKey.value
          ? AES.encrypt(
              `${backToken.value}---${JSON.stringify({ x: moveLeftDistance, y: 5 })}`,
              secretKey.value,
            )
          : `${backToken.value}---${JSON.stringify({ x: moveLeftDistance, y: 5 })}`;
        setTimeout(() => {
          tipWords.value = '';
          emit('onSuccess', { captchaVerification });
          emit('onClose');
        }, 1000);
      } else {
        moveBlockBackgroundColor.value = '#d9534f';
        leftBarBorderColor.value = '#d9534f';
        iconColor.value = '#fff';
        iconClass.value = 'icon-close';
        passFlag.value = false;
        setTimeout(() => {
          refresh();
        }, 1000);
        emit('onError', proxy);
        tipWords.value = $t('ui.captcha.sliderRotateFailTip');
        setTimeout(() => {
          tipWords.value = '';
        }, 1000);
      }
    });
    status.value = false;
  }
}

async function refresh() {
  showRefresh.value = true;
  finishText.value = '';

  transitionLeft.value = 'left .3s';
  moveBlockLeft.value = 0;

  leftBarWidth.value = undefined;
  transitionWidth.value = 'width .3s';

  leftBarBorderColor.value = '#ddd';
  moveBlockBackgroundColor.value = '#fff';
  iconColor.value = '#000';
  iconClass.value = 'icon-right';
  isEnd.value = false;

  await getPictrue();
  setTimeout(() => {
    transitionWidth.value = '';
    transitionLeft.value = '';
    text.value = explain.value;
  }, 300);
}

// 请求背景图片和验证图片
async function getPictrue() {
  const data = {
    captchaType: captchaType.value,
  };
  const res = await getCaptchaApi?.value?.(data);

  if (res?.data?.repCode === '0000') {
    backImgBase.value = `data:image/png;base64,${res?.data?.repData?.originalImageBase64}`;
    blockBackImgBase.value = `data:image/png;base64,${res?.data?.repData?.jigsawImageBase64}`;
    backToken.value = res.data.repData.token;
    secretKey.value = res.data.repData.secretKey;
  } else {
    tipWords.value = res?.data?.repMsg;
  }
}
defineExpose({
  init,
  refresh,
});
</script>

<template>
  <div style="position: relative">
    <div
      v-if="type === '2'"
      :style="{ height: `${Number.parseInt(setSize.imgHeight) + space}px` }"
      class="verify-img-out"
    >
      <div
        :style="{ width: setSize.imgWidth, height: setSize.imgHeight }"
        class="verify-img-panel"
      >
        <img
          :src="backImgBase"
          alt=""
          style="display: block; width: 100%; height: 100%"
        />
        <div v-show="showRefresh" class="verify-refresh" @click="refresh">
          <IconifyIcon icon="lucide:refresh-ccw" class="mr-2 size-5" />
        </div>
        <transition name="tips">
          <span
            v-if="tipWords"
            :class="passFlag ? 'suc-bg' : 'err-bg'"
            class="verify-tips"
          >
            {{ tipWords }}
          </span>
        </transition>
      </div>
    </div>
    <!-- 公共部分 -->
    <div
      :style="{
        width: setSize.imgWidth,
        height: barSize.height,
        'line-height': barSize.height,
      }"
      class="verify-bar-area"
    >
      <span class="verify-msg" v-text="text"></span>
      <div
        :style="{
          width: leftBarWidth !== undefined ? leftBarWidth : barSize.height,
          height: barSize.height,
          'border-color': leftBarBorderColor,
          transition: transitionWidth,
        }"
        class="verify-left-bar"
      >
        <span class="verify-msg" v-text="finishText"></span>
        <div
          :style="{
            width: barSize.height,
            height: barSize.height,
            'background-color': moveBlockBackgroundColor,
            left: moveBlockLeft,
            transition: transitionLeft,
          }"
          class="verify-move-block"
          @mousedown="start"
          @touchstart="start"
        >
          <i
            :class="[iconClass]"
            :style="{ color: iconColor }"
            class="iconfont verify-icon"
          ></i>
          <div
            v-if="type === '2'"
            :style="{
              width: `${Math.floor((Number.parseInt(setSize.imgWidth) * 47) / 310)}px`,
              height: setSize.imgHeight,
              top: `-${Number.parseInt(setSize.imgHeight) + space}px`,
              'background-size': `${setSize.imgWidth} ${setSize.imgHeight}`,
            }"
            class="verify-sub-block"
          >
            <img
              :src="blockBackImgBase"
              alt=""
              style="
                display: block;
                width: 100%;
                height: 100%;
                -webkit-user-drag: none;
              "
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
