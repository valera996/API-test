# Project rules for Codex

Work conservatively in this repository.

## Default behavior

- First inspect and understand the existing code.
- Do not modify files unless the user explicitly asks you to make changes.
- If the user asks only to explain, investigate, review, debug, or analyze, do not edit anything.
- Before changing code, briefly state which files you intend to modify and why.
- Make the smallest possible change required for the task.
- Do not refactor unrelated code.
- Do not delete existing code unless the user explicitly asks for deletion.
- Do not rename or move files unless explicitly requested.
- Do not create new files unless necessary for the requested task.

## Git safety

- Never run `git reset --hard`.
- Never run `git clean`.
- Never run `git checkout -- .`.
- Never run `git restore .` or restore unrelated files.
- Never amend, rewrite, squash, rebase, or delete existing commits unless explicitly requested.
- Never force-push.
- Never switch branches unless explicitly requested.
- Do not commit or push changes unless explicitly requested.
- Before modifying files, run `git status`.
- After modifications, run `git diff` and summarize exactly what changed.

## Project safety

- Do not change dependencies, build configuration, project settings, CI configuration, credentials, secrets, tokens, cookies, databases, or environment files unless explicitly requested.
- Do not execute destructive commands.
- Do not run scripts that perform external actions unless explicitly requested.
- Do not make broad automated changes across the repository.
- Preserve existing behavior unless the requested task requires changing it.

## When unsure

- Stop and ask the user before making a potentially destructive or broad change.
- Prefer explanation over modification when the request is ambiguous.