# 主题

框架基于 [shadcn-vue](https://www.shadcn-vue.com/themes.html) 和 [tailwindcss](https://tailwindcss.com/) 构建，提供了丰富的主题配置，可以通过简单的配置实现各种主题切换，满足个性化需求。您可以选择使用 CSS 变量或 Tailwind CSS 实用程序类进行主题设置。

## Css 变量

项目遵循 [shadcn-vue](https://www.shadcn-vue.com/themes.html) 的主题配置，示例：

```html
<div class="bg-background text-foreground" />
```

我们对颜色使用一个简单的约定。`background`变量用于组件的背景颜色，`foreground`变量用于文本颜色。

以下组件的`background`将为`hsl(var(--primary))`，`foreground`将为`hsl(var(--primary-foreground))`。

## 详细的CSS变量列表

::: warning 注意

css 变量内的颜色，必须使用 `hsl` 格式，如 `0 0% 100%`，不需要加 `hsl()`和 `，`。

:::

你可以查看下面的CSS变量列表，以了解所有可用的变量。

::: details 默认主题 css 变量

```css
:root {
  --font-family:
    -apple-system, blinkmacsystemfont, 'Segoe UI', roboto, 'Helvetica Neue',
    arial, 'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji',
    'Segoe UI Symbol', 'Noto Color Emoji';

  /* Default background color of <body />...etc */
  --background: 0 0% 100%;

  /* 主体区域背景色 */
  --background-deep: 216 20.11% 95.47%;
  --foreground: 210 6% 21%;

  /* Background color for <Card /> */
  --card: 0 0% 100%;
  --card-foreground: 222.2 84% 4.9%;

  /* Background color for popovers such as <DropdownMenu />, <HoverCard />, <Popover /> */
  --popover: 0 0% 100%;
  --popover-foreground: 222.2 84% 4.9%;

  /* Muted backgrounds such as <TabsList />, <Skeleton /> and <Switch /> */
  --muted: 210 40% 96.1%;
  --muted-foreground: 215.4 16.3% 46.9%;

  /* 主题颜色 */

  --primary: 212 100% 45%;
  --primary-foreground: 0 0% 98%;

  /* Used for destructive actions such as <Button variant="destructive"> */

  --destructive: 0 78% 68%;
  --destructive-foreground: 0 0% 98%;

  /* Used for success actions such as <message> */

  --success: 144 57% 58%;
  --success-foreground: 0 0% 98%;

  /* Used for warning actions such as <message> */

  --warning: 42 84% 61%;
  --warning-foreground: 0 0% 98%;

  /* Secondary colors for <Button /> */

  --secondary: 240 5% 96%;
  --secondary-foreground: 240 6% 10%;

  /* Used for accents such as hover effects on <DropdownMenuItem>, <SelectItem>...etc */
  --accent: 240 5% 96%;
  --accent-hover: 200deg 10% 90%;
  --accent-foreground: 240 6% 10%;

  /* Darker color */
  --heavy: 192deg 9.43% 89.61%;
  --heavy-foreground: var(--accent-foreground);

  /* Default border color */
  --border: 240 5.9% 90%;

  /* Border color for inputs such as <Input />, <Select />, <Textarea /> */
  --input: 240deg 5.88% 90%;
  --input-placeholder: 217 10.6% 65%;
  --input-background: 0 0% 100%;

  /* Used for focus ring */
  --ring: 222.2 84% 4.9%;

  /* Border radius for card, input and buttons */
  --radius: 0.5rem;

  /* ============= custom ============= */

  /* 遮罩颜色 */
  --overlay: 0deg 0% 0% / 30%;

  /* 基本文字大小 */
  --font-size-base: 16px;

  /* =============component & UI============= */

  /* menu */
  --sidebar: 0 0% 100%;
  --sidebar-deep: 216 20.11% 95.47%;
  --menu: var(--sidebar);

  /* header */
  --header: 0 0% 100%;

  accent-color: var(--primary);
  color-scheme: light;
}
```

:::

::: details 默认主题黑暗模式 css 变量

```css
.dark,
.dark[data-theme='custom'],
.dark[data-theme='default'] {
  /* Default background color of <body />...etc */
  --background: 222.34deg 10.43% 12.27%;

  /* 主体区域背景色 */
  --background-deep: 220deg 13.06% 9%;
  --foreground: 0 0% 95%;

  /* Background color for <Card /> */
  --card: 222.34deg 10.43% 12.27%;

  /* --card: 222.2 84% 4.9%; */
  --card-foreground: 210 40% 98%;

  /* Background color for popovers such as <DropdownMenu />, <HoverCard />, <Popover /> */
  --popover: 222.82deg 8.43% 12.27%;
  --popover-foreground: 210 40% 98%;

  /* Muted backgrounds such as <TabsList />, <Skeleton /> and <Switch /> */
  --muted: 220deg 6.82% 17.25%;
  --muted-foreground: 215 20.2% 65.1%;

  /* 主题颜色 */

  /* --primary: 245 82% 67%; */
  --primary-foreground: 0 0% 98%;

  /* Used for destructive actions such as <Button variant="destructive"> */

  --destructive: 0 78% 68%;
  --destructive-foreground: 0 0% 98%;

  /* Used for success actions such as <message> */

  --success: 144 57% 58%;
  --success-foreground: 0 0% 98%;

  /* Used for warning actions such as <message> */

  --warning: 42 84% 61%;
  --warning-foreground: 0 0% 98%;

  /* 颜色次要 */
  --secondary: 240 5% 17%;
  --secondary-foreground: 0 0% 98%;

  /* Used for accents such as hover effects on <DropdownMenuItem>, <SelectItem>...etc */
  --accent: 0deg 0% 100% / 8%;
  --accent-hover: 0deg 0% 100% / 12%;
  --accent-foreground: 0 0% 98%;

  /* Darker color */
  --heavy: 0deg 0% 100% / 12%;
  --heavy-foreground: var(--accent-foreground);

  /* Default border color */
  --border: 240 3.7% 15.9%;

  /* Border color for inputs such as <Input />, <Select />, <Textarea /> */
  --input: 0deg 0% 100% / 10%;
  --input-placeholder: 218deg 11% 65%;
  --input-background: 0deg 0% 100% / 5%;

  /* Used for focus ring */
  --ring: 222.2 84% 4.9%;

  /* 基本圆角大小 */
  --radius: 0.5rem;

  /* ============= Custom ============= */

  /* 遮罩颜色 */
  --overlay: 0deg 0% 0% / 40%;

  /* 基本文字大小 */
  --font-size-base: 16px;

  /* =============component & UI============= */

  --sidebar: 222.34deg 10.43% 12.27%;
  --sidebar-deep: 220deg 13.06% 9%;
  --menu: var(--sidebar);
  --header: 222.34deg 10.43% 12.27%;

  color-scheme: dark;
}
```

:::

## 覆盖默认的 CSS 变量

你只需要在你的项目中覆盖你想要修改的 CSS 变量即可。例如，要更改默认卡片背景色，你可以在你的 CSS 文件中添加以下内容进行覆盖：

### 默认主题下

```css
:root {
  /* Background color for <Card /> */
  --card: 0 0% 30%;
}
```

### 黑暗模式下

```css
.dark,
.dark[data-theme='custom'],
.dark[data-theme='default'] {
  /* Background color for <Card /> */
  --card: 222.34deg 10.43% 12.27%;
}
```

## 更改品牌主色

::: tip

- 需要使用 `hsl` 格式颜色格式。
- 修改后需要清空缓存才可生效。
- 你可以借助 [第三方工具](https://www.w3schools.com/colors/colors_hsl.asp)来转换颜色。

:::

只需要在应用目录下的`preferences.ts`，自定义配置主色即可：

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  theme: {
    // 错误色
    colorDestructive: 'hsl(348 100% 61%)',
    // 主题色
    colorPrimary: 'hsl(212 100% 45%)',
    // 成功色
    colorSuccess: 'hsl(144 57% 58%)',
    // 警告色
    colorWarning: 'hsl(42 84% 61%)',
  },
});
```

## 内置主题

框架中内置了多种主题，你可以在`preferences.ts`中进行配置：

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  theme: {
    builtinType: 'default',
  },
});
```

### 内置主题列表

框架内置了 16种主题，且还支持自定义主题。理论上，你可以无限制的扩展主题。

::: details 内置主题类型列表

```ts
type BuiltinThemeType =
  | 'custom'
  | 'deep-blue'
  | 'deep-green'
  | 'default'
  | 'gray'
  | 'green'
  | 'neutral'
  | 'orange'
  | 'pink'
  | 'red'
  | 'rose'
  | 'sky-blue'
  | 'slate'
  | 'stone'
  | 'violet'
  | 'yellow'
  | 'zinc'
  | (Record<never, never> & string);
```

:::

::: details 内置主题css变量 - light

```css
:root {
  --font-family:
    -apple-system, blinkmacsystemfont, 'Segoe UI', roboto, 'Helvetica Neue',
    arial, 'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji',
    'Segoe UI Symbol', 'Noto Color Emoji';

  /* Default background color of <body />...etc */
  --background: 0 0% 100%;

  /* 主体区域背景色 */
  --background-deep: 216 20.11% 95.47%;
  --foreground: 222 84% 5%;

  /* Background color for <Card /> */
  --card: 0 0% 100%;
  --card-foreground: 222.2 84% 4.9%;

  /* Background color for popovers such as <DropdownMenu />, <HoverCard />, <Popover /> */
  --popover: 0 0% 100%;
  --popover-foreground: 222.2 84% 4.9%;

  /* Muted backgrounds such as <TabsList />, <Skeleton /> and <Switch /> */

  /* --muted: 210 40% 96.1%;
  --muted-foreground: 215.4 16.3% 46.9%; */

  --muted: 240 4.8% 95.9%;
  --muted-foreground: 240 3.8% 46.1%;

  /* 主题颜色 */

  --primary: 212 100% 45%;
  --primary-foreground: 0 0% 98%;

  /* Used for destructive actions such as <Button variant="destructive"> */

  --destructive: 0 78% 68%;
  --destructive-foreground: 0 0% 98%;

  /* Used for success actions such as <message> */

  --success: 144 57% 58%;
  --success-foreground: 0 0% 98%;

  /* Used for warning actions such as <message> */

  --warning: 42 84% 61%;
  --warning-foreground: 0 0% 98%;

  /* Secondary colors for <Button /> */

  --secondary: 240 5% 96%;
  --secondary-foreground: 240 6% 10%;

  /* Used for accents such as hover effects on <DropdownMenuItem>, <SelectItem>...etc */
  --accent: 240 5% 96%;
  --accent-hover: 200deg 10% 90%;
  --accent-foreground: 240 6% 10%;

  /* Darker color */
  --heavy: 192deg 9.43% 89.61%;
  --heavy-foreground: var(--accent-foreground);

  /* Default border color */
  --border: 240 5.9% 90%;

  /* Border color for inputs such as <Input />, <Select />, <Textarea /> */
  --input: 240deg 5.88% 90%;
  --input-placeholder: 217 10.6% 65%;
  --input-background: 0 0% 100%;

  /* Used for focus ring */
  --ring: 222.2 84% 4.9%;

  /* Border radius for card, input and buttons */
  --radius: 0.5rem;

  /* ============= custom ============= */

  /* 遮罩颜色 */
  --overlay: 0deg 0% 0% / 30%;

  /* 基本文字大小 */
  --font-size-base: 16px;

  /* =============component & UI============= */

  /* menu */
  --sidebar: 0 0% 100%;
  --sidebar-deep: 0 0% 100%;
  --menu: var(--sidebar);

  /* header */
  --header: 0 0% 100%;

  accent-color: var(--primary);
  color-scheme: light;
}

[data-theme='violet'] {
  /* --background: 0 0% 100%; */
  --foreground: 224 71.4% 4.1%;
  --card: 0 0% 100%;
  --card-foreground: 224 71.4% 4.1%;
  --popover: 0 0% 100%;
  --popover-foreground: 224 71.4% 4.1%;
  --primary-foreground: 210 20% 98%;
  --secondary: 220 14.3% 95.9%;
  --secondary-foreground: 220.9 39.3% 11%;
  --muted: 220 14.3% 95.9%;
  --muted-foreground: 220 8.9% 46.1%;
  --accent: 220 14.3% 95.9%;
  --accent-foreground: 220.9 39.3% 11%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 210 20% 98%;
  --border: 220 13% 91%;
  --input: 220 13% 91%;
  --ring: 262.1 83.3% 57.8%;
}

[data-theme='pink'] {
  /* --background: 0 0% 100%; */
  --foreground: 240 10% 3.9%;
  --card: 0 0% 100%;
  --card-foreground: 240 10% 3.9%;
  --popover: 0 0% 100%;
  --popover-foreground: 240 10% 3.9%;
  --primary-foreground: 355.7 100% 97.3%;
  --secondary: 240 4.8% 95.9%;
  --secondary-foreground: 240 5.9% 10%;
  --muted: 240 4.8% 95.9%;
  --muted-foreground: 240 3.8% 46.1%;
  --accent: 240 4.8% 95.9%;
  --accent-foreground: 240 5.9% 10%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 0 0% 98%;
  --border: 240 5.9% 90%;
  --input: 240 5.9% 90%;
  --ring: 346.8 77.2% 49.8%;
}

[data-theme='rose'] {
  /* --background: 0 0% 100%; */
  --foreground: 240 10% 3.9%;
  --card: 0 0% 100%;
  --card-foreground: 240 10% 3.9%;
  --popover: 0 0% 100%;
  --popover-foreground: 240 10% 3.9%;
  --primary-foreground: 355.7 100% 97.3%;
  --secondary: 240 4.8% 95.9%;
  --secondary-foreground: 240 5.9% 10%;
  --muted: 240 4.8% 95.9%;
  --muted-foreground: 240 3.8% 46.1%;
  --accent: 240 4.8% 95.9%;
  --accent-foreground: 240 5.9% 10%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 0 0% 98%;
  --border: 240 5.9% 90%;
  --input: 240 5.9% 90%;
  --ring: 346.8 77.2% 49.8%;
}

[data-theme='sky-blue'] {
  /* --background: 0 0% 100%; */
  --foreground: 222.2 84% 4.9%;
  --card: 0 0% 100%;
  --card-foreground: 222.2 84% 4.9%;
  --popover: 0 0% 100%;
  --popover-foreground: 222.2 84% 4.9%;
  --primary-foreground: 210 40% 98%;
  --secondary: 210 40% 96.1%;
  --secondary-foreground: 222.2 47.4% 11.2%;
  --muted: 210 40% 96.1%;
  --muted-foreground: 215.4 16.3% 46.9%;
  --accent: 210 40% 96.1%;
  --accent-foreground: 222.2 47.4% 11.2%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 210 40% 98%;
  --border: 214.3 31.8% 91.4%;
  --input: 214.3 31.8% 91.4%;
  --ring: 221.2 83.2% 53.3%;
}

[data-theme='deep-blue'] {
  /* --background: 0 0% 100%; */
  --foreground: 222.2 84% 4.9%;
  --card: 0 0% 100%;
  --card-foreground: 222.2 84% 4.9%;
  --popover: 0 0% 100%;
  --popover-foreground: 222.2 84% 4.9%;
  --primary-foreground: 210 40% 98%;
  --secondary: 210 40% 96.1%;
  --secondary-foreground: 222.2 47.4% 11.2%;
  --muted: 210 40% 96.1%;
  --muted-foreground: 215.4 16.3% 46.9%;
  --accent: 210 40% 96.1%;
  --accent-foreground: 222.2 47.4% 11.2%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 210 40% 98%;
  --border: 214.3 31.8% 91.4%;
  --input: 214.3 31.8% 91.4%;
  --ring: 221.2 83.2% 53.3%;
}

[data-theme='green'] {
  /* --background: 0 0% 100%; */
  --foreground: 240 10% 3.9%;
  --card: 0 0% 100%;
  --card-foreground: 240 10% 3.9%;
  --popover: 0 0% 100%;
  --popover-foreground: 240 10% 3.9%;
  --primary-foreground: 355.7 100% 97.3%;
  --secondary: 240 4.8% 95.9%;
  --secondary-foreground: 240 5.9% 10%;
  --muted: 240 4.8% 95.9%;
  --muted-foreground: 240 3.8% 46.1%;
  --accent: 240 4.8% 95.9%;
  --accent-foreground: 240 5.9% 10%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 0 0% 98%;
  --border: 240 5.9% 90%;
  --input: 240 5.9% 90%;
  --ring: 142.1 76.2% 36.3%;
}

[data-theme='deep-green'] {
  /* --background: 0 0% 100%; */
  --foreground: 240 10% 3.9%;
  --card: 0 0% 100%;
  --card-foreground: 240 10% 3.9%;
  --popover: 0 0% 100%;
  --popover-foreground: 240 10% 3.9%;
  --primary-foreground: 355.7 100% 97.3%;
  --secondary: 240 4.8% 95.9%;
  --secondary-foreground: 240 5.9% 10%;
  --muted: 240 4.8% 95.9%;
  --muted-foreground: 240 3.8% 46.1%;
  --accent: 240 4.8% 95.9%;
  --accent-foreground: 240 5.9% 10%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 0 0% 98%;
  --border: 240 5.9% 90%;
  --input: 240 5.9% 90%;
  --ring: 142.1 76.2% 36.3%;
}

[data-theme='orange'] {
  /* --background: 0 0% 100%; */
  --foreground: 20 14.3% 4.1%;
  --card: 0 0% 100%;
  --card-foreground: 20 14.3% 4.1%;
  --popover: 0 0% 100%;
  --popover-foreground: 20 14.3% 4.1%;
  --primary-foreground: 60 9.1% 97.8%;
  --secondary: 60 4.8% 95.9%;
  --secondary-foreground: 24 9.8% 10%;
  --muted: 60 4.8% 95.9%;
  --muted-foreground: 25 5.3% 44.7%;
  --accent: 60 4.8% 95.9%;
  --accent-foreground: 24 9.8% 10%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 60 9.1% 97.8%;
  --border: 20 5.9% 90%;
  --input: 20 5.9% 90%;
  --ring: 24.6 95% 53.1%;
}

[data-theme='yellow'] {
  /* --background: 0 0% 100%; */
  --foreground: 20 14.3% 4.1%;
  --card: 0 0% 100%;
  --card-foreground: 20 14.3% 4.1%;
  --popover: 0 0% 100%;
  --popover-foreground: 20 14.3% 4.1%;
  --primary-foreground: 26 83.3% 14.1%;
  --secondary: 60 4.8% 95.9%;
  --secondary-foreground: 24 9.8% 10%;
  --muted: 60 4.8% 95.9%;
  --muted-foreground: 25 5.3% 44.7%;
  --accent: 60 4.8% 95.9%;
  --accent-foreground: 24 9.8% 10%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 60 9.1% 97.8%;
  --border: 20 5.9% 90%;
  --input: 20 5.9% 90%;
  --ring: 20 14.3% 4.1%;
}

[data-theme='zinc'] {
  /* --background: 0 0% 100%; */
  --foreground: 240 10% 3.9%;
  --card: 0 0% 100%;
  --card-foreground: 240 10% 3.9%;
  --popover: 0 0% 100%;
  --popover-foreground: 240 10% 3.9%;
  --primary-foreground: 0 0% 98%;
  --secondary: 240 4.8% 95.9%;
  --secondary-foreground: 240 5.9% 10%;
  --muted: 240 4.8% 95.9%;
  --muted-foreground: 240 3.8% 46.1%;
  --accent: 240 4.8% 95.9%;
  --accent-foreground: 240 5.9% 10%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 0 0% 98%;
  --border: 240 5.9% 90%;
  --input: 240 5.9% 90%;
  --ring: 240 5.9% 10%;
}

[data-theme='neutral'] {
  /* --background: 0 0% 100%; */
  --foreground: 0 0% 3.9%;
  --card: 0 0% 100%;
  --card-foreground: 0 0% 3.9%;
  --popover: 0 0% 100%;
  --popover-foreground: 0 0% 3.9%;
  --primary-foreground: 0 0% 98%;
  --secondary: 0 0% 96.1%;
  --secondary-foreground: 0 0% 9%;
  --muted: 0 0% 96.1%;
  --muted-foreground: 0 0% 45.1%;
  --accent: 0 0% 96.1%;
  --accent-foreground: 0 0% 9%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 0 0% 98%;
  --border: 0 0% 89.8%;
  --input: 0 0% 89.8%;
  --ring: 0 0% 3.9%;
}

[data-theme='slate'] {
  /* --background: 0 0% 100%; */
  --foreground: 222.2 84% 4.9%;
  --card: 0 0% 100%;
  --card-foreground: 222.2 84% 4.9%;
  --popover: 0 0% 100%;
  --popover-foreground: 222.2 84% 4.9%;
  --primary-foreground: 210 40% 98%;
  --secondary: 210 40% 96.1%;
  --secondary-foreground: 222.2 47.4% 11.2%;
  --muted: 210 40% 96.1%;
  --muted-foreground: 215.4 16.3% 46.9%;
  --accent: 210 40% 96.1%;
  --accent-foreground: 222.2 47.4% 11.2%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 210 40% 98%;
  --border: 214.3 31.8% 91.4%;
  --input: 214.3 31.8% 91.4%;
  --ring: 222.2 84% 4.9%;
}

[data-theme='gray'] {
  /* --background: 0 0% 100%; */
  --foreground: 224 71.4% 4.1%;
  --card: 0 0% 100%;
  --card-foreground: 224 71.4% 4.1%;
  --popover: 0 0% 100%;
  --popover-foreground: 224 71.4% 4.1%;
  --primary-foreground: 210 20% 98%;
  --secondary: 220 14.3% 95.9%;
  --secondary-foreground: 220.9 39.3% 11%;
  --muted: 220 14.3% 95.9%;
  --muted-foreground: 220 8.9% 46.1%;
  --accent: 220 14.3% 95.9%;
  --accent-foreground: 220.9 39.3% 11%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 210 20% 98%;
  --border: 220 13% 91%;
  --input: 220 13% 91%;
  --ring: 224 71.4% 4.1%;
}
```

:::

::: details 内置主题css变量 - dark

```css
.dark,
.dark[data-theme='custom'],
.dark[data-theme='default'] {
  /* Default background color of <body />...etc */
  --background: 222.34deg 10.43% 12.27%;

  /* 主体区域背景色 */
  --background-deep: 220deg 13.06% 9%;
  --foreground: 0 0% 95%;

  /* Background color for <Card /> */
  --card: 222.34deg 10.43% 12.27%;

  /* --card: 222.2 84% 4.9%; */
  --card-foreground: 210 40% 98%;

  /* Background color for popovers such as <DropdownMenu />, <HoverCard />, <Popover /> */
  --popover: 222.82deg 8.43% 12.27%;
  --popover-foreground: 210 40% 98%;

  /* Muted backgrounds such as <TabsList />, <Skeleton /> and <Switch /> */

  /* --muted: 220deg 6.82% 17.25%; */

  /* --muted-foreground: 215 20.2% 65.1%; */

  --muted: 240 3.7% 15.9%;
  --muted-foreground: 240 5% 64.9%;

  /* 主题颜色 */

  /* --primary: 245 82% 67%; */
  --primary-foreground: 0 0% 98%;

  /* Used for destructive actions such as <Button variant="destructive"> */

  --destructive: 0 78% 68%;
  --destructive-foreground: 0 0% 98%;

  /* Used for success actions such as <message> */

  --success: 144 57% 58%;
  --success-foreground: 0 0% 98%;

  /* Used for warning actions such as <message> */

  --warning: 42 84% 61%;
  --warning-foreground: 0 0% 98%;

  /* 颜色次要 */
  --secondary: 240 5% 17%;
  --secondary-foreground: 0 0% 98%;

  /* Used for accents such as hover effects on <DropdownMenuItem>, <SelectItem>...etc */
  --accent: 216 5% 19%;
  --accent-hover: 216 5% 24%;
  --accent-foreground: 0 0% 98%;

  /* Darker color */
  --heavy: 216 5% 24%;
  --heavy-foreground: var(--accent-foreground);

  /* Default border color */
  --border: 240 3.7% 22%;

  /* Border color for inputs such as <Input />, <Select />, <Textarea /> */
  --input: 0deg 0% 100% / 10%;
  --input-placeholder: 218deg 11% 65%;
  --input-background: 0deg 0% 100% / 5%;

  /* Used for focus ring */
  --ring: 222.2 84% 4.9%;

  /* 基本圆角大小 */
  --radius: 0.5rem;

  /* ============= Custom ============= */

  /* 遮罩颜色 */
  --overlay: 0deg 0% 0% / 40%;

  /* 基本文字大小 */
  --font-size-base: 16px;

  /* =============component & UI============= */

  --sidebar: 222.34deg 10.43% 12.27%;
  --sidebar-deep: 220deg 13.06% 9%;
  --menu: var(--sidebar);

  /* header */
  --header: 222.34deg 10.43% 12.27%;

  color-scheme: dark;
}

.dark[data-theme='violet'],
[data-theme='violet'] .dark {
  --background: 224 71.4% 4.1%;
  --background-deep: var(--background);
  --foreground: 210 20% 98%;
  --card: 224 71.4% 4.1%;
  --card-foreground: 210 20% 98%;
  --popover: 224 71.4% 4.1%;
  --popover-foreground: 210 20% 98%;
  --primary-foreground: 210 20% 98%;
  --secondary: 215 27.9% 16.9%;
  --secondary-foreground: 210 20% 98%;
  --muted: 215 27.9% 16.9%;
  --muted-foreground: 217.9 10.6% 64.9%;
  --accent: 215 27.9% 16.9%;
  --accent-foreground: 210 20% 98%;
  --destructive: 0 62.8% 30.6%;
  --destructive-foreground: 210 20% 98%;
  --border: 215 27.9% 16.9%;
  --input: 215 27.9% 16.9%;
  --ring: 263.4 70% 50.4%;
  --sidebar: 224 71.4% 4.1%;
  --sidebar-deep: 224 71.4% 4.1%;
  --header: 224 71.4% 4.1%;
}

.dark[data-theme='pink'],
[data-theme='pink'] .dark {
  --background: 20 14.3% 4.1%;
  --background-deep: var(--background);
  --foreground: 0 0% 95%;
  --card: 0 0% 9%;
  --card-foreground: 0 0% 95%;
  --popover: 0 0% 9%;
  --popover-foreground: 0 0% 95%;
  --primary-foreground: 355.7 100% 97.3%;
  --secondary: 240 3.7% 15.9%;
  --secondary-foreground: 0 0% 98%;
  --muted: 0 0% 15%;
  --muted-foreground: 240 5% 64.9%;
  --accent: 12 6.5% 15.1%;
  --accent-foreground: 0 0% 98%;
  --destructive: 0 62.8% 30.6%;
  --destructive-foreground: 0 85.7% 97.3%;
  --border: 240 3.7% 15.9%;
  --input: 240 3.7% 15.9%;
  --ring: 346.8 77.2% 49.8%;
  --sidebar: 20 14.3% 4.1%;
  --sidebar-deep: 20 14.3% 4.1%;
  --header: 20 14.3% 4.1%;
}

.dark[data-theme='rose'],
[data-theme='rose'] .dark {
  --background: 0 0% 3.9%;
  --background-deep: var(--background);
  --foreground: 0 0% 98%;
  --card: 0 0% 3.9%;
  --card-foreground: 0 0% 98%;
  --popover: 0 0% 3.9%;
  --popover-foreground: 0 0% 98%;
  --primary-foreground: 0 85.7% 97.3%;
  --secondary: 0 0% 14.9%;
  --secondary-foreground: 0 0% 98%;
  --muted: 0 0% 14.9%;
  --muted-foreground: 0 0% 63.9%;
  --accent: 0 0% 14.9%;
  --accent-foreground: 0 0% 98%;
  --destructive: 0 62.8% 30.6%;
  --destructive-foreground: 0 0% 98%;
  --border: 0 0% 14.9%;
  --input: 0 0% 14.9%;
  --ring: 0 72.2% 50.6%;
  --sidebar: 0 0% 3.9%;
  --sidebar-deep: 0 0% 3.9%;
  --header: 0 0% 3.9%;
}

.dark[data-theme='sky-blue'],
[data-theme='sky-blue'] .dark {
  --background: 222.2 84% 4.9%;
  --background-deep: var(--background);
  --foreground: 210 40% 98%;
  --card: 222.2 84% 4.9%;
  --card-foreground: 210 40% 98%;
  --popover: 222.2 84% 4.9%;
  --popover-foreground: 210 40% 98%;
  --primary-foreground: 210 20% 98%;
  --secondary: 217.2 32.6% 17.5%;
  --secondary-foreground: 210 40% 98%;
  --muted: 217.2 32.6% 17.5%;
  --muted-foreground: 215 20.2% 65.1%;
  --accent: 217.2 32.6% 17.5%;
  --accent-foreground: 210 40% 98%;
  --destructive: 0 62.8% 30.6%;
  --destructive-foreground: 210 40% 98%;
  --border: 217.2 32.6% 17.5%;
  --input: 217.2 32.6% 17.5%;
  --ring: 224.3 76.3% 48%;
  --sidebar: 222.2 84% 4.9%;
  --sidebar-deep: 222.2 84% 4.9%;
  --header: 222.2 84% 4.9%;
}

.dark[data-theme='deep-blue'],
[data-theme='deep-blue'] .dark {
  --background: 222.2 84% 4.9%;
  --background-deep: var(--background);
  --foreground: 210 40% 98%;
  --card: 222.2 84% 4.9%;
  --card-foreground: 210 40% 98%;
  --popover: 222.2 84% 4.9%;
  --popover-foreground: 210 40% 98%;
  --primary-foreground: 210 20% 98%;
  --secondary: 217.2 32.6% 17.5%;
  --secondary-foreground: 210 40% 98%;
  --muted: 217.2 32.6% 17.5%;
  --muted-foreground: 215 20.2% 65.1%;
  --accent: 217.2 32.6% 17.5%;
  --accent-foreground: 210 40% 98%;
  --destructive: 0 62.8% 30.6%;
  --destructive-foreground: 210 40% 98%;
  --border: 217.2 32.6% 17.5%;
  --input: 217.2 32.6% 17.5%;
  --ring: 224.3 76.3% 48%;
  --sidebar: 222.2 84% 4.9%;
  --sidebar-deep: 222.2 84% 4.9%;
  --header: 222.2 84% 4.9%;
}

.dark[data-theme='green'],
[data-theme='green'] .dark {
  --background: 20 14.3% 4.1%;
  --background-deep: var(--background);
  --foreground: 0 0% 95%;
  --card: 24 9.8% 6%;
  --card-foreground: 0 0% 95%;
  --popover: 0 0% 9%;
  --popover-foreground: 0 0% 95%;
  --primary-foreground: 210 20% 98%;
  --secondary: 240 3.7% 15.9%;
  --secondary-foreground: 0 0% 98%;
  --muted: 0 0% 15%;
  --muted-foreground: 240 5% 64.9%;
  --accent: 12 6.5% 15.1%;
  --accent-foreground: 0 0% 98%;
  --destructive: 0 62.8% 30.6%;
  --destructive-foreground: 0 85.7% 97.3%;
  --border: 240 3.7% 15.9%;
  --input: 240 3.7% 15.9%;
  --ring: 142.4 71.8% 29.2%;
  --sidebar: 20 14.3% 4.1%;
  --sidebar-deep: 20 14.3% 4.1%;
  --header: 20 14.3% 4.1%;
}

.dark[data-theme='deep-green'],
[data-theme='deep-green'] .dark {
  --background: 20 14.3% 4.1%;
  --background-deep: var(--background);
  --foreground: 0 0% 95%;
  --card: 24 9.8% 6%;
  --card-foreground: 0 0% 95%;
  --popover: 0 0% 9%;
  --popover-foreground: 0 0% 95%;
  --primary-foreground: 210 20% 98%;
  --secondary: 240 3.7% 15.9%;
  --secondary-foreground: 0 0% 98%;
  --muted: 0 0% 15%;
  --muted-foreground: 240 5% 64.9%;
  --accent: 12 6.5% 15.1%;
  --accent-foreground: 0 0% 98%;
  --destructive: 0 62.8% 30.6%;
  --destructive-foreground: 0 85.7% 97.3%;
  --border: 240 3.7% 15.9%;
  --input: 240 3.7% 15.9%;
  --ring: 142.4 71.8% 29.2%;
  --sidebar: 20 14.3% 4.1%;
  --sidebar-deep: 20 14.3% 4.1%;
  --header: 20 14.3% 4.1%;
}

.dark[data-theme='orange'],
[data-theme='orange'] .dark {
  --background: 20 14.3% 4.1%;
  --background-deep: var(--background);
  --foreground: 60 9.1% 97.8%;
  --card: 20 14.3% 4.1%;
  --card-foreground: 60 9.1% 97.8%;
  --popover: 20 14.3% 4.1%;
  --popover-foreground: 60 9.1% 97.8%;
  --primary-foreground: 60 9.1% 97.8%;
  --secondary: 12 6.5% 15.1%;
  --secondary-foreground: 60 9.1% 97.8%;
  --muted: 12 6.5% 15.1%;
  --muted-foreground: 24 5.4% 63.9%;
  --accent: 12 6.5% 15.1%;
  --accent-foreground: 60 9.1% 97.8%;
  --destructive: 0 72.2% 50.6%;
  --destructive-foreground: 60 9.1% 97.8%;
  --border: 12 6.5% 15.1%;
  --input: 12 6.5% 15.1%;
  --ring: 20.5 90.2% 48.2%;
  --sidebar: 20 14.3% 4.1%;
  --sidebar-deep: 20 14.3% 4.1%;
  --header: 20 14.3% 4.1%;
}

.dark[data-theme='yellow'],
[data-theme='yellow'] .dark {
  --background: 20 14.3% 4.1%;
  --background-deep: var(--background);
  --foreground: 60 9.1% 97.8%;
  --card: 20 14.3% 4.1%;
  --card-foreground: 60 9.1% 97.8%;
  --popover: 20 14.3% 4.1%;
  --popover-foreground: 60 9.1% 97.8%;
  --primary-foreground: 26 83.3% 14.1%;
  --secondary: 12 6.5% 15.1%;
  --secondary-foreground: 60 9.1% 97.8%;
  --muted: 12 6.5% 15.1%;
  --muted-foreground: 24 5.4% 63.9%;
  --accent: 12 6.5% 15.1%;
  --accent-foreground: 60 9.1% 97.8%;
  --destructive: 0 62.8% 30.6%;
  --destructive-foreground: 60 9.1% 97.8%;
  --border: 12 6.5% 15.1%;
  --input: 12 6.5% 15.1%;
  --ring: 35.5 91.7% 32.9%;
  --sidebar: 20 14.3% 4.1%;
  --sidebar-deep: 20 14.3% 4.1%;
  --header: 20 14.3% 4.1%;
}

.dark[data-theme='zinc'],
[data-theme='zinc'] .dark {
  --background: 240 10% 3.9%;
  --background-deep: var(--background);
  --foreground: 0 0% 98%;
  --card: 240 10% 3.9%;
  --card-foreground: 0 0% 98%;
  --popover: 240 10% 3.9%;
  --popover-foreground: 0 0% 98%;
  --primary-foreground: 240 5.9% 10%;
  --secondary: 240 3.7% 15.9%;
  --secondary-foreground: 0 0% 98%;
  --muted: 240 3.7% 15.9%;
  --muted-foreground: 240 5% 64.9%;
  --accent: 240 3.7% 15.9%;
  --accent-foreground: 0 0% 98%;
  --destructive: 0 62.8% 30.6%;
  --destructive-foreground: 0 0% 98%;
  --border: 240 3.7% 15.9%;
  --input: 240 3.7% 15.9%;
  --ring: 240 4.9% 83.9%;
  --sidebar: 240 10% 3.9%;
  --sidebar-deep: 240 10% 3.9%;
  --header: 240 4.9% 83.9%;
}

.dark[data-theme='neutral'],
[data-theme='neutral'] .dark {
  --background: 0 0% 3.9%;
  --background-deep: var(--background);
  --foreground: 0 0% 98%;
  --card: 0 0% 3.9%;
  --card-foreground: 0 0% 98%;
  --popover: 0 0% 3.9%;
  --popover-foreground: 0 0% 98%;
  --primary-foreground: 0 0% 9%;
  --secondary: 0 0% 14.9%;
  --secondary-foreground: 0 0% 98%;
  --muted: 0 0% 14.9%;
  --muted-foreground: 0 0% 63.9%;
  --accent: 0 0% 14.9%;
  --accent-foreground: 0 0% 98%;
  --destructive: 0 62.8% 30.6%;
  --destructive-foreground: 0 0% 98%;
  --border: 0 0% 14.9%;
  --input: 0 0% 14.9%;
  --ring: 0 0% 83.1%;
  --sidebar: 0 0% 3.9%;
  --sidebar-deep: 0 0% 3.9%;
  --header: 0 0% 3.9%;
}

.dark[data-theme='slate'],
[data-theme='slate'] .dark {
  --background: 222.2 84% 4.9%;
  --background-deep: var(--background);
  --foreground: 210 40% 98%;
  --card: 222.2 84% 4.9%;
  --card-foreground: 210 40% 98%;
  --popover: 222.2 84% 4.9%;
  --popover-foreground: 210 40% 98%;
  --primary-foreground: 222.2 47.4% 11.2%;
  --secondary: 217.2 32.6% 17.5%;
  --secondary-foreground: 210 40% 98%;
  --muted: 217.2 32.6% 17.5%;
  --muted-foreground: 215 20.2% 65.1%;
  --accent: 217.2 32.6% 17.5%;
  --accent-foreground: 210 40% 98%;
  --destructive: 0 62.8% 30.6%;
  --destructive-foreground: 210 40% 98%;
  --border: 217.2 32.6% 17.5%;
  --input: 217.2 32.6% 17.5%;
  --ring: 212.7 26.8% 83.9;
  --sidebar: 222.2 84% 4.9%;
  --sidebar-deep: 222.2 84% 4.9%;
  --header: 222.2 84% 4.9%;
}

.dark[data-theme='gray'],
[data-theme='gray'] .dark {
  --background: 224 71.4% 4.1%;
  --background-deep: var(--background);
  --foreground: 210 20% 98%;
  --card: 224 71.4% 4.1%;
  --card-foreground: 210 20% 98%;
  --popover: 224 71.4% 4.1%;
  --popover-foreground: 210 20% 98%;
  --primary-foreground: 220.9 39.3% 11%;
  --secondary: 215 27.9% 16.9%;
  --secondary-foreground: 210 20% 98%;
  --muted: 215 27.9% 16.9%;
  --muted-foreground: 217.9 10.6% 64.9%;
  --accent: 215 27.9% 16.9%;
  --accent-foreground: 210 20% 98%;
  --destructive: 0 62.8% 30.6%;
  --destructive-foreground: 210 20% 98%;
  --border: 215 27.9% 16.9%;
  --input: 215 27.9% 16.9%;
  --ring: 216 12.2% 83.9%;
  --sidebar: 224 71.4% 4.1%;
  --sidebar-deep: 224 71.4% 4.1%;
  --header: 224 71.4% 4.1%;
}
```

:::

## 新增主题

想要新增主题，只需按照以下步骤进行：

- 在应用的 `src/preferences.ts`内新增一个主题配置。

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  theme: {
    builtinType: 'my-theme',
  },
});
```

- 在你的css文件中，新增主题的css变量。

```css
/* light */
[data-theme='my-theme'] {
  --foreground: 224 71.4% 4.1%;
  --card: 0 0% 100%;
  --card-foreground: 224 71.4% 4.1%;
  --popover: 0 0% 100%;
  --popover-foreground: 224 71.4% 4.1%;
  --primary-foreground: 210 20% 98%;
  --secondary: 220 14.3% 95.9%;
  --secondary-foreground: 220.9 39.3% 11%;
  --muted: 220 14.3% 95.9%;
  --muted-foreground: 220 8.9% 46.1%;
  --accent: 220 14.3% 95.9%;
  --accent-foreground: 220.9 39.3% 11%;
  --destructive: 0 84.2% 60.2%;
  --destructive-foreground: 210 20% 98%;
  --border: 220 13% 91%;
  --input: 220 13% 91%;
  --ring: 262.1 83.3% 57.8%;
}

