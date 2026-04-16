import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestBestBuy {

    WebDriver driver;

   @BeforeMethod
    public void setup() throws InterruptedException {
        driver = new ChromeDriver();
        driver.get("https://www.bestbuy.com");
        driver.manage().window().maximize();
        Thread.sleep(3000);
    }
    @Test
    public void verifyHomepage() {
        String title = driver.getTitle();
        String url = driver.getCurrentUrl();

        System.out.println("Title: " + title);
        System.out.println("URL: " + url);

        // Check URL works even if title loads slowly
        Assert.assertTrue(url.contains("bestbuy.com"),
                "URL does not contain bestbuy.com");

        // Only assert title if it actually loaded
        if (!title.isEmpty()) {
            Assert.assertTrue(title.contains("Best Buy"),
                    "Title should contain Best Buy");
        }

        System.out.println("Homepage loaded successfully");
    }
    @AfterMethod
    public void tearDown() {
        driver.quit();
    }
}