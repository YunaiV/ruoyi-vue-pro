# Common Features

A collection of some commonly used features.

## Login Authentication Expiry

When the interface returns a `401` status code, the framework will consider the login authentication to have expired. Upon login timeout, it will redirect to the login page or open a login popup. This can be configured in `preferences.ts` in the application directory:

### Redirect to Login Page

Upon login timeout, it will redirect to the login page.

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    loginExpiredMode: 'page',
  },
});
```

### Open Login Popup

When login times out, a login popup will open.

![login-expired](/guide/login-expired.png)

Configuration:

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    loginExpiredMode: 'modal',
  },
});
```

## Dynamic Title

- Default value: `true`

When enabled, the webpage title changes according to the route's `title`. You can enable or disable this in the `preferences.ts` file in your application directory.

```ts
export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    dynamicTitle: true,
  },
});
```

## Page Watermark

- Default value: `false`

When enabled, the webpage will display a watermark. You can enable or disable this in the `preferences.ts` file in your application directory.

```ts
export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    watermark: true,
  },
});
```

If you want to update the content of the watermark, you can do so. The parameters can be referred to [watermark-js-plus](https://zhensherlock.github.io/watermark-js-plus/):

```ts
import { useWatermark } from '@vben/hooks';

const { destroyWatermark, updateWatermark } = useWatermark();

await updateWatermark({
  // watermark content
  content: 'hello my watermark',
});
```
