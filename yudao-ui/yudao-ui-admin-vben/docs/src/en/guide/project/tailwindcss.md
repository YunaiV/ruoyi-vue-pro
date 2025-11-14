# Tailwind CSS

[Tailwind CSS](https://tailwindcss.com/) is a utility-first CSS framework for quickly building custom designs.

## Configuration

The project's configuration file is located in `internal/tailwind-config`, where you can modify the Tailwind CSS configuration.

::: tip Restrictions on using tailwindcss in packages

Tailwind CSS compilation will only be enabled if there is a `tailwind.config.mjs` file present in the corresponding package. Otherwise, Tailwind CSS will not be enabled. If you have a pure SDK package that does not require Tailwind CSS, you do not need to create a `tailwind.config.mjs` file.

:::
