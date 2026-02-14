import org.testng.annotations.Test;
import java.util.List;

public class ScraperTest extends SeleniumTest {

    @Test
    public void runScraperTest() {

        Scraper scraper = new Scraper(driver);

        List<Article> articles = scraper.fetchFirstFiveArticles();

        scraper.analyzeTranslatedTitles(articles);
    }
}
