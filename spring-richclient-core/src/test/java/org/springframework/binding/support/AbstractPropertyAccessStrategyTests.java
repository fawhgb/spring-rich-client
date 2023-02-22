/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.binding.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.binding.MutablePropertyAccessStrategy;
import org.springframework.binding.PropertyMetadataAccessStrategy;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.test.SpringRichTestCase;
import org.springframework.rules.closure.Closure;
import org.springframework.rules.closure.support.Block;

/**
 * Tests class {@link AbstractPropertyAccessStrategy}.
 *
 * @author Oliver Hutchison
 * @author Arne Limburg
 */
public abstract class AbstractPropertyAccessStrategyTests extends SpringRichTestCase {

	protected AbstractPropertyAccessStrategy pas;

	protected TestBean testBean;

	protected ValueModel vm;

	protected TestPropertyChangeListener pcl;

	@Override
	protected void doSetUp() throws Exception {
		testBean = new TestBean();
		pas = createPropertyAccessStrategy(testBean);
		pcl = new TestPropertyChangeListener(ValueModel.VALUE_PROPERTY);
	}

	protected abstract AbstractPropertyAccessStrategy createPropertyAccessStrategy(Object target);

	protected boolean isStrictNullHandlingEnabled() {
		return true;
	}

	@Test
	public void testSimpleProperty() {
		vm = pas.getPropertyValueModel("simpleProperty");
		Block setValueDirectly = new Block() {
			@Override
			public void handle(Object newValue) {
				testBean.setSimpleProperty((String) newValue);
			}
		};
		Closure getValueDirectly = new Closure() {
			@Override
			public Object call(Object ignore) {
				return testBean.getSimpleProperty();
			}
		};
		Object[] valuesToTest = new Object[] { "1", "2", null, "3" };

		testSettingAndGetting(valuesToTest, getValueDirectly, setValueDirectly);
	}

	@Test
	public void testNestedProperty() {
		final TestBean nestedProperty = new TestBean();
		testBean.setNestedProperty(nestedProperty);
		vm = pas.getPropertyValueModel("nestedProperty.simpleProperty");
		Block setValueDirectly = new Block() {
			@Override
			public void handle(Object newValue) {
				nestedProperty.setSimpleProperty((String) newValue);
			}
		};
		Closure getValueDirectly = new Closure() {
			@Override
			public Object call(Object ignored) {
				return nestedProperty.getSimpleProperty();
			}
		};
		Object[] valuesToTest = new Object[] { "1", "2", null, "3" };

		testSettingAndGetting(valuesToTest, getValueDirectly, setValueDirectly);
	}

	@Test
	public void testChildPropertyAccessStrategy() {
		final TestBean nestedProperty = new TestBean();
		testBean.setNestedProperty(nestedProperty);
		MutablePropertyAccessStrategy cpas = pas.getPropertyAccessStrategyForPath("nestedProperty");

		assertEquals(pas.getPropertyValueModel("nestedProperty"), cpas.getDomainObjectHolder(),
				"Child domainObjectHolder should equal equivalent parent ValueModel");

		vm = cpas.getPropertyValueModel("simpleProperty");
		assertEquals(pas.getPropertyValueModel("nestedProperty.simpleProperty"), vm,
				"Child should return the same ValueModel as parent");

		Block setValueDirectly = new Block() {
			@Override
			public void handle(Object newValue) {
				nestedProperty.setSimpleProperty((String) newValue);
			}
		};
		Closure getValueDirectly = new Closure() {
			@Override
			public Object call(Object ignore) {
				return nestedProperty.getSimpleProperty();
			}
		};
		Object[] valuesToTest = new Object[] { "1", "2", null, "3" };

		testSettingAndGetting(valuesToTest, getValueDirectly, setValueDirectly);

		try {
			pas.getPropertyValueModel("nestedProperty").setValue(null);
			if (isStrictNullHandlingEnabled())
				fail("Should have thrown a NullValueInNestedPathException");
		} catch (NullValueInNestedPathException e) {
			if (!isStrictNullHandlingEnabled())
				fail("Should not have thrown a NullValueInNestedPathException");
		}
	}

	@Test
	public void testMapProperty() {
		final Map map = new HashMap();
		testBean.setMapProperty(map);
		vm = pas.getPropertyValueModel("mapProperty[.key]");
		Block setValueDirectly = new Block() {
			@Override
			public void handle(Object newValue) {
				map.put(".key", newValue);
			}
		};
		Closure getValueDirectly = new Closure() {
			@Override
			public Object call(Object ignore) {
				return map.get(".key");
			}
		};
		Object[] valuesToTest = new Object[] { "1", "2", null, "3" };
		testSettingAndGetting(valuesToTest, getValueDirectly, setValueDirectly);

		try {
			pas.getPropertyValueModel("mapProperty").setValue(null);
			if (isStrictNullHandlingEnabled())
				fail("Should have thrown a InvalidPropertyException");
		} catch (InvalidPropertyException e) {
			if (!isStrictNullHandlingEnabled())
				fail("Should not have thrown a InvalidPropertyException");
			if (!(e instanceof NullValueInNestedPathException)
					&& !(e.getCause() instanceof NullValueInNestedPathException))
				fail("Cause should be a NullValueInNestedPathException");
		}
	}

