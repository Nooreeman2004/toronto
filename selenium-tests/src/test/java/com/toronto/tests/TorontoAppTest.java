package com.toronto.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic Selenium Tests for Toronto Web Application
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TorontoAppTest {

    private static WebDriver driver;
    private static String baseUrl;

    @BeforeAll
    public static void setUp() {
        System.out.println("==========================================");
        System.out.println("🧪 Toronto Web App - Selenium Test Suite");
        System.out.println("==========================================");

        // Get app URL from environment or use default
        baseUrl = System.getProperty("app.url");
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = System.getenv("APP_URL");
        }
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://toronto_web_dev:3000";
        }
        System.out.println("Testing URL: " + baseUrl);

        // Setup ChromeDriver
        WebDriverManager.chromedriver().setup();

        // Configure Chrome options for headless mode
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        System.out.println("==========================================");
        System.out.println("🏁 Test Suite Completed");
        System.out.println("==========================================");
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Homepage loads successfully")
    public void testHomepageLoads() {
        System.out.println("\n🧪 Test 1: Testing homepage loads...");
        
        driver.get(baseUrl);
        
        // Verify page loaded by checking title exists
        String title = driver.getTitle();
        assertNotNull(title, "Page title should not be null");
        
        System.out.println("✅ Homepage loaded successfully - Title: " + title);
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Page has body content")
    public void testPageHasContent() {
        System.out.println("\n🧪 Test 2: Testing page has content...");
        
        driver.get(baseUrl);
        
        WebElement body = driver.findElement(By.tagName("body"));
        assertNotNull(body, "Body element should exist");
        
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.length() > 100, "Page should have content");
        
        System.out.println("✅ Page has content - Source length: " + pageSource.length() + " chars");
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: No server errors on page")
    public void testNoServerErrors() {
        System.out.println("\n🧪 Test 3: Testing for server errors...");
        
        driver.get(baseUrl);
        
        String pageSource = driver.getPageSource().toLowerCase();
        
        assertFalse(pageSource.contains("500 internal server error"), 
            "Page should not have 500 error");
        assertFalse(pageSource.contains("502 bad gateway"), 
            "Page should not have 502 error");
        assertFalse(pageSource.contains("503 service unavailable"), 
            "Page should not have 503 error");
        assertFalse(pageSource.contains("cannot get"), 
            "Page should not have routing error");
        
        System.out.println("✅ No server errors detected");
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Page loads within acceptable time")
    public void testPageLoadTime() {
        System.out.println("\n🧪 Test 4: Testing page load time...");
        
        long startTime = System.currentTimeMillis();
        driver.get(baseUrl);
        long loadTime = System.currentTimeMillis() - startTime;
        
        assertTrue(loadTime < 30000, "Page should load within 30 seconds");
        
        System.out.println("✅ Page loaded in " + loadTime + "ms");
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: HTML structure is valid")
    public void testHtmlStructure() {
        System.out.println("\n🧪 Test 5: Testing HTML structure...");
        
        driver.get(baseUrl);
        
        // Check for basic HTML elements
        WebElement html = driver.findElement(By.tagName("html"));
        assertNotNull(html, "HTML element should exist");
        
        WebElement head = driver.findElement(By.tagName("head"));
        assertNotNull(head, "HEAD element should exist");
        
        WebElement body = driver.findElement(By.tagName("body"));
        assertNotNull(body, "BODY element should exist");
        
        System.out.println("✅ HTML structure is valid");
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Check for JavaScript errors")
    public void testNoJsErrors() {
        System.out.println("\n🧪 Test 6: Checking for JavaScript errors...");
        
        driver.get(baseUrl);
        
        // Check page doesn't show JS error messages
        String pageSource = driver.getPageSource().toLowerCase();
        assertFalse(pageSource.contains("uncaught error"), 
            "Page should not have uncaught JS errors");
        assertFalse(pageSource.contains("script error"), 
            "Page should not have script errors");
        
        System.out.println("✅ No JavaScript errors detected");
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Page is accessible via HTTP")
    public void testHttpAccess() {
        System.out.println("\n🧪 Test 7: Testing HTTP accessibility...");
        
        driver.get(baseUrl);
        
        // If we get here without exception, HTTP access works
        String currentUrl = driver.getCurrentUrl();
        assertNotNull(currentUrl, "Current URL should not be null");
        assertTrue(currentUrl.startsWith("http"), "URL should start with http");
        
        System.out.println("✅ HTTP access successful - URL: " + currentUrl);
    }
}
