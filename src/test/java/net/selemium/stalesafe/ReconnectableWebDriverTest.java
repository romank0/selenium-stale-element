package net.selemium.stalesafe;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ReconnectableWebDriverTest {

	private static final By ELEMENT_LOCATOR = By.id("id");
	private static final String ATTRIBUTE_NAME = "name";
	private static final String VALUE = "value";
	private WebDriver wrappedDriverMock = mock(WebDriver.class);
	private ReconnectableWebDriver sut = new ReconnectableWebDriver(wrappedDriverMock);
	private WebElement elementMock = mock(WebElement.class);

	@Test
	public void findElementsDelegatesToWrappedDriver() {
		sut.findElements(ELEMENT_LOCATOR);
		verify(wrappedDriverMock).findElements(ELEMENT_LOCATOR);
	}

	@Test
	public void elementsRetrievedFromDriverIgnoreStaleElementReferenceException() {
		Mockito.when(wrappedDriverMock.findElement(ELEMENT_LOCATOR)).thenReturn(elementMock);
		Mockito.when(elementMock.getAttribute(ATTRIBUTE_NAME))
			.thenThrow(new StaleElementReferenceException(""))
			.thenReturn(VALUE);
		assertThat(sut.findElement(ELEMENT_LOCATOR).getAttribute(ATTRIBUTE_NAME), equalTo(VALUE));
	}

}
