# Check Updates

## Introduction

When there are updates to the website, you might need to check for updates. The framework provides this functionality. By periodically checking for updates, you can configure the `checkUpdatesInterval` and `enableCheckUpdates` fields in your application's preferences.ts file to enable and set the interval for checking updates (in minutes).

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    // Whether to enable check for updates
    enableCheckUpdates: true,
    // The interval for checking updates, in minutes
    checkUpdatesInterval: 1,
  },
});
```

## Effect

When an update is detected, a prompt will pop up asking the user whether to refresh the page:

![check-updates](/guide/update-notice.png)

## Replacing with Other Update Checking Methods

If you need to check for updates in other ways, such as through an API to more flexibly control the update logic (such as force refresh, display update content, etc.), you can do so by modifying the `src/widgets/check-updates/check-updates.vue` file under `@vben/layouts`.

```ts
// Replace this with your update checking logic
async function getVersionTag() {
  try {
    const response = await fetch('/', {
      cache: 'no-cache',
      method: 'HEAD',
    });

    return (
      response.headers.get('etag') || response.headers.get('last-modified')
    );
  } catch {
    console.error('Failed to fetch version tag');
    return null;
  }
}
```
