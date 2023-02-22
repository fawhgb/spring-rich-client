/*
 * Copyright (c) 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.richclient.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationServicesLocator;
import org.springframework.richclient.application.config.DefaultApplicationLifecycleAdvisor;
import org.springframework.richclient.security.support.DefaultApplicationSecurityManager;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.LockedException;
import org.springframework.security.SpringSecurityException;

/**
 * Test cases for the DefaultApplicationSecurityManager implementation.
 * 
 * @author Larry Streepy
 * 
 */
public class DefaultApplicationSecurityManagerTests {

	private ClassPathXmlApplicationContext ac;
	private EventCounter eventCounter;

	/**
	 * Configure an Application instance with the specified context file.
	 * 
	 * @param ctxFileName Name of context configuration file to read, may be null
	 */
	private void prepareApplication(String ctxFileName) {
		Application.load(null);
		ApplicationServicesLocator.load(null);
		Application app = new Application(new DefaultApplicationLifecycleAdvisor());

		if (ctxFileName != null) {
			ac = new ClassPathXmlApplicationContext("org/springframework/richclient/security/" + ctxFileName);
			app.setApplicationContext(ac);
		}
	}

	@Test
	public void testConfiguration() {
		prepareApplication("security-test-ctx.xml");

		Object asm = ac.getBean("applicationSecurityManager");
		Object am = ac.getBean("authenticationManager");

		assertTrue(asm instanceof ApplicationSecurityManager,
				"securityManager must implement ApplicationSecurityManager");
		assertTrue(asm instanceof DefaultApplicationSecurityManager,
				"securityManager must be instance of DefaultApplicationSecurityManager");
		assertTrue(am instanceof AuthenticationManager, "authenticationManager must implement AuthenticationManager");
		assertTrue(am instanceof TestAuthenticationManager,
				"authenticationManager must be instance of TestAuthenticationManager");
		assertEquals(asm, ApplicationServicesLocator.services().getService(ApplicationSecurityManager.class));
	}

	@Test
	public void testSecurityEvents() {
		prepareApplication("security-test-ctx.xml");
		eventCounter = (EventCounter) ac.getBean("eventCounter");

		ApplicationSecurityManager asm = (ApplicationSecurityManager) ApplicationServicesLocator.services()
				.getService(ApplicationSecurityManager.class);
		eventCounter.resetCounters();
		asm.doLogin(TestAuthenticationManager.VALID_USER1);
		testCounters(1, 0, 1, 0);
		assertTrue(asm.isUserLoggedIn(), "User should be logged in now");
		assertEquals(asm.getAuthentication(), TestAuthenticationManager.VALID_USER1,
				"Authentiation token should be == VALID_USER1");

		// Test various failed logins, current login shouldn't be affected
		doOneFailed(TestAuthenticationManager.BAD_CREDENTIALS, BadCredentialsException.class);
		doOneFailed(TestAuthenticationManager.LOCKED, LockedException.class);

		// Logout - generates an Authentication event and a Logout event
		eventCounter.resetCounters();
		asm.doLogout();
		testCounters(1, 0, 0, 1);
		assertNull(asm.getAuthentication(), "Authentication token should now be null");
	}

	@Test
	public void testUserInRole() {
		prepareApplication("security-test-ctx.xml");
		ApplicationSecurityManager asm = (ApplicationSecurityManager) ApplicationServicesLocator.services()
				.getService(ApplicationSecurityManager.class);
		asm.doLogin(TestAuthenticationManager.VALID_USER1);

		assertTrue(asm.isUserInRole(TestAuthenticationManager.ROLE_EXPECTED), "User should be in role ROLE_EXPECTED");
		assertFalse(asm.isUserInRole("ROLE_UNEXPECTED"), "User should not be in role ROLE_UNEXPECTED");
	}

	@Test
	public void testLoginAfterLogin() {
		prepareApplication("security-test-ctx.xml");
		eventCounter = (EventCounter) ac.getBean("eventCounter");

		ApplicationSecurityManager asm = (ApplicationSecurityManager) ApplicationServicesLocator.services()
				.getService(ApplicationSecurityManager.class);

		asm.doLogout(); // Start with no one logged in
		asm.doLogin(TestAuthenticationManager.VALID_USER1);

		eventCounter.resetCounters();
		asm.doLogin(TestAuthenticationManager.VALID_USER2);

		testCounters(1, 0, 1, 0);
		assertTrue(asm.isUserLoggedIn(), "User should be logged in now");
		assertEquals(asm.getAuthentication(), TestAuthenticationManager.VALID_USER2,
				"Authentiation token should be == VALID_USER2");
	}

