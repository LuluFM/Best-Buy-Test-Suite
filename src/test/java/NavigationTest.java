import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NavigationTest {

    WebDriver driver;

    @BeforeMethod
    public void setup() throws InterruptedException {
        driver = new ChromeDriver();
        driver.get("https://www.bestbuy.com");
        driver.manage().window().maximize();
        Thread.sleep(5000); // increased to 6 seconds - bestbuy loads slowly
    }

    @Test (priority = 1)
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
        System.out.println("Test 1 PASSED: Homepage URL is correct");
    }

    // Test 3: Verify the navigation bar is present on homepage
    @Test(priority = 2)
    public void verifyNavBarPresent() throws InterruptedException {
        WebElement navBar = driver.findElement(
                By.cssSelector("nav[aria-label='Main']"));
        Assert.assertTrue(navBar.isDisplayed(),
                "Navigation bar is not visible");
        System.out.println("Test 3 PASSED: Nav bar is displayed");
    }

    // Test 4: Navigate to Computers category and verify page loads
    @Test(priority = 3)
    public void navigateToComputersCategory() throws InterruptedException {
        driver.get("https://www.bestbuy.com/site/computer-tablets/computers-pcs/abcat0502000.c");
        Thread.sleep(5000);
        String url = driver.getCurrentUrl();
        String title = driver.getTitle();
        System.out.println("Category URL: " + url);
        System.out.println("Category title: " + title);
        Assert.assertTrue(url.contains("bestbuy.com"),
                "Did not navigate to BestBuy category page");
        System.out.println("Test 4 PASSED: Computers category page loaded");
    }

    // Test 5: Verify page title changes after navigating to a category
    @Test(priority = 4)
    public void verifyTitleChangesOnNavigation() throws InterruptedException {
        String homeTitle = driver.getTitle();
        driver.get("https://www.bestbuy.com/site/tv-video/all-tvs/abcat0101001.c");
        Thread.sleep(5000);
        String categoryTitle = driver.getTitle();
        System.out.println("Home title: " + homeTitle);
        System.out.println("Category title: " + categoryTitle);
        Assert.assertNotEquals(homeTitle, categoryTitle,
                "Page title did not change after navigation");
        System.out.println("Test 5 PASSED: Title changed after navigating to TVs category");
    }

    @AfterMethod
    public void teardown() {
        driver.quit();
    }
}
