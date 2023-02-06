package org.springframework.richclient.taskpane;

import javax.swing.ImageIcon;

public interface IconGenerator<T> {
	public ImageIcon generateIcon(T forObject);
}
