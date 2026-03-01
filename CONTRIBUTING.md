# Contributing to SL SE Jobs

First off, thank you for considering contributing! It's people like you that make this a useful tool for the entire Sri Lankan developer community.

### 🛠️ Development Setup
1. Fork the Project.
2. Create your Feature Branch (`git checkout -b feature/NewScraper`).
3. Commit your Changes (`git commit -m 'Add Scraper for [Site Name]'`).
4. Push to the Branch (`git push origin feature/NewScraper`).
5. Open a **Pull Request**.

### 🧪 Technical Requirements
- Use **Jsoup** for all HTML parsing.
- Avoid using heavy libraries like Selenium (we keep it fast and light for GitHub Actions).
- Ensure your scraper handles "No Jobs Found" gracefully without crashing the engine.

### 💡 Ideas for Contribution
* Adding scrapers for company-specific career pages (e.g., WSO2, LSEG, IFS).
* Improving the Markdown table layout.
* Adding a "Notification" system (Email/Discord) when new jobs are found.