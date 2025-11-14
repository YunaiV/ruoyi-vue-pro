# Global Loading

Global loading refers to the loading effect that appears when the page is refreshed, usually a spinning icon:

![Global loading spinner](/guide/loading.png)

## Principle

Implemented by the `vite-plugin-inject-app-loading` plugin, the plugin injects a global `loading html` into each application.

## Disable

If you do not need global loading, you can disable it in the `.env` file:

```bash
VITE_INJECT_APP_LOADING=false
```

## Customization

If you want to customize the global loading, you can create a `loading.html` file in the application directory, at the same level as `index.html`. The plugin will automatically read and inject this HTML. You can define the style and animation of this HTML as you wish.

::: tip

- You can use the same syntax as in `index.html`, such as the `VITE_APP_TITLE` variable, to get the application's title.
- You must ensure there is an element with `id="__app-loading__"`.
- Add a `hidden` class to the element with `id="__app-loading__"`.
- You must ensure there is a `style[data-app-loading="inject-css"]` element.

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
