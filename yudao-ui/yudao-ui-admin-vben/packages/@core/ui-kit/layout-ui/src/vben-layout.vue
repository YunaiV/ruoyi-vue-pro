<script setup lang="ts">
import type { CSSProperties } from 'vue';

import type { VbenLayoutProps } from './vben-layout';

import { computed, ref, watch } from 'vue';

import {
  SCROLL_FIXED_CLASS,
  useLayoutFooterStyle,
  useLayoutHeaderStyle,
} from '@vben-core/composables';
import { IconifyIcon } from '@vben-core/icons';
import { VbenIconButton } from '@vben-core/shadcn-ui';
import { ELEMENT_ID_MAIN_CONTENT } from '@vben-core/shared/constants';

import { useMouse, useScroll, useThrottleFn } from '@vueuse/core';

import {
  LayoutContent,
  LayoutFooter,
  LayoutHeader,
  LayoutSidebar,
  LayoutTabbar,
} from './components';
import { useLayout } from './hooks/use-layout';

interface Props extends VbenLayoutProps {}

defineOptions({
  name: 'VbenLayout',
});

const props = withDefaults(defineProps<Props>(), {
  contentCompact: 'wide',
  contentCompactWidth: 1200,
  contentPadding: 0,
  contentPaddingBottom: 0,
  contentPaddingLeft: 0,
  contentPaddingRight: 0,
  contentPaddingTop: 0,
  footerEnable: false,
  footerFixed: true,
  footerHeight: 32,
  headerHeight: 50,
  headerHidden: false,
  headerMode: 'fixed',
  headerToggleSidebarButton: true,
  headerVisible: true,
  isMobile: false,
  layout: 'sidebar-nav',
  sidebarCollapsedButton: true,
  sidebarCollapseShowTitle: false,
  sidebarExtraCollapsedWidth: 60,
  sidebarFixedButton: true,
  sidebarHidden: false,
  sidebarMixedWidth: 80,
  sidebarTheme: 'dark',
  sidebarWidth: 180,
  sideCollapseWidth: 60,
  tabbarEnable: true,
  tabbarHeight: 40,
  zIndex: 200,
});

const emit = defineEmits<{ sideMouseLeave: []; toggleSidebar: [] }>();
const sidebarCollapse = defineModel<boolean>('sidebarCollapse', {
  default: false,
});
const sidebarExtraVisible = defineModel<boolean>('sidebarExtraVisible');
const sidebarExtraCollapse = defineModel<boolean>('sidebarExtraCollapse', {
  default: false,
});
const sidebarExpandOnHover = defineModel<boolean>('sidebarExpandOnHover', {
  default: false,
});
const sidebarEnable = defineModel<boolean>('sidebarEnable', { default: true });

// side是否处于hover状态展开菜单中
const sidebarExpandOnHovering = ref(false);
const headerIsHidden = ref(false);
const contentRef = ref();

const {
  arrivedState,
  directions,
  isScrolling,
  y: scrollY,
} = useScroll(document);

const { setLayoutHeaderHeight } = useLayoutHeaderStyle();
const { setLayoutFooterHeight } = useLayoutFooterStyle();

const { y: mouseY } = useMouse({ target: contentRef, type: 'client' });

const {
  currentLayout,
  isFullContent,
  isHeaderMixedNav,
  isHeaderNav,
  isMixedNav,
  isSidebarMixedNav,
} = useLayout(props);

/**
 * 顶栏是否自动隐藏
 */
const isHeaderAutoMode = computed(() => props.headerMode === 'auto');

const headerWrapperHeight = computed(() => {
  let height = 0;
  if (props.headerVisible && !props.headerHidden) {
    height += props.headerHeight;
  }
  if (props.tabbarEnable) {
    height += props.tabbarHeight;
  }
  return height;
});

const getSideCollapseWidth = computed(() => {
  const { sidebarCollapseShowTitle, sidebarMixedWidth, sideCollapseWidth } =
    props;

  return sidebarCollapseShowTitle ||
    isSidebarMixedNav.value ||
    isHeaderMixedNav.value
    ? sidebarMixedWidth
    : sideCollapseWidth;
});

/**
 * 动态获取侧边区域是否可见
 */
const sidebarEnableState = computed(() => {
  return !isHeaderNav.value && sidebarEnable.value;
});

/**
 * 侧边区域离顶部高度
 */
const sidebarMarginTop = computed(() => {
  const { headerHeight, isMobile } = props;
  return isMixedNav.value && !isMobile ? headerHeight : 0;
});

