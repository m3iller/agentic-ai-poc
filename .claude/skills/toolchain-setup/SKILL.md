---
name: toolchain-setup
description: Verify (and, with confirmation, install) this project's build toolchains — Java 21 + Maven for java/, .NET 10 SDK for dotnet/. Use before handing work to java-agent or dotnet-agent on a machine that hasn't run this project before, or whenever a build/test command fails with "command not found" / SDK-not-found errors. Trigger phrases: "set up the toolchain", "check the dev environment", "install the .NET SDK", "can this machine build the project".
---

This project is a parallel Java↔.NET migration (`java/` + `dotnet/`, see root `CLAUDE.md`). Each
stack needs its own SDK on the machine before its agent (`java-agent` / `dotnet-agent`) can do
anything real — this skill is the deterministic "can we build?" check that runs before that, so
neither agent wastes a turn hitting a missing-toolchain error.

## Process

1. **Detect the platform first** — installer choice and paths differ:
   - `uname -s` → `Darwin` (macOS), `Linux`, or check for WSL (`grep -qi microsoft /proc/version`
     on Linux indicates WSL, which affects whether Homebrew or a Linux package manager applies).
   - On macOS, note the chip (`uname -m`: `arm64` vs `x86_64`) — Homebrew lives at
     `/opt/homebrew` on Apple Silicon vs `/usr/local` on Intel; this matters if PATH doesn't
     already include it.

2. **Detect what's already present** — run version checks, don't assume, and don't stop at the
   first binary found on PATH:
   - **Java**: `java -version` (stderr, not stdout) and `mvn -version` (need Java 21+;
     `java/pom.xml`'s `<maven.compiler.release>`/`<java.version>` property pins the exact version
     if you need to confirm). Also check `echo $JAVA_HOME` and compare it against what `java` on
     PATH resolves to (`readlink -f "$(which java)"` on Linux, or `/usr/libexec/java_home -V` on
     macOS to list *all* installed JDKs) — a mismatch between `JAVA_HOME` and PATH is the single
     most common source of "wrong Java version" confusion, especially on machines with multiple
     JDKs from IDEs, sdkman, or prior Homebrew installs.
   - **.NET**: `dotnet --version` (reports the SDK used for the *current directory*, which can be
     pinned lower by a `global.json` — check for one with `find . -name global.json` before
     trusting this alone), `dotnet --list-sdks` (every SDK actually installed — need a `10.x`
     entry), and `dotnet --list-runtimes` (confirm an `Microsoft.AspNetCore.App 10.x` runtime is
     present, not just the SDK, since `dotnet-agent` will be running ASP.NET Core). Also check
     `echo $DOTNET_ROOT` if set.
   - Record exact version strings (e.g. `21.0.4`, `10.0.100`), not just "found" — a `9.x` SDK
     satisfying a naive `dotnet --version` check is a real failure mode here.

3. **If everything needed is present**, say so plainly with the versions found and stop — don't
   install or modify anything.

4. **If something is missing, confirm before installing.** Installing an SDK is a system change,
   not a project-file edit — always state what you're about to run and get a go-ahead first, even
   if the user already asked you to "set up" the toolchain in general terms (that's not the same
   as pre-authorizing a specific `brew install`/package-manager command).
   - **macOS, prefer Homebrew**: `brew install --cask dotnet-sdk` for .NET, `brew install
     openjdk@21 maven` for Java. `openjdk@21` is keg-only — after install it won't be on PATH or
     resolved by `/usr/libexec/java_home` until you either symlink it
     (`sudo ln -sfn $(brew --prefix)/opt/openjdk@21/libexec/openjdk.jdk
     /Library/Java/JavaVirtualMachines/openjdk-21.jdk`) or export
     `JAVA_HOME="$(brew --prefix)/opt/openjdk@21"` — mention this step, don't leave the install
     silently inert. Adjust formula/cask names if they've changed — search first with
     `brew search` if unclear rather than guessing.
   - **Linux**: prefer the distro's package manager if the user has one set up (`apt`, `dnf`) for
     Java (`openjdk-21-jdk`), but .NET's distro packages lag upstream — prefer Microsoft's install
     script (`dotnet-install.sh`) or documented apt feed for a reliably current `10.x` SDK. Ask
     which the user prefers rather than assuming.
   - If Homebrew isn't available or the user prefers otherwise, ask rather than picking an
     alternate installer unilaterally (manual download, sdkman, etc. all have different
     implications for PATH/version management).
   - **Never run an installer with sudo/system-wide changes without the explicit go-ahead for
     *that* command** — a prior "yes, set up the toolchain" does not cover, e.g., a `sudo ln`
     symlink step discovered mid-install; surface it and confirm separately.

5. **Re-verify after installing** — re-run the *same* version checks from step 2, don't assume
   the install succeeded from exit code alone. If a freshly installed binary isn't found:
   - Try `hash -r` (bash) to clear the shell's command path cache, or open a fresh shell/terminal
     tab — installers often update `/etc/paths.d/`, shell rc files, or `/etc/profile.d/` that only
     apply to new shells.
   - If still not found, check the install actually landed where PATH looks (e.g.
     `brew --prefix openjdk@21`, `ls /usr/local/share/dotnet`) — a successful package install with
     a PATH that doesn't include it looks identical to a failed install from the command's exit
     code alone.

6. **Report the concrete versions found/installed** (not just "it worked") — this is what
   `java-agent`/`dotnet-agent` and any teammate reading the conversation need to trust the
   environment is ready. Include: Java/Maven/.NET SDK versions, `JAVA_HOME`/`DOTNET_ROOT` if set,
   and whether a `global.json` or other pin is in play.

7. **Hand off**: once both toolchains needed for the task at hand are confirmed, proceed to the
   relevant agent (`java-agent` for `java/` work, `dotnet-agent` for `dotnet/` work) — this skill
   only gets the environment ready, it doesn't scaffold or write project code itself.

## Common failure modes worth checking explicitly

- **Multiple JDKs installed, wrong one on PATH** — e.g. a Java 17 install from an IDE shadows a
  newer Homebrew 21 install. `/usr/libexec/java_home -V` (macOS) lists every candidate; resolving
  this may mean setting `JAVA_HOME` rather than reinstalling anything.
- **`mvn -version` reports a different Java version than `java -version`** — Maven uses
  `JAVA_HOME` at launch, which can diverge from the `java` binary resolved via PATH. Report both.
- **`dotnet --version` looks fine but the project still won't build** — check for a `global.json`
  pinning an SDK version/rollForward policy that doesn't match what's installed; this overrides
  whatever the latest installed SDK is.
- **Corporate/managed machines** — MDM-managed macOS machines may block Homebrew cask installs
  requiring admin rights, or route through an internal package mirror. If a `brew install` fails
  with a permissions error, ask rather than retrying with `sudo`.
- **CI / non-interactive environments** — if this skill is invoked somewhere there's no user to
  confirm an install (e.g. inside an automated pipeline), don't install; report what's missing and
  stop, since the confirm-before-installing rule in step 4 still applies.

## Out of scope

- Scaffolding the actual `dotnet/` solution or `java/` project structure — that's
  `dotnet-agent`'s/`java-agent`'s job once the SDK is confirmed present.
- Managing SDK versions across multiple projects (e.g. `sdkman`, `asdf`, global vs. project-local
  pins) unless this project's `CLAUDE.md` says otherwise — keep it to "does this machine have
  what `java/` and `dotnet/` need."
- Installing IDE tooling, Docker, database engines, or anything beyond the two SDKs — a build
  toolchain check, not a full dev-environment bootstrap.
