# Carbohydrate Calculator Tests

This project contains three automated tests for the [Carbohydrate Calculator](https://www.calculator.net/carbohydrate-calculator.html). The test numbers match the cases in the Excel test document.

The tests use Java, Selenium to control Chrome, and JUnit to check the results. All three tests are in `src/test/java/CarbohydrateCalculatorTest.java`.

## What the tests check

- **TC-005:** Enter Metric inputs and select Mifflin-St Jeor. Calculate the expected Calories and carbohydrate grams in the test, then compare them with all five rows on the website.
- **TC-015:** Do the same using Katch-McArdle and 20% Body Fat.
- **TC-020:** Enter height in feet/inches and weight in pounds, with Katch-McArdle selected. Check that the result table has the expected headings and goal names, and shows Calories and grams greater than zero. This test does not check the exact US calculation.

Each test opens its own browser and closes it when finished.

## How to run in IntelliJ IDEA

You need Java JDK 21, Google Chrome and an internet connection.

1. Download this repository using **Code > Download ZIP**, then extract it.
2. Open the extracted project folder in IntelliJ IDEA. It should contain `pom.xml`.
3. Under **File > Project Structure > Project**, select JDK 21 as the SDK and set the language level to **SDK default**. If Java is not listed, use **Add JDK from disk** to select your JDK folder.
4. Allow Maven to download the libraries listed in `pom.xml`.
5. Open **src > test > java > CarbohydrateCalculatorTest**.
6. Right-click the test class and choose **Run** to run all three tests. To run one test, use the green arrow beside that test method.

The Run panel shows which tests passed or failed. Chrome opens and closes during the tests. Selenium handles the ChromeDriver download when needed.

If Java and Maven are already set up on your command line, you can also run all three tests from the project folder with:

```shell
mvn clean test
```

## How the expected results are calculated

TC-005 and TC-015 use male, age 30, height 180 cm, weight 80 kg and sedentary activity. TC-015 also uses 20% Body Fat.

The tests use the Mifflin-St Jeor and Katch-McArdle formulas described on Calculator.net's [Calorie Calculator](https://www.calculator.net/calorie-calculator.html), with a sedentary multiplier of 1.2. The expected maintenance values are **2136 Calories** for TC-005 and **2103 Calories** after rounding for TC-015.

For the other goal rows, the tests subtract or add 500 and 1000 Calories. They calculate carbohydrate grams using each column's percentage, divide by 3.75, then round the answer.

**Assumption:** the 500/1000 calorie changes and 3.75 Calories per gram are based on the site's current results. The assignment does not specify these rules, so they would need confirmation. For example, 2136 Calories at 40% gives 228 grams when divided by 3.75.

## Test run

All three tests passed together in IntelliJ IDEA on Windows with Chrome 152 on September 16, 2026.
