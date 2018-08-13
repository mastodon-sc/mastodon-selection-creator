package org.mastodon.revised.ui.selection.creator.components;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class VertexExtendSelectionPanel extends JPanel
{

	/**
	 * Create the panel.
	 */
	public VertexExtendSelectionPanel()
	{

		final JCheckBox chckbxVertices = new JCheckBox( "vertex" );
		add( chckbxVertices );

		final JCheckBox chckbxIncomingEdges = new JCheckBox( "incoming edges" );
		add( chckbxIncomingEdges );

		final JCheckBox chckbxOutgoingEdges = new JCheckBox( "outgoing edges" );
		add( chckbxOutgoingEdges );

	}

}