/* dark */
.dark[data-theme='my-theme'],
[data-theme='my-theme'] .dark {
  --background: 224 71.4% 4.1%;
  --background-deep: var(--background);
  --foreground: 210 20% 98%;
  --card: 224 71.4% 4.1%;
  --card-foreground: 210 20% 98%;
  --popover: 224 71.4% 4.1%;
  --popover-foreground: 210 20% 98%;
  --primary-foreground: 210 20% 98%;
  --secondary: 215 27.9% 16.9%;
  --secondary-foreground: 210 20% 98%;
  --muted: 215 27.9% 16.9%;
  --muted-foreground: 217.9 10.6% 64.9%;
  --accent: 215 27.9% 16.9%;
  --accent-foreground: 210 20% 98%;
  --destructive: 0 62.8% 30.6%;
  --destructive-foreground: 210 20% 98%;
  --border: 215 27.9% 16.9%;
  --input: 215 27.9% 16.9%;
  --ring: 263.4 70% 50.4%;
  --sidebar: 224 71.4% 4.1%;
  --sidebar-deep: 224 71.4% 4.1%;
}
```

## 黑暗模式

框架中内置了多种主题，你可以在`preferences.ts`中进行配置，黑暗主题同样会读取css变量来进行配置：

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  theme: {
    mode: 'dark',
  },
});
```

## 自定义侧边栏颜色

侧边栏颜色通过`--sidebar`变量来配置

### 默认主题下

```css
:root {
  --sidebar: 0 0% 100%;
}
```

### 黑暗模式下

```css
.dark,
.dark[data-theme='custom'],
.dark[data-theme='default'] {
  --sidebar: 222.34deg 10.43% 12.27%;
}
```

## 自定义顶栏颜色

侧边栏颜色通过`--header`变量来配置

### 默认主题下

```css
:root {
  --header: 0 0% 100%;
}
```

### 黑暗模式下

```css
.dark,
.dark[data-theme='custom'],
.dark[data-theme='default'] {
  --header: 222.34deg 10.43% 12.27%;
}
```

## 色弱模式

一般用于特殊场景，将设置为色弱模式，你可以在`preferences.ts`中进行配置：

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    colorWeakMode: true,
  },
});
```

## 灰色模式

一般用于特殊场景，将网页置灰，你可以在`preferences.ts`中进行配置：

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    colorGrayMode: true,
  },
});
```
