import org.apache.parquet.example.data.simple.IntegerValue;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Maitis Antonios
 * @author Tzortzidis Georgios
 * @version 1.0
 */
public class StartProcessCure {
    private static SparkSession ss;
    protected static List<ArrayList<Point2D>> pointsGatheredAll = new ArrayList<>();
    private static ArrayList<Point2D> pointsGathered = new ArrayList<>();
    protected static AtomicInteger list_ID;
    protected static long startTime;

    public static void main(String args[]) {
        initSparkParams();
        CURE_Algorithm algorithm = new CURE_Algorithm();
        AtomicInteger counter_ID = new AtomicInteger(0);
        Scanner scan = new Scanner(System.in); //Creates a new scanner
        System.out.println("Welcome to CURE and Hybrid Cure using Kmeans algorithm."+ '\n');//Asks question
        System.out.println("Please define sample size?  "); //Asks question
        String sampleSize = scan.nextLine(); //Waits for input
        System.out.println("Please define representatives size for each cluster?  "); //Asks question
        String repr = scan.nextLine(); //Waits for input
        System.out.println("Please provide the number of known Clusters - otherwise set 0"); //Asks question
        String clzise = scan.nextLine(); //Waits for input
        System.out.println("Thank you."+ '\n');
        System.out.println("Start Reading files...");
        boolean notgivenCluster=false;
        if(clzise.equals("0")){
            notgivenCluster=true;
        }
        list_ID = new AtomicInteger(0);
        startTime = System.currentTimeMillis();
        long startTimeReading = System.currentTimeMillis();
        for (int u = 1; u <= 100; u++) {
            try {
                Dataset<Row> spots = ss.read().text("C:\\Users\\Antonis\\Desktop\\backup\\MScDataSciense\\data1-20\\data" + String.valueOf(u) + ".txt").cache();

                spots.foreach(point -> {
                    pointsGathered.add(new Point2D(Double.valueOf(point.toString().substring(1, point.toString().length() - 1).split(",")[0])
                            , Double.valueOf(point.toString().substring(1, point.toString().length() - 1).split(",")[1]), counter_ID.intValue()));
                    counter_ID.getAndIncrement();
                });
                pointsGatheredAll.add(list_ID.intValue(), pointsGathered);
                CURE_Algorithm.pointsGathered.addAll(pointsGathered);
                list_ID.getAndIncrement();
                pointsGathered = new ArrayList<>();
            } catch (Exception an) {
                System.out.print("Some null values detected");
            }
        }
        long time = System.currentTimeMillis() - startTimeReading;
        System.out.println("Reading files took " + time + " milliseconds to complete.");
        algorithm.initValues_andSample("euclidean",  Integer.valueOf(sampleSize), Integer.valueOf(repr),notgivenCluster,Integer.valueOf(clzise));
        algorithm.startClustering(ss);
        VisualizeClusters.runVisual();
    }

    private static void initSparkParams() {
        SparkConf conf = new SparkConf();

        System.setProperty("hadoop.home.dir", "C:\\Users\\Antonis\\tools\\spark-3.1.2");
        System.setProperty("spark.home.dir", "C:\\Users\\Antonis\\tools\\spark-3.1.2");
        conf.setAppName("sparkcure")
                .setMaster("local[*]")
                .set("spark.executor.instances", "4")
                .set("spark.memory.storageFraction", "0.90")
                .set("spark.yarn.scheduler.reporterThread.maxFailures", "35")
                .set("spark.storage.level", "MEMORY_AND_DISK_SER")
                .set("spark.rdd.compress", "true")
                .set("spark.shuffle.compress", "true")
                .set("spark.shuffle.service.enabled", "true")
                .set("spark.scheduler.allocation.file", "src/main/resources/fairscheduler.xml")
                .set("spark.scheduler.pool", "default")
                .set("spark.scheduler.mode", "FAIR")
                .set("spark.memory.fraction", "0.90")
                .set("spark.default.parallelism", "4")
                .set("spark.sql.shuffle.partitions", "80")
                .set("spark.network.timeout", "10000001")
                .set("spark.executor.memory", "10g")
                .set("spark.driver.memory", "10g")
                .set("spark.executor.heartbeatInterval", "10000000")
                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .set("spark.streaming.backPressure.enabled", "true");
        ss = SparkSession.builder().config(conf).getOrCreate();
        ss.sparkContext().setLogLevel("ERROR");
        Class[] arrayClass = new Class[5];
        arrayClass[0] = (Point2D.class);
        arrayClass[1] = (ClusterBean.class);
        arrayClass[2] = (CURE_Algorithm.class);
        arrayClass[3] = (FarthestPair.class);
        arrayClass[4] = (ClosestPair.class);
        conf.registerKryoClasses(arrayClass);
    }

}