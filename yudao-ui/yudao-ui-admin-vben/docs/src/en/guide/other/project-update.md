# PROJECT UPDATE

## Why Can't It Be Updated Like a npm Plugin

Because the project is a complete project template, not a plugin or a package, it cannot be updated like a plugin. After you use the code, you will develop it further based on business needs, and you need to manually merge and upgrade.

## What Should I Do

The project is managed using a `Monorepo` approach and has abstracted some of the more core code, such as `packages/@core`, `packages/effects`. As long as the business code has not modified this part of the code, you can directly pull the latest code and then merge it into your branch. You only need to handle some conflicts simply. Other folders will only make some minor adjustments, which will not affect the business code.

::: tip Recommendation

It is recommended to follow the repository updates actively and merge them; do not accumulate over a long time, Otherwise, it will lead to too many merge conflicts and increase the difficulty of merging.

:::

## Updating Code Using Git

1. Clone the code

```bash
git clone https://github.com/vbenjs/vue-vben-admin.git
```

2. Add your company's git source address

```bash
# up is the source name, can be set arbitrarily
# gitUrl is the latest open-source code
git remote add up gitUrl;
```

3. Push the code to your company's git

```bash
# Push the code to your company
# main is the branch name, adjust according to your situation
git push up main
# Sync the company's code
# main is the branch name, adjust according to your situation
git pull up main
```

4. How to sync the latest open-source code

```bash
git pull origin main
```

::: tip Tip

When syncing the code, conflicts may occur. Just resolve the conflicts.

:::
