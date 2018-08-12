package org.mastodon.revised.ui.selection.util;

import java.util.Locale;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class DataThresholdUIExample
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		Locale.setDefault( Locale.ROOT );

		final int N = 2000000;
		final double[] data = new double[ N ];
		final Random ran = new Random();
		for ( int i = 0; i < 3 * N / 4; i++ )
			data[ i ] = 5. + 0.5 * ran.nextGaussian();
		for ( int i = 3 * N / 4; i < data.length; i++ )
			data[ i ] = 8. + 1. * ran.nextGaussian();

		final DataThresholdUI thresholdUI = new DataThresholdUI( data );
		thresholdUI.listeners().add( () -> System.out.println( thresholdUI.getThreshold().toString() ) );

		final JFrame frame = new JFrame( "Treshold panel" );
		frame.getContentPane().add( thresholdUI.getPanel() );
		frame.pack();
		frame.setVisible( true );
	}
}