	@Test
	public void testAutoConfigurationOnNew() {
		// Ensure that the DefaultApplicationSecurityManager will properly
		// auto-configure when it is created with "new" instead of through an
		// application context.

		prepareApplication("security-test-autoconfig-ctx.xml");
		ApplicationSecurityManager asm = new DefaultApplicationSecurityManager(true);

		// Ensure it's the right one
		Object am = ac.getBean("authenticationManager");
		assertEquals(am, asm.getAuthenticationManager(), "Wrong authentication manager configured");
	}

	@Test
	public void testAutoConfigurationFailsWithoutContext() {
		// Test that the auto-configuration fails when there is no context
		prepareApplication(null); // No context
		try {
			ApplicationServicesLocator.services().getService(ApplicationSecurityManager.class);
			fail("Shouldn't be able to auto-configure without context");
		} catch (Exception e) {
			// expected
		}
	}

	@Test
	public void testAutoConfigurationFromServices() {
		// Test that the application services will provide a properly auto-configured
		// security manager.

		prepareApplication("security-test-autoconfig-ctx.xml");
		ApplicationSecurityManager asm = (ApplicationSecurityManager) ApplicationServicesLocator.services()
				.getService(ApplicationSecurityManager.class);

		// Ensure it's the right one
		Object am = ac.getBean("authenticationManager");
		assertEquals(am, asm.getAuthenticationManager(), "Wrong authentication manager configured");
	}

	/**
	 * Do one failed authentication invocation and test results.
	 * 
	 * @param authentication token to use
	 * @param exceptionType  Type of exception that should be thrown
	 */
	private void doOneFailed(Authentication authentication, Class exceptionType) {
		ApplicationSecurityManager asm = (ApplicationSecurityManager) ApplicationServicesLocator.services()
				.getService(ApplicationSecurityManager.class);
		Authentication current = asm.getAuthentication();

		eventCounter.resetCounters();
		try {
			asm.doLogin(authentication);
			fail(exceptionType.getName() + " should have been thrown");
		} catch (SpringSecurityException e) {
			// We expect an exception
			assertTrue(exceptionType.isAssignableFrom(e.getClass()),
					"Wrong exception thrown; expecting: " + exceptionType.getName());
			testCounters(0, 1, 0, 0);
			assertTrue(asm.isUserLoggedIn(), "User should still be logged in now");
			// Shouldn't have changed
			assertEquals(asm.getAuthentication(), current, "Authentiation token should not have changed");
		}
	}

	/**
	 * Test the event counters and ensure they all match.
	 */
	@Test
	private void testCounters(int authCount, int authFailedCount, int loginCount, int logoutCount) {
		assertEquals(authCount, eventCounter.authEventCount, "AuthenticationEventCount wrong");
		assertEquals(authFailedCount, eventCounter.authFailedEventCount, "AuthenticationFailedEventCount wrong");
		assertEquals(loginCount, eventCounter.loginEventCount, "LoginEventCount wrong");
		assertEquals(logoutCount, eventCounter.logoutEventCount, "LogoutEventCount wrong");
	}

	/**
	 * Class to count interesting security lifecycle events.
	 */
	public static class EventCounter implements ApplicationListener {
		public int authEventCount = 0;
		public int authFailedEventCount = 0;
		public int loginEventCount = 0;
		public int logoutEventCount = 0;

		public EventCounter() {
		}

		@Override
		public void onApplicationEvent(ApplicationEvent event) {
			if (event instanceof AuthenticationEvent) {
				authEventCount += 1;
			} else if (event instanceof AuthenticationFailedEvent) {
				authFailedEventCount += 1;
			} else if (event instanceof LoginEvent) {
				loginEventCount += 1;
			} else if (event instanceof LogoutEvent) {
				logoutEventCount += 1;
			}
		}

		public void resetCounters() {
			authEventCount = authFailedEventCount = loginEventCount = logoutEventCount = 0;
		}
	}
}
