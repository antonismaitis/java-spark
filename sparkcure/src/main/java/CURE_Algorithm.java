import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Random;
import java.util.*;
import java.io.*;

import static org.jfree.util.Log.debug;

public class CURE_Algorithm implements Serializable {
    protected static ArrayList<Point2D> pointsGathered = new ArrayList<>();
    private static Dataset<Row> pointsGathered_df;
    private static List<Dataset<Row>> pointsGathered_dfAll = new ArrayList<>();
    private static ArrayList<ClusterBean> Clusters = new ArrayList<>();
    private static ArrayList<ClusterBean> Clusters_ForHybrid = new ArrayList<>();
    private static ArrayList<Point2D> samples;
    private static String distance_type;
    private static int clusterSize;    // number of Clusters
    private static int representatives;    // number of representative points
    private static int totalNumberOfPointsInCluster;
    private static Random random = new Random();
    private static int dataSetFullSize;
    private static boolean givenCluster = false;

    public void initValues_andSample(String distance_type_, int totalNumberOfPointsInCluster_, int representatives_, boolean givenCluster_, int clusterSize_) {
        distance_type = distance_type_;
        samples = new ArrayList<>();
        representatives = representatives_;
        totalNumberOfPointsInCluster = totalNumberOfPointsInCluster_;
        givenCluster = givenCluster_;
        clusterSize = clusterSize_;
    }


    public void startClustering(SparkSession ss) {
        dataSetFullSize = pointsGathered.size();
        long beginTime = System.currentTimeMillis();

        //init KdTree for finding Clusters number
//        if (givenCluster) {
//            initKdtreeForFindingNumberOfClusters();
//        }
//
//        //random sampling
        randomSamples(totalNumberOfPointsInCluster);
//        // First part: pick representative points using agglomerative ClusterBeaning
//        Clusters = new ArrayList<ClusterBean>();
//        ArrayList<Double> sses = new ArrayList<Double>();
//        // to set so that only Unique points are taken
        Set<Point2D> uniquePoints = new HashSet<Point2D>(samples);
        samples.clear();
        samples.addAll(uniquePoints);
//        for (Point2D sample : uniquePoints) {
//            ClusterBean c = new ClusterBean(distance_type);
//            c.add(sample);
//            Clusters.add(c);
//        }
//
//        sses.add(SSEs(Clusters));
//
//        while (Clusters.size() > clusterSize) {
//            mergeClusterBean(Clusters);
//            sses.add(SSEs(Clusters));
//        }
//        if (Clusters.size() == clusterSize) {
//            sses.add(SSEs(Clusters));
//        }
//
//        // for each Cluster, pick as many representative points as representatives
//        for (ClusterBean c : Clusters) {
//            c.pickRepresentatives(representatives, clusterSize);
//            c.moveRepresentatives();
//        }

        int listsize = StartProcessCure.list_ID.intValue();
        for (int i = 0; i < listsize; i++) {
            pointsGathered_dfAll.add(i, ss.createDataFrame(StartProcessCure.pointsGatheredAll.get(i), Point2D.class));
        }

//        pointsGathered_dfAll.forEach(df ->{
//            df.select(("x"), ("y"))
//                    .foreach(point -> {
//                                assignDataPointToCorrectCluster(point, Clusters);
//                            }
//                    );
//        });
//
//        Collections.sort(Clusters, ClusterBean.COMPARE_BY_QUALITY);
//
//        // print out the result
//        printClusterResults(Clusters, "CURE", "points_clusters");
//        int id2 = 0;
//        //The Result of SSE for inspection the cluster is shown
//        //in Table 2. The table 2 shows the SSE value and rate of
//        //change of the SSE when k = 2 to 10, found that when k = 4
//        //SSE is the maximum rate of change.
//        for (int sse_count = sses.size() - 1; sse_count > sses.size() - 11; sse_count--) {
//            Double change = ((sses.get(sse_count - 1) - sses.get(sse_count)) * 100) / sses.get(sse_count);
//            System.out.print("SSE of Clusters number (" + id2 + ") -> " + sses.get(id2).toString() + "___");
//            System.out.print("Change =" + change.toString() + '\n');
//            id2++;
//        }
//
//        long time = System.currentTimeMillis() - beginTime;
//        System.out.println("The Algorithm took " + time + " milliseconds to complete.");
//        System.out.println("-------START SECOND PART-------");
//        System.out.println(pointsGathered.size());
        startSecondPart(ss);
    }

