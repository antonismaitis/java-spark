import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class VisualizeClusters extends JFrame {

	private static final int SIZE = 700;
	private static final String curename = "CURE";
	private static final String hybridname = "KMEANS-CURE";
	private final XYSeries a1 = new XYSeries("a1");
	private final XYSeries a2 = new XYSeries("a2");
	private final XYSeries a3 = new XYSeries("a3");
	private final XYSeries a4 = new XYSeries("a4");
	private final XYSeries a5 = new XYSeries("a5");

	public VisualizeClusters(String title,String file) {
		super(title);
		final ChartPanel chartPanel = createDemoPanel(title,file);
		chartPanel.setPreferredSize(new Dimension(SIZE, SIZE));
		this.add(chartPanel, BorderLayout.CENTER);
		JPanel control = new JPanel();

		this.add(control, BorderLayout.SOUTH);
	}

	private ChartPanel createDemoPanel(String name,String filename) {
		JFreeChart jfreechart = null;
		try {
			jfreechart = ChartFactory.createScatterPlot(
					name, "X", "Y", createDataset(filename),
					PlotOrientation.VERTICAL, true, true, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
		xyPlot.setDomainCrosshairVisible(true);
		xyPlot.setRangeCrosshairVisible(true);
		XYItemRenderer renderer = xyPlot.getRenderer();
		renderer.setSeriesPaint(0, Color.blue);
		renderer.setSeriesPaint(1, Color.RED);
		renderer.setSeriesPaint(2, Color.black);
		renderer.setSeriesPaint(3, Color.GREEN);
		renderer.setSeriesPaint(4, Color.magenta);
		adjustAxis((NumberAxis) xyPlot.getDomainAxis(), true);
		adjustAxis((NumberAxis) xyPlot.getRangeAxis(), false);
		xyPlot.setBackgroundPaint(Color.white);
		return new ChartPanel(jfreechart);
	}

	private void adjustAxis(NumberAxis axis, boolean vertical) {
		axis.setRange(-3.0, 3.0);
		axis.setTickUnit(new NumberTickUnit(0.5));
		axis.setVerticalTickLabels(vertical);
	}

	public XYSeriesCollection createDataset(String fileName) throws IOException {
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String s = null;
		assignEveryPointToTheCorrectPlace(in);
		xySeriesCollection.addSeries(a1);
		xySeriesCollection.addSeries(a2);
		xySeriesCollection.addSeries(a3);
		xySeriesCollection.addSeries(a4);
		xySeriesCollection.addSeries(a5);
		return xySeriesCollection;
	}

	private void assignEveryPointToTheCorrectPlace(BufferedReader in) throws IOException {
		String s;
		while ((s = in.readLine()) != null) {
			String[] a = s.split(",");
			if (a.length == 3) {
				if (a[0].toString().equals("Cluster-0")) {
					a1.add(Double.valueOf(a[1]), Double.valueOf(a[2]));
				} else if (a[0].toString().equals("Cluster-1")) {
					a2.add(Double.valueOf(a[1]), Double.valueOf(a[2]));
				} else if (a[0].toString().equals("Cluster-2")) {
					a3.add(Double.valueOf(a[1]), Double.valueOf(a[2]));
				} else if (a[0].toString().equals("Cluster-3")) {
					a4.add(Double.valueOf(a[1]), Double.valueOf(a[2]));
				} else {
					a5.add(Double.valueOf(a[1]), Double.valueOf(a[2]));
				}
			}
		}
	}

	public XYSeriesCollection createDataset_AfterHybrid() throws IOException {
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader("points_clusters_hybrid.csv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String s = null;
		assignEveryPointToTheCorrectPlace(in);
		xySeriesCollection.addSeries(a1);
		xySeriesCollection.addSeries(a2);
		xySeriesCollection.addSeries(a3);
		xySeriesCollection.addSeries(a4);
		xySeriesCollection.addSeries(a5);
		return xySeriesCollection;
	}

	public static void runVisual() {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				//VisualizeClusters cure = new VisualizeClusters(curename,"points_clusters.csv");
				VisualizeClusters hybrid = new VisualizeClusters(hybridname,"points_clusters_hybrid.csv");
//				cure.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//				cure.pack();
//				cure.setLocationRelativeTo(null);
//				cure.setVisible(true);
				hybrid.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				hybrid.pack();
				hybrid.setLocationRelativeTo(null);
				hybrid.setVisible(true);
			}
		});
	}

}