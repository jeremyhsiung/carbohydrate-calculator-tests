# Carbohydrate Calculator Tests

Three end-to-end tests for the [Calculator.net Carbohydrate Calculator](https://www.calculator.net/carbohydrate-calculator.html), using Java, Selenium WebDriver and JUnit.

## Test coverage

| Test case | Scenario | Checks |
| --- | --- | --- |
| TC-005 | Metric / Mifflin-St Jeor | Calculated Calories and carbohydrate grams for all five goal rows, plus table headings and labels. |
| TC-015 | Metric / Katch-McArdle | The same result checks with 20% Body Fat and the Katch formula. |
| TC-020 | US / Katch-McArdle | Feet/inches and pounds inputs, selected unit mode and formula, table headings, US goal labels, and positive Calories and grams. |

Each test opens a new Chrome session, enters its own inputs, clicks Calculate, checks the results and closes the browser. TC-020 checks result format and positive values; it does not verify exact US calculations.

## Requirements

- JDK 21
- Apache Maven 3.9.x
- Google Chrome
- Internet access for the live website and dependency/driver downloads

Set JAVA_HOME to the JDK folder and make Java and Maven available on PATH. Confirm with `java -version` and `mvn -version`. Selenium Manager obtains ChromeDriver when needed.

## Run the tests

Open a terminal in the project folder containing `pom.xml`.

Run all three tests:

```shell
mvn clean test
```

Run only TC-005:

```shell
mvn test "-Dtest=CarbohydrateCalculatorTest#tc005_metricMifflinCalculation"
```

Run with the Chrome window hidden:

```shell
mvn test "-Dheadless=true"
```

In IntelliJ IDEA, open the project folder, set the Project SDK to JDK 21 and allow Maven to load the dependencies. Right-click `CarbohydrateCalculatorTest` under `src/test/java` and choose Run to execute all three tests.

Maven writes test reports to `target/surefire-reports`. A successful full run reports three tests with no failures or errors.

## Expected values and assumptions

Both Metric tests use male, age 30, height 180 cm, weight 80 kg and sedentary activity (factor 1.2). TC-015 also uses 20% Body Fat.

- Mifflin-St Jeor: `(10 * 80 + 6.25 * 180 - 5 * 30 + 5) * 1.2 = 2136` maintenance Calories.
- Katch-McArdle: `(370 + 21.6 * (1 - 0.20) * 80) * 1.2 = 2102.88`, displayed as 2103 maintenance Calories.

The BMR formulas are described on Calculator.net's [Calorie Calculator](https://www.calculator.net/calorie-calculator.html). Expected Calories are calculated in the test, separately from the displayed results.

The tests use observed calorie adjustments of -500/-1000 for weight loss and +500/+1000 for weight gain. Carbohydrate grams are calculated as expected Calories multiplied by the column percentage (40%, 55%, 65% or 75%), divided by 3.75, then rounded to a whole number.

The 3.75 Calories/g conversion and goal adjustments follow observed site behavior. The assignment does not specify these rules, so they remain assumptions to confirm. For example, 2136 Calories at 40% gives 228 g on the site, consistent with dividing by 3.75. Using 4 Calories/g would give 214 g.

TC-020 uses male, age 30, height 5 ft 10 in, weight 176 lb, 20% Body Fat and sedentary activity.

## Verification

All three tests passed together on September 16, 2026 in IntelliJ IDEA on Windows with Chrome 152. These tests depend on the live website; network problems or changes to its HTML can cause failures that need investigation.
