# Frequently Asked Questions #{faq}

::: tip Listed are some common questions

If you have a question, you can first look here. If not found, you can search or submit your question on [GitHub Issue](https://github.com/vbenjs/vue-vben-admin/issues), or if it's a discussion-type question, you can go to [GitHub Discussions](https://github.com/vbenjs/vue-vben-admin/discussions)

:::

## Instructions

If you encounter a problem, you can start looking from the following aspects:

1. Search the corresponding module's GitHub repository [issue](https://github.com/vbenjs/vue-vben-admin/issues)
2. Search for the problem on [Google](https://www.google.com)
3. Search for the problem on [Baidu](https://www.baidu.com)
4. If you can't find the issue in the list below, you can ask in [issues](https://github.com/vbenjs/vue-vben-admin/issues)
5. If it's not a problem type and needs discussion, please go to [discussions](https://github.com/vbenjs/vue-vben-admin/discussions) to discuss

## Dependency Issues

In a `Monorepo` project, it's important to get into the habit of running `pnpm install` after every `git pull` because new dependencies are often added. The project has configured automatic execution of `pnpm install` in `lefthook.yml`, but sometimes there might be issues. If it does not execute automatically, it is recommended to execute it manually once.

## About Cache Update Issues

The project configuration is by default cached in `localStorage`, so some configurations may not change after a version update.

The solution is to modify the `version` number in `package.json` each time the code is updated. This is because the key for `localStorage` is based on the version number. Therefore, after an update, the configurations from a previous version will become invalid. Simply re-login to apply the new settings.

## About Modifying Configuration Files

When modifying environment files such as `.env` or the `vite.config.ts` file, Vite will automatically restart the service.

There's a chance that automatic restarts may encounter issues. Simply rerunning the project can resolve these problems.

## Errors When Running Locally

Since Vite does not transform code locally and the code uses relatively new syntax such as optional chaining, local development requires using a higher version of the browser (`Chrome 90+`).

## Blank Page After Switching Pages

This issue occurs because route switching animations are enabled, and the corresponding page component has multiple root nodes. Adding a `<div></div>` at the outermost layer of the page can solve this problem.

**Incorrect Example**

```vue
<template>
  <!-- Annotations are also considered a node -->
  <h1>text h1</h1>
  <h2>text h2</h2>
</template>
```

**正确示例**

```vue
<template>
  <div>
    <h1>text h1</h1>
    <h2>text h2</h2>
  </div>
</template>
```

::: tip Tip

- If you want to use multiple root tags, you can disable route switching animations.
- Root comment nodes under `template` are also counted as a node.

:::

## My code works locally but not when packaged

The reason for this issue could be one of the following. You can check these reasons, and if there are other possibilities, feel free to add them:

- The variable `ctx` was used, which is not exposed in the instance type. The Vue official documentation also advises against using this property as it is intended for internal use only.

```ts
import { getCurrentInstance } from 'vue';
getCurrentInstance().ctx.xxxx;
```

## Dependency Installation Issues

- If you cannot install dependencies or the startup reports an error, you can try executing `pnpm run reinstall`.
- If you cannot install dependencies or encounter errors, you can try switching to a mobile hotspot for installing dependencies.
- If that still doesn't work, you can configure a domestic mirror for installation.
- You can also create a `.npmrc` file in the project root directory with the following content:

```bash
# .npmrc
registry = https://registry.npmmirror.com/
```

## Package File Too Large

- First, the full version will be larger because it includes many library files. You can use the simplified version for development.

- Secondly, it is recommended to enable gzip, which can reduce the size to about 1/3 of the original. Gzip can be enabled directly by the server. If so, the frontend does not need to build `.gz` format files. If the frontend has built `.gz` files, for example, with nginx, you need to enable the `gzip_static: on` option.

- While enabling gzip, you can also enable `brotli` for better compression than gzip. Both can coexist.

**Note**

- gzip_static: This module requires additional installation in nginx, as the default nginx does not include this module.

- Enabling `brotli` also requires additional nginx module installation.

## Runtime Errors

If you encounter errors similar to the following, please check that the full project path (including all parent paths) does not contain Chinese, Japanese, or Korean characters. Otherwise, you will encounter a 404 error for the path, leading to the following issue:

```ts
[vite] Failed to resolve module import "ant-design-vue/dist/antd.css-vben-adminode_modulesant-design-vuedistantd.css". (imported by /@/setup/ant-design-vue/index.ts)
```

## Console Route Warning Issue

If you see the following warning in the console, and the page `can open normally`, you can ignore this warning.

Future versions of `vue-router` may provide an option to disable this warning.

```ts
[Vue Router warn]: No match found for location with path "xxxx"
```

## Startup Error

If you encounter the following error message, please check if your nodejs version meets the requirements.

```bash
TypeError: str.matchAll is not a function
at Object.extractor (vue-vben-admin-main\node_modules@purge-icons\core\dist\index.js:146:27)
at Extract (vue-vben-admin-main\node_modules@purge-icons\core\dist\index.js:173:54)
```

## nginx Deployment

After deploying to `nginx`，you might encounter the following error:

```bash
Failed to load module script: Expected a JavaScript module script but the server responded with a MIME type of "application/octet-stream". Strict MIME type checking is enforced for module scripts per HTML spec.
```

Solution 1:

```bash
http {
    #If there is such a configuration, it needs to be commented out
    #include       mime.types;

    types {
      application/javascript js mjs;
    }
}
```

Solution 2：

Open the `mime.types` file under `nginx` and change `application/javascript js;` to `application/javascript js mjs;`
