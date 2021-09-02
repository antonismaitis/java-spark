import java.io.Serializable;

public class ClusterNumber implements Serializable {

	public static int cluster_sizeByKdTree;
	public static void buildTree(ClusterBean root, int level, int pointsInCluster){

		ClusterBean left=new ClusterBean(pointsInCluster);
		ClusterBean right=new ClusterBean(pointsInCluster);
		//setting parent node
		left.parent=root;
		right.parent=root;
		root.left=left;
		root.right=right;

		int leftClusterPoint=0;
		int rightClusterPoint=0;

		for(int i=0;i<root.getDataPoints().size() ;i++){
			Point2D point= root.getDataPoints().get(i);

			if(point.getValue()[level]<root.medianCluster.getValue()[level]){
				try{
					left.getDataPoints().add(leftClusterPoint,point);
				}catch(IndexOutOfBoundsException ex){
					left.getDataPoints().set(leftClusterPoint,point);
				}
				leftClusterPoint++;
			}else{
				try{
					right.getDataPoints().add(rightClusterPoint,point);
				}catch (IndexOutOfBoundsException ex){
					right.getDataPoints().set(rightClusterPoint,point);
				}
				rightClusterPoint++;
			}

		}
		level=(level+1)%2; // 2 number of dimensions

		left.pointsInCluster=leftClusterPoint;
		right.pointsInCluster=rightClusterPoint;

		calculateMedianMinMax(left);
		calculateMedianMinMax(right);

		calculateDensity(left);
		calculateDensity(right);

		left.isLeaf=true;
		int treeflag=0;
		treeflag = getTreeflag(level, pointsInCluster, left, treeflag, left.density>root.density);

		right.isLeaf=true;
		treeflag = getTreeflag(level, pointsInCluster, right, treeflag, right.density > right.density);
		if(treeflag==0){
			root.isLeaf=true;
			root.left=null;
			root.right=null;
		}
	}

	private static int getTreeflag(int level, int pointsInCluster, ClusterBean right, int treeflag, boolean b) {
		if(right.pointsInCluster>5&& b){
			right.isLeaf=false;
			treeflag=1;
			ClusterNumber.buildTree(right,level,pointsInCluster);

		}else{
			right.isLeaf=true;
		}
		return treeflag;
	}

	public static void calculateMedianMinMax(ClusterBean cluster){
		try{
			double [] dim=new double[2]; // number of dimensions
			double [] dmax=new double[2];
			double [] dmin=new double[2];
			double [] sum=new double[2];

			for(int j=0;j<2;j++){
				dmax[j]=-1000000.0;
				dmin[j]=10000000.0;
				sum[j]=0.0;
			}

			for(int i=0;i<cluster.pointsInCluster;i++){

				for(int j=0;j<2;j++){ //number of dimensions
					sum[j]+= cluster.getDataPoints().get(i).getValue()[j];
					if(dmax[j]<cluster.getDataPoints().get(i).getValue()[j]){
						dmax[j]= cluster.getDataPoints().get(i).getValue()[j];
					}
					if(dmin[j]>cluster.getDataPoints().get(i).getValue()[j]){
						dmin[j]=cluster.getDataPoints().get(i).getValue()[j];
					}
				}

			}
			if(cluster.pointsInCluster>0){

				for(int j=0;j<2;j++){ //number of dimensions
					cluster.medianCluster.getValue()[j]=sum[j]/cluster.pointsInCluster;
					cluster.getMinDimesion().set(j,dmin[j]);
					cluster.getMaxDimesion().set(j,dmax[j]);
				}
//					medians


			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void calculateDensity(ClusterBean cluster){
		double volume=1;
		for(int j=0;j<2;j++){ //n of dimensions
			volume=volume*(cluster.getMaxDimesion().get(j)-cluster.getMinDimesion().get(j));
			//volume=volume*(this.maxDimesion[j]-this.minDimesion[j])/(this.maxDimesion[j]*this.minDimesion[j]);
		}
		cluster.density=cluster.pointsInCluster/volume;
	}

	public static void inOrder (ClusterBean root)
	{
		if(root == null) return;
		if(root.isLeaf)
		{
			cluster_sizeByKdTree++;
		}
		inOrder( root.left );
		inOrder( root.right );
	}

}