/**
 * 动态获取侧边宽度
 */
const getSidebarWidth = computed(() => {
  const { isMobile, sidebarHidden, sidebarMixedWidth, sidebarWidth } = props;
  let width = 0;

  if (sidebarHidden) {
    return width;
  }

  if (
    !sidebarEnableState.value ||
    (sidebarHidden &&
      !isSidebarMixedNav.value &&
      !isMixedNav.value &&
      !isHeaderMixedNav.value)
  ) {
    return width;
  }

  if ((isHeaderMixedNav.value || isSidebarMixedNav.value) && !isMobile) {
    width = sidebarMixedWidth;
  } else if (sidebarCollapse.value) {
    width = isMobile ? 0 : getSideCollapseWidth.value;
  } else {
    width = sidebarWidth;
  }
  return width;
});

/**
 * 获取扩展区域宽度
 */
const sidebarExtraWidth = computed(() => {
  const { sidebarExtraCollapsedWidth, sidebarWidth } = props;

  return sidebarExtraCollapse.value ? sidebarExtraCollapsedWidth : sidebarWidth;
});

/**
 * 是否侧边栏模式，包含混合侧边
 */
const isSideMode = computed(
  () =>
    currentLayout.value === 'mixed-nav' ||
    currentLayout.value === 'sidebar-mixed-nav' ||
    currentLayout.value === 'sidebar-nav' ||
    currentLayout.value === 'header-mixed-nav' ||
    currentLayout.value === 'header-sidebar-nav',
);

/**
 * header fixed值
 */
const headerFixed = computed(() => {
  const { headerMode } = props;
  return (
    isMixedNav.value ||
    headerMode === 'fixed' ||
    headerMode === 'auto-scroll' ||
    headerMode === 'auto'
  );
});

const showSidebar = computed(() => {
  return isSideMode.value && sidebarEnable.value && !props.sidebarHidden;
});

/**
 * 遮罩可见性
 */
const maskVisible = computed(() => !sidebarCollapse.value && props.isMobile);

const mainStyle = computed(() => {
  let width = '100%';
  let sidebarAndExtraWidth = 'unset';
  if (
    headerFixed.value &&
    currentLayout.value !== 'header-nav' &&
    currentLayout.value !== 'mixed-nav' &&
    currentLayout.value !== 'header-sidebar-nav' &&
    showSidebar.value &&
    !props.isMobile
  ) {
    // fixed模式下生效
    const isSideNavEffective =
      (isSidebarMixedNav.value || isHeaderMixedNav.value) &&
      sidebarExpandOnHover.value &&
      sidebarExtraVisible.value;

    if (isSideNavEffective) {
      const sideCollapseWidth = sidebarCollapse.value
        ? getSideCollapseWidth.value
        : props.sidebarMixedWidth;
      const sideWidth = sidebarExtraCollapse.value
        ? props.sidebarExtraCollapsedWidth
        : props.sidebarWidth;

      // 100% - 侧边菜单混合宽度 - 菜单宽度
      sidebarAndExtraWidth = `${sideCollapseWidth + sideWidth}px`;
      width = `calc(100% - ${sidebarAndExtraWidth})`;
    } else {
      sidebarAndExtraWidth =
        sidebarExpandOnHovering.value && !sidebarExpandOnHover.value
          ? `${getSideCollapseWidth.value}px`
          : `${getSidebarWidth.value}px`;
      width = `calc(100% - ${sidebarAndExtraWidth})`;
    }
  }
  return {
    sidebarAndExtraWidth,
    width,
  };
});

// 计算 tabbar 的样式
const tabbarStyle = computed((): CSSProperties => {
  let width = '';
  let marginLeft = 0;

  // 如果不是混合导航，tabbar 的宽度为 100%
  if (!isMixedNav.value || props.sidebarHidden) {
    width = '100%';
  } else if (sidebarEnable.value) {
    // 鼠标在侧边栏上时，且侧边栏展开时的宽度
    const onHoveringWidth = sidebarExpandOnHover.value
      ? props.sidebarWidth
      : getSideCollapseWidth.value;

    // 设置 marginLeft，根据侧边栏是否折叠来决定
    marginLeft = sidebarCollapse.value
      ? getSideCollapseWidth.value
      : onHoveringWidth;

    // 设置 tabbar 的宽度，计算方式为 100% 减去侧边栏的宽度
    width = `calc(100% - ${sidebarCollapse.value ? getSidebarWidth.value : onHoveringWidth}px)`;
  } else {
    // 默认情况下，tabbar 的宽度为 100%
    width = '100%';
  }

  return {
    marginLeft: `${marginLeft}px`,
    width,
  };
});

