package org.mastodon.revised.ui.selection.creator;

import javax.swing.JPanel;

public interface FilterItem
{

	public static interface UpdateListener
	{
		public void filterChanged();
	}

	public JPanel getPanel();
}
