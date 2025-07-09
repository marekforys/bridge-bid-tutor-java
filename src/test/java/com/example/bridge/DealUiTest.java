package com.example.bridge;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DealUiTest {
    private static WebDriver driver;

    @BeforeAll
    public static void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testEachHandDisplays13Cards() {
        driver.get("http://localhost:8080/");
        // For each hand (N, W, E, S), count the number of card elements in the suit
        // rows
        // We assume the handIndex=0 (N), 3 (W), 1 (E), 2 (S) order as in the template
        int[] handIndices = { 0, 3, 1, 2 };
        for (int handIndex : handIndices) {
            int cardCount = 0;
            List<WebElement> suitRows = driver.findElements(By.xpath(
                    String.format("//div[@th:with='handIndex=%d']//table//tr/td", handIndex)));
            for (WebElement row : suitRows) {
                // Get the text, remove the icon, split by space, and count cards
                String text = row.getText().trim();
                if (text.length() > 2) {
                    String[] parts = text.substring(2).trim().split(" ");
                    for (String part : parts) {
                        if (!part.isEmpty())
                            cardCount++;
                    }
                }
            }
            assertEquals(13, cardCount, "Hand index " + handIndex + " should have 13 cards displayed");
        }
    }
}
