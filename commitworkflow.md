Hier ist ein bewährter Multi-Agent-Workflow (Committer ↔ Reviewer), der genau diesen Kreislauf aus Entwickeln, Prüfen, Korrigieren und Mergen abbildet.
Am besten lässt sich das über GitHub Actions (oder GitLab CI) in Kombination mit einer Agenten-Orchestrierung (z. B. LangGraph, CrewAI oder direkt via GitHub API/CLI) umsetzen.

1. Der Ablauf im Überblick (Loop)

Show Code
Download diagram SVG

2. Die Rollen & System-Prompts der Agenten

Damit die Agenten sauber zusammenarbeiten, brauchen sie klar getrennte Aufgaben und Werkzeuge (Tools).

Agent 1: Der Committer (Developer Agent)

• Aufgabe: Code schreiben, Tests ausführen, Commits erstellen, Pull Requests (PR) öffnen und auf Review-Kommentare reagieren.
• Benötigte Tools: git (checkout, commit, push), gh (GitHub CLI zum Erstellen/Lesen von PRs), Dateisystem-Zugriff.
• System-Prompt (Kernanweisung):„Du bist ein Software-Entwickler. Erstelle für jede Aufgabe einen neuen Feature-Branch. Schreibe sauberen Code, committe ihn und erstelle einen Pull Request gegen main. Wenn ein Reviewer Kommentare im PR hinterlässt, lies diese genau, passe den Code an, markiere die Konversation als gelöst und pushe die Änderungen in denselben Branch.“

Agent 2: Der Reviewer (QA / Code-Review Agent)

• Aufgabe: Den PR-Diff auf Bugs, Sicherheit und Code-Qualität prüfen, Inline-Kommentare schreiben und am Ende freigeben (APPROVE) oder Änderungen anfordern (REQUEST_CHANGES).
• Benötigte Tools: gh pr diff, gh pr review, GitHub API (für zeilengenaue Code-Kommentare).
• System-Prompt (Kernanweisung):„Du bist ein strenger Senior Code-Reviewer. Prüfe den Pull Request auf Fehler, Logikprobleme und Sauberkeit. Wenn etwas geändert werden muss, schreibe präzise Kommentare direkt an die betroffenen Code-Zeilen und setze den Status auf REQUEST_CHANGES. Wenn der Code fehlerfrei ist, setze den Status auf APPROVE und merge den PR.“

3. Technische Umsetzung (Beispiel mit GitHub Actions)

Du kannst diesen Kreislauf komplett ereignisgesteuert über GitHub Actions automatisieren, sodass kein Server dauerhaft laufen muss:

Schritt A: Trigger für den Reviewer (.github/workflows/agent-reviewer.yml)

Wird ausgelöst, sobald der Committer einen PR öffnet oder neuen Code pusht (synchronize).
yaml
name: Agent Reviewer
on:
  pull_request:
    types: [opened, synchronize]

jobs:
  review:
    runs-on: ubuntu-latest
    # Verhindert, dass der Reviewer auf seine eigenen Aktionen reagiert
    if: github.event.pull_request.user.login == 'committer-bot'
    steps:
      - uses: actions/checkout@v4
      - name: Run Reviewer Agent
        env:
          GITHUB_TOKEN: ${{ secrets.REVIEWER_BOT_TOKEN }}
          LLM_API_KEY: ${{ secrets.LLM_API_KEY }}
        run: |
          # Skript ruft PR-Diff ab, lässt LLM prüfen und postet Review via GitHub API
          python scripts/run_reviewer.py --pr ${{ github.event.pull_request.number }}
Use code with caution.

Schritt B: Trigger für den Committer bei Feedback (.github/workflows/agent-fix.yml)

Wird ausgelöst, sobald der Reviewer das Review abschließt.
yaml
name: Agent Committer Fix
on:
  pull_request_review:
    types: [submitted]

jobs:
  fix_or_merge:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          ref: ${{ github.event.pull_request.head.ref }}

      # Fall 1: Änderungen angefordert -> Committer fixt den Code
      - name: Fix Code based on Review
        if: github.event.review.state == 'changes_requested'
        env:
          GITHUB_TOKEN: ${{ secrets.COMMITTER_BOT_TOKEN }}
          LLM_API_KEY: ${{ secrets.LLM_API_KEY }}
        run: |
          python scripts/run_committer_fix.py --pr ${{ github.event.pull_request.number }}
          git add .
          git commit -m "fix: address review comments"
          git push

      # Fall 2: Approved -> Automatischer Merge in main
      - name: Auto-Merge PR
        if: github.event.review.state == 'approved'
        env:
          GITHUB_TOKEN: ${{ secrets.REVIEWER_BOT_TOKEN }}
        run: |
          gh pr merge ${{ github.event.pull_request.number }} --squash --delete-branch
Use code with caution.

4. Wichtige Schutzmechanismen (Best Practices)

Damit der Workflow in der Praxis nicht außer Kontrolle gerät, solltest du drei Regeln einbauen:
1. Max-Loops begrenzen: Baue einen Zähler ein (z. B. maximal 3 Korrekturschleifen). Wenn die Agenten sich nach 3 Versuchen nicht einig sind, bricht der Workflow ab und benachrichtigt einen Menschen.
2. Zwei getrennte Bot-Accounts/Tokens nutzen: GitHub erlaubt es nicht, dass ein User (oder Bot) seinen eigenen Pull Request genehmigt (APPROVE). Nutze daher für den Committer und den Reviewer zwei unterschiedliche GitHub-Tokens (oder GitHub Apps).
3. CI-Tests als Pflicht-Gate: Lass vor dem Reviewer-Agenten automatische Unit-Tests und Linter laufen. Der Reviewer sollte erst starten, wenn die Basis-Tests grün sind.
Welches Framework oder Setup nutzt du aktuell (z. B. GitHub Actions, GitLab, n8n, LangGraph oder lokale CLI-Tools wie Claude Code / Aider)? Wenn du mir das sagst, kann ich dir den konkreten Code für die Agenten-Skripte schreiben.
