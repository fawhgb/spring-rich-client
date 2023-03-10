/**
 *
 */
package org.springframework.richclient.security.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationServicesLocator;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.ApplicationWindowFactory;
import org.springframework.richclient.application.config.ApplicationWindowConfigurer;
import org.springframework.richclient.application.config.DefaultApplicationLifecycleAdvisor;
import org.springframework.richclient.application.support.DefaultApplicationWindow;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.CommandManager;
import org.springframework.richclient.security.ApplicationSecurityManager;
import org.springframework.richclient.security.SecurityController;
import org.springframework.richclient.security.SecurityControllerManager;
import org.springframework.richclient.security.TestAuthenticationManager;
import org.springframework.security.core.Authentication;

/**
 * @author Larry Streepy
 *
 */
public class DefaultSecurityControllerManagerTests {
	private ClassPathXmlApplicationContext applicationContext;
	private TestAuthorizable testAuth1;
	private SecurityControllerManager manager;

	/*
	 * @see TestCase#setUp()
	 */
	@BeforeEach
	protected void setUp() throws Exception {
		Application.load(null);
		TestApplicationLifecycleAdvisor ala = new TestApplicationLifecycleAdvisor();
		ala.setWindowCommandBarDefinitions("org/springframework/richclient/security/support/test-command-ctx.xml");
		Application app = new Application(ala);
		applicationContext = new ClassPathXmlApplicationContext(
				"org/springframework/richclient/security/support/test-security-controller-ctx.xml");
		app.setApplicationContext(applicationContext);

		ala.setStartingPageId("start");
		ala.setApplication(app);
		app.openWindow("start");

		testAuth1 = (TestAuthorizable) applicationContext.getBean("testAuth1");
		manager = (SecurityControllerManager) ApplicationServicesLocator.services()
				.getService(SecurityControllerManager.class);

		// Prepare the command context
		ala.createWindowCommandManager();
	}

	/**
	 * Test alias registration
	 */
	@Test
	public void testRegisterSecurityControllerAlias() {
		SecurityController controller = new UserRoleSecurityController();
		manager.registerSecurityControllerAlias("newAlias", controller);

		assertEquals(controller, manager.getSecurityController("newAlias"), "Should be same controller");
	}

	/**
	 * Test obtaining controllers
	 */
	@Test
	public void testGetSecurityController() {
		SecurityController write = applicationContext.getBean("writeController", SecurityController.class);
		SecurityController admin = applicationContext.getBean("adminController", SecurityController.class);

		// test defaulting to bean id if no alias registered
		assertEquals(write, manager.getSecurityController("writeController"), "Should be same controller");
		assertEquals(admin, manager.getSecurityController("adminController"), "Should be same controller");

		// Test registered alias
		assertEquals(admin, manager.getSecurityController("adminAlias"), "Should be same controller");
	}

	/**
	 * Test the processing of beans referenced in the app context.
	 */
	@Test
	public void testApplicationContext() {
		ApplicationSecurityManager securityManager = (ApplicationSecurityManager) ApplicationServicesLocator.services()
				.getService(ApplicationSecurityManager.class);

		int authorizeCount = 1;

		assertFalse(testAuth1.isAuthorized(), "Object should not be authorized");
		assertEquals(authorizeCount++, testAuth1.getAuthCount(), "Object should be updated");

		CommandManager cmgr = Application.instance().getActiveWindow().getCommandManager();
		ActionCommand cmdWrite = cmgr.getActionCommand("cmdWrite");
		ActionCommand cmdAdmin = cmgr.getActionCommand("cmdAdmin");
		ActionCommand cmdAdminAlias = cmgr.getActionCommand("cmdAdminAlias");

		assertFalse(cmdWrite.isAuthorized(), "Object should not be authorized");
		assertFalse(cmdAdmin.isAuthorized(), "Object should not be authorized");
		assertFalse(cmdAdminAlias.isAuthorized(), "Object should not be authorized");

		// Now login with ROLE_WRITE
		Authentication auth = TestAuthenticationManager.makeAuthentication("test", "test", "ROLE_WRITE");
		securityManager.doLogin(auth);

		assertTrue(cmdWrite.isAuthorized(), "Object should be authorized");
		assertFalse(cmdAdmin.isAuthorized(), "Object should not be authorized");
		assertFalse(cmdAdminAlias.isAuthorized(), "Object should not be authorized");
		assertFalse(testAuth1.isAuthorized(), "Object should not be authorized");
		assertEquals(authorizeCount++, testAuth1.getAuthCount(), "Object should be updated");

		// Now login with ROLE_ADMIN
		auth = TestAuthenticationManager.makeAuthentication("test", "test", "ROLE_ADMIN");
		securityManager.doLogin(auth);

		assertTrue(cmdWrite.isAuthorized(), "Object should be authorized");
		assertTrue(cmdAdmin.isAuthorized(), "Object should be authorized");
		assertTrue(cmdAdminAlias.isAuthorized(), "Object should be authorized");
		assertTrue(testAuth1.isAuthorized(), "Object should be authorized");
		assertEquals(authorizeCount++, testAuth1.getAuthCount(), "Object should be updated");
	}

