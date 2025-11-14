/**
 * 移除并销毁loading
 * 放在这里是而不是放在 index.html 的app标签内，是因为这样比较不会生硬，渲染过快可能会有闪烁
 * 通过先添加css动画隐藏，在动画结束后在移除loading节点来改善体验
 * 不好的地方是会增加一些代码量
 * 自定义loading可以见：https://doc.vben.pro/guide/in-depth/loading.html
 */
export function unmountGlobalLoading() {
  // 查找全局 loading 元素
  const loadingElement = document.querySelector('#__app-loading__');

  if (loadingElement) {
    // 添加隐藏类，触发过渡动画
    loadingElement.classList.add('hidden');

    // 查找所有需要移除的注入 loading 元素
    const injectLoadingElements = document.querySelectorAll(
      '[data-app-loading^="inject"]',
    );

    // 当过渡动画结束时，移除 loading 元素和所有注入的 loading 元素
    loadingElement.addEventListener(
      'transitionend',
      () => {
        loadingElement.remove(); // 移除 loading 元素
        injectLoadingElements.forEach((el) => el.remove()); // 移除所有注入的 loading 元素
      },
      { once: true },
    ); // 确保事件只触发一次
  }
}