	@Test
	public void testListProperty() {
		final List list = new ArrayList();
		list.add(null);
		testBean.setListProperty(list);
		vm = pas.getPropertyValueModel("listProperty[0]");

		Block setValueDirectly = new Block() {
			@Override
			public void handle(Object newValue) {
				list.set(0, newValue);
			}
		};
		Closure getValueDirectly = new Closure() {
			@Override
			public Object call(Object ignore) {
				return list.get(0);
			}
		};
		Object[] valuesToTest = new Object[] { "1", "2", null, "3" };
		testSettingAndGetting(valuesToTest, getValueDirectly, setValueDirectly);

		list.add("a");
		ValueModel vm2 = pas.getPropertyValueModel("listProperty[1]");
		assertEquals("a", vm2.getValue());

		try {
			List newList = new ArrayList();
			pas.getPropertyValueModel("listProperty").setValue(newList);
			if (isStrictNullHandlingEnabled())
				fail("Should have thrown an InvalidPropertyException");
		} catch (InvalidPropertyException e) {
			if (!isStrictNullHandlingEnabled())
				fail("Should not have thrown an InvalidPropertyException");
		}

		try {
			pas.getPropertyValueModel("listProperty").setValue(null);
			if (isStrictNullHandlingEnabled())
				fail("Should have thrown a InvalidPropertyException");
		} catch (InvalidPropertyException e) {
			if (!isStrictNullHandlingEnabled())
				fail("Should not have thrown a InvalidPropertyException");
			if (!(e instanceof NullValueInNestedPathException)
					&& !(e.getCause() instanceof NullValueInNestedPathException))
				fail("Cause should be a NullValueInNestedPathException");
		}
	}

	@Test
	public void testReadOnlyProperty() {
		vm = pas.getPropertyValueModel("readOnly");

		testBean.readOnly = "1";
		assertEquals(testBean.readOnly, vm.getValue());

		try {
			vm.setValue("2");
			fail("should have thrown NotWritablePropertyException");
		} catch (NotWritablePropertyException e) {
			// expected
		}
	}

	@Test
	public void testWriteOnlyProperty() {
		vm = pas.getPropertyValueModel("writeOnly");

		vm.setValue("2");
		assertEquals("2", testBean.writeOnly);

		try {
			vm.getValue();
			fail("should have thrown NotReadablePropertyException");
		} catch (NotReadablePropertyException e) {
			// expected
		}
	}

	@Test
	public void testBeanThatImplementsPropertyChangePublisher() {
		TestBeanWithPCP testBeanPCP = new TestBeanWithPCP();
		pas.getDomainObjectHolder().setValue(testBeanPCP);

		vm = pas.getPropertyValueModel("boundProperty");
		assertEquals(1, testBeanPCP.getPropertyChangeListeners("boundProperty").length,
				"ValueModel should have registered a PropertyChangeListener");

		vm.addValueChangeListener(pcl);
		testBeanPCP.setBoundProperty("1");
		assertEquals(1, pcl.getEventsRecevied().size(),
				"Change to bound property should have been detected by ValueModel");
		PropertyChangeEvent e = (PropertyChangeEvent) pcl.getEventsRecevied().get(0);
		assertEquals(vm, e.getSource());
		assertEquals(null, e.getOldValue());
		assertEquals("1", e.getNewValue());

		pcl.reset();
		vm.setValue("2");
		assertEquals(1, pcl.getEventsRecevied().size(),
				"Change to bound property should have been detected by ValueModel");
		e = (PropertyChangeEvent) pcl.getEventsRecevied().get(0);
		assertEquals(vm, e.getSource());
		assertEquals("1", e.getOldValue());
		assertEquals("2", e.getNewValue());

		TestBeanWithPCP testBeanPCP2 = new TestBeanWithPCP();
		pas.getDomainObjectHolder().setValue(testBeanPCP2);

		assertEquals(0, testBeanPCP.getPropertyChangeListeners("boundProperty").length,
				"ValueModel should have removed the PropertyChangeListener");
		assertEquals(1, testBeanPCP2.getPropertyChangeListeners("boundProperty").length,
				"ValueModel should have registered a PropertyChangeListener");
	}

	@Test
	private void testSettingAndGetting(Object[] valuesToTest, Closure getValueDirectly, Block setValueDirectly) {
		vm.addValueChangeListener(pcl);
		for (int i = 0; i < valuesToTest.length; i++) {
			final Object valueToTest = valuesToTest[i];
			pcl.reset();
			assertEquals(getValueDirectly.call(null), vm.getValue(),
					"ValueModel does not have same value as bean property");
			setValueDirectly.call(valueToTest);
			assertEquals(valueToTest, vm.getValue(), "Change to bean not picked up by ValueModel");
			setValueDirectly.call(null);
			assertEquals(null, vm.getValue(), "Change to bean not picked up by ValueModel");
			vm.setValue(valueToTest);
			assertEquals(valueToTest, getValueDirectly.call(null), "Change to ValueModel not reflected in bean");
			assertEquals(valueToTest, vm.getValue(), "Change to ValueModel had no effect");
			if (valueToTest != null) {
				assertEquals(1, pcl.getEventsRecevied().size(),
						"Incorrect number of property change events fired by value model");
				PropertyChangeEvent e = (PropertyChangeEvent) pcl.getEventsRecevied().get(0);
				assertEquals(vm, e.getSource());
				assertEquals(null, e.getOldValue());
				assertEquals(valueToTest, e.getNewValue());
			}
		}
	}

	protected void assertPropertyMetadata(PropertyMetadataAccessStrategy mas, String property, Class type,
			boolean isReadable, boolean isWriteable) {
		assertEquals(type, mas.getPropertyType(property));
		assertEquals(isReadable, mas.isReadable(property));
		assertEquals(isWriteable, mas.isWriteable(property));
	}
}