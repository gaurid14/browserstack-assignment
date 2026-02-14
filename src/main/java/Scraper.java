import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;

public class Scraper {

    Dotenv dotenv = Dotenv.load();
    String apiKey = dotenv.get("RAPIDAPI_KEY");

    private static final ObjectMapper MAPPER = new ObjectMapper();
    // WebDriver object to control the browser
    private WebDriver driver;

    // Explicit wait object to wait for elements to load
    private WebDriverWait wait;

    public Scraper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public List<Article> fetchFirstFiveArticles() {

        List<Article> articleList = new ArrayList<>();

        driver.get("https://elpais.com/opinion/");

        // Wait unitil page title contains opinion to ensure page is loaded
        wait.until(ExpectedConditions.titleContains("Opinión"));

        // Wait to ensure articles is loaded
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("article")));

        closeCookiePopup();

        List<String> articleLinks = collectFirstFiveLinks();

        createImagesFolder();

        for (int i = 0; i < articleLinks.size(); i++) {

            String link = articleLinks.get(i);
            driver.get(link);

            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

                Article article = extractArticleFromJsonLd(i + 1);

                // If json doesn't work, try normal HTML method
                if (article == null) {
                    article = extractArticleFromHtml(i + 1);
                }

                if (article != null) {
                    articleList.add(article);
                }

            } catch (Exception e) {
                System.out.println("Failed to process: " + link);
            }
        }

        return articleList;
    }

    // Close cookie popup
    private void closeCookiePopup() {
        try {
            WebElement acceptBtn = driver.findElement(
                    By.xpath("//button[contains(text(),'Aceptar') or contains(text(),'Accept')]"));
            acceptBtn.click();
        } catch (Exception e) {
            System.out.println("Failed to close popup");
        }
    }

    // Collect first five unique articles links
    private List<String> collectFirstFiveLinks() {

        List<WebElement> articles = driver.findElements(By.tagName("article"));

        Set<String> links = new LinkedHashSet<>();

        for (WebElement article : articles) {
            try {
                // Headline link
                WebElement linkElement = article.findElement(By.cssSelector("h2 a"));
                String link = linkElement.getAttribute("href");

                if (link != null) links.add(link);

                if (links.size() == 5) break;

            } catch (Exception e) {
                System.out.println("Failed to fetch links");
            }
        }

        return new ArrayList<>(links);
    }

    private void createImagesFolder() {
        try {
            Path imagesDir = Paths.get("images");
            if (!Files.exists(imagesDir)) {
                Files.createDirectory(imagesDir);
            }
        } catch (Exception e) {
            System.out.println("Failed to create filder");
        }
    }

    // Json extraction
    private Article extractArticleFromJsonLd(int index) {
        try {
            List<WebElement> scripts = driver.findElements(
                    By.xpath("//script[@type='application/ld+json']"));

            for (WebElement script : scripts) {
                String jsonRaw = script.getAttribute("innerHTML");

                // Fast check before parsing heavy JSON
                if (!jsonRaw.contains("articleBody")) continue;

                // Parse the whole thing into a Node tree
                JsonNode root = MAPPER.readTree(jsonRaw);

                // Accessing fields safely via Jackson's pathing
                String title = root.path("headline").asText(null);
                String content = root.path("articleBody").asText(null);
                String image = root.path("image").path("url").asText(
                        root.path("image").asText(null) // Handles both Object or String types
                );

                if (title != null && content != null) {
                    if (image != null) {
                        downloadImage(image, "article_" + index + ".jpg");
                    }
                    return new Article(title, content, image);
                }
            }
        } catch (Exception e) {
            // A human dev logs the specific failure for debugging
            System.err.println("JSON-LD parsing failed: " + e.getMessage());
        }
        return null;
    }

    // JSON value extractor
    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern);

            if (start == -1) return null;

            start = json.indexOf(":", start) + 1;
            int end = json.indexOf("\",", start);

            if (end == -1) {
                end = json.indexOf("\"}", start);
            }

            return json.substring(start, end)
                    .replace("\"", "")
                    .trim();

        } catch (Exception e) {
            return null;
        }
    }

    // Using html
    private Article extractArticleFromHtml(int index) {
        try {
            String title = driver.findElement(By.tagName("h1")).getText().trim();

            StringBuilder content = new StringBuilder();

            // Get paragraphs inside main section
            List<WebElement> paragraphs =
                    driver.findElements(By.xpath("//main//p"));

            for (WebElement p : paragraphs) {
                content.append(p.getText().trim()).append("\n");
            }

            String imageUrl = null;

            List<WebElement> images =
                    driver.findElements(By.xpath("//main//img"));

            for (WebElement img : images) {
                String src = img.getAttribute("src");

                if (src != null && src.contains("imagenes.elpais.com")) {
                    imageUrl = src;
                    downloadImage(imageUrl, "article_" + index + ".jpg");
                    break;
                }
            }

            return new Article(title, content.toString(), imageUrl);

        } catch (Exception e) {
            return null;
        }
    }

    // Download images locally
    private void downloadImage(String imageUrl, String fileName) {

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            // Set headers to avoid blocking and pretend being browser
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Referer", "https://elpais.com/");
            connection.connect();

            InputStream in = connection.getInputStream();
            Files.copy(in, Paths.get("images", fileName));
            in.close();
            connection.disconnect();

        } catch (Exception e) {
            System.out.println("Image download failed: " + imageUrl);
        }
    }

    public String translateToEnglish(String text) {

        try {
            URL url = new URL(
                    "https://rapid-translate-multi-traduction.p.rapidapi.com/t"
            );

            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty(
                    "x-rapidapi-host",
                    "rapid-translate-multi-traduction.p.rapidapi.com"
            );
            connection.setRequestProperty(
                    "x-rapidapi-key",
                    apiKey
            );

            connection.setDoOutput(true);

            String jsonBody =
                    "{"
                            + "\"from\":\"es\","
                            + "\"to\":\"en\","
                            + "\"q\":[\"" + text.replace("\"","\\\"") + "\"]"
                            + "}";

            connection.getOutputStream().write(jsonBody.getBytes());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode response =
                    mapper.readTree(connection.getInputStream());

            String translated =
                    response.get(0).asText();

            connection.disconnect();

            return translated;

        } catch (Exception e) {
            System.out.println("Translation failed: " + e.getMessage());
            return text;
        }
    }

    public void analyzeTranslatedTitles(List<Article> articles) {

        List<String> translatedTitles = new ArrayList<>();

        // Translate and print
        for (Article article : articles) {

            String translated = translateToEnglish(article.getTitle());

            translatedTitles.add(translated);

            System.out.println("Original in Spanish: " + article.getTitle());
            System.out.println("Translated in English: " + translated);
        }

        Map<String, Integer> wordCount = new HashMap<>();

        for (String title : translatedTitles) {
            String cleaned = title
                    .toLowerCase()
                    .replaceAll("[^a-zA-Z ]", "");

            String[] words = cleaned.split("\\s+");

            for (String word : words) {

                if (word.length() < 3) continue;

                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }

        System.out.println("\nRepeated words >2 times:");

        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (entry.getValue() > 2) {
                System.out.println(entry.getKey() + " → " + entry.getValue());
            }
        }
    }


}
