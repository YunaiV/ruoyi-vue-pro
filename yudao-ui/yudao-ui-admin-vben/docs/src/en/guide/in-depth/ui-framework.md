# UI Framework Switching

`Vue Admin` supports your freedom to choose the UI framework. The default UI framework for the demo site is `Ant Design Vue`, consistent with the older version. The framework also has built-in versions for `Element Plus` and `Naive UI`, allowing you to choose according to your preference.

## Adding a New UI Framework

If you want to use a different UI framework, you only need to follow these steps:

1. Create a new folder inside `apps`, for example, `apps/web-xxx`.
2. Change the `name` field in `apps/web-xxx/package.json` to `web-xxx`.
3. Remove dependencies and code from other UI frameworks and replace them with your chosen UI framework's logic, which requires minimal changes.
4. Adjust the language files within `locales`.
5. Adjust the components in `app.vue`.
6. Adapt the theme of the UI framework to match `Vben Admin`.
7. Adjust the application name in `.env`.
8. Add a `dev:xxx` script in the root directory of the repository.
9. Run `pnpm install` to install dependencies.
