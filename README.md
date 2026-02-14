# BrowserStack Assignment

Round 2 Technical Assignment – Customer Engineer Role at BrowserStack


## Overview

This project is a Selenium-based automation solution developed in Java. The solution performs web scraping, translation, analysis, and cross-browser execution using BrowserStack.

The assignment demonstrates:

- Web scraping using Selenium
- JSON-LD parsing with HTML fallback
- API integration (RapidAPI translation)
- Parallel test execution with TestNG
- Cross-browser testing on BrowserStack (desktop and mobile)


## Functional Requirements Implemented

1. Navigate to the El País Opinion section.
2. Extract the first five unique articles.
3. Retrieve:
    - Article title
    - Article content
    - Main image URL
4. Download article images locally.
5. Translate article titles from Spanish to English using RapidAPI.
6. Perform word frequency analysis on translated titles.
7. Execute tests:
    - Locally
    - On BrowserStack
    - Across five parallel threads
    - On both desktop and mobile environments


## Technology Stack

- Java 17
- Selenium 4
- TestNG
- Maven
- BrowserStack Automate
- RapidAPI (Translation API)
- dotenv-java


## Project Structure

browserstack-assignment/

src/main/java/
Article.java
Scraper.java


src/test/java/
ScraperTest.java
SeleniumTest.java

testng.xml
pom.xml
.env
images/


## Environment Configuration

Create a `.env` file in the project root with the following values:

BROWSERSTACK_USERNAME=your_browserstack_username  
BROWSERSTACK_ACCESS_KEY=your_browserstack_access_key  
RAPIDAPI_KEY=your_rapidapi_key


## Running Locally

To validate functionality locally:

1. Configure SeleniumTest to use a local WebDriver.
2. Execute:

mvn clean test

This verifies scraping, translation, image handling, and analysis logic.


## BrowserStack Execution

Parallel execution is configured in testng.xml:

parallel="tests"  
thread-count="5"

The test suite runs across five environments in parallel.


## Cross-Browser Coverage

Desktop:
- Windows 11 – Chrome
- Windows 11 – Firefox
- macOS Ventura – Safari

Mobile:
- iPhone 14 – iOS Safari
- Samsung Galaxy S22 – Android Chrome


## Execute on BrowserStack

Run using Maven:

mvn clean test -DsuiteXmlFile=testng.xml

Or execute testng.xml directly from the IDE.


## Expected Output

Console output includes:

- Original Spanish titles
- Translated English titles
- Word repetition analysis
- Parallel execution summary

Example:

Total tests run: 5  
Passes: 5  
Failures: 0  
Skips: 0


## BrowserStack Dashboard

Results can be viewed under:

BrowserStack → Automate → Builds

Each build contains:
- Five parallel sessions
- Desktop and real device execution
- Video recordings
- Logs and session details


## Final Status

Local execution successful.  
BrowserStack execution successful.  
Five parallel threads executed.  
Desktop and mobile coverage achieved.  
All tests passed.


Candidate:  
Gauri Desai  
Round 2 Technical Assignment – Customer Engineer at BrowserStack
