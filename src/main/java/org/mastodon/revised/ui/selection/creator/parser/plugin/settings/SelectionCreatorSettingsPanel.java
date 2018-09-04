package org.mastodon.revised.ui.selection.creator.parser.plugin.settings;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class SelectionCreatorSettingsPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	public SelectionCreatorSettingsPanel( final SelectionCreatorSettings settings )
	{
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblExpression = new JLabel( "Expression:" );
		lblExpression.setFont( lblExpression.getFont().deriveFont( lblExpression.getFont().getStyle() | Font.BOLD ) );
		final GridBagConstraints gbc_lblExpression = new GridBagConstraints();
		gbc_lblExpression.anchor = GridBagConstraints.WEST;
		gbc_lblExpression.insets = new Insets( 0, 0, 5, 5 );
		gbc_lblExpression.gridx = 0;
		gbc_lblExpression.gridy = 0;
		add( lblExpression, gbc_lblExpression );

		final JButton btnHelp = new JButton( "Help" );
		final GridBagConstraints gbc_btnHelp = new GridBagConstraints();
		gbc_btnHelp.insets = new Insets( 0, 0, 5, 0 );
		gbc_btnHelp.gridx = 1;
		gbc_btnHelp.gridy = 0;
		add( btnHelp, gbc_btnHelp );

		final JTextPane textAreaExpression = new JTextPane();
		textAreaExpression.setFont( new Font(
				"Monospaced",
				textAreaExpression.getFont().getStyle(),
				textAreaExpression.getFont().getSize() + 2 ) );
		textAreaExpression.setText( settings.expression() );
		final GridBagConstraints gbc_textAreaExpression = new GridBagConstraints();
		gbc_textAreaExpression.gridwidth = 2;
		gbc_textAreaExpression.insets = new Insets( 0, 0, 5, 0 );
		gbc_textAreaExpression.fill = GridBagConstraints.BOTH;
		gbc_textAreaExpression.gridx = 0;
		gbc_textAreaExpression.gridy = 1;
		add( textAreaExpression, gbc_textAreaExpression );

		final StyledDocument doc = textAreaExpression.getStyledDocument();
		final SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment( center, StyleConstants.ALIGN_CENTER );
		doc.setParagraphAttributes( 0, doc.getLength(), center, false );

		final JLabel lblDescription = new JLabel( "Description:" );
		lblDescription.setFont( lblDescription.getFont().deriveFont( lblDescription.getFont().getStyle() | Font.BOLD ) );
		final GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.insets = new Insets( 0, 0, 5, 5 );
		gbc_lblDescription.anchor = GridBagConstraints.WEST;
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 2;
		add( lblDescription, gbc_lblDescription );

		final JToggleButton tglbtnEdit = new JToggleButton( "Edit" );
		final GridBagConstraints gbc_tglbtnEdit = new GridBagConstraints();
		gbc_tglbtnEdit.insets = new Insets( 0, 0, 5, 0 );
		gbc_tglbtnEdit.gridx = 1;
		gbc_tglbtnEdit.gridy = 2;
		add( tglbtnEdit, gbc_tglbtnEdit );

		final JEditorPane editorPaneDescription = new JEditorPane();
		final HTMLEditorKit kit = new HTMLEditorKit();
		final HTMLDocument htmlDoc = new HTMLDocument();
		editorPaneDescription.setEditorKit( kit );
		editorPaneDescription.setDocument( htmlDoc );
		editorPaneDescription.setOpaque( false );
		editorPaneDescription.setEditable( false );
		editorPaneDescription.setText( settings.expression() );
		final GridBagConstraints gbc_editorPaneDescription = new GridBagConstraints();
		gbc_editorPaneDescription.insets = new Insets( 0, 0, 5, 0 );
		gbc_editorPaneDescription.gridwidth = 2;
		gbc_editorPaneDescription.fill = GridBagConstraints.BOTH;
		gbc_editorPaneDescription.gridx = 0;
		gbc_editorPaneDescription.gridy = 3;
		add( editorPaneDescription, gbc_editorPaneDescription );

		final JSeparator separator = new JSeparator();
		final GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.insets = new Insets( 0, 0, 5, 0 );
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.gridwidth = 2;
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 4;
		add( separator, gbc_separator );

		final JTextArea textAreaMessage = new JTextArea();
		textAreaMessage.setOpaque( false );
		textAreaMessage.setEditable( false );
		final GridBagConstraints gbc_textAreaMessage = new GridBagConstraints();
		gbc_textAreaMessage.insets = new Insets( 0, 0, 0, 5 );
		gbc_textAreaMessage.fill = GridBagConstraints.BOTH;
		gbc_textAreaMessage.gridx = 0;
		gbc_textAreaMessage.gridy = 5;
		add( textAreaMessage, gbc_textAreaMessage );

		final JButton btnRun = new JButton( "Run" );
		final GridBagConstraints gbc_btnRun = new GridBagConstraints();
		gbc_btnRun.gridx = 1;
		gbc_btnRun.gridy = 5;
		add( btnRun, gbc_btnRun );

		/*
		 * Update display on settings changes.
		 */

		settings.updateListeners().add( () -> {
			editorPaneDescription.setText( settings.description() );
			textAreaExpression.setText( settings.expression() );
			repaint();
		} );

		// Edit description
		tglbtnEdit.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final boolean selected = tglbtnEdit.isSelected();
				if ( selected )
				{
					final StyledEditorKit kit = new StyledEditorKit();
					final DefaultStyledDocument doc = new DefaultStyledDocument();
					doc.addDocumentListener( new MyDocumentListener( settings ) );
					editorPaneDescription.setEditorKit( kit );
					editorPaneDescription.setDocument( doc );

				}
				else
				{
					final HTMLEditorKit kit = new HTMLEditorKit();
					final HTMLDocument htmlDoc = new HTMLDocument();
					editorPaneDescription.setEditorKit( kit );
					editorPaneDescription.setDocument( htmlDoc );
				}
				editorPaneDescription.setText( settings.description() );
				editorPaneDescription.setEditable( selected );
				editorPaneDescription.setOpaque( selected );
				editorPaneDescription.repaint();
			}
		} );
	}

	private static final class MyDocumentListener implements DocumentListener
	{

		private final SelectionCreatorSettings settings;

		public MyDocumentListener( final SelectionCreatorSettings settings )
		{
			this.settings = settings;
		}

		@Override
		public void insertUpdate( final DocumentEvent e )
		{
			settings.setDescription( getText( e.getDocument() ) );
		}

		@Override
		public void removeUpdate( final DocumentEvent e )
		{
			settings.setDescription( getText( e.getDocument() ) );
		}

		@Override
		public void changedUpdate( final DocumentEvent e )
		{
			settings.setDescription( getText( e.getDocument() ) );
		}

		private static final String getText(final Document doc )
		{
			try
			{
				return doc.getText( 0, doc.getLength() );
			}
			catch ( final BadLocationException e )
			{
				return "";
			}
		}

	}
}
