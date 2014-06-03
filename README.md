
[![Build Status](https://travis-ci.org/romank0/selenium-stale-element.svg?branch=master)](https://travis-ci.org/romank0/selenium-stale-element)

This is the answer to [post](http://biercoff.com/found-my-own-solution-for-webdriver-staleelementreferenceexception-problem/) 
about the issue with `StaleElementReferenceException` in selenium test.


_Disclaimer_: I never used selenium so I may misundertand something. I still hope my thoughs make some sense.

===========================

When I read the post my first though was that this functionality should be built in into `WebDriver`.

Quick search shows that there really is very similar thing to tackle such situation - `WebDriverWait`. It does the same thing and it addresses the first issue of the solution - the necessity to create `RetryStrategy`. I would say that the real problem is not in creation of `RetryStrategy` but in lot of boilerplate code. `WebDriverWait` is stateless so one instance can be reused and it requires much less boilerpate (especially in java 8).

With the following definition of `Manager.wait`:

```
 public WebDriverWait wait() {
    // creation of new instance is not necessary here
    return new WebDriverWait(this.getDriver(), TIMEOUT, SLEEP_BETWEEN_RETRIES)
            .ignoring(StaleElementReferenceException.class);
 }

```

this function 

```
  public String getValueFromId(String id, String attribute) throws Exception {
      RetryStrategy retry = new RetryStrategy();
      WebElement element = null;
      String result = null;
      while(retry.shouldRetry()) {
          try {
              element = manager.getDriver().findElement(By.id(id));
              result = element.getAttribute(attribute);
              break;
          } catch (StaleElementReferenceException e){
              logger.warning("Got into StaleElement exception with id " + id);
              retry.errorOccured(e);
          }
      }
      return result;
  }

```

can be rewritten with a use of `WebDriverWait` as

```
 public String getValueFromId(final String id, final String attribute) throws Exception {
        
    return manager.wait().until(
        new Function<WebDriver, WebElement>() {
          public WebElement apply(WebDriver driver) {
            return driver.findElement(By.id(id)).getAttribute(attribute);
          }
        }
    );

 }
```

Or in java 8 as 

```
 public String getValueFromId(final String id, final String attribute) throws Exception {
        
    return manager.wait().until(
      (Driver driver) -> driver.findElement(By.id(id)).getAttribute(attribute);
    );

 }
```

While this may be a good improvement I think this is not enough. For big test codebase changing all the places where elements are used requires lot of effort and is painful.

Let's consider the test:

```
     element = manager.getDriver().findElement(By.id("some id"));
     result = element.getAttribute("some_attribute");
     assertThat(result, equalTo("some-value"))
     
     element.click();
     
     result = element.getAttribute("some_other_attribute");
     assertThat(result, equalTo("some-other-value"))
     
```

In ideal world selenium should automatically handle `StaleElementReferenceException` when `WebElement` is used, search for it again and try to reexecute failed operation. We can easily do that without touching the tests. 


### ReconnectableWebElement

A decorator for `WebElement` can be created which 

1. wraps real `WebElement` and stores its identity
2. has reference to `WebDriver` and can requery element in case of `StaleElementReferenceException`   

The simple and straighforward implementation will look like this:

```
class ReconnectableWebElement implements WebElement {

 private WebDriver driver;
 private By elementIdentity;
 
 public ReconnectableWebElement(WebDriver driver, By elementIdentity) {
     this.driver = driver;
     this.elementIdentity = elementIdentity;
     this.wait = new WebDriverWait(this.getDriver(), TIMEOUT, SLEEP_BETWEEN_RETRIES)
            .ignoring(StaleElementReferenceException.class);
 }
 
 public String getAttribute(String attribute) {
     return wait.until(
        new Function<WebDriver, WebElement>() {
          public WebElement apply(WebDriver driver) {
            return driver.findElement(elementIdentity).getAttribute(attribute);
          }
        }
    );
 }
 
 // all other methods of WebElement are implemented similarly to getAttribute
}
```

This will require to define all 15 methods in `WebElement`. An alternative would be to create jdk proxy (I'm not sure however what will be easier).

Having `ReconnectableWebElement` we need a way to get them instead of what usually is returned by `WebDriver`. For this we need to wrap `WebDriver` itself.

```
class ReconnectableWebDriver implements WebDriver {
 private WebDriver webDriver;
 
 public ReconnectableWebDriver(WebDriver webDriver) {
    this.webDriver = webDriver;
 }
 
 public WebElement findElement(By identity) {
    return new ReconnectableWebElement(webDriver, identity);
 }
 
 // delegate all other methods to webDriver
}

```

Usually webDriver is obtained from one place (manager in the post) so all the tests wouldn't be changed at all.


This naive implementation doesn't really wraps the element and doesn't cache it. In real world I think it should do this because I have suspicion that `findElement` may be a slow operation.
