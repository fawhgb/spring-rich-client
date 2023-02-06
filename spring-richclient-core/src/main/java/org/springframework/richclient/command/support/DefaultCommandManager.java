/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.richclient.command.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.richclient.application.ApplicationServicesLocator;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.ActionCommandExecutor;
import org.springframework.richclient.command.ActionCommandInterceptor;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.CommandGroupFactoryBean;
import org.springframework.richclient.command.CommandManager;
import org.springframework.richclient.command.CommandNotOfRequiredTypeException;
import org.springframework.richclient.command.CommandRegistry;
import org.springframework.richclient.command.CommandRegistryListener;
import org.springframework.richclient.command.CommandServices;
import org.springframework.richclient.command.ExclusiveCommandGroup;
import org.springframework.richclient.command.TargetableActionCommand;
import org.springframework.richclient.command.config.CommandButtonConfigurer;
import org.springframework.richclient.command.config.CommandConfigurer;
import org.springframework.richclient.command.config.CommandFaceDescriptor;
import org.springframework.richclient.factory.ButtonFactory;
import org.springframework.richclient.factory.ComponentFactory;
import org.springframework.richclient.factory.MenuFactory;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class DefaultCommandManager implements CommandManager, BeanPostProcessor, BeanFactoryAware {
	private final Log logger = LogFactory.getLog(getClass());

	private BeanFactory beanFactory;

	private final DefaultCommandRegistry commandRegistry = new DefaultCommandRegistry();

	private CommandServices commandServices;

	private CommandConfigurer commandConfigurer;

	public DefaultCommandManager() {

	}

	public DefaultCommandManager(CommandRegistry parent) {
		setParent(parent);
	}

	public DefaultCommandManager(CommandServices commandServices) {
		setCommandServices(commandServices);
	}

	public void setCommandServices(CommandServices commandServices) {
		Assert.notNull(commandServices, "A command services implementation is required");
		this.commandServices = commandServices;
	}

	public CommandServices getCommandServices() {
		if (commandServices == null) {
			commandServices = (CommandServices) ApplicationServicesLocator.services().getService(CommandServices.class);
		}
		return commandServices;
	}

	public void setParent(CommandRegistry parent) {
		commandRegistry.setParent(parent);
	}

	public CommandConfigurer getCommandConfigurer() {
		if (commandConfigurer == null) {
			commandConfigurer = (CommandConfigurer) ApplicationServicesLocator.services()
					.getService(CommandConfigurer.class);
		}
		return commandConfigurer;
	}

	public void setCommandConfigurer(CommandConfigurer commandConfigurer) {
		Assert.notNull(commandConfigurer, "command configurer must not be null");
		this.commandConfigurer = commandConfigurer;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public ComponentFactory getComponentFactory() {
		return getCommandServices().getComponentFactory();
	}

	@Override
	public ButtonFactory getToolBarButtonFactory() {
		return getCommandServices().getButtonFactory();
	}

	@Override
	public ButtonFactory getButtonFactory() {
		return getCommandServices().getButtonFactory();
	}

	@Override
	public MenuFactory getMenuFactory() {
		return getCommandServices().getMenuFactory();
	}

	@Override
	public CommandButtonConfigurer getDefaultButtonConfigurer() {
		return getCommandServices().getDefaultButtonConfigurer();
	}

	@Override
	public CommandButtonConfigurer getToolBarButtonConfigurer() {
		return getCommandServices().getToolBarButtonConfigurer();
	}

	@Override
	public CommandButtonConfigurer getMenuItemButtonConfigurer() {
		return getCommandServices().getMenuItemButtonConfigurer();
	}

	@Override
	public CommandButtonConfigurer getPullDownMenuButtonConfigurer() {
		return getCommandServices().getPullDownMenuButtonConfigurer();
	}

	@Override
	public CommandFaceDescriptor getFaceDescriptor(AbstractCommand command, String faceDescriptorId) {
		if (beanFactory == null) {
			return null;
		}
		try {
			return (CommandFaceDescriptor) beanFactory.getBean(command.getId() + "." + faceDescriptorId,
					CommandFaceDescriptor.class);
		} catch (NoSuchBeanDefinitionException e) {
			try {
				return (CommandFaceDescriptor) beanFactory.getBean(faceDescriptorId, CommandFaceDescriptor.class);
			} catch (NoSuchBeanDefinitionException ex) {
				return null;
			}
		}
	}

	@Override
	public ActionCommand getActionCommand(String commandId) {
		return (ActionCommand) commandRegistry.getCommand(commandId, ActionCommand.class);
	}

	@Override
	public CommandGroup getCommandGroup(String groupId) {
		return (CommandGroup) commandRegistry.getCommand(groupId, CommandGroup.class);
	}

	@Override
	public boolean containsCommandGroup(String groupId) {
		return commandRegistry.containsCommandGroup(groupId);
	}

	@Override
	public boolean containsActionCommand(String commandId) {
		return commandRegistry.containsActionCommand(commandId);
	}

	@Override
	public void addCommandInterceptor(String commandId, ActionCommandInterceptor interceptor) {
		getActionCommand(commandId).addCommandInterceptor(interceptor);
	}

	@Override
	public void removeCommandInterceptor(String commandId, ActionCommandInterceptor interceptor) {
		getActionCommand(commandId).removeCommandInterceptor(interceptor);
	}

	@Override
	public void registerCommand(AbstractCommand command) {
		if (logger.isDebugEnabled()) {
			logger.debug("Configuring and registering new command '" + command.getId() + "'");
		}
		configure(command);
		commandRegistry.registerCommand(command);
	}

	@Override
	public void setTargetableActionCommandExecutor(String commandId, ActionCommandExecutor executor) {
		commandRegistry.setTargetableActionCommandExecutor(commandId, executor);
	}

	@Override
	public void addCommandRegistryListener(CommandRegistryListener l) {
		this.commandRegistry.addCommandRegistryListener(l);
	}

	@Override
	public void removeCommandRegistryListener(CommandRegistryListener l) {
		this.commandRegistry.removeCommandRegistryListener(l);
	}

	@Override
	public TargetableActionCommand createTargetableActionCommand(String commandId, ActionCommandExecutor delegate) {
		Assert.notNull(commandId, "Registered targetable action commands must have an id.");
		TargetableActionCommand newCommand = new TargetableActionCommand(commandId, delegate);
		registerCommand(newCommand);
		return newCommand;
	}

	@Override
	public CommandGroup createCommandGroup(String groupId, Object[] members) {
		Assert.notNull(groupId, "Registered command groups must have an id.");
		CommandGroup newGroup = new CommandGroupFactoryBean(groupId, this.commandRegistry, this, members)
				.getCommandGroup();
		registerCommand(newGroup);
		return newGroup;
	}

	@Override
	public ExclusiveCommandGroup createExclusiveCommandGroup(String groupId, Object[] members) {
		Assert.notNull(groupId, "Registered exclusive command groups must have an id.");
		CommandGroupFactoryBean newGroupFactory = new CommandGroupFactoryBean(groupId, this.commandRegistry, this,
				members);
		newGroupFactory.setExclusive(true);
		registerCommand(newGroupFactory.getCommandGroup());
		return (ExclusiveCommandGroup) newGroupFactory.getCommandGroup();
	}

	@Override
	public AbstractCommand configure(AbstractCommand command) {
		return getCommandConfigurer().configure(command);
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof AbstractCommand) {
			registerCommand((AbstractCommand) bean);
		}
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof CommandGroupFactoryBean) {
			CommandGroupFactoryBean factory = (CommandGroupFactoryBean) bean;
			factory.setCommandRegistry(commandRegistry);
		} else if (bean instanceof AbstractCommand) {
			configure((AbstractCommand) bean);
		}
		return bean;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsCommand(String commandId) {
		return this.commandRegistry.containsCommand(commandId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getCommand(String commandId, Class requiredType) throws CommandNotOfRequiredTypeException {
		return this.commandRegistry.getCommand(commandId, requiredType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getCommand(String commandId) {
		return this.commandRegistry.getCommand(commandId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class getType(String commandId) {
		return this.commandRegistry.getType(commandId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTypeMatch(String commandId, Class targetType) {
		return this.commandRegistry.isTypeMatch(commandId, targetType);
	}

}