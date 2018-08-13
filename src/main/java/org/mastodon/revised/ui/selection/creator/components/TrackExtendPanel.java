package org.mastodon.revised.ui.selection.creator.components;

import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class TrackExtendPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	public TrackExtendPanel()
	{
		final FlowLayout flowLayout = ( FlowLayout ) getLayout();
		flowLayout.setAlignment( FlowLayout.LEFT );

		final JCheckBox chckbxWholeTrack = new JCheckBox( "whole track" );
		add( chckbxWholeTrack );

		final JCheckBox chckbxTrackDownward = new JCheckBox( "track downward" );
		add( chckbxTrackDownward );

		final JCheckBox chckbxTrackUpward = new JCheckBox( "track upward" );
		add( chckbxTrackUpward );

		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add( chckbxWholeTrack );
		buttonGroup.add( chckbxTrackDownward );
		buttonGroup.add( chckbxTrackUpward );
	}
}
