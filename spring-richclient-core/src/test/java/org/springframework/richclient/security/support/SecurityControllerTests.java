/**
 * 
 */
package org.springframework.richclient.security.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.AccessDecisionManager;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttribute;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.providers.TestingAuthenticationToken;

/**
 * @author Larry Streepy
 * 
 */
public class SecurityControllerTests {

	private TestAbstractSecurityController controller;
	private TestAccessDecisionManager accessDecisionManager;

	/*
	 * @see TestCase#setUp()
	 */
	@BeforeEach
	protected void setUp() throws Exception {
		controller = new TestAbstractSecurityController();
		accessDecisionManager = new TestAccessDecisionManager();
		controller.setAccessDecisionManager(accessDecisionManager);
	}

	/**
	 * Test that setting the authentication token updates all controlled objects.
	 */
	@Test
	public void testSetAuthenticationToken() {

		// Validate that the controller updates the controlled objects whenever the
		// Authentication token is updated
		TestAuthorizable a1 = new TestAuthorizable(true);
		TestAuthorizable a2 = new TestAuthorizable(true);
		controller.addControlledObject(a1);
		controller.addControlledObject(a2);
		assertFalse(a1.isAuthorized(), "Object should not be authorized");
		assertFalse(a2.isAuthorized(), "Object should not be authorized");

		a1.resetAuthCount();
		a2.resetAuthCount();

		// Set the decision manager to authorize
		accessDecisionManager.setDecisionValue(true);

		// Now install a token, a should be updated
		controller.setAuthenticationToken(new TestingAuthenticationToken("USER2", "FOO"));
		assertTrue(a1.isAuthorized(), "Object should be authorized");
		assertEquals(a1.getAuthCount(), 1, "Object should be updated");
		assertTrue(a2.isAuthorized(), "Object should be authorized");
		assertEquals(a2.getAuthCount(), 1, "Object should be updated");

		controller.setAuthenticationToken(null);
		assertFalse(a1.isAuthorized(), "Object should not be authorized");
		assertEquals(a1.getAuthCount(), 2, "Object should be updated");
		assertFalse(a2.isAuthorized(), "Object should not be authorized");
		assertEquals(a2.getAuthCount(), 2, "Object should be updated");

		// Set the decision manager to NOT authorize
		accessDecisionManager.setDecisionValue(false);

		// Now install a token, a should be updated
		controller.setAuthenticationToken(new TestingAuthenticationToken("USER2", "FOO"));
		assertFalse(a1.isAuthorized(), "Object should not be authorized");
		assertEquals(a1.getAuthCount(), 3, "Object should be updated");
		assertFalse(a2.isAuthorized(), "Object should not be authorized");
		assertEquals(a2.getAuthCount(), 3, "Object should be updated");
	}

	@Test
	public void testSetControlledObjects() {

		TestAuthorizable a1 = new TestAuthorizable(true);
		TestAuthorizable a2 = new TestAuthorizable(true);
		ArrayList goodList = new ArrayList();
		goodList.add(a1);
		goodList.add(a2);

		ArrayList badList = new ArrayList();
		badList.add(new Object());

		try {
			controller.setControlledObjects(badList);
			fail("Should reject objects that aren't Authorizable");
		} catch (IllegalArgumentException e) {
			// expected
		}

		controller.setControlledObjects(goodList);
		assertFalse(a1.isAuthorized(), "Object should not be authorized");
		assertTrue(a1.getAuthCount() == 1, "Object should be updated");
		assertFalse(a2.isAuthorized(), "Object should not be authorized");
		assertTrue(a2.getAuthCount() == 1, "Object should be updated");
	}

	/**
	 * Test that added objects are initially configured.
	 */
	@Test
	public void testAddControlledObject() {

		// Install an authentication token
		controller.setAuthenticationToken(new TestingAuthenticationToken("USER2", "FOO"));

		// Set the decision manager to authorize
		accessDecisionManager.setDecisionValue(true);

		TestAuthorizable a1 = new TestAuthorizable(false);
		controller.addControlledObject(a1);
		assertTrue(a1.isAuthorized(), "Object should be authorized");
		assertTrue(a1.getAuthCount() == 1, "Object should be updated");

		// Set the decision manager to NOT authorize
		accessDecisionManager.setDecisionValue(false);

		TestAuthorizable a2 = new TestAuthorizable(true);
		controller.addControlledObject(a2);
		assertFalse(a2.isAuthorized(), "Object should not be authorized");
		assertTrue(a2.getAuthCount() == 1, "Object should be updated");
	}

	/**
	 * Test that once removed an object is no longer updated.
	 */
	@Test
	public void testRemoveControlledObject() {
		TestAuthorizable a = new TestAuthorizable(true);
		controller.addControlledObject(a);
		assertFalse(a.isAuthorized(), "Object should not be authorized");
		assertTrue(a.getAuthCount() == 1, "Object should be updated");

		controller.removeControlledObject(a);
		a.resetAuthCount();

		// Set the decision manager to authorize
		accessDecisionManager.setDecisionValue(true);
		controller.setAuthenticationToken(new TestingAuthenticationToken("USER2", "FOO"));

		assertFalse(a.isAuthorized(), "Object should not be authorized");
		assertTrue(a.getAuthCount() == 0, "Object should not be updated");
	}

	/**
	 * Concrete implementation under test.
	 */
	public class TestAbstractSecurityController extends AbstractSecurityController {

		public TestAbstractSecurityController() {
		}

		@Override
		protected Object getSecuredObject() {
			return null;
		}

		@Override
		protected ConfigAttributeDefinition getConfigAttributeDefinition(Object securedObject) {
			return null;
		}
	}

	/**
	 * Controllable AccessDecisionManager
	 */
	public class TestAccessDecisionManager implements AccessDecisionManager {

		private boolean decisionValue;

		public void setDecisionValue(boolean decisionValue) {
			this.decisionValue = decisionValue;
		}

		@Override
		public void decide(Authentication authentication, Object object, ConfigAttributeDefinition config)
				throws AccessDeniedException {
			if (!decisionValue) {
				throw new AccessDeniedException("access denied");
			}
		}

		@Override
		public boolean supports(ConfigAttribute attribute) {
			return false;
		}

		@Override
		public boolean supports(Class clazz) {
			return false;
		}
	}
}
