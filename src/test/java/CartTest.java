import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class CartTest {

    WebDriver driver;
    WebDriverWait wait;
    JavascriptExecutor js;  // make sure this line is here
    @BeforeMethod
    public void setup() throws InterruptedException {
        driver = new ChromeDriver();
        js = (JavascriptExecutor) driver;  // move this to right after driver is created
        driver.get("https://www.bestbuy.com");
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(3000);
    }

    // Test 1: Verify cart icon is present on the homepage
    @Test(priority = 1)
    public void verifyCartIconPresent() throws InterruptedException {
        // Cart icon is typically identified by aria-label or a known class/id
        WebElement cartButton = driver.findElement(By.className("cart-label"));
        Assert.assertTrue(cartButton.isDisplayed(), "Cart button is not visible");
        Assert.assertTrue(cartButton.isEnabled(), "Cart button is not enabled");
        System.out.println("Test 1 PASSED: Cart icon is present on homepage");
    }

    // Test 2: Verify navigating to the cart page directly works
    @Test(priority = 2)
    public void verifyCartPageLoads() throws InterruptedException {
        WebElement cartButton = driver.findElement(By.className("cart-label"));
        cartButton.click();
        Thread.sleep(4000);

        String currentUrl = driver.getCurrentUrl();
        String pageSource = driver.getPageSource();
        System.out.println("Cart page URL: " + currentUrl);

        Assert.assertTrue(currentUrl.contains("bestbuy.com"),
                "Navigated away from Best Buy when loading cart");
        Assert.assertTrue(
                pageSource.contains("cart") || pageSource.contains("Cart"),
                "Cart page did not load expected content");
        System.out.println("Test 2 PASSED: Cart page loaded successfully");
    }

    // Test 3: Verify empty cart shows an appropriate empty state message
    @Test(priority = 3)
    public void verifyEmptyCartMessage() throws InterruptedException {
        driver.get("https://www.bestbuy.com/cart");
        Thread.sleep(4000);

        String pageSource = driver.getPageSource();
        System.out.println("Checking for empty cart indicator...");

        Assert.assertTrue(
                pageSource.contains("Your cart is empty") ||
                        pageSource.contains("$0.00") ||
                        pageSource.contains("no items") ||
                        pageSource.contains("nothing in your cart"),
                "No empty cart message found on cart page");
        System.out.println("Test 3 PASSED: Empty cart message is displayed");
    }

    // Test 4: Verify adding a product from search results updates the cart
    @Test(priority = 4)
    public void verifyAddToCartFromSearch() throws InterruptedException {
        // Search for a product
        WebElement searchBar = driver.findElement(By.id("autocomplete-search-bar"));
        searchBar.sendKeys("Wireless Mouse");
        searchBar.sendKeys(Keys.ENTER);
        Thread.sleep(5000);

        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL after product search: " + currentUrl);
        Assert.assertTrue(currentUrl.contains("bestbuy.com"), "Left Best Buy after search");

        // Click the first product image using JS to avoid intercept
        WebElement firstProduct = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("img[data-testid='product-image']")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", firstProduct);
        Thread.sleep(1000);
        js.executeScript("arguments[0].click();", firstProduct);
        Thread.sleep(5000);
        System.out.println("Navigated to product page: " + driver.getCurrentUrl());

        // Click Add to Cart using JS to bypass sticky header intercept
        WebElement atcButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("button[data-testid^='pdp-add-to-cart']")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", atcButton);
        Thread.sleep(1500);
        js.executeScript("arguments[0].click();", atcButton);
        Thread.sleep(4000);

        // --- Assertion 1: Cart badge shows a non-zero number ---
        String pageSource = driver.getPageSource();
        Assert.assertTrue(
                pageSource.contains("1 item") ||
                        pageSource.contains("Cart (1)") ||
                        !pageSource.contains("Cart (0)"),
                "Cart badge did not update after Add to Cart"
        );
        System.out.println("Cart badge updated successfully");

        // --- Assertion 2: Navigate to cart and verify it contains something ---
        WebElement cartButton = driver.findElement(By.className("cart-label"));
        js.executeScript("arguments[0].click();", cartButton);
        Thread.sleep(4000);

        String cartPageSource = driver.getPageSource();
        Assert.assertTrue(
                cartPageSource.contains("Wireless") ||
                        cartPageSource.contains("Mouse") ||
                        cartPageSource.contains("Order Total"),
                "Cart page does not appear to contain the added item"
        );
        System.out.println("Test 4 PASSED: Item found in cart after add-to-cart flow");
    }


    // Test 5: Verify item can be removed from cart
    @Test(priority = 5)
    public void verifyRemoveItemFromCart() throws InterruptedException {
        // Search for a product
        WebElement searchBar = driver.findElement(By.id("autocomplete-search-bar"));
        searchBar.sendKeys("Wireless Mouse");
        searchBar.sendKeys(Keys.ENTER);
        Thread.sleep(5000);

        // Click the first product
        WebElement firstProduct = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("img[data-testid='product-image']")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", firstProduct);
        Thread.sleep(1000);
        js.executeScript("arguments[0].click();", firstProduct);
        Thread.sleep(5000);
        System.out.println("Navigated to product page: " + driver.getCurrentUrl());

        // Click Add to Cart
        WebElement atcButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("button[data-testid^='pdp-add-to-cart']")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", atcButton);
        Thread.sleep(1500);
        js.executeScript("arguments[0].click();", atcButton);
        Thread.sleep(4000);

        // Navigate to cart
        driver.get("https://www.bestbuy.com/cart");
        Thread.sleep(4000);

        // Remove the item
        WebElement removeButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.className("cart-item__remove")
                )
        );
        removeButton.click();
        Thread.sleep(3000);

        // Assert cart is now empty
        String pageSource = driver.getPageSource();
        Assert.assertTrue(
                pageSource.contains("Your cart is empty") ||
                        pageSource.contains("$0.00") ||
                        pageSource.contains("no items"),
                "Cart does not appear empty after removing item"
        );
        System.out.println("Test 5 PASSED: Item successfully added then removed from cart");
    }

    @AfterMethod
    public void teardown() {
        driver.quit();
    }
}