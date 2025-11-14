import { updatePreferences, usePreferences } from '@vben/preferences';
/**
 * 主体区域最大化
 */
export function useContentMaximize() {
  const { contentIsMaximize } = usePreferences();

  function toggleMaximize() {
    const isMaximize = contentIsMaximize.value;

    updatePreferences({
      header: {
        hidden: !isMaximize,
      },
      sidebar: {
        hidden: !isMaximize,
      },
    });
  }

  /**
   * 切换最大化和隐藏 tabbar
   */
  function toggleMaximizeAndTabbarHidden() {
    const isMaximize = contentIsMaximize.value;
    updatePreferences({
      header: {
        hidden: !isMaximize,
      },
      sidebar: {
        hidden: !isMaximize,
      },
      tabbar: {
        enable: isMaximize,
      },
    });
  }

  return {
    contentIsMaximize,
    toggleMaximize,
    toggleMaximizeAndTabbarHidden,
  };
}
