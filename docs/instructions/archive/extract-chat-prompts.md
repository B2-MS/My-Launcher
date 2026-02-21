# Extract Chat Prompts

Prompt for extracting all user prompts from a Copilot session.

---

## Usage

At the end of a session, paste this into Copilot:

```
Review our entire conversation and APPEND to docs/prompts-used.md

Extract EVERY prompt/request I made - use my EXACT words, not summaries.

Format:
## Session: [Date] - [Topic] (vX.X.X)
1. "[exact prompt 1]"
2. "[exact prompt 2]"

Then UPDATE the Summary section counts at the top of the file.
```
