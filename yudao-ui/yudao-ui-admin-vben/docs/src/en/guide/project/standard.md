# Standards

::: tip Contributing Code

- If you want to contribute code to the project, please ensure your code complies with the project's coding standards.
- If you are using `vscode`, you need to install the following plugins:
  - [ESLint](https://marketplace.visualstudio.com/items?itemName=dbaeumer.vscode-eslint) - Script code checking
  - [Prettier](https://marketplace.visualstudio.com/items?itemName=esbenp.prettier-vscode) - Code formatting
  - [Code Spell Checker](https://marketplace.visualstudio.com/items?itemName=streetsidesoftware.code-spell-checker) - Word syntax checking
  - [Stylelint](https://marketplace.visualstudio.com/items?itemName=stylelint.vscode-stylelint) - CSS formatting

:::

## Purpose

Students with basic engineering literacy always pay attention to coding standards, and code style checking (Code Linting, simply called Lint) is an important means to ensure the consistency of coding standards.

Following the corresponding coding standards has the following benefits:

- Lower bug error rate
- Efficient development efficiency
- Higher readability

## Tools

The project's configuration files are located in `internal/lint-configs`, where you can modify various lint configurations.

The project integrates the following code verification tools:

- [ESLint](https://eslint.org/) for JavaScript code checking
- [Stylelint](https://stylelint.io/) for CSS style checking
- [Prettier](https://prettier.io/) for code formatting
- [Commitlint](https://commitlint.js.org/) for checking the standard of git commit messages
- [Publint](https://publint.dev/) for checking the standard of npm packages
- [Cspell](https://cspell.org/) for checking spelling errors
- [lefthook](https://github.com/evilmartians/lefthook) for managing Git hooks, automatically running code checks and formatting before commits

## ESLint

ESLint is a code standard and error checking tool used to identify and report syntax errors in TypeScript code.

### Command

```bash
pnpm eslint .
```

### Configuration

The ESLint configuration file is `eslint.config.mjs`, with its core configuration located in the `internal/lint-configs/eslint-config` directory, which can be modified according to project needs.

## Stylelint

Stylelint is used to check the style of CSS within the project. Coupled with the editor's auto-fix feature, it can effectively unify the CSS style within the project.

### Command

```bash
pnpm stylelint "**/*.{vue,css,less.scss}"
```

### Configuration

The Stylelint configuration file is `stylelint.config.mjs`, with its core configuration located in the `internal/lint-configs/stylelint-config` directory, which can be modified according to project needs.

## Prettier

Prettier Can be used to unify project code style, consistent indentation, single and double quotes, trailing commas, and other styles.

### Command

```bash
pnpm prettier .
```

### Configuration

The Prettier configuration file is `.prettier.mjs`, with its core configuration located in the `internal/lint-configs/prettier-config` directory, which can be modified according to project needs.

## CommitLint

In a team, everyone's git commit messages can vary widely, making it difficult to ensure standardization without a mechanism. How can standardization be achieved? You might think of using git's hook mechanism to write shell scripts to implement this. Of course, this is possible, but actually, JavaScript has a great tool for implementing this template, which is commitlint (used for verifying the standard of git commit messages).

### Configuration

The CommitLint configuration file is `.commitlintrc.mjs`, with its core configuration located in the `internal/lint-configs/commitlint-config` directory, which can be modified according to project needs.

### Git Commit Standards

Refer to [Angular](https://github.com/conventional-changelog/conventional-changelog/tree/master/packages/conventional-changelog-angular)

- `feat` Add new features
- `fix` Fix problems/BUGs
- `style` Code style changes that do not affect the outcome
- `perf` Optimization/performance improvement
- `refactor` Refactoring
- `revert` Revert changes
- `test` Related to tests
- `docs` Documentation/comments
- `chore` Dependency updates/scaffold configuration modifications, etc.
- `workflow` Workflow improvements
- `ci` Continuous integration
- `types` Type modifications

### Disabling Git Commit Standard Checks

If you want to disable Git commit standard checks, there are two ways:

::: code-group

```bash [Temporary disable]
git commit -m 'feat: add home page' --no-verify
```

```bash [Permanent closed]
# Comment out the following code in .husky/commit-msg to disable
pnpm exec commitlint --edit "$1" # [!code --]
```

:::

## Publint

Publint is a tool for checking the standard of npm packages, which can check whether the package version conforms to the standard, whether it conforms to the standard ESM package specification, etc.

### Command

```bash
pnpm vsh publint
```

## Cspell

Cspell is a tool for checking spelling errors, which can check for spelling errors in the code, avoiding bugs caused by spelling errors.

### Command

```bash
pnpm cspell lint \"**/*.ts\"  \"**/README.md\" \".changeset/*.md\" --no-progress
```

### Configuration

The cspell configuration file is `cspell.json`, which can be modified according to project needs.

## Git Hook

Git hooks are generally combined with various lints to check code style during git commits. If the check fails, the commit will not proceed. Developers need to modify and resubmit.

### lefthook

One issue is that the check will verify all code, but we only want to check the code we are committing. This is where lefthook comes in.

The most effective solution is to perform Lint checks locally before committing. A common practice is to use lefthook to perform a Lint check before local submission.

The project defines corresponding hooks inside `lefthook.yml`:

- `pre-commit`: Runs before commit, used for code formatting and checking
  - `code-workspace`: Updates VSCode workspace configuration
  - `lint-md`: Formats Markdown files
  - `lint-vue`: Formats and checks Vue files
  - `lint-js`: Formats and checks JavaScript/TypeScript files
  - `lint-style`: Formats and checks style files
  - `lint-package`: Formats package.json
  - `lint-json`: Formats other JSON files

- `post-merge`: Runs after merge, used for automatic dependency installation
  - `install`: Runs `pnpm install` to install new dependencies

- `commit-msg`: Runs during commit, used for checking commit message format
  - `commitlint`: Uses commitlint to check commit messages

#### How to Disable lefthook

If you want to disable lefthook, there are two ways:

::: code-group

```bash [Temporary disable]
git commit -m 'feat: add home page' --no-verify
```

```bash [Permanent disable]
# Simply delete the lefthook.yml file
rm lefthook.yml
```

:::

#### How to Modify lefthook Configuration

If you want to modify lefthook's configuration, you can edit the `lefthook.yml` file. For example:

```yaml
pre-commit:
  parallel: true # Execute tasks in parallel
  jobs:
    - name: lint-js
      run: pnpm prettier --cache --ignore-unknown --write {staged_files}
      glob: '*.{js,jsx,ts,tsx}'
```

Where:

- `parallel`: Whether to execute tasks in parallel
- `jobs`: Defines the list of tasks to execute
- `name`: Task name
- `run`: Command to execute
- `glob`: File pattern to match
- `{staged_files}`: Represents the list of staged files
