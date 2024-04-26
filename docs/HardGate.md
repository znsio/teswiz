# Setting up a Hard Gate with Teswiz

Teswiz now offers the capability to establish a Hard Gate for your test execution results, ensuring precise control over your testing process.

### **For passing tests, any failure within the executed scenarios results in the build being marked as FAILED.**

### **For failing tests, any occurrence of a _passing_ scenario(s) automatically flags the build as FAILED.**

This stringent control mechanism not only upholds the reliability of your testing process but also ensures that identified issues are accurately reflected in the build status, facilitating efficient debugging and resolution.

By implementing the Hard Gate functionality in teswiz, you can enhance the efficiency and effectiveness of your test management, enabling precise control and clear insights into your testing outcomes.

## Enabling the Hard Gate

To activate the Hard Gate feature, simply include the following parameter in your test execution command or set it as an environment variable:

    SET_HARD_GATE=true

Once enabled, the Hard Gate provides distinct functionalities for both passing and failing tests.

### Handling Passing Tests

With the Hard Gate enabled, passing tests become the focus of execution. By default, all tests tagged with **@failing** are excluded from execution, ensuring that only non-failing tests, i.e., the ones expected to pass, are run.

To explicitly disable the execution of failing tests, use the following parameter in your test execution command or set it as an environment variable:

    IS_FAILING_TEST_SUITE=false

If any scenario marked as passing fails during execution, the build status will be marked as **FAILED**.

## Dealing with Failing Tests

In the context of the Hard Gate, failing tests are rigorously managed to ensure comprehensive evaluation.

### Executing Failing Tests

To specifically execute failing tests, utilize the following parameter in your test execution command:

    IS_FAILING_TEST_SUITE=true

With this parameter, only tests tagged with **@failing** will be executed. If all scenarios marked as failing indeed fail during execution, the build will be marked as **PASS**. This provides clarity on whether a previously failing test now passes due to a fix in either the test itself or the underlying product issue.

### Examples:

1. Command to execute all the passing tests (scenarios which do not have the @failing tag).

   `SET_HARD_GATE=true IS_FAILING_TEST_SUITE=false ./gradlew run`


2. Command to Execute all the Failing Tests [i.e, only scenarios which have @failing tag].
   
    `SET_HARD_GATE=true IS_FAILING_TEST_SUITE=true ./gradlew run`
   
    **Note:** When all the scenarios with the @failing tag fail, the build succeeds.


