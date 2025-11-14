---
outline: deep
---

# CLI

In the project, some command-line tools are provided for common operations, located in `scripts`.

## vsh

Used for some project operations, such as cleaning the project, checking the project, etc.

### Usage

```bash
pnpm vsh [command] [options]
```

### vsh check-circular

Check for circular references throughout the project. If there are circular references, the modules involved will be output to the console.

#### Usage

```bash
pnpm vsh check-circular
```

#### Options

| Option     | Description                                               |
| ---------- | --------------------------------------------------------- |
| `--staged` | Only check files in the git staging area, default `false` |

### vsh check-dep

Check the dependency situation of the entire project and output `unused dependencies`, `uninstalled dependencies` information to the console.

#### Usage

```bash
pnpm vsh check-dep
```

### vsh lint

Lint checks the project to see if the code in the project conforms to standards.

#### Usage

```bash
pnpm vsh lint
```

#### Options

| Option     | Description                                  |
| ---------- | -------------------------------------------- |
| `--format` | Check and try to fix errors, default `false` |

### vsh publint

Perform package standard checks on `Monorepo` projects to see if the packages in the project conform to standards.

#### Usage

```bash
pnpm vsh publint
```

#### Options

| Option    | Description                          |
| --------- | ------------------------------------ |
| `--check` | Only perform checks, default `false` |

### vsh code-workspace

Generate `vben-admin.code-workspace` file. Currently, it does not need to be executed manually and will be executed automatically when code is committed.

#### Usage

```bash
pnpm vsh code-workspace
```

#### Options

| Option          | Description                                               |
| --------------- | --------------------------------------------------------- |
| `--auto-commit` | Automatically commit during `git commit`, default `false` |
| `--spaces`      | Indentation format, default `2` spaces                    |

## turbo-run

Used to quickly execute scripts in the large repository and provide option-based interactive selection.

### Usage

```bash
pnpm turbo-run [command]
```

### turbo-run dev

Quickly execute the `dev` command and provide option-based interactive selection.
