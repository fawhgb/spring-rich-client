package org.springframework.richclient.application.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;
import org.springframework.richclient.application.PageLayoutBuilder;

public class MultiViewPageDescriptorTests {

	@Test
	public void testBuildInitialLayout() {
		MultiViewPageDescriptor pageDescriptor = new MultiViewPageDescriptor();

		List descriptors = new ArrayList();
		descriptors.add("view0");
		descriptors.add("view1");
		descriptors.add("view2");
		descriptors.add("view3");

		pageDescriptor.setViewDescriptors(descriptors);
		assertSame(descriptors, pageDescriptor.getViewDescriptors());

		PageLayoutBuilder mockBuilder = (PageLayoutBuilder) EasyMock.createMock(PageLayoutBuilder.class);
		// expectations
		mockBuilder.addView("view0");
		mockBuilder.addView("view1");
		mockBuilder.addView("view2");
		mockBuilder.addView("view3");
		EasyMock.replay(mockBuilder);

		pageDescriptor.buildInitialLayout(mockBuilder);

		EasyMock.verify(mockBuilder);
	}

	@Test
	public void testBeanAware() {
		MultiViewPageDescriptor pageDescriptor = new MultiViewPageDescriptor();

		pageDescriptor.setBeanName("bean name");

		assertEquals("bean name", pageDescriptor.getId(), "the bean name must be set as id");
	}
}
