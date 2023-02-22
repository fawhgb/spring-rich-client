/**
 * 
 */
package org.springframework.richclient.security.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttribute;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.TestingAuthenticationToken;

/**
 * @author Larry Streepy
 * 
 */
public class UserRoleSecurityControllerTests {

	private TestUserRoleSecurityController controller;

	/*
	 * @see TestCase#setUp()
	 */
	@BeforeEach
	protected void setUp() throws Exception {
		controller = new TestUserRoleSecurityController();
	}

	/**
	 * Test that the role string is properly parsed
	 */
	@Test
	public void testSetAuthorizingRoles() {
		controller.setAuthorizingRoles("ROLE_1,ROLE_2");

		ConfigAttributeDefinition cad = controller.getParsedConfigs();
		assertTrue(cad.getConfigAttributes().size() == 2, "Should be 2 roles");

		Iterator iter = cad.getConfigAttributes().iterator();
		ConfigAttribute attr1 = (ConfigAttribute) iter.next();
		ConfigAttribute attr2 = (ConfigAttribute) iter.next();

		assertEquals(attr1.getAttribute(), "ROLE_1", "Should be ROLE_1");
		assertEquals(attr2.getAttribute(), "ROLE_2", "Should be ROLE_2");
	}

	/**
	 * Test that objects are properly authorized when the user holds any of the
	 * indicated roles.
	 */
	@Test
	public void testAuthorization() {
		controller.setAuthorizingRoles("ROLE_1,ROLE_2");

		TestAuthorizable a1 = new TestAuthorizable(false);

		controller.addControlledObject(a1);
		assertFalse(a1.isAuthorized(), "Object should not be authorized");

		// Now set the authentication token so that it contains one of these roles
		Authentication auth = new TestingAuthenticationToken("USER1", "FOO",
				new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_1") });
		controller.setAuthenticationToken(auth);

		assertTrue(a1.isAuthorized(), "Object should be authorized");
		assertEquals(a1.getAuthCount(), 2, "Object should be updated");

		// Now to a token that does not contain one of the roles
		auth = new TestingAuthenticationToken("USER1", "FOO",
				new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_NOTFOUND") });
		controller.setAuthenticationToken(auth);

		assertFalse(a1.isAuthorized(), "Object should not be authorized");
		assertEquals(a1.getAuthCount(), 3, "Object should be updated");

		// Now to a null
		controller.setAuthenticationToken(null);

		assertFalse(a1.isAuthorized(), "Object should not be authorized");
		assertEquals(a1.getAuthCount(), 4, "Object should be updated");
	}

	/**
	 * More accessible implementation.
	 */
	public class TestUserRoleSecurityController extends UserRoleSecurityController {
		public ConfigAttributeDefinition getParsedConfigs() {
			return getConfigAttributeDefinition(null);
		}
	}
}
