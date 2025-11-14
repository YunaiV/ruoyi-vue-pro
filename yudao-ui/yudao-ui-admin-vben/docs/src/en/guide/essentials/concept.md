# Basic Concepts

In the new version, the entire project has been restructured. Now, we will introduce some basic concepts to help you better understand the entire document. Please make sure to read this section first.

## Monorepo

Monorepo refers to the repository of the entire project, which includes all code, packages, applications, standards, documentation, configurations, etc., that is, the entire content of a `Monorepo` directory.

## Applications

Applications refer to a complete project; a project can contain multiple applications, which can reuse the code, packages, standards, etc., within the monorepo. Applications are placed in the `apps` directory. Each application is independent and can be run, built, tested, and deployed separately; it can also include different component libraries, etc.

::: tip

Applications are not limited to front-end applications; they can also be back-end applications, mobile applications, etc. For example, `apps/backend-mock` is a back-end service.

:::

## Packages

A package refers to an independent module, which can be a component, a tool, a library, etc. Packages can be referenced by multiple applications or other packages. Packages are placed in the `packages` directory.

You can consider these packages as independent `npm` packages, and they are used in the same way as `npm` packages.

### Package Import

Importing a package in `package.json`:

```json {3}
{
  "dependencies": {
    "@vben/utils": "workspace:*"
  }
}
```

### Package Usage

Importing a package in the code:

```ts
import { isString } from '@vben/utils';
```

## Aliases

In the project, you can see some paths starting with `#`, such as `#/api`, `#/views`. These paths are aliases, used for quickly locating a certain directory. They are not implemented through `vite`'s `alias`, but through the principle of [subpath imports](https://nodejs.org/api/packages.html#subpath-imports) in `Node.js` itself. You only need to configure the `imports` field in `package.json`.

```json {3}
{
  "imports": {
    "#/*": "./src/*"
  }
}
```

To make these aliases recognizable by the IDE, we also need to configure them in `tsconfig.json`:

```json {5}
{
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "#/*": ["src/*"]
    }
  }
}
```

This way, you can use aliases in your code.
