# ZenCrawler Goal
The goal of this task is to perform a web crawl on a URL string provided by the user. From the crawl, you will need to parse out all of the images on that web page and return a JSON array of strings that represent the URLs of all images on the page.

### How it works
- Install Maven 3.5 or higher and Java 8
- git clone
- Run following commands:
  - mvn clean test package jetty:run
- Enter localhost:8080 in your browser
- 
### Current Features
- Support crawling for subpages in the same domain
- Support multithreading for subpage crawling
- Check domain's robots.txt file to avoid banning

### Areas to improve
- Improve UI to make it user friendly
- Improve robots.txt - related logic to work on more websites
- Provide storage option for found images
- Provide customizable option to limit numbers or layers of subpage
- Group images by the type or the content
