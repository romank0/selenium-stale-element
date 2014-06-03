package net.selemium.stalesafe;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ReconnectableWebDriver implements WebDriver {

	private static final long TIMEOUT_IN_SECONDS = 5;
	private WebDriver delegate;
	private FluentWait<WebDriver> wait;
	
	public ReconnectableWebDriver(WebDriver wrappedDriver) {
		this(wrappedDriver, TIMEOUT_IN_SECONDS);
	}

	public ReconnectableWebDriver(WebDriver wrappedDriver, long timeoutInSeconds) {
		this(wrappedDriver, new WebDriverWait(wrappedDriver, timeoutInSeconds));
	}

	public ReconnectableWebDriver(WebDriver wrappedDriver, long timeoutInSeconds, long sleepInMilliseconds) {
		this(wrappedDriver, new WebDriverWait(wrappedDriver, timeoutInSeconds, sleepInMilliseconds));
	}

	public ReconnectableWebDriver(WebDriver wrappedDriver, FluentWait<WebDriver> wait) {
		this.delegate = wrappedDriver;
		this.wait = wait.ignoring(StaleElementReferenceException.class);
	}
	
	public void get(String url) {
		delegate.get(url);
	}

	public String getCurrentUrl() {
		return delegate.getCurrentUrl();
	}

	public String getTitle() {
		return delegate.getTitle();
	}

	public List<WebElement> findElements(By by) {
		return delegate.findElements(by);
	}

	public WebElement findElement(By by) {
		return (WebElement) Proxy.newProxyInstance(getClass().getClassLoader(), 
				new Class[] { WebElement.class }, 
				new ReconnactableWebElementInvocationHandler(by, wait));
	}

	public String getPageSource() {
		return delegate.getPageSource();
	}

	public void close() {
		delegate.close();
	}

	public void quit() {
		delegate.quit();
	}

	public Set<String> getWindowHandles() {
		return delegate.getWindowHandles();
	}

	public String getWindowHandle() {
		return delegate.getWindowHandle();
	}

	public TargetLocator switchTo() {
		return delegate.switchTo();
	}

	public Navigation navigate() {
		return delegate.navigate();
	}

	public Options manage() {
		return delegate.manage();
	}

}
