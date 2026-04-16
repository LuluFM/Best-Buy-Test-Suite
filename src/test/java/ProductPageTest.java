import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductPageTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() throws InterruptedException {
        FirefoxOptions options = new FirefoxOptions();

        driver = new FirefoxDriver(options);

        driver.manage().window().fullscreen();
        driver.manage().window().maximize();
        driver.manage().window().setSize(new Dimension(1920, 1080));

        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        Thread.sleep(1000);
    }

    // Step 1: Search for DDR5 RAM and open the first valid product once
    @Test(priority = 1)
    public void openDDR5ProductPage() throws InterruptedException {
        driver.get("https://www.bestbuy.com/");
        wait.until(pageLoaded());

        WebElement searchBox = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("autocomplete-search-bar"))
        );

        System.out.println("Typing: DDR5 ram");
        searchBox.sendKeys("DDR5 ram");
        Thread.sleep(500);
        searchBox.sendKeys(Keys.ENTER);

        wait.until(ExpectedConditions.urlContains("searchpage.jsp"));
        wait.until(pageLoaded());
        Thread.sleep(4000);

        WebElement productLink = findFirstDDR5Link();
        Assert.assertNotNull(productLink, "Could not find a valid DDR5 product link.");

        scrollIntoView(productLink);
        highlightElement(productLink);
        Thread.sleep(1200);

        String text = getText(productLink);
        String href = productLink.getAttribute("href");

        System.out.println("Clicking product: " + text);
        System.out.println("URL: " + href);

        safeClick(productLink);

        wait.until(d -> !driver.getCurrentUrl().contains("searchpage.jsp"));
        wait.until(pageLoaded());
        Thread.sleep(4000);

        System.out.println("Product page opened successfully.");
    }

    // Step 2: Product title visible
    @Test(priority = 2, dependsOnMethods = "openDDR5ProductPage")
    public void testProductTitleVisible() throws InterruptedException {
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assert.assertTrue(title.isDisplayed(), "Product title is not visible.");

        scrollIntoView(title);
        highlightElement(title);

        System.out.println("Title found: " + title.getText());
        Thread.sleep(1500);
    }

    // Step 3: Price displayed
    @Test(priority = 3, dependsOnMethods = "openDDR5ProductPage")
    public void testPriceDisplayed() throws InterruptedException {
        WebElement priceElement = findPriceElementByScrolling();
        Assert.assertNotNull(priceElement, "Price is not displayed.");
        Assert.assertTrue(priceElement.isDisplayed(), "Price is not visible.");

        scrollIntoView(priceElement);
        highlightElement(priceElement);

        String priceText = getText(priceElement);
        if (!looksLikePrice(priceText)) {
            priceText = detectPriceText();
        }

        Assert.assertTrue(
                looksLikePrice(priceText),
                "Detected price text is not valid. Found: " + priceText
        );

        System.out.println("Price found: " + priceText);
        Thread.sleep(1500);
    }

    // Step 4: Product image loads
    @Test(priority = 4, dependsOnMethods = "openDDR5ProductPage")
    public void testProductImageLoads() throws InterruptedException {
        WebElement image = findElementByScrolling(List.of(
                By.cssSelector("img.primary-image"),
                By.cssSelector("img[alt]"),
                By.xpath("//img")
        ), 10);

        Assert.assertNotNull(image, "Product image not found.");
        Assert.assertTrue(image.isDisplayed(), "Product image is not visible.");

        scrollIntoView(image);
        highlightElement(image);

        Boolean imageLoaded = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].complete && arguments[0].naturalWidth > 0;", image
        );

        Assert.assertTrue(imageLoaded, "Product image did not load properly.");
        System.out.println("Product image loaded.");
        Thread.sleep(1500);
    }

    // Step 5: Add to Cart button exists
    @Test(priority = 5, dependsOnMethods = "openDDR5ProductPage")
    public void testAddToCartButtonExists() throws InterruptedException {
        WebElement addToCart = findElementByScrolling(List.of(
                By.xpath("//button[contains(.,'Add to Cart')]"),
                By.xpath("//button[contains(.,'Add to cart')]"),
                By.xpath("//*[self::button or self::a][contains(.,'Add to Cart')]"),
                By.xpath("//*[self::button or self::a][contains(.,'Add to cart')]")
        ), 12);

        Assert.assertNotNull(addToCart, "Add to Cart button not found.");
        Assert.assertTrue(addToCart.isDisplayed(), "Add to Cart button is not visible.");

        scrollIntoView(addToCart);
        highlightElement(addToCart);

        System.out.println("Add to Cart button found.");
        Thread.sleep(1500);
    }

    // Step 6: Product description visible
    @Test(priority = 6, dependsOnMethods = "openDDR5ProductPage")
    public void testSpecificationsVisible() throws InterruptedException {
        WebElement specifications = findSpecificationsSection();

        Assert.assertNotNull(specifications, "Specifications section not found.");
        Assert.assertTrue(specifications.isDisplayed(), "Specifications section is not visible.");

        scrollIntoView(specifications);
        highlightElement(specifications);

        System.out.println("Specifications section found.");
        Thread.sleep(1500);
    }

    private WebElement findFirstDDR5Link() {
        WebElement main = driver.findElement(By.tagName("main"));
        List<WebElement> links = main.findElements(By.partialLinkText("DDR5"));

        System.out.println("Found DDR5 links: " + links.size());

        for (WebElement link : links) {
            try {
                if (!link.isDisplayed()) continue;

                String href = link.getAttribute("href");
                String text = getText(link).toLowerCase();

                if (href == null || href.isBlank()) continue;

                if (text.contains("category")
                        || text.contains("search")
                        || text.contains("results")
                        || text.length() < 10) {
                    continue;
                }

                System.out.println("Using: " + text);
                return link;

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private WebElement findPriceElementByScrolling() throws InterruptedException {
        for (int i = 0; i < 12; i++) {
            WebElement el = findPriceElement();
            if (el != null) {
                return el;
            }

            System.out.println("Price not found yet. Scrolling... attempt " + (i + 1));
            scrollPageDown(500);
            Thread.sleep(1500);
        }

        return findPriceElement();
    }

    private WebElement findPriceElement() {
        List<By> priceLocators = List.of(
                By.cssSelector("[data-testid='customer-price']"),
                By.cssSelector(".priceView-hero-price"),
                By.cssSelector(".priceView-customer-price"),
                By.cssSelector("[class*='priceView']"),
                By.xpath("//*[contains(text(),'See price in cart')]"),
                By.xpath("//*[contains(text(),'$')]")
        );

        for (By locator : priceLocators) {
            try {
                List<WebElement> elements = driver.findElements(locator);
                for (WebElement el : elements) {
                    if (!el.isDisplayed()) continue;

                    String text = getText(el);
                    if (looksLikePrice(text)) {
                        return el;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String detectPriceText() {
        try {
            String bodyText = driver.findElement(By.tagName("body")).getText();
            String pageMatch = extractPriceFromText(bodyText);
            if (pageMatch != null) {
                return pageMatch;
            }
        } catch (Exception ignored) {
        }

        try {
            Object raw = ((JavascriptExecutor) driver).executeScript(
                    "return document.body ? (document.body.innerText || document.body.textContent || '') : '';"
            );
            if (raw != null) {
                String jsText = raw.toString();
                String pageMatch = extractPriceFromText(jsText);
                if (pageMatch != null) {
                    return pageMatch;
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private String extractPriceFromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String lower = text.toLowerCase();
        if (lower.contains("see price in cart")) {
            return "See price in cart";
        }

        Pattern pricePattern = Pattern.compile("\\$\\s?\\d[\\d,]*(?:\\.\\d{2})?");
        Matcher matcher = pricePattern.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }

        return null;
    }

    private boolean looksLikePrice(String text) {
        if (text == null) return false;

        String cleaned = text.trim().toLowerCase();
        return cleaned.contains("see price in cart")
                || cleaned.matches(".*\\$\\s?\\d+[\\d,]*(\\.\\d{2})?.*");
    }

    private WebElement findSpecificationsSection() throws InterruptedException {
        List<By> locators = List.of(
                By.xpath("//h5[normalize-space()='Specifications']"),
                By.xpath("//*[normalize-space()='Specifications']"),
                By.xpath("//h5[contains(.,'Specifications')]"),
                By.xpath("//*[contains(text(),'Specifications')]")
        );

        for (By locator : locators) {
            try {
                List<WebElement> elements = driver.findElements(locator);
                for (WebElement element : elements) {
                    if (element.isDisplayed()) {
                        return element;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        for (int i = 0; i < 15; i++) {
            scrollPageDown(450);
            Thread.sleep(1200);

            for (By locator : locators) {
                try {
                    List<WebElement> elements = driver.findElements(locator);
                    for (WebElement element : elements) {
                        if (element.isDisplayed()) {
                            return element;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return null;
    }

    private WebElement findElementByScrolling(List<By> locators, int maxScrolls) throws InterruptedException {
        WebElement current = findVisibleElement(locators);
        if (current != null) {
            scrollIntoView(current);
            Thread.sleep(800);
            return current;
        }

        for (int i = 0; i < maxScrolls; i++) {
            scrollPageDown(500);
            Thread.sleep(1200);

            current = findVisibleElement(locators);
            if (current != null) {
                scrollIntoView(current);
                Thread.sleep(800);
                return current;
            }
        }

        return null;
    }

    private WebElement findVisibleElement(List<By> locators) {
        for (By locator : locators) {
            try {
                List<WebElement> elements = driver.findElements(locator);
                for (WebElement element : elements) {
                    if (element.isDisplayed()) {
                        return element;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String getText(WebElement element) {
        try {
            Object value = ((JavascriptExecutor) driver).executeScript(
                    "return arguments[0].innerText || arguments[0].textContent || '';",
                    element
            );
            return value == null ? "" : value.toString().trim();
        } catch (Exception e) {
            try {
                return element.getText();
            } catch (Exception ignored) {
                return "";
            }
        }
    }

    private void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", element
        );
    }

    private void scrollPageDown(int pixels) {
        ((JavascriptExecutor) driver).executeScript(
                "window.scrollBy(0, arguments[0]);", pixels
        );
    }

    private void safeClick(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private void highlightElement(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].style.outline='4px solid red';" +
                            "arguments[0].style.backgroundColor='rgba(255,255,0,0.15)';" +
                            "arguments[0].style.border='3px solid red';",
                    element
            );
        } catch (Exception ignored) {
        }
    }

    private ExpectedCondition<Boolean> pageLoaded() {
        return wd -> "complete".equals(
                ((JavascriptExecutor) wd).executeScript("return document.readyState")
        );
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}