const contentStyle = computed((): CSSProperties => {
  const fixed = headerFixed.value;

  const { footerEnable, footerFixed, footerHeight } = props;
  return {
    marginTop:
      fixed &&
      !isFullContent.value &&
      !headerIsHidden.value &&
      (!isHeaderAutoMode.value || scrollY.value < headerWrapperHeight.value)
        ? `${headerWrapperHeight.value}px`
        : 0,
    paddingBottom: `${footerEnable && footerFixed ? footerHeight : 0}px`,
  };
});

const headerZIndex = computed(() => {
  const { zIndex } = props;
  const offset = isMixedNav.value ? 1 : 0;
  return zIndex + offset;
});

const headerWrapperStyle = computed((): CSSProperties => {
  const fixed = headerFixed.value;
  return {
    height: isFullContent.value ? '0' : `${headerWrapperHeight.value}px`,
    left: isMixedNav.value ? 0 : mainStyle.value.sidebarAndExtraWidth,
    position: fixed ? 'fixed' : 'static',
    top:
      headerIsHidden.value || isFullContent.value
        ? `-${headerWrapperHeight.value}px`
        : 0,
    width: mainStyle.value.width,
    'z-index': headerZIndex.value,
  };
});

/**
 * 侧边栏z-index
 */
const sidebarZIndex = computed(() => {
  const { isMobile, zIndex } = props;
  let offset = isMobile || isSideMode.value ? 1 : -1;

  if (isMixedNav.value) {
    offset += 1;
  }

  return zIndex + offset;
});

const footerWidth = computed(() => {
  if (!props.footerFixed) {
    return '100%';
  }

  return mainStyle.value.width;
});

const maskStyle = computed((): CSSProperties => {
  return { zIndex: props.zIndex };
});

const showHeaderToggleButton = computed(() => {
  return (
    props.isMobile ||
    (props.headerToggleSidebarButton &&
      isSideMode.value &&
      !isSidebarMixedNav.value &&
      !isMixedNav.value &&
      !props.isMobile)
  );
});

const showHeaderLogo = computed(() => {
  return !isSideMode.value || isMixedNav.value || props.isMobile;
});

watch(
  () => props.isMobile,
  (val) => {
    if (val) {
      sidebarCollapse.value = true;
    }
  },
  {
    immediate: true,
  },
);

watch(
  [() => headerWrapperHeight.value, () => isFullContent.value],
  ([height]) => {
    setLayoutHeaderHeight(isFullContent.value ? 0 : height);
  },
  {
    immediate: true,
  },
);

watch(
  () => props.footerHeight,
  (height: number) => {
    setLayoutFooterHeight(height);
  },
  {
    immediate: true,
  },
);

{
  const mouseMove = () => {
    mouseY.value > headerWrapperHeight.value
      ? (headerIsHidden.value = true)
      : (headerIsHidden.value = false);
  };
  watch(
    [() => props.headerMode, () => mouseY.value],
    () => {
      if (!isHeaderAutoMode.value || isMixedNav.value || isFullContent.value) {
        if (props.headerMode !== 'auto-scroll') {
          headerIsHidden.value = false;
        }
        return;
      }
      headerIsHidden.value = true;
      mouseMove();
    },
    {
      immediate: true,
    },
  );
}

{
  const checkHeaderIsHidden = useThrottleFn((top, bottom, topArrived) => {
    if (scrollY.value < headerWrapperHeight.value) {
      headerIsHidden.value = false;
      return;
    }
    if (topArrived) {
      headerIsHidden.value = false;
      return;
    }

    if (top) {
      headerIsHidden.value = false;
    } else if (bottom) {
      headerIsHidden.value = true;
    }
  }, 300);

  watch(
    () => scrollY.value,
    () => {
      if (
        props.headerMode !== 'auto-scroll' ||
        isMixedNav.value ||
        isFullContent.value
      ) {
        return;
      }
      if (isScrolling.value) {
        checkHeaderIsHidden(
          directions.top,
          directions.bottom,
          arrivedState.top,
        );
      }
    },
  );
}

function handleClickMask() {
  sidebarCollapse.value = true;
}

function handleHeaderToggle() {
  if (props.isMobile) {
    sidebarCollapse.value = false;
  } else {
    emit('toggleSidebar');
  }
}

