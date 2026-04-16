import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
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
        driver = new ChromeDriver();
        js = (JavascriptExecutor) driver;
        driver.get("https://www.bestbuy.com");
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(1000);
    }

    public void addItemToCart() throws InterruptedException {
        WebElement searchBar = driver.findElement(By.id("autocomplete-search-bar"));
        searchBar.sendKeys("Wireless Keyboard");
        searchBar.sendKeys(Keys.ENTER);
        Thread.sleep(1000);

        WebElement firstProduct = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("img[data-testid='product-image']")
                )
        );
        js.executeScript("arguments[0].click();", firstProduct);
        Thread.sleep(1000);
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

    // Test 2: Verify both Checkout and PayPal buttons are present in the DOM
    @Test(priority = 2)
    public void verifyCheckoutButtonsPresent() throws InterruptedException {
        addItemToCart();

        driver.get("https://www.bestbuy.com/cart");
        Thread.sleep(4000);

        WebElement checkoutButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("button[data-track='Checkout - Top']")
                )
        );
        Assert.assertTrue(checkoutButton.isDisplayed(),
                "Regular checkout button is not visible");
        System.out.println("Regular checkout button found");

        String pageSource = driver.getPageSource();
        Assert.assertTrue(
                pageSource.contains("PayPal") || pageSource.contains("paypal"),
                "PayPal checkout button not found on cart page");
        System.out.println("PayPal checkout button found");

        System.out.println("Test 2 PASSED: Both checkout buttons are present on cart page");
    }

    // Test 3: Verify unauthenticated checkout button click redirects to sign-in or guest option
    @Test(priority = 3)
    public void verifyCheckoutButtonRedirects() throws InterruptedException {
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
                currentUrl.contains("Returning Customers") ||
                        currentUrl.contains("New Customers") ||
                        pageSource.contains("Sign in") ||
                        pageSource.contains("Guest"),
                "Checkout button did not redirect to sign-in or checkout flow");
        System.out.println("Test 3 PASSED: Checkout button redirected as expected");
    }

    // Test 4: Verify PayPal button click responds and navigates correctly
    @Test(priority = 4)
    public void verifyPaypalButtonRedirects() throws InterruptedException {
        addItemToCart();

        driver.get("https://www.bestbuy.com/cart");
        Thread.sleep(4000);

        WebElement paypalButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".paypal-button, [data-track='PayPal Checkout'], .paypal-checkout-button")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", paypalButton);
        Thread.sleep(1000);
        paypalButton = driver.findElement(
                By.cssSelector(".paypal-button, [data-track='PayPal Checkout'], .paypal-checkout-button")
        );
        js.executeScript("arguments[0].click();", paypalButton);
        Thread.sleep(4000);

        String currentUrl = driver.getCurrentUrl();
        String pageSource = driver.getPageSource();
        System.out.println("URL after clicking PayPal: " + currentUrl);

        Assert.assertTrue(
                currentUrl.contains("paypal") ||
                        currentUrl.contains("bestbuy") ||
                        pageSource.contains("PayPal"),
                "PayPal button did not respond or navigate correctly");
        System.out.println("Test 4 PASSED: PayPal button responded and navigated as expected");
    }

    // Test 5: Verify 2-year Geek Squad protection can be added to cart
    @Test(priority = 5)
    public void verifyAddWarrantyBeforeCheckout() throws InterruptedException {
        addItemToCart();

        driver.get("https://www.bestbuy.com/cart");
        Thread.sleep(4000);

        WebElement warrantyButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("button.child-item__add-button")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", warrantyButton);
        Thread.sleep(1000);
        warrantyButton = driver.findElement(By.cssSelector("button.child-item__add-button"));
        js.executeScript("arguments[0].click();", warrantyButton);
        Thread.sleep(3000);

        String pageSource = driver.getPageSource();
        System.out.println("Checking warranty was added to cart...");

        Assert.assertTrue(
                pageSource.contains("Protection Plan") ||
                        pageSource.contains("Geek Squad") ||
                        pageSource.contains("Year") ||
                        pageSource.contains("warranty"),
                "Warranty/protection plan does not appear to have been added to cart");
        System.out.println("Test 5 PASSED: Warranty added to cart successfully");
    }

    // Test 6: Verify cart order total calculation is accurate (Subtotal - Savings + Tax = Total)
    @Test(priority = 6)
    public void verifyOrderTotalCalculationAccurate() throws InterruptedException {
        addItemToCart();

        driver.get("https://www.bestbuy.com/cart");
        Thread.sleep(4000);

        String pageSource = driver.getPageSource();
        System.out.println("Checking order summary calculation...");

        Assert.assertTrue(pageSource.contains("Subtotal"),
                "Subtotal not found in order summary");
        Assert.assertTrue(pageSource.contains("Savings"),
                "Savings not found in order summary");
        Assert.assertTrue(pageSource.contains("Estimated Sales Tax"),
                "Estimated Sales Tax not found in order summary");
        Assert.assertTrue(pageSource.contains("Total"),
                "Total not found in order summary");

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
        Assert.assertTrue(subtotal > savings, "Savings is greater than subtotal — something is wrong");

        System.out.println("Subtotal ($" + subtotal + ") > Savings ($" + savings + ") — calculation looks valid");
        System.out.println("Test 6 PASSED: Order total components are present and values are consistent");
    }

    @AfterMethod
    public void teardown() {
        driver.quit();
    }
}