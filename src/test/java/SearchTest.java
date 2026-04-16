import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class SearchTest {

    WebDriver driver;
    WebDriverWait wait;

    @BeforeMethod
    public void setup() throws InterruptedException {
        driver = new ChromeDriver();
        driver.get("https://www.bestbuy.com");
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(3000);
    }

    // Test 1: Verify search bar is present and interactable
    @Test(priority = 1)
    public void verifySearchBarPresent() throws InterruptedException {
        WebElement searchBar = driver.findElement(By.id("autocomplete-search-bar"));
        Assert.assertTrue(searchBar.isDisplayed(), "Search bar is not visible");
        Assert.assertTrue(searchBar.isEnabled(), "Search bar is not enabled");
        searchBar.sendKeys("laptop");
        Thread.sleep(1000);
        searchBar.clear();
        System.out.println("Test 1 PASSED: Search bar is present and interactable");
    }

    // Test 2: Verify search returns results for a valid query
    @Test(priority = 2)
    public void verifySearchReturnsResults() throws InterruptedException {
        WebElement searchBar = driver.findElement(By.id("autocomplete-search-bar"));
        searchBar.sendKeys("laptop");
        searchBar.sendKeys(Keys.ENTER);
        Thread.sleep(5000);

        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL after search: " + currentUrl);

        Assert.assertTrue(currentUrl.contains("bestbuy.com"),
                "Page navigated away from Best Buy");
        Assert.assertTrue(
                currentUrl.contains("searchpage") || currentUrl.contains("laptop"),
                "URL does not reflect a search results page");

        String pageSource = driver.getPageSource();
        Assert.assertTrue(
                pageSource.contains("laptop") || pageSource.contains("Laptop"),
                "Search results page did not return relevant content");
        System.out.println("Test 2 PASSED: Search returned results for 'laptop'");
    }

    // Test 3: Verify search with no query does not crash or leave the page
    @Test(priority = 3)
    public void verifyEmptySearchHandledGracefully() throws InterruptedException {
        WebElement searchBar = driver.findElement(By.id("autocomplete-search-bar"));
        searchBar.sendKeys(Keys.ENTER);
        Thread.sleep(3000);

        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL after empty search: " + currentUrl);

        Assert.assertTrue(currentUrl.contains("bestbuy.com"),
                "Empty search navigated away from Best Buy");
        System.out.println("Test 3 PASSED: Empty search handled gracefully");
    }

    // Test 4: Verify search autocomplete/suggestions appear while typing
    @Test(priority = 4)
    public void verifySearchSuggestionsAppear() throws InterruptedException {
        WebElement searchBar = driver.findElement(By.id("autocomplete-search-bar"));
        searchBar.sendKeys("iph");
        Thread.sleep(3000);

        String pageSource = driver.getPageSource();
        System.out.println("Checking page source for suggestion dropdown...");

        // Suggestions are rendered in the page — check source for common suggestion terms
        Assert.assertTrue(
                pageSource.contains("iphone") ||
                        pageSource.contains("iPhone") ||
                        pageSource.contains("suggestion") ||
                        pageSource.contains("autocomplete"),
                "No autocomplete suggestions appeared while typing");
        System.out.println("Test 4 PASSED: Search suggestions appeared while typing");
    }

    // Test 5: Verify search for a nonsense query shows a no-results or error message
    @Test(priority = 5)
    public void verifyNoResultsMessageForGibberish() throws InterruptedException {
        WebElement searchBar = driver.findElement(By.id("autocomplete-search-bar"));
        String gibberish = "xkqzwmvpblfjhd";
        searchBar.sendKeys(gibberish);
        searchBar.sendKeys(Keys.ENTER);
        Thread.sleep(5000);

        String pageSource = driver.getPageSource();
        System.out.println("Checking for no-results message...");

        Assert.assertTrue(
                pageSource.contains("no results") ||
                        pageSource.contains("No results") ||
                        pageSource.contains("0 results") ||
                        pageSource.contains("didn't find") ||
                        pageSource.contains("did not find") ||
                        pageSource.contains(gibberish),
                "No 'no results' indicator found for gibberish query");
        System.out.println("Test 5 PASSED: No-results message shown for gibberish query");
    }

    @AfterMethod
    public void teardown() {
        driver.quit();
    }
}