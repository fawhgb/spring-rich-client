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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationServicesLocator;
import org.springframework.richclient.application.config.DefaultApplicationLifecycleAdvisor;
import org.springframework.richclient.security.support.DefaultApplicationSecurityManager;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.BadCredentialsException;

/**
 * @author Larry Streepy
 * 
 */
public class SecurityAwareConfigurerTests {

	private ClassPathXmlApplicationContext applicationContext;
	private AuthAwareBean authAwareBean;
	private LoginAwareBean loginAwareBean;
	private static int sequence = 0;
	private ApplicationSecurityManager securityManager;

	@BeforeEach
	protected void setUp() throws Exception {
		applicationContext = new ClassPathXmlApplicationContext(
				"org/springframework/richclient/security/security-test-configurer-ctx.xml");
		Application.load(null);
		Application app = new Application(new DefaultApplicationLifecycleAdvisor());
		app.setApplicationContext(applicationContext);

		securityManager = (ApplicationSecurityManager) ApplicationServicesLocator.services()
				.getService(ApplicationSecurityManager.class);
		authAwareBean = (AuthAwareBean) applicationContext.getBean("authAwareBean");
		loginAwareBean = (LoginAwareBean) applicationContext.getBean("loginAwareBean");
	}

	@Test
	public void testConfiguration() {
		Object asm = applicationContext.getBean("applicationSecurityManager");
		Object am = applicationContext.getBean("authenticationManager");
		Object sc = applicationContext.getBean("securityConfigurer");

		assertTrue(asm instanceof ApplicationSecurityManager,
				"securityManager must implement ApplicationSecurityManager");
		assertTrue(asm instanceof DefaultApplicationSecurityManager,
				"securityManager must be instance of DefaultApplicationSecurityManager");
		assertTrue(am instanceof AuthenticationManager, "authenticationManager must implement AuthenticationManager");
		assertTrue(am instanceof TestAuthenticationManager,
				"authenticationManager must be instance of TestAuthenticationManager");
		assertEquals(asm, ApplicationServicesLocator.services().getService(ApplicationSecurityManager.class));
		assertTrue(sc instanceof SecurityAwareConfigurer, "securityConfigurer must implement SecurityAwareConfigurer");
	}

	@Test
	public void testAuthenticationAware() {

		securityManager.doLogin(TestAuthenticationManager.VALID_USER1);
		assertEquals(authAwareBean.authentication, TestAuthenticationManager.VALID_USER1,
				"Authentication token should be VALID_USER1");

		securityManager.doLogin(TestAuthenticationManager.VALID_USER2);
		assertEquals(authAwareBean.authentication, TestAuthenticationManager.VALID_USER2,
				"Authentication token should be VALID_USER2");

		try {
			securityManager.doLogin(TestAuthenticationManager.BAD_CREDENTIALS);
			fail("Exception should have been thrown");
		} catch (BadCredentialsException e) {
			// Shouldn't have been changed
			assertEquals(authAwareBean.authentication, TestAuthenticationManager.VALID_USER2,
					"Authentication token should be VALID_USER2");
		}

		securityManager.doLogout();
		assertNull(authAwareBean.authentication, "Authentication token should have been cleared");
	}

	@Test
	public void testLoginAware() {

		securityManager.doLogin(TestAuthenticationManager.VALID_USER1);
		assertEquals(loginAwareBean.authentication, TestAuthenticationManager.VALID_USER1,
				"Authentication token should be VALID_USER1");
		assertEquals(authAwareBean.authentication, loginAwareBean.authentication,
				"Authentication tokens on beans should be equal ");
		assertTrue(authAwareBean.sequence < loginAwareBean.sequence,
				"LoginAware notifications should happen after AuthAware");

		loginAwareBean.reset();
		securityManager.doLogout();
		assertTrue(loginAwareBean.logoutCalled, "Logout should be called");
		assertEquals(loginAwareBean.oldAuthentication, TestAuthenticationManager.VALID_USER1,
				"Previous token should be VALID_USER1");
		assertTrue(authAwareBean.sequence < loginAwareBean.sequence,
				"LoginAware notifications should happen after AuthAware");
	}

	/**
	 * Class to test automatic notification.
	 */
	public static class AuthAwareBean implements AuthenticationAware {

		public Authentication authentication = null;
		public int sequence;

		@Override
		public void setAuthenticationToken(Authentication authentication) {
			this.authentication = authentication;
			sequence = SecurityAwareConfigurerTests.sequence++;
		}

		public void reset() {
			authentication = null;
			sequence = 0;
		}
	}

	/**
	 * Class to test automatic notification of login/logout events.
	 */
	public static class LoginAwareBean implements LoginAware {
		public Authentication authentication = null;
		public Authentication oldAuthentication = null;
		public int sequence;
		public boolean logoutCalled = false;

		@Override
		public void userLogin(Authentication authentication) {
			this.authentication = authentication;
			sequence = SecurityAwareConfigurerTests.sequence++;
		}

		@Override
		public void userLogout(Authentication authentication) {
			this.oldAuthentication = authentication;
			logoutCalled = true;
			sequence = SecurityAwareConfigurerTests.sequence++;
		}

		public void reset() {
			authentication = null;
			oldAuthentication = null;
			sequence = 0;
			logoutCalled = false;
		}
	}
}