    public void assignDataPointToCorrectCluster(Row point, ArrayList<ClusterBean> clusters) {
        String values_xy_Spot = point.toString().substring(1, point.toString().length() - 1);
        double[] xyvalues;
        try{
            xyvalues = new double[]{Double.valueOf(values_xy_Spot.split(",")[0])
                    , Double.valueOf(values_xy_Spot.split(",")[1])};
        }catch (Exception ex){
            xyvalues = new double[]{Double.valueOf(values_xy_Spot.split(",")[2])
                    , Double.valueOf(values_xy_Spot.split(",")[3])};
        }
        double min_distance = Double.MAX_VALUE;
        int min_ClusterBean_index = 0;

        for (int j = 0; j < clusterSize; j++) {
            ClusterBean c = clusters.get(j);
            ClosestPair closest = new ClosestPair(c.getRepresentatives().toArray(new Point2D[c.getRepresentatives().size()]));
            double dist = distance(xyvalues, closest.either().value);
            if (dist < min_distance) {
                min_distance = dist;
                min_ClusterBean_index = j;
            }
        }
        // assign data point
        clusters.get(min_ClusterBean_index).addPointToCluster(new Point2D(xyvalues));
    }

    // use centroid distance to measure distance between ClusterBeans
    private void mergeClusterBean(ArrayList<ClusterBean> ClusterBeans) {
        // find two closest ClusterBeans
        ClusterBean min1 = null;
        ClusterBean min2 = null;
        int clsize = ClusterBeans.size();
        Point2D[] points = new Point2D[clsize];
        for (int i = 0; i < clsize; i++) {
            points[i] = (new Point2D(i, ClusterBeans.get(i).getCentroid()));
        }

        // merge the two
        // groups that have the smallest dissimilarity
        ClosestPair closest = new ClosestPair(points);
        min1 = ClusterBeans.stream().filter(c -> c.getCentroid()[0] == closest.either().x && c.getCentroid()[1] == closest.either().y).findFirst().get();
        min2 = ClusterBeans.stream().filter(c -> c.getCentroid()[0] == closest.other().x && c.getCentroid()[1] == closest.other().y).findFirst().get();
        min1.mergeClusterBean(min2);
        // remove min2 from ClusterBean list
        ClusterBeans.remove(min2);
    }

    // euclidean distance of two 2 dimensional vectors
    private double distance(double[] v1, double[] v2) {
        double dist = 0;
        for (int i = 0; i < v1.length; i++) {
            dist += Math.pow(v1[i] - v2[i], 2);
        }
        dist = Math.sqrt(dist);
        return dist;
    }

    // randomly shuffle county array and select the first 'sample_size' pointsGathered
    private void randomSamples(int sample_size) {
        for (int y = 0; y < sample_size; y++) {
            samples.add(pointsGathered.get(random.nextInt((dataSetFullSize) + 1)));
        }
    }

    // return the sum of errors of all ClusterBeans
    private double SSEs(ArrayList<ClusterBean> ClusterBeans) {
        double sum = 0;
        for (ClusterBean c : ClusterBeans) {
            sum += c.SSE();
        }
        return sum;
    }

    //Kdtree
    public void initKdtreeForFindingNumberOfClusters() {
        // Count the number of clusters
        ClusterBean node = new ClusterBean(totalNumberOfPointsInCluster);
        for (int j = 0; j < totalNumberOfPointsInCluster; j++) {
            node.getDataPoints().add(j, new Point2D(2, j));
        }


        for (int j = 0; j < totalNumberOfPointsInCluster; j++) {
            Point2D p = node.getDataPoints().get(j);
            if (node.getDataPoints() != null && node.getDataPoints().size() != 0) {
                p.setValue(pointsGathered.get(random.nextInt((dataSetFullSize) + 0)).getValue());
            }
        }

        node.parent = null;
        node.left = null;
        node.right = null;
        ClusterNumber.buildTree(node, 0, totalNumberOfPointsInCluster);
        ClusterNumber.cluster_sizeByKdTree = 0;
        ClusterNumber.inOrder(node);
        clusterSize = ClusterNumber.cluster_sizeByKdTree;
    }

