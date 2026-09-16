import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;

class CarbohydrateCalculatorTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void openCalculator() {
        // Open a new browser for each test so earlier tests cannot affect its settings.
        ChromeOptions options = new ChromeOptions();
        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,1000");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get("https://www.calculator.net/carbohydrate-calculator.html");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cage")));
    }

    @AfterEach
    void closeBrowser() {
        // Close the browser even if the test fails.
        if (driver != null) {
            driver.quit();
        }
    }

    // TC-005: Check the Metric results using the Mifflin-St Jeor formula.
    // Inputs: male, age 30, 180 cm, 80 kg, sedentary. Expected maintenance: 2136 Calories.
    // Check the Calories and carbohydrate grams in all five goal rows.
    @Test
    @DisplayName("TC-005: Metric Mifflin-St Jeor calculation")
    void tc005_metricMifflinCalculation() {
        // Use the test data from TC-005. Select Metric and Mifflin instead of relying on defaults.
        driver.findElement(By.cssSelector("#topmenu a[onclick*=metric]")).click();
        enter(By.id("cage"), "30");
        driver.findElement(By.cssSelector("label[for='csex1']")).click();
        enter(By.id("cheightmeter"), "180");
        enter(By.id("ckg"), "80");
        new Select(driver.findElement(By.id("cactivity")))
                .selectByVisibleText("Sedentary: little or no exercise");
        driver.findElement(By.cssSelector("#ccsettingtitle a")).click();
        driver.findElement(By.cssSelector("label[for='cformula1']")).click();

        WebElement result = calculate();

        // Calculate BMR from weight (kg), height (cm) and age, then multiply by 1.2 for sedentary.
        // Calculate our expected answer here, separately from the website.
        double bmr = 10 * 80 + 6.25 * 180 - 5 * 30 + 5;
        double maintenanceCalories = bmr * 1.2;

        // Compare the table with our expected values: five calorie values and twenty grams values.
        assertMetricCalculations(result, maintenanceCalories);
    }

    // TC-015: Check the Metric results using Katch-McArdle and 20% Body Fat.
    // Inputs: male, age 30, 180 cm, 80 kg, 20% Body Fat, sedentary.
    // Expect 2103 maintenance Calories after rounding. Check Calories and grams in all five rows.
    @Test
    @DisplayName("TC-015: Metric Katch-McArdle calculation")
    void tc015_metricKatchCalculation() {
        // Use the same Metric inputs as TC-005, then choose Katch and enter Body Fat.
        driver.findElement(By.cssSelector("#topmenu a[onclick*=metric]")).click();
        enter(By.id("cage"), "30");
        driver.findElement(By.cssSelector("label[for='csex1']")).click();
        enter(By.id("cheightmeter"), "180");
        enter(By.id("ckg"), "80");
        new Select(driver.findElement(By.id("cactivity")))
                .selectByVisibleText("Sedentary: little or no exercise");
        driver.findElement(By.cssSelector("#ccsettingtitle a")).click();
        driver.findElement(By.cssSelector("label[for='cformula2']")).click();
        enter(By.name("cfatpct"), "20");

        WebElement result = calculate();

        // Katch uses weight without body fat: (1 - 0.20) * 80 = 64 kg.
        // The formula below gives 2102.88 maintenance Calories, rounded to 2103 on screen.
        double bodyFatFraction = 20 / 100.0;
        double bmr = 370 + 21.6 * (1 - bodyFatFraction) * 80;
        double maintenanceCalories = bmr * 1.2;
        // Check that Weight Maintenance shows our calculated answer, rounded to 2103 Calories.
        assertEquals(Math.round(maintenanceCalories),
                number(result.findElement(By.xpath(".//tr[td[1]='Weight Maintenance']/td[2]"))),
                "TC-015: maintenance Calories should match Katch-McArdle × sedentary factor");
        // Compare the table with our expected values: five calorie values and twenty grams values.
        assertMetricCalculations(result, maintenanceCalories);
    }

    // TC-020: Check that valid US inputs produce a result table using Katch-McArdle.
    // Inputs: male, age 30, 5 ft 10 in, 176 lb, 20% Body Fat, sedentary.
    // Check the headings, US goal names, units and values greater than zero.
    // This test checks that results appear in the expected format. It does not check exact US calculations.
    @Test
    @DisplayName("TC-020: Valid US inputs return Katch-McArdle results")
    void tc020_usKatchReturnsResults() {
        // TC-020 does not give exact input values, so use the same valid example on every run.
        // Enter US height and weight, then select Katch as required by the test case.
        driver.findElement(By.cssSelector("#topmenu a[onclick*=standard]")).click();
        enter(By.id("cage"), "30");
        driver.findElement(By.cssSelector("label[for='csex1']")).click();
        enter(By.id("cheightfeet"), "5");
        enter(By.id("cheightinch"), "10");
        enter(By.id("cpound"), "176");
        new Select(driver.findElement(By.id("cactivity")))
                .selectByVisibleText("Sedentary: little or no exercise");
        driver.findElement(By.cssSelector("#ccsettingtitle a")).click();
        driver.findElement(By.cssSelector("label[for='cformula2']")).click();
        enter(By.name("cfatpct"), "20");

        WebElement result = calculate();

        // Check that the result page still uses US units. The website calls this mode "standard".
        assertEquals("standard", driver.findElement(By.id("ctype")).getDomProperty("value"),
                "The submitted calculation should use US units");
        // Check that Katch is still selected after clicking Calculate.
        assertTrue(driver.findElement(By.id("cformula2")).isSelected(), "Katch should remain selected");
        String[] headers = {"Goal", "Daily Calorie Allowance", "40%*", "55%*", "65%*", "75%*"};
        String[] goals = {"Weight Maintenance", "Lose 1 lb/week", "Lose 2 lb/week",
                "Gain 1 lb/week", "Gain 2 lb/week"};
        List<WebElement> rows = result.findElements(By.cssSelector("tr"));
        // Check that all five goal rows and the header row are present.
        assertEquals(6, rows.size(), "One header and five goal rows should appear");
        List<WebElement> headerCells = rows.getFirst().findElements(By.cssSelector("td"));
        // Check for six headings: Goal, Calories and the four percentages.
        assertEquals(headers.length, headerCells.size());
        for (int column = 0; column < headers.length; column++) {
            // Check each heading is correct and in the expected order.
            assertEquals(headers[column], headerCells.get(column).getText(), "Result header");
        }
        for (int goal = 0; goal < goals.length; goal++) {
            List<WebElement> cells = rows.get(goal + 1).findElements(By.cssSelector("td"));
            // Check this row has all six cells before reading its values.
            assertEquals(6, cells.size(), "Columns for " + goals[goal]);
            // Check the goal name. Treat the website's special spaces as normal spaces.
            assertEquals(goals[goal], cells.getFirst().getText().replace('\u00a0', ' '));
            // Check that the cell shows a number followed by "Calories".
            assertTrue(cells.get(1).getText().matches("[0-9,]+\\s+Calories"),
                    goals[goal] + ": numeric Calories and unit");
            // Check that Calories are greater than zero for these inputs.
            assertTrue(number(cells.get(1)) > 0, goals[goal] + ": positive Calories");
            for (int column = 2; column < cells.size(); column++) {
                // US cells also show ounces and pounds. Read only the bold grams value.
                WebElement grams = cells.get(column).findElement(By.tagName("b"));
                // Check that the value is a number followed by "grams".
                assertTrue(grams.getText().replace('\u00a0', ' ').matches("[0-9,]+\\s+grams"),
                        goals[goal] + ": numeric carbohydrate target and grams unit");
                // Check that grams are greater than zero.
                assertTrue(number(grams) > 0, goals[goal] + ": positive carbohydrate target");
            }
        }
    }

    private void assertMetricCalculations(WebElement result, double maintenanceCalories) {
        // Both Metric tests use these checks for all five goals and four percentages.
        // Each goal uses the calorie adjustment at the same position in the list below.
        String[] goals = {"Weight Maintenance", "Lose 0.5 kg/week", "Lose 1 kg/week",
                "Gain 0.5 kg/week", "Gain 1 kg/week"};
        int[] calorieAdjustments = {0, -500, -1000, 500, 1000};
        double[] percentages = {0.40, 0.55, 0.65, 0.75};
        String[] headers = {"Goal", "Daily Calorie Allowance", "40%*", "55%*", "65%*", "75%*"};
        List<WebElement> rows = result.findElements(By.cssSelector("tr"));
        // Check that all five goal rows and the header row are present.
        assertEquals(6, rows.size(), "One header and five goal rows should appear");
        List<WebElement> headerCells = rows.getFirst().findElements(By.cssSelector("td"));
        // Check for six headings: Goal, Calories and the four percentages.
        assertEquals(headers.length, headerCells.size(), "Result column count");
        for (int column = 0; column < headers.length; column++) {
            // Check each heading is correct and in the expected order.
            assertEquals(headers[column], headerCells.get(column).getText(), "Result header");
        }

        // Assumption: the site appears to use 3.75 Calories per gram (2136 * 40% / 3.75 rounds to 228 g).
        // The assignment does not define this conversion or the 500/1000 calorie changes for each goal.
        // These checks follow the observed results. Those rules still need confirmation.
        double caloriesPerGram = 3.75;
        for (int goal = 0; goal < goals.length; goal++) {
            List<WebElement> cells = rows.get(goal + 1).findElements(By.cssSelector("td"));
            // Check this row has all six cells before reading its values.
            assertEquals(6, cells.size(), "Columns for " + goals[goal]);
            // Check the goal name. Treat the website's special spaces as normal spaces.
            assertEquals(goals[goal], cells.getFirst().getText().replace('\u00a0', ' '));
            double expectedCalories = maintenanceCalories + calorieAdjustments[goal];
            // Compare this row's Calories with our calculated value, rounded to a whole number.
            assertEquals(Math.round(expectedCalories), number(cells.get(1)),
                    goals[goal] + ": Calories");
            for (int column = 0; column < percentages.length; column++) {
                // Calculate grams using the full calorie value, then round the final answer.
                long expectedGrams = Math.round(expectedCalories * percentages[column] / caloriesPerGram);
                WebElement carbohydrateCell = cells.get(column + 2);
                // Check that the cell includes the unit "grams".
                assertTrue(carbohydrateCell.getText().contains("grams"), "Carbohydrate units");
                // Compare the displayed grams with our calculated answer for this row and percentage.
                assertEquals(expectedGrams, number(carbohydrateCell),
                        goals[goal] + ": carbohydrate grams at " + headers[column + 2]);
            }
        }
    }

    private void enter(By locator, String value) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        field.clear();
        field.sendKeys(value);
    }

    private WebElement calculate() {
        WebElement form = driver.findElement(By.name("calform"));
        driver.findElement(By.cssSelector("input[type='submit'][value='Calculate']")).click();
        // Calculate loads a new page. Wait for the old form to disappear so we do not read old results.
        wait.until(ExpectedConditions.stalenessOf(form));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#content table.cinfoT")));
    }

    private long number(WebElement cell) {
        // Read one whole-number value, such as "2,136 Calories" or "228 grams".
        // This removes everything except digits, so do not use it for decimals, negatives or multiple values.
        return Long.parseLong(cell.getText().replaceAll("[^0-9]", ""));
    }
}
