import { computed } from 'vue'
import { useAppStore } from '@/store/modules/app'
import { Menu } from '@/layout/components/Menu'
import { TabMenu } from '@/layout/components/TabMenu'
import { TagsView } from '@/layout/components/TagsView'
import { Logo } from '@/layout/components/Logo'
import AppView from './AppView.vue'
import ToolHeader from './ToolHeader.vue'
import { ElScrollbar } from 'element-plus'
import { useDesign } from '@/hooks/web/useDesign'

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('layout')

const appStore = useAppStore()

const pageLoading = computed(() => appStore.getPageLoading)

// 标签页
const tagsView = computed(() => appStore.getTagsView)

// 菜单折叠
const collapse = computed(() => appStore.getCollapse)

// logo
const logo = computed(() => appStore.logo)

// 固定头部
const fixedHeader = computed(() => appStore.getFixedHeader)

// 是否是移动端
const mobile = computed(() => appStore.getMobile)

// 固定菜单
const fixedMenu = computed(() => appStore.getFixedMenu)

export const useRenderLayout = () => {
  const renderClassic = () => {
    return (
      <>
        <div class={['absolute top-0 left-0 h-full', { '!fixed z-3000': mobile.value }]}>
          {logo.value ? (
            <Logo
              class={[
                'bg-[var(--left-menu-bg-color)] border-bottom-1 border-solid border-[var(--logo-border-color)] dark:border-[var(--el-border-color)]',
                {
                  '!pl-0': mobile.value && collapse.value,
                  'w-[var(--left-menu-min-width)]': appStore.getCollapse,
                  'w-[var(--left-menu-max-width)]': !appStore.getCollapse
                }
              ]}
              style="transition: all var(--transition-time-02);"
            ></Logo>
          ) : undefined}
          <Menu class={[{ '!h-[calc(100%-var(--logo-height))]': logo.value }]}></Menu>
        </div>
        <div
          class={[
            `${prefixCls}-content`,
            'absolute top-0 h-[100%]',
            {
              'w-[calc(100%-var(--left-menu-min-width))] left-[var(--left-menu-min-width)]':
                collapse.value && !mobile.value && !mobile.value,
              'w-[calc(100%-var(--left-menu-max-width))] left-[var(--left-menu-max-width)]':
                !collapse.value && !mobile.value && !mobile.value,
              'fixed !w-full !left-0': mobile.value
            }
          ]}
          style="transition: all var(--transition-time-02);"
        >
          <ElScrollbar
            v-loading={pageLoading.value}
            class={[
              `${prefixCls}-content-scrollbar`,
              {
                '!h-[calc(100%-var(--top-tool-height)-var(--tags-view-height))] mt-[calc(var(--top-tool-height)+var(--tags-view-height))]':
                  fixedHeader.value
              }
            ]}
          >
            <div
              class={[
                {
                  'fixed top-0 left-0 z-10': fixedHeader.value,
                  'w-[calc(100%-var(--left-menu-min-width))] left-[var(--left-menu-min-width)]':
                    collapse.value && fixedHeader.value && !mobile.value,
                  'w-[calc(100%-var(--left-menu-max-width))] left-[var(--left-menu-max-width)]':
                    !collapse.value && fixedHeader.value && !mobile.value,
                  '!w-full !left-0': mobile.value
                }
              ]}
              style="transition: all var(--transition-time-02);"
            >
              <ToolHeader class="border-bottom-1 border-solid border-[var(--top-tool-border-color)] bg-[var(--top-header-bg-color)] dark:border-[var(--el-border-color)]"></ToolHeader>

              {tagsView.value ? (
                <TagsView class="border-bottom-1 border-top-1 border-solid border-[var(--tags-view-border-color)] dark:border-[var(--el-border-color)]"></TagsView>
              ) : undefined}
            </div>

            <AppView></AppView>
          </ElScrollbar>
        </div>
      </>
    )
  }

  const renderTopLeft = () => {
    return (
      <>
        <div class="flex items-center bg-[var(--top-header-bg-color)] border-bottom-1 border-solid border-[var(--top-tool-border-color)] dark:border-[var(--el-border-color)]">
          {logo.value ? <Logo class="hover-trigger !pr-15px"></Logo> : undefined}

          <ToolHeader class="flex-1"></ToolHeader>
        </div>
        <div class="absolute top-[var(--logo-height)+1px] left-0 w-full h-[calc(100%-1px-var(--logo-height))] flex">
          <Menu class="!h-full"></Menu>
          <div
            class={[
              `${prefixCls}-content`,
              'h-[100%]',
              {
                'w-[calc(100%-var(--left-menu-min-width))] left-[var(--left-menu-min-width)]':
                  collapse.value,
                'w-[calc(100%-var(--left-menu-max-width))] left-[var(--left-menu-max-width)]':
                  !collapse.value
              }
            ]}
            style="transition: all var(--transition-time-02);"
          >
            <ElScrollbar
              v-loading={pageLoading.value}
              class={[
                `${prefixCls}-content-scrollbar`,
                {
                  '!h-[calc(100%-var(--tags-view-height))] mt-[calc(var(--tags-view-height))]':
                    fixedHeader.value && tagsView.value
                }
              ]}
            >
              {tagsView.value ? (
                <TagsView
                  class={[
                    'border-bottom-1 border-top-1 border-solid border-[var(--tags-view-border-color)] dark:border-[var(--el-border-color)]',
                    {
                      '!fixed top-0 left-0 z-10': fixedHeader.value,
                      'w-[calc(100%-var(--left-menu-min-width))] left-[var(--left-menu-min-width)] mt-[var(--logo-height)]':
                        collapse.value && fixedHeader.value,
                      'w-[calc(100%-var(--left-menu-max-width))] left-[var(--left-menu-max-width)] mt-[var(--logo-height)]':
                        !collapse.value && fixedHeader.value
                    }
                  ]}
                  style="transition: width var(--transition-time-02), left var(--transition-time-02);"
                ></TagsView>
              ) : undefined}

              <AppView></AppView>
            </ElScrollbar>
          </div>
        </div>
      </>
    )
  }

  const renderTop = () => {
    return (
      <>
        <div class="flex items-center justify-between bg-[var(--top-header-bg-color)] border-bottom-1 border-solid border-[var(--top-tool-border-color)] dark:border-[var(--el-border-color)]">
          {logo.value ? <Logo class="hover-trigger"></Logo> : undefined}
          <Menu class="flex-1 px-10px h-[var(--top-tool-height)]"></Menu>
          <ToolHeader></ToolHeader>
        </div>
        <div class={[`${prefixCls}-content`, 'h-full w-full']}>
          <ElScrollbar
            v-loading={pageLoading.value}
            class={[
              `${prefixCls}-content-scrollbar`,
              {
                'mt-[var(--tags-view-height)]': fixedHeader.value
              }
            ]}
          >
            {tagsView.value ? (
              <TagsView
                class={[
                  'border-bottom-1 border-top-1 border-solid border-[var(--tags-view-border-color)] dark:border-[var(--el-border-color)]',
                  {
                    '!fixed w-full top-[var(--top-tool-height)] left-0': fixedHeader.value
                  }
                ]}
                style="transition: width var(--transition-time-02), left var(--transition-time-02);"
              ></TagsView>
            ) : undefined}

            <AppView></AppView>
          </ElScrollbar>
        </div>
      </>
    )
  }

  const renderCutMenu = () => {
    return (
      <>
        <div class="flex items-center bg-[var(--top-header-bg-color)] border-bottom-1 border-solid border-[var(--top-tool-border-color)] dark:border-[var(--el-border-color)]">
          {logo.value ? <Logo class="hover-trigger !pr-15px"></Logo> : undefined}

          <ToolHeader class="flex-1"></ToolHeader>
        </div>
        <div class="absolute top-[var(--logo-height)] left-0 w-full h-[calc(100%-var(--logo-height))] flex">
          <TabMenu></TabMenu>
          <div
            class={[
              `${prefixCls}-content`,
              'h-[100%]',
              {
                'w-[calc(100%-var(--tab-menu-min-width))] left-[var(--tab-menu-min-width)]':
                  collapse.value && !fixedMenu.value,
                'w-[calc(100%-var(--tab-menu-max-width))] left-[var(--tab-menu-max-width)]':
                  !collapse.value && !fixedMenu.value,
                'w-[calc(100%-var(--tab-menu-min-width)-var(--left-menu-max-width))] ml-[var(--left-menu-max-width)]':
                  collapse.value && fixedMenu.value,
                'w-[calc(100%-var(--tab-menu-max-width)-var(--left-menu-max-width))] ml-[var(--left-menu-max-width)]':
                  !collapse.value && fixedMenu.value
              }
            ]}
            style="transition: all var(--transition-time-02);"
          >
            <ElScrollbar
              v-loading={pageLoading.value}
              class={[
                `${prefixCls}-content-scrollbar`,
                {
                  '!h-[calc(100%-var(--tags-view-height))] mt-[calc(var(--tags-view-height))]':
                    fixedHeader.value && tagsView.value
                }
              ]}
            >
              {tagsView.value ? (
                <TagsView
                  class={[
                    'border-bottom-1 border-top-1 border-solid border-[var(--tags-view-border-color)] dark:border-[var(--el-border-color)]',
                    {
                      '!fixed top-0 left-0 z-10': fixedHeader.value,
                      'w-[calc(100%-var(--tab-menu-min-width))] left-[var(--tab-menu-min-width)] mt-[var(--logo-height)]':
                        collapse.value && fixedHeader.value,
                      'w-[calc(100%-var(--tab-menu-max-width))] left-[var(--tab-menu-max-width)] mt-[var(--logo-height)]':
                        !collapse.value && fixedHeader.value,
                      '!fixed top-0 left-[var(--tab-menu-min-width)+var(--left-menu-max-width)] z-10':
                        fixedHeader.value && fixedMenu.value,
                      'w-[calc(100%-var(--tab-menu-min-width)-var(--left-menu-max-width))] left-[var(--tab-menu-min-width)+var(--left-menu-max-width)] mt-[var(--logo-height)]':
                        collapse.value && fixedHeader.value && fixedMenu.value,
                      'w-[calc(100%-var(--tab-menu-max-width)-var(--left-menu-max-width))] left-[var(--tab-menu-max-width)+var(--left-menu-max-width)] mt-[var(--logo-height)]':
                        !collapse.value && fixedHeader.value && fixedMenu.value
                    }
                  ]}
                  style="transition: width var(--transition-time-02), left var(--transition-time-02);"
                ></TagsView>
              ) : undefined}

              <AppView></AppView>
            </ElScrollbar>
          </div>
        </div>
      </>
    )
  }

  return {
    renderClassic,
    renderTopLeft,
    renderTop,
    renderCutMenu
  }
}
