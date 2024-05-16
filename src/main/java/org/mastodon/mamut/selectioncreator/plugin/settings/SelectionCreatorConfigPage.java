/*-
 * #%L
 * mastodon-selection-creator
 * %%
 * Copyright (C) 2018 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.selectioncreator.plugin.settings;

import java.util.function.Function;

import javax.swing.JPanel;

import org.scijava.listeners.Listeners;

import bdv.ui.settings.ModificationListener;
import bdv.ui.settings.SelectAndEditProfileSettingsPage;
import bdv.ui.settings.style.StyleProfile;
import bdv.ui.settings.style.StyleProfileManager;

public class SelectionCreatorConfigPage extends SelectAndEditProfileSettingsPage< StyleProfile< SelectionCreatorSettings > >
{
	/**
	 * @param treePath
	 *                                            path of this page in the settings
	 *                                            tree.
	 * @param selectionCreatorSettingsManager
	 *                                            the selection expression manager.
	 * @param evaluator
	 *                                            the expression evaluator: takes a
	 *                                            string (the expression) and
	 *                                            returns a message to the user.
	 */
	public SelectionCreatorConfigPage(
			final String treePath,
			final SelectionCreatorSettingsManager selectionCreatorSettingsManager,
			final Function< String, String > evaluator )
	{
		super(
				treePath,
				new StyleProfileManager<>( selectionCreatorSettingsManager, new SelectionCreatorSettingsManager( false ) ),
				new SelectionCreatorSettingsEditPanel( selectionCreatorSettingsManager.getSelectedStyle(), evaluator ) );
	}

	static class SelectionCreatorSettingsEditPanel implements SelectionCreatorSettings.UpdateListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< StyleProfile< SelectionCreatorSettings > >
	{
		private final Listeners.SynchronizedList< ModificationListener > modificationListeners;

		private final SelectionCreatorSettings editedStyle;

		private final SelectionCreatorSettingsPanel styleEditorPanel;

		public SelectionCreatorSettingsEditPanel( final SelectionCreatorSettings initialStyle, final Function< String, String > evaluator )
		{
			editedStyle = initialStyle.copy( "Edited" );
			styleEditorPanel = new SelectionCreatorSettingsPanel( editedStyle, evaluator );
			modificationListeners = new Listeners.SynchronizedList<>();
			editedStyle.updateListeners().add( this );
		}

		private boolean trackModifications = true;

		@Override
		public void settingsChanged()
		{
			if ( trackModifications )
				modificationListeners.list.forEach( ModificationListener::setModified );
		}

		@Override
		public void loadProfile( final StyleProfile< SelectionCreatorSettings > profile )
		{
			trackModifications = false;
			editedStyle.set( profile.getStyle() );
			trackModifications = true;
		}

		@Override
		public void storeProfile( final StyleProfile< SelectionCreatorSettings > profile )
		{
			trackModifications = false;
			editedStyle.setName( profile.getStyle().getName() );
			trackModifications = true;
			profile.getStyle().set( editedStyle );
		}

		@Override
		public Listeners< ModificationListener > modificationListeners()
		{
			return modificationListeners;
		}

		@Override
		public JPanel getJPanel()
		{
			return styleEditorPanel;
		}
	}
}
