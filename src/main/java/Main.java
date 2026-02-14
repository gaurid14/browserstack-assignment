import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

public class Main {
    public static void main(String[] args){
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        Scraper scraper = new Scraper(driver);
        List<Article> articles = scraper.fetchFirstFiveArticles();

        for (Article a : articles) {
            System.out.println("Title in Spanish: " + a.getTitle());
            System.out.println("Content in Spanish: " + a.getContent());
            System.out.println("Img url: " + a.getImageUrl());
        }

        scraper.analyzeTranslatedTitles(articles);

        driver.quit();

    }
}
