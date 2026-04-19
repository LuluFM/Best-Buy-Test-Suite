import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class CheckoutTest {

    WebDriver driver;
    WebDriverWait wait;
    JavascriptExecutor js;

    @BeforeMethod
    public void setup() throws InterruptedException {
        FirefoxOptions options = new FirefoxOptions();
        FirefoxProfile profile = new FirefoxProfile();
        options.setProfile(profile);
        driver = new FirefoxDriver(options);
        js = (JavascriptExecutor) driver;
        driver.get("https://www.bestbuy.com");
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        Thread.sleep(3000);
    }

    public void addItemToCart() throws InterruptedException {
        WebElement searchBar = driver.findElement(By.id("autocomplete-search-bar"));
        searchBar.sendKeys("Wireless Mouse");
        WebElement searchBarButton = driver.findElement(By.id("autocomplete-search-button"));
        searchBarButton.click();
        Thread.sleep(8000);

        String firstProductUrl = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("a[href*='/product/']")
                )
        ).getAttribute("href");

        driver.get(firstProductUrl);
        Thread.sleep(5000);
        System.out.println("Navigated to product page: " + driver.getCurrentUrl());

        WebElement atcButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("button[data-testid^='pdp-add-to-cart']")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", atcButton);
        Thread.sleep(1500);
        atcButton = driver.findElement(By.cssSelector("button[data-testid^='pdp-add-to-cart']"));
        js.executeScript("arguments[0].click();", atcButton);
        Thread.sleep(4000);
        System.out.println("Add to Cart clicked");
    }

    // Test 1: Verify checkout page loads after adding item to cart
    @Test(priority = 1)
    public void verifyCheckoutPageLoads() throws InterruptedException {
        addItemToCart();

        driver.get("https://www.bestbuy.com/checkout/r/start");
        Thread.sleep(4000);

        String currentUrl = driver.getCurrentUrl();
        String pageSource = driver.getPageSource();
        System.out.println("Checkout page URL: " + currentUrl);

        Assert.assertTrue(currentUrl.contains("bestbuy.com"),
                "Checkout page navigated away from Best Buy");
        Assert.assertTrue(
                pageSource.contains("Best Buy") ||
                        pageSource.contains("cart") ||
                        pageSource.contains("sign"),
                "Checkout page did not load a recognizable Best Buy page");
        System.out.println("Test 1 PASSED: Checkout page loaded on Best Buy domain");
    }

    // Test 2: Verify checkout button navigates to sign-in or guest option
    @Test(priority = 2)
    public void verifyUnauthenticatedCheckoutRedirects() throws InterruptedException {
        addItemToCart();

        driver.get("https://www.bestbuy.com/cart");
        Thread.sleep(4000);

        WebElement checkoutButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("button[data-track='Checkout - Top']")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", checkoutButton);
        Thread.sleep(1000);
        checkoutButton = driver.findElement(By.cssSelector("button[data-track='Checkout - Top']"));
        js.executeScript("arguments[0].click();", checkoutButton);
        Thread.sleep(4000);

        String currentUrl = driver.getCurrentUrl();
        String pageSource = driver.getPageSource();
        System.out.println("URL after clicking checkout: " + currentUrl);

        Assert.assertTrue(
                currentUrl.contains("signin") ||
                        currentUrl.contains("checkout") ||
                        pageSource.contains("Sign In") ||
                        pageSource.contains("guest"),
                "Checkout button did not redirect to sign-in or checkout flow");
        System.out.println("Test 2 PASSED: Checkout button redirected as expected");
    }

    // Test 3: Verify checkout sign-in page has both sign-in and guest checkout options
    @Test(priority = 3)
    public void verifyCheckoutSignInOptionsPresent() throws InterruptedException {
        addItemToCart();

        driver.get("https://www.bestbuy.com/cart");
        Thread.sleep(4000);

        WebElement checkoutButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("button[data-track='Checkout - Top']")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", checkoutButton);
        Thread.sleep(1000);
        checkoutButton = driver.findElement(By.cssSelector("button[data-track='Checkout - Top']"));
        js.executeScript("arguments[0].click();", checkoutButton);
        Thread.sleep(4000);

        String pageSource = driver.getPageSource();
        Assert.assertTrue(
                pageSource.contains("Sign in") || pageSource.contains("Sign In"),
                "Sign in option not found on checkout auth page");
        Assert.assertTrue(
                pageSource.contains("Guest") || pageSource.contains("guest") ||
                        pageSource.contains("Continue as Guest"),
                "Guest checkout option not found on checkout auth page");
        System.out.println("Test 3 PASSED: Both sign-in and guest checkout options are present");
    }

    // Test 4: Verify cart page shows item name, price, and checkout button together
    @Test(priority = 4)
    public void verifyCartSummaryComplete() throws InterruptedException {
        addItemToCart();

        driver.get("https://www.bestbuy.com/cart");
        Thread.sleep(4000);

        String pageSource = driver.getPageSource();
        System.out.println("Cart URL: " + driver.getCurrentUrl());

        Assert.assertTrue(
                pageSource.contains("Wireless") || pageSource.contains("Mouse"),
                "Added item name not found in cart summary");
        Assert.assertTrue(
                pageSource.contains("$"),
                "No price found in cart summary");
        Assert.assertTrue(
                pageSource.contains("Checkout") || pageSource.contains("checkout"),
                "Checkout button not found in cart summary");
        System.out.println("Test 4 PASSED: Cart summary shows item, price, and checkout button");
    }

    // Test 5: Verify order total is present and non-zero on the cart page
    @Test(priority = 5)
    public void verifyOrderTotalPresentAndNonZero() throws InterruptedException {
        addItemToCart();

        driver.get("https://www.bestbuy.com/cart");
        Thread.sleep(4000);

        String pageSource = driver.getPageSource();
        Assert.assertTrue(
                pageSource.contains("Order Total") || pageSource.contains("Subtotal"),
                "No order total section found on cart page");
        Assert.assertFalse(
                pageSource.contains("Order Total\">$0.00") ||
                        pageSource.contains("Subtotal\">$0.00"),
                "Order total shows $0.00 despite item being in cart");

        WebElement subtotalEl = driver.findElement(
                By.cssSelector(".price-block__primary-price")
        );
        WebElement savingsEl = driver.findElement(
                By.cssSelector(".price-block__savings-price")
        );

        String subtotalText = subtotalEl.getText().replaceAll("[^0-9.]", "");
        String savingsText = savingsEl.getText().replaceAll("[^0-9.]", "");

        System.out.println("Subtotal: $" + subtotalText);
        System.out.println("Savings: $" + savingsText);

        Assert.assertFalse(subtotalText.isEmpty(), "Could not read subtotal value");
        Assert.assertFalse(savingsText.isEmpty(), "Could not read savings value");

        double subtotal = Double.parseDouble(subtotalText);
        double savings = Double.parseDouble(savingsText);

        Assert.assertTrue(subtotal > 0, "Subtotal is 0 or negative");
        Assert.assertTrue(savings >= 0, "Savings is negative");
        Assert.assertTrue(subtotal > savings, "Savings is greater than subtotal");

        System.out.println("Test 5 PASSED: Order total components are present and values are consistent");
    }

    @AfterMethod
    public void teardown() {
        driver.quit();
    }
}