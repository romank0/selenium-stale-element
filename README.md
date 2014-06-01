

This is the answer to [post](http://biercoff.com/found-my-own-solution-for-webdriver-staleelementreferenceexception-problem/) 
about solution of `StaleElementReferenceException` in selenium test.


_Disclaimer_: I never used selenium so I may misundertand something. I still hope my thoughs make some sense.

===========================

When I read the post my first though was that this functionality should be built in into `WebDriver`. And if it is not there should be reasons.

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
            return driver.findElement(By.id(id).getAttribute(attribute);
          }
        }
    );

 }
```

Or in java 8 as 

```
 public String getValueFromId(final String id, final String attribute) throws Exception {
        
    return manager.wait().until(
      (Driver driver) -> driver.findElement(By.id(id).getAttribute(attribute);
    );

 }
```

While this may be a good improvement I think this is not enough. For big codebase of test changing all the places where elements are used requires lot of effort and is painful. I can see several options how to deal with it

### ReconnectableElement

A decorator for `WebElement` can be created which 
1. wraps real `WebElement` and stores its identity
2. has reference to `WebDriver` and can requery element in case of `StaleElementReferenceException`   

The implementation will look like this:

```
class ReconnectableWebElement implements WebElement {

 private WebElement wrappedElement;
 private WebDriver driver;

}






