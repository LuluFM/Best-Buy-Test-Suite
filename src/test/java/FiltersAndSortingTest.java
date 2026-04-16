import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FiltersAndSortingTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() throws InterruptedException {
        FirefoxOptions options = new FirefoxOptions();

        driver = new FirefoxDriver(options);
        driver.manage().window().fullscreen();
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        driver.get("https://www.bestbuy.com/");
        wait.until(pageLoaded());
        Thread.sleep(1000);

        // Close popup if it appears, but do not fail if it does not.
        closePopupIfPresent();

        WebElement search = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("autocomplete-search-bar"))
        );

        search.clear();
        search.sendKeys("DDR5 ram");
        Thread.sleep(1000);
        search.sendKeys(Keys.ENTER);

        wait.until(ExpectedConditions.urlContains("searchpage"));
        Thread.sleep(5000);

        closePopupIfPresent();
    }

    @Test(priority = 1)
    public void filterPanelPresentTest() throws InterruptedException {
        WebElement panel = findFilterPanel();
        Assert.assertNotNull(panel, "Filter panel was not found.");
        scroll(panel);
        highlight(panel);
        Thread.sleep(1500);
        Assert.assertTrue(panel.isDisplayed(), "Filter panel is not visible.");
    }

    @Test(priority = 2)
    public void resultsLoadAfterSearchTest() throws InterruptedException {
        int count = getResultItemCount();
        Assert.assertTrue(count > 0, "Results did not load after search.");
        System.out.println("Visible results found: " + count);

        WebElement firstResult = findFirstResultCard();
        if (firstResult != null) {
            scroll(firstResult);
            highlight(firstResult);
            Thread.sleep(1500);
        }
    }

    @Test(priority = 3)
    public void sortByPriceLowToHighTest() throws InterruptedException {
        boolean low = openAndSelect(4, "Price Low to High");
        Assert.assertTrue(low, "Price Low to High selection failed.");
        Thread.sleep(4000);
    }

    @Test(priority = 4)
    public void sortByPriceHighToLowTest() throws InterruptedException {
        boolean high = openAndSelect(5, "Price High to Low");
        Assert.assertTrue(high, "Price High to Low selection failed.");
        Thread.sleep(4000);
    }

    @Test(priority = 5)
    public void resultsCountDisplayedTest() throws InterruptedException {
        WebElement countElement = findResultsCountElement();
        Assert.assertNotNull(countElement, "Results count element was not found.");
        scroll(countElement);
        highlight(countElement);
        Thread.sleep(1500);

        String text = countElement.getText().trim();
        System.out.println("Results count text: " + text);
        Assert.assertFalse(text.isEmpty(), "Results count text is empty.");
    }

    private boolean openAndSelect(int index, String label) throws InterruptedException {
        WebElement dropdown = findSortDropdown();
        if (dropdown == null) {
            System.out.println("Dropdown not found");
            return false;
        }

        scroll(dropdown);
        highlight(dropdown);
        Thread.sleep(1500);

        dropdown.click();
        System.out.println("Opened dropdown for: " + label);

        Thread.sleep(2500);

        List<WebElement> options = getSortOptions();

        System.out.println("Visible sort options found: " + options.size());
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ": " + options.get(i).getText());
        }

        if (options.size() < index) {
            return false;
        }

        WebElement target = options.get(index - 1);

        scroll(target);
        highlight(target);
        Thread.sleep(2000);

        target.click();
        System.out.println("Clicked: " + label);

        waitForChange();
        return true;
    }

    private WebElement findSortDropdown() {
        List<By> locators = List.of(
                By.cssSelector("#sort-by [role='button']"),
                By.cssSelector("#sort-by [aria-label]"),
                By.xpath("//*[contains(text(),'Price High to Low')]/ancestor::*[@id='sort-by']//*[self::div or self::button][1]"),
                By.xpath("//*[@id='sort-by']//*[@data-testid='aria-expanded']")
        );

        for (By locator : locators) {
            try {
                for (WebElement el : driver.findElements(locator)) {
                    if (el.isDisplayed()) {
                        return el;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private List<WebElement> getSortOptions() {
        List<WebElement> list = new ArrayList<>();
        List<WebElement> all = driver.findElements(By.xpath("//*[@id='sort-by']//li"));

        for (WebElement el : all) {
            if (!el.isDisplayed()) continue;

            String t = el.getText().toLowerCase().trim();
            if (t.contains("best") ||
                    t.contains("price") ||
                    t.contains("rating") ||
                    t.contains("arrival")) {
                list.add(el);
            }
        }
        return list;
    }

    private WebElement findFilterPanel() {
        List<By> locators = List.of(
                By.cssSelector("aside"),
                By.xpath("//*[contains(text(),'Brand')]/ancestor::*[self::aside or self::div][1]"),
                By.xpath("//*[contains(text(),'Price')]/ancestor::*[self::aside or self::div][1]"),
                By.xpath("//*[contains(text(),'Shipping')]/ancestor::*[self::aside or self::div][1]"),
                By.xpath("//*[contains(text(),'Condition')]/ancestor::*[self::aside or self::div][1]")
        );

        for (By locator : locators) {
            try {
                for (WebElement el : driver.findElements(locator)) {
                    if (el.isDisplayed() && el.getText().trim().length() > 0) {
                        return el;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private int getResultItemCount() {
        List<By> locators = List.of(
                By.cssSelector(".sku-item"),
                By.cssSelector("li.sku-item"),
                By.cssSelector("h4.sku-title"),
                By.cssSelector("a[href*='/site/']")
        );

        for (By locator : locators) {
            try {
                List<WebElement> items = driver.findElements(locator);
                int visibleCount = 0;

                for (WebElement item : items) {
                    if (item.isDisplayed()) {
                        visibleCount++;
                    }
                }

                if (visibleCount > 0) {
                    return visibleCount;
                }
            } catch (Exception ignored) {}
        }
        return 0;
    }

    private WebElement findFirstResultCard() {
        List<By> locators = List.of(
                By.cssSelector(".sku-item"),
                By.cssSelector("li.sku-item"),
                By.cssSelector("h4.sku-title"),
                By.cssSelector("a[href*='/site/']")
        );

        for (By locator : locators) {
            try {
                for (WebElement el : driver.findElements(locator)) {
                    if (el.isDisplayed()) {
                        return el;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private WebElement findResultsCountElement() {
        List<By> locators = List.of(
                By.xpath("//*[contains(text(),'results')]"),
                By.xpath("//*[contains(text(),'result')]"),
                By.cssSelector("span[class*='result']"),
                By.cssSelector("div[class*='result']")
        );

        Pattern digits = Pattern.compile(".*\\d+.*");

        for (By locator : locators) {
            try {
                for (WebElement el : driver.findElements(locator)) {
                    if (!el.isDisplayed()) continue;

                    String text = el.getText().trim().toLowerCase();
                    if ((text.contains("result")) && digits.matcher(text).matches()) {
                        return el;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void closePopupIfPresent() {
        List<By> popupClosers = List.of(
                By.cssSelector("[aria-label='Close']"),
                By.cssSelector(".c-close-icon"),
                By.cssSelector("button[aria-label='Close']")
        );

        for (By locator : popupClosers) {
            try {
                List<WebElement> buttons = driver.findElements(locator);
                for (WebElement button : buttons) {
                    if (button.isDisplayed()) {
                        button.click();
                        Thread.sleep(1000);
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private void waitForChange() {
        try {
            Thread.sleep(2000);
        } catch (Exception ignored) {}
    }

    private void scroll(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", el);
    }

    private void highlight(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.border='4px solid red';", el);
    }

    private ExpectedCondition<Boolean> pageLoaded() {
        return d -> ((JavascriptExecutor) d)
                .executeScript("return document.readyState")
                .equals("complete");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