	/**
	 * Test that the authorized state overrides the enabled state
	 */
	@Test
	public void testAuthorizedOverridesEnabled() {
		ApplicationSecurityManager securityManager = (ApplicationSecurityManager) ApplicationServicesLocator.services()
				.getService(ApplicationSecurityManager.class);
		CommandManager cmgr = Application.instance().getActiveWindow().getCommandManager();
		ActionCommand cmdWrite = cmgr.getActionCommand("cmdWrite");

		// We start with no authentication, so nothing should be authorized
		assertFalse(cmdWrite.isAuthorized(), "Object should not be authorized");
		assertFalse(cmdWrite.isEnabled(), "Object should not be enabled");

		// Try to enable them, should not happen
		cmdWrite.setEnabled(true);
		assertFalse(cmdWrite.isEnabled(), "Object should not be enabled");

		// Now authorize it
		Authentication auth = TestAuthenticationManager.makeAuthentication("test", "test", "ROLE_WRITE");
		securityManager.doLogin(auth);

		assertTrue(cmdWrite.isAuthorized(), "Object should be authorized");
		assertTrue(cmdWrite.isEnabled(), "Object should be enabled");

		// Now we should be able to disable and re-enabled it
		cmdWrite.setEnabled(false);
		assertFalse(cmdWrite.isEnabled(), "Object should not be enabled");
		cmdWrite.setEnabled(true);
		assertTrue(cmdWrite.isEnabled(), "Object should be enabled");

		// Now leave it disabled, remove the authorization, re-authorize and it
		// should still be disabled
		cmdWrite.setEnabled(false);
		assertFalse(cmdWrite.isEnabled(), "Object should not be enabled");
		securityManager.doLogout();

		assertFalse(cmdWrite.isAuthorized(), "Object should not be authorized");
		assertFalse(cmdWrite.isEnabled(), "Object should not be enabled");

		securityManager.doLogin(auth);

		assertTrue(cmdWrite.isAuthorized(), "Object should be authorized");
		assertFalse(cmdWrite.isEnabled(), "Object should not be enabled");
	}

	public static class TestApplicationWindowFactory implements ApplicationWindowFactory {
		@Override
		public ApplicationWindow createApplicationWindow() {
			return new TestApplicationWindow();
		}
	}

	/**
	 * Special ApplicationWindow class for testing.
	 */
	public static class TestApplicationWindow extends DefaultApplicationWindow {

		public TestApplicationWindow() {
			super(1);
		}

		@Override
		public void showPage(String pageId) {
			System.out.println("showPage: " + pageId);
		}
	}

	public static class TestApplicationLifecycleAdvisor extends DefaultApplicationLifecycleAdvisor {

		public TestApplicationLifecycleAdvisor() {
			setWindowCommandManagerBeanName("windowCommandManager");
		}

		@Override
		public void onPreWindowOpen(ApplicationWindowConfigurer configurer) {
			// Do nothing
		}
	}
}
