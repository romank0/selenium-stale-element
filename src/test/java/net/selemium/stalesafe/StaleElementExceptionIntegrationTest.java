package net.selemium.stalesafe;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestSite.class)
@WebAppConfiguration
@IntegrationTest
public class StaleElementExceptionIntegrationTest  {
	
	private static final String CONTAINER_ID = "elementContainer";

	private static final String ELEMENT_ID = "element";

	private WebDriver driver;
	
	private WebDriverWait wait;

	@Before
	public  void setUp() {
	   driver = new FirefoxDriver();

       driver.navigate().to("http://localhost:8080");
       
       wait = new WebDriverWait(driver, 10);
       
       wait.until(new Function<WebDriver, WebElement>() {
		@Override
		public WebElement apply(WebDriver input) {
			return driver.findElement(By.id(ELEMENT_ID));
		}
       });
	}

	@After
	public  void tearDown() {
		if (driver == null) {
			driver.close();
		}
	}
	
    @Test
	public void testChangingElement() throws InterruptedException {
        WebElement element = driver.findElement(By.id(ELEMENT_ID));
        final WebElement container = driver.findElement(By.id(CONTAINER_ID));
        container.click();
        waitForElementDoDisappear(By.id(ELEMENT_ID));
        
        element.getAttribute("id");
    }

	private void waitForElementDoDisappear(final By element) {
		wait.until(new Predicate<WebDriver>() {
			@Override
			public boolean apply(WebDriver input) {
				try {
					driver.findElement(element);
					return false;
				} catch (NoSuchElementException e) {
					return true;
				}
			}
		});
	}
}