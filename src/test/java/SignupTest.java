import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SignupTest {

    WebDriver driver;
    String firstName = "Camilo";
    String lastName = "Palacio";
    String email = "tcase6213@gmail.com";
    String password = "Testaccount@123";

    @BeforeMethod
    public void setup() throws InterruptedException {
        driver = new ChromeDriver();
        driver.get("https://www.bestbuy.com/identity/global/createAccount");
        driver.manage().window().maximize();
        Thread.sleep(5000);
    }

    // Test 1: Verify signup page loads correctly
    @Test(priority = 1)
    public void verifySignupPageLoads() {
        String url = driver.getCurrentUrl();
        String title = driver.getTitle();
        System.out.println("Signup URL: " + url);
        System.out.println("Signup title: " + title);

        // BestBuy redirects to newAccount URL so check for bestbuy.com
        Assert.assertTrue(url.contains("bestbuy.com"),
                "Signup page did not load correctly");
        Assert.assertTrue(title.contains("Best Buy"),
                "Signup page title incorrect");
        System.out.println("Test 1 PASSED: Signup page loaded");
    }

    // Test 2: Verify all form fields are present
    @Test(priority = 2)
    public void verifyFormFieldsPresent() throws InterruptedException {
        Thread.sleep(2000);

        // Print all input fields to find correct IDs
        driver.findElements(By.tagName("input")).forEach(el -> {
            System.out.println("Input - id: " + el.getAttribute("id")
                    + " | name: " + el.getAttribute("name")
                    + " | type: " + el.getAttribute("type"));
        });

        // Use flexible selectors since IDs may differ from login page
        WebElement firstNameField = driver.findElement(
                By.cssSelector("input[name='firstName'], input[id='firstName']"));
        WebElement emailField = driver.findElement(
                By.cssSelector("input[type='email'], input[name='email']"));

        Assert.assertTrue(firstNameField.isDisplayed(),
                "First name field not visible");
        Assert.assertTrue(emailField.isDisplayed(),
                "Email field not visible");

        System.out.println("Test 2 PASSED: Form fields are present");
    }

    // Test 3: Verify empty form submission shows validation errors
    @Test(priority = 3)
    public void verifyEmptyFormShowsErrors() throws InterruptedException {
        driver.findElement(
                By.cssSelector(".cia-form__controls__submit")).click();
        Thread.sleep(3000);

        // BestBuy uses tb-input-wrapper-error class for validation errors
        java.util.List<WebElement> errors = driver.findElements(
                By.cssSelector(".tb-input-wrapper-error"));
        Assert.assertTrue(errors.size() > 0,
                "No validation errors shown for empty form");
        System.out.println("Number of validation errors: " + errors.size());
        errors.forEach(e -> System.out.println("Error: " + e.getText()));
        System.out.println("Test 3 PASSED: Empty form shows validation errors");
    }

    // Helper method to close survey popup if it appears
    public void closeSurveyIfPresent() {
        try {
            WebElement survey = driver.findElement(By.id("survey_window"));
            if (survey.isDisplayed()) {
                WebElement closeBtn = driver.findElement(
                        By.cssSelector("#survey_window .cancel, #survey_window .close, [class*='survey'] button"));
                closeBtn.click();
                Thread.sleep(1000);
                System.out.println("Survey popup closed");
            }
        } catch (Exception e) {
            System.out.println("No survey popup found");
        }
    }
    // Test 4: Verify invalid email format shows error
    @Test(priority = 4)
    public void verifyInvalidEmailShowsError() throws InterruptedException {
        driver.findElement(By.id("firstName")).sendKeys(firstName);
        driver.findElement(By.id("lastName")).sendKeys(lastName);
        driver.findElement(By.id("email")).sendKeys("notanemail");
        driver.findElement(By.id("fld-p1")).sendKeys(password);
        driver.findElement(By.id("reenterPassword")).sendKeys(password);
        driver.findElement(By.id("phone")).sendKeys("2395550123");
        Thread.sleep(1000);

        // Close popup if present before clicking
        closeSurveyIfPresent();

        // Use JavaScript click to bypass any overlay
        WebElement submitBtn = driver.findElement(
                By.cssSelector(".cia-form__controls__submit"));
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", submitBtn);
        Thread.sleep(3000);

        WebElement emailError = driver.findElement(
                By.cssSelector(".tb-input-wrapper-error"));
        Assert.assertTrue(emailError.isDisplayed(),
                "No error shown for invalid email format");
        System.out.println("Error shown: " + emailError.getText());
        System.out.println("Test 4 PASSED: Invalid email shows error");
    }

    // Test 5: Complete signup with valid credentials
    @Test(priority = 5)
    public void completeSignup() throws InterruptedException {
        driver.findElement(By.id("firstName")).sendKeys(firstName);
        Thread.sleep(500);
        driver.findElement(By.id("lastName")).sendKeys(lastName);
        Thread.sleep(500);
        driver.findElement(By.id("email")).sendKeys(email);
        Thread.sleep(500);
        driver.findElement(By.id("fld-p1")).sendKeys(password);
        Thread.sleep(500);
        driver.findElement(By.id("reenterPassword")).sendKeys(password);
        Thread.sleep(500);
        driver.findElement(By.id("phone")).sendKeys("2395550123");
        Thread.sleep(1000);

        System.out.println("Filling signup form with: " + email);

        // Close popup if present before clicking
        closeSurveyIfPresent();

        // Use JavaScript click to bypass any overlay
        WebElement submitBtn = driver.findElement(
                By.cssSelector(".cia-form__controls__submit"));
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", submitBtn);
        Thread.sleep(8000);

        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL after signup: " + currentUrl);

        Assert.assertFalse(currentUrl.contains("newAccount"),
                "Still on signup page — account creation may have failed");
        System.out.println("Test 5 PASSED: Account created successfully");
    }
    @AfterMethod
    public void teardown() {
        driver.quit();
    }
}