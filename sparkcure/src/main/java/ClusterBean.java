import java.io.Serializable;
import java.util.*;

public class ClusterBean implements Serializable {
	private ArrayList<Point2D> clusters;
	private ArrayList<Point2D> dataPoints;
	private ArrayList<Point2D> representatives;
	private double[] centroid;
	private static Random random = new Random();
	//for cluster number
	private ArrayList<Double> minDimesion; //For density calculation
	private ArrayList<Double> maxDimesion; //For density Calculation
	public ClusterBean left;
	public ClusterBean right;
	public Point2D medianCluster;
	public ClusterBean parent;
	public int pointsInCluster;
	public double density;
	public boolean isLeaf;

	// Compares Clusters by subjective quality of distribution
	public static Comparator<ClusterBean> COMPARE_BY_QUALITY = new Comparator<ClusterBean>() {
		public int compare(ClusterBean cluster1, ClusterBean cluster2) {
			double totalVal1 = cluster1.centroid[1] - cluster1.centroid[0];
			double totalVal2 = cluster2.centroid[1] - cluster2.centroid[0];
			return Double.compare(totalVal1, totalVal2);
		}
	};

	public ClusterBean(String distance_type) {
		this.clusters = new ArrayList<Point2D>();
		this.dataPoints = new ArrayList<Point2D>();
		this.representatives = new ArrayList<Point2D>();
		this.centroid = new double[2];
	}

	public ClusterBean(int size) {
		dataPoints = new ArrayList<Point2D>();
		minDimesion = new ArrayList();
		minDimesion.add(new Double(0));
		minDimesion.add(new Double(0));
		minDimesion.add(new Double(0));
		maxDimesion = new ArrayList();
		maxDimesion.add(new Double(0));
		maxDimesion.add(new Double(0));
		maxDimesion.add(new Double(0));
		medianCluster = new Point2D(size);
		left = null;
		right = null;
	}

	// add a Point2D to clusters
	public void add(Point2D c) {
		this.clusters.add(c);
		updateCentroid(this.clusters);
	}

	public void addPointToCluster(Point2D c) {
		try {
			this.dataPoints.add(c);
		}catch (ArrayIndexOutOfBoundsException ex){
			addPointToCluster(c);
		}
	}

	// pick representative points from the clusters
	public void pickRepresentatives(int n, int clusterSize) {
		// include all points if it has less than n data points
		if (clusters.size() <= n) {
			this.representatives.addAll(clusters);
		} else {
			ArrayList<Point2D> cloneClusters = new ArrayList<>(clusters);
			// pick the first random point
			Point2D current = clusters.get(random.nextInt(clusterSize + 1));
			this.representatives.add(current);
			cloneClusters.remove(current);
			// choose farthest data points
			for (int i = 1; i < n; i=i+2) {
				Point2D[] clusterArray = cloneClusters.toArray(new Point2D[clusterSize]);
				FarthestPair farthestPair = new FarthestPair(clusterArray);
				Point2D farthest_other = clusters.stream().filter(c -> c == farthestPair.other()).findFirst().get();
				this.representatives.add(farthest_other);
				cloneClusters.remove(current);
			}
		}
	}

	// move repesentative points toward the centroid by 50%
	public void moveRepresentatives() {
		for (Point2D c : this.representatives) {
			if (c != null) {
				double[] diff = new double[2];
				double[] v = c.getValue();
				for (int i = 0; i < diff.length; i++) {
					diff[i] = centroid[i] - v[i];
				}

				for (int i = 0; i < v.length; i++) {
					v[i] = v[i] + 0.5 * diff[i];
				}
				c.setValue(v);
			}
		}
	}

	// compute SSE
	public double SSE() {
		double sse = 0;
		for (Point2D c : clusters) {
			double[] vector = c.getValue();
			for (int i = 0; i < vector.length; i++) {
				double diff = vector[i] - centroid[i];
				sse += Math.pow(diff, 2);
			}
		}
		return sse;
	}

	// merge one clusters into another
	public void mergeClusterBean(ClusterBean c) {
		clusters.addAll(c.getClusters());
	}

	// get clusters size
	public int size() {
		return this.clusters.size();
	}

	// print out clusters information such as centroid, number of data points
	public void print() {
		System.out.println("Centroid: [" + centroid[0] + "," + centroid[1] + "], Size: " + dataPoints.size());
	}

	private void updateCentroid(ArrayList<Point2D> list) {
		ArrayList<Point2D> listclone = new ArrayList<>();
		listclone.addAll(list);
		int size = list.size();
		double[] sum = new double[2];

		listclone.forEach(aList -> {
			if (aList != null && aList.getValue() != null) {
				double[] v = aList.getValue();
				for (int i = 0; i < v.length; i++) {
					sum[i] += v[i];
				}
			}
		});
		for (int i = 0; i < sum.length; i++) {
			centroid[i] = sum[i] / size;
		}
	}

	// getters - setters

	public double[] getCentroid() {
		return this.centroid;
	}

	public ArrayList<Point2D> getClusters() {
		return this.clusters;
	}

	public ArrayList<Point2D> getRepresentatives() {
		return this.representatives;
	}

	public ArrayList<Point2D> getDataPoints() {
		return this.dataPoints;
	}

	public void setCentroid(double[] centroid) {
		this.centroid = centroid;
	}

	public ArrayList<Double> getMinDimesion() {
		return minDimesion;
	}

	public void setMinDimesion(ArrayList minDimesion) {
		this.minDimesion = minDimesion;
	}

	public ArrayList<Double> getMaxDimesion() {
		return maxDimesion;
	}

	public void setMaxDimesion(ArrayList maxDimesion) {
		this.maxDimesion = maxDimesion;
	}

	public ClusterBean getLeft() {
		return left;
	}

	public void setLeft(ClusterBean left) {
		this.left = left;
	}

	public ClusterBean getRight() {
		return right;
	}

	public void setRight(ClusterBean right) {
		this.right = right;
	}

	public Point2D getMedianCluster() {
		return medianCluster;
	}

	public void setMedianCluster(Point2D medianCluster) {
		this.medianCluster = medianCluster;
	}

	public void setDataPoints(ArrayList<Point2D> dataPoints) {
		this.dataPoints = dataPoints;
	}

	public int getPointsInCluster() {
		return pointsInCluster;
	}

	public void setPointsInCluster(int pointsInCluster) {
		this.pointsInCluster = pointsInCluster;
	}

	public double getDensity() {
		return density;
	}

	public void setDensity(double density) {
		this.density = density;
	}
}