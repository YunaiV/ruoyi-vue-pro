# 全局loading

全局 loading 指的是页面刷新时出现的加载效果，通常是一个旋转的图标：

![Global loading spinner](/guide/loading.png)

## 原理

由 `vite-plugin-inject-app-loading` 插件实现，插件会在每个应用都注入一个全局的 `loading html`。

## 关闭

如果你不需要全局 loading，可以在 `.env` 文件中关闭：

```bash
VITE_INJECT_APP_LOADING=false
```

## 自定义

如果你想要自定义全局 loading，可以在应用目录下，与`index.html`同级，创建一个`loading.html`文件，插件会自动读取并注入。这个html可以自行定义样式和动画。

::: tip

- 你可以使用跟`index.html`一样的语法，比如`VITE_APP_TITLE`变量，来获取应用的标题。
- 必须保证有一个`id="__app-loading__"`的元素。
- 给`id="__app-loading__"`的元素，加一个 `hidden` class。
- 必须保证有一个`style[data-app-loading="inject-css"]`的元素。

```html{1,4}
<style data-app-loading="inject-css">
  #__app-loading__.hidden {
    pointer-events: none;
    visibility: hidden;
    opacity: 0;
    transition: all 1s ease-out;
  }
  /* ... */
</style>
<div id="__app-loading__">
  <!-- ... -->
  <div class="title"><%= VITE_APP_TITLE %></div>
</div>
```

:::
