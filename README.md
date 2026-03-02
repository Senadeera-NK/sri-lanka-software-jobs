# 🇱🇰 sl-software-engineering-jobs
🚀 Automated Software Engineering Job Tracker for Sri Lanka. Scrapes and categorizes Intern, Associate, and SE roles daily using Java (Jsoup) and GitHub Actions.

[![Daily Update](https://github.com/Senadeera-NK/sl-software-engineering-jobs/actions/workflows/scrape.yml/badge.svg)](https://github.com/Senadeera-NK/sl-software-engineering-jobs/actions)

## 📊 Current Job Openings
> 🟢 **Last Updated:** March 2, 2026 | **Total Jobs Found:** 3

| Title | Company | Level |  Source | Posted | Link |
| :--- | :--- | :--- |  :--- | :--- | :--- |
| Software Engineering Intern (Non-paid) | Panhinda Solutions | Intern | ITPro.lk | 14 hours ago | [View](https://itpro.lk/job/13174/software-engineering-intern-nonpaid-at-panhinda-solutions/) |
| QA Engineer (Manual + Automation) | Pasovit Technologies Private Limited | Junior/SE | ITPro.lk | 14 hours ago | [View](https://itpro.lk/job/13173/qa-engineer-manual-automation-at-pasovit-technologies-private-limited/) |
| Senior QA Engineer | BISTEC Global | Senior | ITPro.lk | 14 hours ago | [View](https://itpro.lk/job/13172/senior-qa-engineer-at-bistec-global/) |

## 🛠️ How it Works
1. **Engine:** A Java 21 console application using **Jsoup**.
2. **Sources:** Scrapes `TopJobs.lk`, `ITPro.lk`, and `Rooster.jobs`.
3. **Automation:** Runs every 12 hours via **GitHub Actions**.
4. **Storage:** Updates this `README.md` and a `jobs.json` file automatically.

<details>
<summary><b>📂 View Project Structure</b></summary>

```text
src/main/java/lk/jobs/
├── engine/           # Logic for sorting, cleaning, and README updates
├── model/            # Data models (Job Record)
├── scrapers/         # Individual site scrapers
└── utils/            # JSON and Date parsing utilities

## 🚀 Usage
If you want to run the scraper locally:
1. Clone the repo.
2. Ensure you have **JDK 21** and **Maven** installed.
3. Run: mvn clean compile exec:java -Dexec.mainClass="lk.jobs.engine.ScraperManager"

## 🤝 Contributing
Contributions are what make the open-source community such an amazing place to learn, inspire, and create.
Any contributions you make are **greatly appreciated**.

* **Found a bug?** Open an [Issue](https://github.com/Senadeera-NK/sl-software-engineering-jobs/issues).
* **Want to add a new site?** Check out our [Contributing Guidelines](CONTRIBUTING.md) to see how to implement a new scraper.
* **Missing a job?** Feel free to submit a Pull Request to manually update the table!