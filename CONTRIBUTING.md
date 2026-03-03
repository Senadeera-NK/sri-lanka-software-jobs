# Contributing to SL SE Jobs

Thank you for helping centralize the Sri Lankan tech market! 🇱🇰

### 🏗️ Project Architecture
To keep the project maintainable, please follow our package structure:

* **`lk.jobs.scrapers`**: Create  a new class here for every new site. It *must* implement the `JobScraper` interface.
* **`lk.jobs.model`**: Contains the `Job` record. Do not modify this without an issue discussion.
* **`lk.jobs.engine`**: The core logic that merges and sorts data.

### 🛠️ Adding a New Scraper
1.  Check if the site has a public API (like ITPro.lk). If so, use `java.net.http.HttpClient`.
2.  If no API exists, use **Jsoup** to parse the HTML.
3.  **Frequency:**  Set a `Thread.sleep(2000)` between requests to avoid IP blocks.
4.  **Keyword Filter:** Ensure your scraper only collects Software Engineering, QA, and Data-related roles.

### 🧪 Technical Requirements
- **Java Version:** 21+ (we use Records and modern HTTP Client).
- **No Selenium:** We aim for light, headless scraping that runs in under 2 minutes.
- **Deduplication:** The engine handles this! Your scraper just needs to return a `List<Job>`.

### 🚀 Getting Started
1. Fork the repo.
2. `git checkout -b feat/add-[site-name]-scraper`
3. Add your class to `src/main/java/lk/jobs/scrapers/`.
4. Register your new scraper in `ScraperManager.java`.
5. Run `mvn test` to ensure no regressions.