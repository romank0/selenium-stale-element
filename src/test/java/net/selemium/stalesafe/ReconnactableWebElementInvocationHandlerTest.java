package net.selemium.stalesafe;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

public class ReconnactableWebElementInvocationHandlerTest {

	static class ExceptionToIgnore extends RuntimeException {

		private static final long serialVersionUID = 6298437479101167259L;
	}
	
	Object proxy = new Object();
	Method getAttributeMethod;
	
	final private static By LOCATOR = By.id("id");
	private static final String ATTRIBUTE_NAME = "some-attribute";
	private static final String ATTRIBUTE_VALUE = "value";
	
	WebDriver driverMock = Mockito.mock(WebDriver.class);
	FluentWait<WebDriver> wait = new FluentWait<WebDriver>(driverMock);
	ReconnactableWebElementInvocationHandler sut = new ReconnactableWebElementInvocationHandler(LOCATOR, wait);
	WebElement elementMock = Mockito.mock(WebElement.class);
	
	@Before
	public void setUp() {
		try {
			getAttributeMethod = WebElement.class.getMethod("getAttribute", String.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void delegatesCallsToElementRetrievedFromWrappedDriver() throws Throwable {
		Mockito.when(driverMock.findElement(LOCATOR)).thenReturn(elementMock);
		Mockito.when(elementMock.getAttribute(ATTRIBUTE_NAME))
			.thenReturn(ATTRIBUTE_VALUE);
		sut.invoke(proxy, getAttributeMethod, new Object[] {ATTRIBUTE_NAME});
		Mockito.verify(elementMock).getAttribute(ATTRIBUTE_NAME);
	}

	@Test
	public void retriesIfCallToDeletageThrowedExceptionToWaitFor() throws Throwable {
		wait.ignoring(ExceptionToIgnore.class);
		Mockito.when(driverMock.findElement(LOCATOR)).thenReturn(elementMock);
		Mockito.when(elementMock.getAttribute(ATTRIBUTE_NAME))
				.thenThrow(new ExceptionToIgnore())
				.thenReturn(ATTRIBUTE_VALUE);
		Object result = sut.invoke(proxy, getAttributeMethod, new Object[] {ATTRIBUTE_NAME});
		assertThat(result, is(instanceOf(String.class)));
		assertThat((String)result, equalTo(ATTRIBUTE_VALUE));
	}
}
