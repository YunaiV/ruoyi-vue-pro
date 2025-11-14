# Theme

The framework is built on [shadcn-vue](https://www.shadcn-vue.com/themes.html) and [tailwindcss](https://tailwindcss.com/), offering a rich theme configuration. You can easily switch between various themes through simple configuration to meet personalized needs. You can choose to use CSS variables or Tailwind CSS utility classes for theme settings.

## CSS Variables

The project follows the theme configuration of [shadcn-vue](https://www.shadcn-vue.com/themes.html), for example:

```html
<div class="bg-background text-foreground" />
```

We use a simple convention for colors. The `background` variable is used for the background color of components, and the `foreground` variable is used for text color.

For the following components, `background` will be `hsl(var(--primary))`, and `foreground` will be `hsl(var(--primary-foreground))`.

## Detailed List of CSS Variables

::: warning Note

The colors inside CSS variables must use the `hsl` format, such as `0 0% 100%`, without adding `hsl()` and `,`.

:::

You can check the list below to understand all the available variables.

::: details Default theme CSS variables

```css
:root {
  --font-family:
    -apple-system, blinkmacsystemfont, 'Segoe UI', roboto, 'Helvetica Neue',
    arial, 'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji',
    'Segoe UI Symbol', 'Noto Color Emoji';

  /* Default background color of <body />...etc */
  --background: 0 0% 100%;

  /* Main area background color */
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

  /* Theme Colors */

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

  /* overlay color */
  --overlay: 0deg 0% 0% / 30%;

  /* base font size */
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

::: details Default theme dark mode CSS variables

```css
.dark,
.dark[data-theme='custom'],
.dark[data-theme='default'] {
  /* Default background color of <body />...etc */
  --background: 222.34deg 10.43% 12.27%;

  /* Main area background color */
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

  /* Theme Colors */

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

  /* secondary color */
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

  /* base radius */
  --radius: 0.5rem;

  /* ============= Custom ============= */

  /* overlay color */
  --overlay: 0deg 0% 0% / 40%;

  /* base font size */
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

## Overriding Default CSS Variables

You only need to override the CSS variables you want to change in your project. For example, to change the default card background color, you can add the following content to your CSS file to override it:

### Under the Default Theme

```css
:root {
  /* Background color for <Card /> */
  --card: 0 0% 30%;
}
```

### In Dark Mode

```css
.dark,
.dark[data-theme='custom'],
.dark[data-theme='default'] {
  /* Background color for <Card /> */
  --card: 222.34deg 10.43% 12.27%;
}
```

## Changing the Brand Primary Color

::: tip

- You need to use the `hsl` color format.
- You must clear the cache for the changes to take effect.
- You can use [third-party tools](https://www.w3schools.com/colors/colors_hsl.asp) to convert colors.

:::

You only need to customize the primary color in the `preferences.ts` file under the application directory:

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  theme: {
    // Error color
    colorDestructive: 'hsl(348 100% 61%)',
    // Primary color
    colorPrimary: 'hsl(212 100% 45%)',
    // Success color
    colorSuccess: 'hsl(144 57% 58%)',
    // Warning color
    colorWarning: 'hsl(42 84% 61%)',
  },
});
```

## Built-in Themes

The framework includes a variety of built-in themes, which you can configure in the `preferences.ts` file:

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  theme: {
    builtinType: 'default',
  },
});
```

### Built-in Theme List

The framework includes 16 built-in themes and also supports custom themes. Theoretically, you can expand the themes without limit.

::: details List of Built-in Theme Types

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

::: details Built-in Theme CSS Variables - Light

```css
:root {
  --font-family:
    -apple-system, blinkmacsystemfont, 'Segoe UI', roboto, 'Helvetica Neue',
    arial, 'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji',
    'Segoe UI Symbol', 'Noto Color Emoji';

  /* Default background color of <body />...etc */
  --background: 0 0% 100%;

  /* Main area background color */
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

  /* Theme Colors */

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

  /* overlay color */
  --overlay: 0deg 0% 0% / 30%;

  /* base font size */
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

  /* overlay color */
  --overlay: 0deg 0% 0% / 40%;

  /* base font size */
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

## Adding a New Theme

To add a new theme, simply follow these steps:

- Add a new theme configuration in the application's `src/preferences.ts`.

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  theme: {
    builtinType: 'my-theme',
  },
});
```

- Add the theme's CSS variables to your CSS file.

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

## Dark Mode

The framework includes a variety of built-in themes, which you can configure in `preferences.ts`. The dark theme also uses CSS variables for configuration:

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  theme: {
    mode: 'dark',
  },
});
```

## Customizing Sidebar Color

The sidebar color is configured through the `--sidebar` variable.

### Under the Default Theme

```css
:root {
  --sidebar: 0 0% 100%;
}
```

### In Dark Mode

```css
.dark,
.dark[data-theme='custom'],
.dark[data-theme='default'] {
  --sidebar: 222.34deg 10.43% 12.27%;
}
```

## Customizing Header Color

The header color is configured through the `--header` variable.

### Under the Default Theme

```css
:root {
  --header: 0 0% 100%;
}
```

### In Dark Mode

```css
.dark,
.dark[data-theme='custom'],
.dark[data-theme='default'] {
  --header: 222.34deg 10.43% 12.27%;
}
```

## Color Weakness Mode

Typically used in special scenarios, you can set the application to color weakness mode. This can be configured in `preferences.ts`:

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    colorWeakMode: true,
  },
});
```

## Gray Mode

Typically used in special scenarios, this mode grays out the webpage. You can configure it in `preferences.ts`:

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    colorGrayMode: true,
  },
});
```
