import io.github.cdimascio.dotenv.Dotenv;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.MutableCapabilities;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.net.URL;
import java.util.HashMap;

public class SeleniumTest {

    protected WebDriver driver;

    @Parameters({"browserName", "browserVersion", "os", "osVersion", "deviceName"})
    @BeforeMethod(alwaysRun = true)
    public void setUp(String browserName,
                      @Optional String browserVersion,
                      @Optional String os,
                      @Optional String osVersion,
                      @Optional String deviceName) throws Exception {

        MutableCapabilities capabilities = new MutableCapabilities();

        HashMap<String, Object> bstackOptions = new HashMap<>();

        if (deviceName != null) {
            // MOBILE CONFIG
            bstackOptions.put("deviceName", deviceName);
            bstackOptions.put("osVersion", osVersion);
            bstackOptions.put("realMobile", true);
            capabilities.setCapability("browserName", browserName);
        } else {
            // DESKTOP CONFIG
            capabilities.setCapability("browserName", browserName);
            capabilities.setCapability("browserVersion", browserVersion);
            bstackOptions.put("os", os);
            bstackOptions.put("osVersion", osVersion);
        }

        bstackOptions.put("buildName", "ElPais Build");
        bstackOptions.put("sessionName", browserName + " Scraper");

        capabilities.setCapability("bstack:options", bstackOptions);

        Dotenv dotenv = Dotenv.load();
        String username = dotenv.get("BROWSERSTACK_USERNAME");
        String accessKey = dotenv.get("BROWSERSTACK_ACCESS_KEY");

        driver = new RemoteWebDriver(
                new URL("https://" + username + ":" + accessKey + "@hub-cloud.browserstack.com/wd/hub"),
                capabilities
        );
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