const idMainContent = ELEMENT_ID_MAIN_CONTENT;
</script>

<template>
  <div class="relative flex min-h-full w-full">
    <LayoutSidebar
      v-if="sidebarEnableState"
      v-model:collapse="sidebarCollapse"
      v-model:expand-on-hover="sidebarExpandOnHover"
      v-model:expand-on-hovering="sidebarExpandOnHovering"
      v-model:extra-collapse="sidebarExtraCollapse"
      v-model:extra-visible="sidebarExtraVisible"
      :show-collapse-button="sidebarCollapsedButton"
      :show-fixed-button="sidebarFixedButton"
      :collapse-width="getSideCollapseWidth"
      :dom-visible="!isMobile"
      :extra-width="sidebarExtraWidth"
      :fixed-extra="sidebarExpandOnHover"
      :header-height="isMixedNav ? 0 : headerHeight"
      :is-sidebar-mixed="isSidebarMixedNav || isHeaderMixedNav"
      :margin-top="sidebarMarginTop"
      :mixed-width="sidebarMixedWidth"
      :show="showSidebar"
      :theme="sidebarTheme"
      :width="getSidebarWidth"
      :z-index="sidebarZIndex"
      @leave="() => emit('sideMouseLeave')"
    >
      <template v-if="isSideMode && !isMixedNav" #logo>
        <slot name="logo"></slot>
      </template>

      <template v-if="isSidebarMixedNav || isHeaderMixedNav">
        <slot name="mixed-menu"></slot>
      </template>
      <template v-else>
        <slot name="menu"></slot>
      </template>

      <template #extra>
        <slot name="side-extra"></slot>
      </template>
      <template #extra-title>
        <slot name="side-extra-title"></slot>
      </template>
    </LayoutSidebar>

    <div
      ref="contentRef"
      class="flex flex-1 flex-col overflow-hidden transition-all duration-300 ease-in"
    >
      <div
        :class="[
          {
            'shadow-[0_16px_24px_hsl(var(--background))]': scrollY > 20,
          },
          SCROLL_FIXED_CLASS,
        ]"
        :style="headerWrapperStyle"
        class="overflow-hidden transition-all duration-200"
      >
        <LayoutHeader
          v-if="headerVisible"
          :full-width="!isSideMode"
          :height="headerHeight"
          :is-mobile="isMobile"
          :show="!isFullContent && !headerHidden"
          :sidebar-width="sidebarWidth"
          :theme="headerTheme"
          :width="mainStyle.width"
          :z-index="headerZIndex"
        >
          <template v-if="showHeaderLogo" #logo>
            <slot name="logo"></slot>
          </template>

          <template #toggle-button>
            <VbenIconButton
              v-if="showHeaderToggleButton"
              class="my-0 mr-1 rounded-md"
              @click="handleHeaderToggle"
            >
              <IconifyIcon v-if="showSidebar" icon="ep:fold" />
              <IconifyIcon v-else icon="ep:expand" />
            </VbenIconButton>
          </template>
          <slot name="header"></slot>
        </LayoutHeader>

        <LayoutTabbar
          v-if="tabbarEnable"
          :height="tabbarHeight"
          :style="tabbarStyle"
        >
          <slot name="tabbar"></slot>
        </LayoutTabbar>
      </div>

      <!-- </div> -->
      <LayoutContent
        :id="idMainContent"
        :content-compact="contentCompact"
        :content-compact-width="contentCompactWidth"
        :padding="contentPadding"
        :padding-bottom="contentPaddingBottom"
        :padding-left="contentPaddingLeft"
        :padding-right="contentPaddingRight"
        :padding-top="contentPaddingTop"
        :style="contentStyle"
        class="transition-[margin-top] duration-200"
      >
        <slot name="content"></slot>

        <template #overlay>
          <slot name="content-overlay"></slot>
        </template>
      </LayoutContent>

      <LayoutFooter
        v-if="footerEnable"
        :fixed="footerFixed"
        :height="footerHeight"
        :show="!isFullContent"
        :width="footerWidth"
        :z-index="zIndex"
      >
        <slot name="footer"></slot>
      </LayoutFooter>
    </div>
    <slot name="extra"></slot>
    <div
      v-if="maskVisible"
      :style="maskStyle"
      class="fixed left-0 top-0 h-full w-full bg-overlay transition-[background-color] duration-200"
      @click="handleClickMask"
    ></div>
  </div>
</template>
