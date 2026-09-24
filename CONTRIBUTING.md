# Contributing to Memento In Abyss

Thank you for considering a contribution.
Please read before opening an issue or pull request.


## Ways to help

- Report bugs with the [bug report template](https://github.com/MIA-Development-Team/Made-In-Abyss/issues/new?template=bug_report.yml)
- Propose features with the [feature request template](https://github.com/MIA-Development-Team/Made-In-Abyss/issues/new?template=feature_request.yml)
- Send pull requests (code, assets, docs, translations)
- Help review PRs and reproduce reports


## Before you start

1. Search existing issues and PRs so we do not duplicate work.
2. For anything larger than a small fix, open an issue first and wait for a maintainer to agree on direction.
3. Target the branch that matches the Minecraft / loader line you are changing. Do not mix lines in one PR.


## Development setup

1. Install [IntelliJ IDEA](https://www.jetbrains.com/idea/) and [JDK 21](https://adoptium.net/temurin/releases/?os=any&version=21) (Temurin).
2. Clone the repository and open it as a Gradle project. Use the wrapper: `./gradlew` (Unix) or `gradlew.bat` (Windows). Do not require a system-wide Gradle install.
3. Let the IDE import the Gradle project and download sources.

Then, from the project root:

```bash
./gradlew runData      # generate assets / data (models, loot, tags, lang, worldgen)
./gradlew runClient    # launch the game
./gradlew build        # full build
./gradlew spotlessApply && ./gradlew spotlessCheck
./gradlew runGameTestServer
```

`src/generated/resources` is **output of datagen**. After changing registrations, recipes, tags, lang keys, or worldgen bootstrap, run `runData` and commit the generated diff together with the Java change. Do not hand-write or AI-invent files under that tree.


## Project conventions

### Code style

- Java 21, UTF-8.
- Format with Spotless + Google Java Format 1.36.1 (AOSP, reflow long strings). CI runs `spotlessCheck`.
- Match the file you are editing: existing names, package layout (`com.altnoir.mia`), and comment language.
- Prefer small, focused commits. Do not reformat unrelated files.


### Git / commits

Branch from the target line:

- `feature/<short-name>`
- `fix/<short-name>`
- `docs/<short-name>`

Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/):

```text
feat: add primo forest mycelium surface rule
fix: keep inverted wood recipes when merging primo set
docs: add contributing guide
ci: run spotless on 1.21.1-NeoForge
deps: bump moddev
refactor: move loot onto Reginth builders
```

Use the type that matches the change (`feat`, `fix`, `docs`, `ci`, `deps`, `refactor`, `test`, `chore`). Write the subject in English, imperative mood, and explain *why* in the body when the diff is not obvious.


### Assets and license

- Code is [MIT](LICENSE).
- Art (`src/main/resources/assets/mia/textures` and similar) is [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/). Do not contribute textures or models you cannot license that way.
- Do not commit secrets, personal Gradle homes, IDE run configs, or huge unrelated binaries.


### Localization

English source strings live in generated `en_us.json` (Reginth + `MiaLangData`). Translations are pulled via Crowdin into `src/main/resources/assets/mia/lang/`. Do not edit Crowdin-managed files by hand unless a maintainer asks you to. UI copy that is not auto-named goes through `MiaLangData` or `assets/mia/lang/default/`.


## Pull requests

1. Fork (or use a branch on this repo if you have write access).
2. Keep the PR scoped: one problem, one approach.
3. Fill in what changed, why, and how you tested it (`runClient`, `runData`, `runGameTestServer`, screenshots).
4. Make sure CI is green: **CI (Build)**, **CI (Spotless)**, **CI (Game Tests)**.
5. Expect review comments. A "request changes" is normal, not a rejection.

We may close PRs that:

- rewrite unrelated code or reformat the tree
- mix two Minecraft lines
- cannot be built or tested from the description


## Reviewer notes

Check that generated resources match the Java change, that new blocks/items are on Reginth, that creative-tab sections are updated when needed, and that worldgen or datapack edits were actually in-game tested.


## Questions

Use GitHub issues for public design discussion. Do not use issues for "how do I set up Gradle" unless the wrapper itself is broken.
