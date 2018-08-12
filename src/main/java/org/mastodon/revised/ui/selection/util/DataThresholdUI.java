package org.mastodon.revised.ui.selection.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.DoublePredicate;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.NumberTickUnitSource;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.Layer;
import org.jfree.data.Range;
import org.mastodon.util.Listeners;
import org.mastodon.util.Listeners.List;

/**
 * A UI component that displays the log10 histogram of a <code>double</code>
 * data array and let the user configure a threshold over it.
 * <p>
 * The threshold is specified by 3 fields:
 * <ul>
 * <li>the threshold value;
 * <li>whether the threshold is above or below this value;
 * <li>whether the comparison is strict.
 * </ul>
 * The value currently set can be accessed by {@link #getThreshold()}. It
 * implements {@link DoublePredicate} and can used in a lambda expression.
 * <p>
 * The JPanel that contains the UI is accessed via {@link #getPanel()}, and
 * relies on JFreeChart. The threshold is set by mouse and keyboard. The arrow
 * keys are used to move the threshold value on the histogram. It is also
 * possible to directly type a value, like "16.34", wait, and this value will be
 * grabbed.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class DataThresholdUI
{

	private static final NumberFormat FORMAT = new DecimalFormat( "0.0" );

	private static final Font SMALL_FONT = new JLabel().getFont().deriveFont( 10f );

	private static final Color ANNOTATION_COLOR = new java.awt.Color( 252, 117, 0 );

	private final JPanel panel;

	private final JButton jButtonAutoThreshold;

	private final JRadioButton jRadioButtonBelow;

	private final JRadioButton jRadioButtonAbove;

	private final JToggleButton jToggleStrictly;

	private double thresholdValue;

	private final ChartPanel chartPanel;

	private final Listeners.List< UpdateListener > listeners = new Listeners.List<>();

	public static interface UpdateListener
	{
		public void filterChanged();
	}

	public static class Threshold implements DoublePredicate
	{

		public final boolean greater;

		public final boolean strictly;

		public final double threshold;

		private final DoublePredicate tester;

		private Threshold( final double threshold, final boolean greater, final boolean strictly )
		{
			this.threshold = threshold;
			this.greater = greater;
			this.strictly = strictly;
			this.tester = greater
					? strictly
							? ( v ) -> v > threshold // >
							: ( v ) -> v >= threshold // >=
					: strictly
							? ( v ) -> v < threshold // <
							: ( v ) -> v <= threshold; // <=;
		}

		@Override
		public String toString()
		{
			return ( greater
					? strictly
							? "> "
							: "\u2265 "
					: strictly
							? "< "
							: "\u2264 " )
									+ Double.toString( threshold );
		}

		/**
		 * Returns <code>true</code> if the specified value satisfies the
		 * condition set by this threshold.
		 * 
		 * @param value
		 *            the value to test.
		 * @return <code>true</code> if the specified value satisfies the
		 *         condition set by this threshold.
		 */
		@Override
		public boolean test( final double value )
		{
			return tester.test( value );
		}
	}

	public DataThresholdUI( final double[] data )
	{
		panel = new JPanel();
		panel.setLayout( new BorderLayout() );

		this.chartPanel = HistogramUtil.createHistogramPlot( data, true );
		final XYPlot plot = chartPanel.getChart().getXYPlot();
		final XYTextSimpleAnnotation annotation = ( XYTextSimpleAnnotation ) plot.getAnnotations().get( 0 );

		// Disable zoom.
		for ( final MouseListener ml : chartPanel.getMouseListeners() )
			chartPanel.removeMouseListener( ml );

		// Re enable the X axis.
		plot.getDomainAxis().setVisible( true );
		final TickUnitSource source = new NumberTickUnitSource( false, FORMAT );
		plot.getDomainAxis().setStandardTickUnits( source );
		final Font smallFont = chartPanel.getFont().deriveFont( chartPanel.getFont().getSize2D() - 2f );
		plot.getDomainAxis().setTickLabelFont( smallFont );

		// Transparent background.
		chartPanel.getChart().setBackgroundPaint( null );

		// Bottom panel with buttons.
		final JPanel panelButtons = new JPanel();
		panelButtons.setLayout( new FlowLayout( FlowLayout.CENTER, 5, 2 ) );

		this.jButtonAutoThreshold = new JButton( "Auto" );
		jButtonAutoThreshold.setToolTipText( "Determine threshold automatically with Ostu method." );
		jButtonAutoThreshold.setFont( SMALL_FONT );
		jButtonAutoThreshold.addActionListener( ( e ) -> {
			thresholdValue = HistogramUtil.otsuThreshold( data );
			redrawThresholdMarker();
		} );
		panelButtons.add( jButtonAutoThreshold );

		this.jToggleStrictly = new JToggleButton( "Strictly", true );
		jToggleStrictly.setFont( SMALL_FONT );
		jToggleStrictly.addActionListener( ( e ) -> redrawThresholdMarker() );
		panelButtons.add( jToggleStrictly );

		this.jRadioButtonAbove = new JRadioButton( "Greater than" );
		jRadioButtonAbove.setFont( SMALL_FONT );
		jRadioButtonAbove.addActionListener( ( e ) -> redrawThresholdMarker() );
		panelButtons.add( jRadioButtonAbove );

		this.jRadioButtonBelow = new JRadioButton( "Less than" );
		jRadioButtonBelow.setFont( SMALL_FONT );
		jRadioButtonBelow.addActionListener( ( e ) -> redrawThresholdMarker() );
		panelButtons.add( jRadioButtonBelow );

		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add( jRadioButtonAbove );
		buttonGroup.add( jRadioButtonBelow );
		jRadioButtonAbove.setSelected( true );

		// Set mouse and focus listeners.
		final MouseListener[] mls = chartPanel.getMouseListeners();
		for ( final MouseListener ml : mls )
			chartPanel.removeMouseListener( ml );

		chartPanel.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final MouseEvent e )
			{
				chartPanel.requestFocusInWindow();
				thresholdValue = getXFromChartEvent( e );
				redrawThresholdMarker();
			}
		} );
		chartPanel.addMouseMotionListener( new MouseAdapter()
		{
			@Override
			public void mouseDragged( final MouseEvent e )
			{
				thresholdValue = getXFromChartEvent( e );
				redrawThresholdMarker();
			}
		} );
		chartPanel.setFocusable( true );
		chartPanel.addFocusListener( new FocusListener()
		{

			@Override
			public void focusLost( final FocusEvent e )
			{
				annotation.setColor( ANNOTATION_COLOR.darker() );
			}

			@Override
			public void focusGained( final FocusEvent e )
			{
				annotation.setColor( Color.RED.darker() );
			}
		} );
		chartPanel.addKeyListener( new MyKeyListener() );

		panel.add( chartPanel, BorderLayout.CENTER );
		panel.add( panelButtons, BorderLayout.SOUTH );
		redrawThresholdMarker();
	}

	/**
	 * Returns the JPanel containing the UI.
	 * 
	 * @return the JPanel.
	 */
	public JPanel getPanel()
	{
		return panel;
	}

	/**
	 * Returns the threshold. This value is immutable and reflect settings at
	 * the time of the method call.
	 * 
	 * @return a new {@link Threshold} instance.
	 */
	public Threshold getThreshold()
	{
		return new Threshold( thresholdValue, jRadioButtonAbove.isSelected(), jToggleStrictly.isSelected() );
	}

	/**
	 * Returns the listener collection, that are notified when the threshold
	 * value is changed.
	 * 
	 * @return the listener collection.
	 */
	public List< UpdateListener > listeners()
	{
		return listeners;
	}

	private double getXFromChartEvent( final MouseEvent mouseEvent )
	{
		final Rectangle2D plotArea = chartPanel.getScreenDataArea();
		final XYPlot plot = ( XYPlot ) chartPanel.getChart().getPlot();
		return plot.getDomainAxis().java2DToValue( mouseEvent.getX(), plotArea, plot.getDomainAxisEdge() );
	}

	private void redrawThresholdMarker()
	{
		final XYPlot plot = ( XYPlot ) chartPanel.getChart().getPlot();
		final IntervalMarker intervalMarker =
				( IntervalMarker ) plot.getDomainMarkers( Layer.FOREGROUND ).iterator().next();
		if ( jRadioButtonAbove.isSelected() )
		{
			intervalMarker.setStartValue( thresholdValue );
			intervalMarker.setEndValue( plot.getDomainAxis().getUpperBound() );
		}
		else
		{
			intervalMarker.setStartValue( plot.getDomainAxis().getLowerBound() );
			intervalMarker.setEndValue( thresholdValue );
		}
		float x, y;
		if ( thresholdValue > 0.85 * plot.getDomainAxis().getUpperBound() )
			x = ( float ) ( thresholdValue - 0.13 * plot.getDomainAxis().getRange().getLength() );
		else
			x = ( float ) ( thresholdValue + 0.02 * plot.getDomainAxis().getRange().getLength() );

		y = ( float ) ( 0.85 * plot.getRangeAxis().getUpperBound() );

		final XYTextSimpleAnnotation annotation =
				( XYTextSimpleAnnotation ) plot.getAnnotations().get( 0 );
		final String str = String.format( "%.2f", thresholdValue );
		annotation.setText( str );
		annotation.setLocation( x, y );
		fireThresholdChanged();
	}

	private void fireThresholdChanged()
	{
		listeners.list.forEach( ( e ) -> e.filterChanged() );
	}

	/**
	 * A class that listen to the user typing a number, building a string
	 * representation as he types, then converting the string to a double after
	 * a wait time. The number typed is used to set the threshold in the chart
	 * panel.
	 *
	 * @author Jean-Yves Tinevez
	 */
	private final class MyKeyListener implements KeyListener
	{

		private static final long WAIT_DELAY = 1; // s

		private static final double INCREASE_FACTOR = 0.1;

		private String strNumber = "";

		private ScheduledExecutorService ex;

		private ScheduledFuture< ? > future;

		private boolean dotAdded = false;

		private final Range range;

		private final Runnable command = new Runnable()
		{
			@Override
			public void run()
			{
				// Convert to double and pass it to threshold value
				try
				{
					final double typedThreshold = Double.parseDouble( strNumber );
					thresholdValue = typedThreshold;
					redrawThresholdMarker();
				}
				catch ( final NumberFormatException nfe )
				{}
				// Reset
				ex = null;
				strNumber = "";
				dotAdded = false;
			}
		};

		public MyKeyListener()
		{
			final XYPlot plot = ( XYPlot ) chartPanel.getChart().getPlot();
			this.range = plot.getDomainAxis().getRange();
		}

		@Override
		public void keyPressed( final KeyEvent e )
		{
			// Is it arrow keys?
			if ( e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_KP_LEFT )
			{
				thresholdValue -= INCREASE_FACTOR * range.getLength();
				redrawThresholdMarker();
			}
			else if ( e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_KP_RIGHT )
			{
				thresholdValue += INCREASE_FACTOR * range.getLength();
				redrawThresholdMarker();
			}
			else if ( e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_KP_UP )
			{
				thresholdValue = range.getUpperBound();
				redrawThresholdMarker();
			}
			else if ( e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_KP_DOWN )
			{
				thresholdValue = range.getLowerBound();
				redrawThresholdMarker();
			}
		}

		@Override
		public void keyReleased( final KeyEvent e )
		{}

		@Override
		public void keyTyped( final KeyEvent e )
		{

			if ( e.getKeyChar() < '0' || e.getKeyChar() > '9' )
			{
				// Ok then it's number

				if ( !dotAdded && e.getKeyChar() == '.' )
				{
					// User added a decimal dot for the first and only time
					dotAdded = true;
				}
				else
				{
					return;
				}
			}

			if ( ex == null )
			{
				// Create new waiting line
				ex = Executors.newSingleThreadScheduledExecutor();
				future = ex.schedule( command, WAIT_DELAY, TimeUnit.SECONDS );
			}
			else
			{
				// Reset waiting line
				future.cancel( false );
				future = ex.schedule( command, WAIT_DELAY, TimeUnit.SECONDS );
			}
			strNumber += e.getKeyChar();
		}
	}
}
