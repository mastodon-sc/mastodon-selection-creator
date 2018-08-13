package org.mastodon.revised.ui.selection;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.revised.mamut.MamutProject;
import org.mastodon.revised.mamut.MamutProjectIO;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.mamut.feature.MamutFeatureComputer;
import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.ui.ProgressListener;
import org.mastodon.revised.ui.selection.creator.components.FilterPanel;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class SelectionCreatorExample
{

	public static void main( final String[] args ) throws IOException, SpimDataException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		Locale.setDefault( Locale.ROOT );

		final Context context = new Context();
		final WindowManager windowManager = new WindowManager( context );
		final MamutProject project = new MamutProjectIO().load( "../TrackMate3/samples/mamutproject" );
		windowManager.getProjectManager().open( project );

		final Model model = windowManager.getAppModel().getModel();
		final FeatureModel featureModel = model.getFeatureModel();
		final TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();

		final TagSetStructure tss = new TagSetStructure();
		final Random ran = new Random( 0l );
		final TagSetStructure.TagSet reviewedByTag = tss.createTagSet( "Reviewed by" );
		reviewedByTag.createTag( "Pavel", ran.nextInt() | 0xFF000000 );
		reviewedByTag.createTag( "Mette", ran.nextInt() | 0xFF000000 );
		reviewedByTag.createTag( "Tobias", ran.nextInt() | 0xFF000000 );
		reviewedByTag.createTag( "JY", ran.nextInt() | 0xFF000000 );
		final TagSetStructure.TagSet locationTag = tss.createTagSet( "Location" );
		locationTag.createTag( "Anterior", ran.nextInt() | 0xFF000000 );
		locationTag.createTag( "Posterior", ran.nextInt() | 0xFF000000 );
		System.out.println( "Initial TagSetStructure:\n" + tss );
		model.getTagSetModel().setTagSetStructure( tss );

		final MamutFeatureComputerService service = context.getService( MamutFeatureComputerService.class );
		final Set< MamutFeatureComputer > featureComputers = new HashSet<>( service.getFeatureComputers() );
		service.compute( model, featureModel, featureComputers, voidLogger() );

		final JFrame frame = new JFrame();
		frame.getContentPane().add(
				new JScrollPane(
						new FilterPanel<>(
								model.getGraph(),
								tagSetModel,
								featureModel,
								Spot.class,
								Link.class ) ) );
		frame.pack();
		frame.setVisible( true );
	}

	private static ProgressListener voidLogger()
	{
		return new ProgressListener()
		{

			@Override
			public void showStatus( final String string )
			{}

			@Override
			public void showProgress( final int current, final int total )
			{}

			@Override
			public void clearStatus()
			{}
		};
	}

	private SelectionCreatorExample()
	{}
}
