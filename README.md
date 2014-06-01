

This is the answer to [post](http://biercoff.com/found-my-own-solution-for-webdriver-staleelementreferenceexception-problem/) 
about solution of `StaleElementReferenceException` in selenium test.


_Disclaimer_: I never used selenium so I may misundertand something. I still hope my thoughs make some sense.

===========================

When I read the post my first though was that this functionality should be built in into `WebDriver`. And if it is not there should be reasons.

Quick search shows that there really is very similar thing to tackle such situation - `WebDriverWait`. It does the same thing and it addresses the first issue of the solution - the necessity to create `RetryStrategy`. `WebDriverWait` is stateless so one instance can be reused.

Instead this 

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
            return manager.getDriver().findElement(By.id(id).getAttribute(attribute);
          }
        }
    );

 }
```

With the following definition of `Manager.wait`:

```
 public WebDriverWait wait() {
    // creation of new instance is not necessary here
    return new WebDriverWait(this.getDriver(), TIMEOUT, SLEEP_BETWEEN_RETRIES)
            .ignoring(StaleElementReferenceException.class);
 }

```