    // VISUALIZATION
    private void logClusters(ClusterBean node, String filename, String name) {
        BufferedWriter out = getWriterHandle(filename);
        try {
            //out.write("name,a,b,c,d" + "\n");
            for (int j = 0; j < node.getDataPoints().size(); j++) {
                Point2D p = node.getDataPoints().get(j);
                out.write("" + name);
                for (int t = 0; t < 1; t++) { //number of dimensions -1
                    out.write("," + p.getValue()[t]);
                }
                out.write("," + p.getValue()[1] + "\n"); //number of dimensions -1
            }
        } catch (Exception e) {
            //debug(e);
        }
        closeWriterHandle(out);
    }

    private static BufferedWriter getWriterHandle(String filename) {
        BufferedWriter out = null;
        try {
            FileWriter fw = new FileWriter(filename, true);
            out = new BufferedWriter(fw);
        } catch (Exception e) {
            debug(e);
        }
        return out;
    }

    private static void closeWriterHandle(BufferedWriter out) {
        try {
            out.flush();
            out.close();
        } catch (Exception e) {
            debug(e);
        }
    }

    // Part 2 hybrid Cure Kmeans
    public void startSecondPart(SparkSession ss) {

        // convert to rdd so that Kmeans of spark library can run
        JavaRDD<Vector> datamain = ss.createDataFrame(samples, Point2D.class).select(("value")).toJavaRDD().map((Function<Row, Vector>) s -> {
                    String values_xy_Spot = s.getAs("value").toString().substring(13, s.getAs("value").toString().length() - 1);
                    double[] xyvalues = {Double.valueOf(values_xy_Spot.split(",")[0])
                            , Double.valueOf(values_xy_Spot.split(",")[1])};
                    return Vectors.dense(xyvalues);
                }
        );

        // Trains a k-means model.
        KMeansModel clusters = KMeans.train(datamain.rdd(), totalNumberOfPointsInCluster, 10, "k-means||");
        // for each Cluster, pick as many representative points as representatives
        for (Vector center : clusters.clusterCenters()) {
            ClusterBean c = new ClusterBean(distance_type);
            c.add(new Point2D(center.asML().toArray()));
            Clusters_ForHybrid.add(c);
        }

        //CURE

            ArrayList<Double> sses = new ArrayList<Double>();
            sses.add(SSEs(Clusters_ForHybrid));

            while (Clusters_ForHybrid.size() > clusterSize) {
                mergeClusterBean(Clusters_ForHybrid);
                sses.add(SSEs(Clusters_ForHybrid));
            }
            if (Clusters_ForHybrid.size() == clusterSize) {
                sses.add(SSEs(Clusters_ForHybrid));
            }

            // for each Cluster, pick as many representative points as representatives
            for (ClusterBean c : Clusters_ForHybrid) {
                c.pickRepresentatives(representatives, clusterSize);
                c.moveRepresentatives();
            }

            for (Dataset<Row> df : pointsGathered_dfAll) {
                df.select(("x"), ("y"));
                df.foreach(point -> {
                            assignDataPointToCorrectCluster(point, Clusters_ForHybrid);
                        }
                );
            }

            Collections.sort(Clusters_ForHybrid, ClusterBean.COMPARE_BY_QUALITY);

            // print out the result
            printClusterResults(Clusters_ForHybrid, "Kmeans-CURE", "points_clusters_hybrid");
            int id2 = 0;
            //The Result of SSE for inspection the cluster is shown
            //in Table 2. The table 2 shows the SSE value and rate of
            //change of the SSE when k = 2 to 10, found that when k = 4
            //SSE is the maximum rate of change.
            for (int sse_count = sses.size() - 1; sse_count > sses.size() - 11; sse_count--) {
                Double change = ((sses.get(sse_count - 1) - sses.get(sse_count)) * 100) / sses.get(sse_count);
                System.out.print("SSE of Hybrid number (" + id2 + ") -> " + sses.get(id2).toString() + "___");
                System.out.print("Change =" + change.toString() + '\n');
                id2++;
            }

        long time = System.currentTimeMillis() - StartProcessCure.startTime;
        System.out.println("The complete 2 versions took " + time + " milliseconds to complete.");
    }

    //points_clusters_hybrid Cure
    public void printClusterResults(ArrayList<ClusterBean> clusterBeans, String title, String csvTitle) {
        int id = 0;
        System.out.println("\n\t" + title + "(Clusting Using Representatives) Result(" + distance_type + ")\n");
        List<double[]> cCenters = new ArrayList<>();
        for (ClusterBean c : clusterBeans) {
            logClusters(c, csvTitle + ".csv", "Cluster-" + id);
            System.out.print("Cluster(" + id + ") -> ");
            c.print();
            id++;
        }
    }

}