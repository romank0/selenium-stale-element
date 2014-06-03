package net.selemium.stalesafe;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;
import com.google.common.base.Throwables;

public class ReconnactableWebElementInvocationHandler implements InvocationHandler {

	static class ExceptionWrapper extends RuntimeException {

		private static final long serialVersionUID = -1345788532431054613L;

		public ExceptionWrapper(Throwable cause) {
			super(cause);
		}
	}
	
	private By locator;
	private FluentWait<WebDriver> wait;

	public ReconnactableWebElementInvocationHandler(By locator, FluentWait<WebDriver> wait) {
		this.locator = locator;
		this.wait = wait;
	}

	@Override
	public Object invoke(Object proxy, final Method method, final Object[] args)
			throws Exception {
		try {
			return wait.until(new Function<WebDriver, Object>() {
				@Override
				public Object apply(WebDriver input) {
					try {
						return method.invoke(input.findElement(locator), args);
					} catch (InvocationTargetException e) {
						Throwables.propagateIfInstanceOf(e.getTargetException(), RuntimeException.class);
						throw new ExceptionWrapper(e.getTargetException());
					} catch (Exception e) {
						Throwables.propagateIfInstanceOf(e, RuntimeException.class);
						throw new ExceptionWrapper(e);
					}
				}
			});
		} catch (ExceptionWrapper e) {
			throw (Exception)e.getCause();
		}
		
	}

}
