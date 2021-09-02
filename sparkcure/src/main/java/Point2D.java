import org.apache.spark.sql.Row;
import org.apache.spark.ml.linalg.Vector;
import java.io.Serializable;
import java.util.Comparator;

public class Point2D implements Serializable, Comparable<Point2D> {
	/**
	 * Compares two points by x-coordinate.
	 */
	public static final Comparator<Point2D> X_ORDER = new XOrder();
	protected double x;
	protected double y;
	private int spot_index;
	protected double[] value;
	public final Comparator<Point2D> POLAR_ORDER = new PolarOrder();

	public Point2D(double x_spot, double y_spot, int index) {
		this.value = new double[2];
		this.value[0] = x_spot;
		this.x = x_spot;
		this.value[1] = y_spot;
		this.y = y_spot;
		this.spot_index = index;
	}

	public Point2D(Vector vector) {
		this.value = new double[2];
		this.value[0] = vector.toArray()[0];
		this.x = vector.toArray()[0];
		this.value[1] = vector.toArray()[1];
		this.y = vector.toArray()[1];
	}

	public Point2D(int spot_index, double[] data) {
		this.spot_index = spot_index;
		this.value = new double[data.length];
		System.arraycopy(data, 0, value, 0, value.length);
		this.x=data[0];
		this.y=data[1];
	}

	public Point2D(double[] data) {
		this.value = new double[data.length];
		System.arraycopy(data, 0, value, 0, value.length);
		this.x=data[0];
		this.y=data[1];
	}

	public void setValue(double[] v) {
		System.arraycopy(v, 0, value, 0, value.length);
		this.x=v[0];
		this.y=v[1];
	}

	public double[] getValue() {
		return this.value;
	}

	protected Point2D(int n) {
		value = new double[n];
	}

	protected Point2D(int n, int index) {
		value = new double[n];
		this.spot_index = index;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		for (double aCoord : value) {
			s.append(aCoord).append(" ");
		}
		return s.toString();
	}

	protected double distance(double[] a, double[] b) {
		return Math.sqrt(sqrdist(a, b));
	}

	private static double sqrdist(double[] a, double[] b) {
		double dist = 0;
		for (int i = 0; i < a.length; ++i) {
			double diff = (a[i] - b[i]);
			dist += diff * diff;
		}
		return dist;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}


	// performance


	/**
	 * Returns the polar radius of this point.
	 * @return the polar radius of this point in polar coordiantes: sqrt(x*x + y*y)
	 */
	public double r() {
		return Math.sqrt(x*x + y*y);
	}

	/**
	 * Returns the angle of this point in polar coordinates.
	 * @return the angle (in radians) of this point in polar coordiantes (between –&pi; and &pi;)
	 */
	public double theta() {
		return Math.atan2(y, x);
	}

	/**
	 * Returns the angle between this point and that point.
	 * @return the angle in radians (between –&pi; and &pi;) between this point and that point (0 if equal)
	 */
	private double angleTo(Point2D that) {
		double dx = that.x - this.x;
		double dy = that.y - this.y;
		return Math.atan2(dy, dx);
	}

	/**
	 * Returns true if a→b→c is a counterclockwise turn.
	 * @param a first point
	 * @param b second point
	 * @param c third point
	 * @return { -1, 0, +1 } if a→b→c is a { clockwise, collinear; counterclocwise } turn.
	 */
	public static int ccw(Point2D a, Point2D b, Point2D c) {
		double area2 = (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x);
		if      (area2 < 0) return -1;
		else if (area2 > 0) return +1;
		else                return  0;
	}

	/**
	 * Returns twice the signed area of the triangle a-b-c.
	 * @param a first point
	 * @param b second point
	 * @param c third point
	 * @return twice the signed area of the triangle a-b-c
	 */
	public static double area2(Point2D a, Point2D b, Point2D c) {
		return (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x);
	}

	/**
	 * Returns the Euclidean distance between this point and that point.
	 * @param that the other point
	 * @return the Euclidean distance between this point and that point
	 */
	public double distanceTo(Point2D that) {
		double dx = this.x - that.x;
		double dy = this.y - that.y;
		return Math.sqrt(dx*dx + dy*dy);
	}

	/**
	 * Returns the square of the Euclidean distance between this point and that point.
	 * @param that the other point
	 * @return the square of the Euclidean distance between this point and that point
	 */
	public double distanceSquaredTo(Point2D that) {
		double dx = this.x - that.x;
		double dy = this.y - that.y;
		return dx*dx + dy*dy;
	}

	/**
	 * Compares two points by y-coordinate, breaking ties by x-coordinate.
	 * Formally, the invoking point (x0, y0) is less than the argument point (x1, y1)
	 * if and only if either {@code y0 < y1} or if {@code y0 == y1} and {@code x0 < x1}.
	 *
	 * @param  that the other point
	 * @return the value {@code 0} if this string is equal to the argument
	 *         string (precisely when {@code equals()} returns {@code true});
	 *         a negative integer if this point is less than the argument
	 *         point; and a positive integer if this point is greater than the
	 *         argument point
	 */
	public int compareTo(Point2D that) {
		if (this.y < that.y) return -1;
		if (this.y > that.y) return +1;
		return Double.compare(this.x, that.x);
	}

	/**
	 * Compares two points by polar angle (between 0 and 2&pi;) with respect to this point.
	 *
	 * @return the comparator
	 */
	public Comparator<Point2D> polarOrder() {
		return new PolarOrder();
	}

	/**
	 * Compares two points by atan2() angle (between –&pi; and &pi;) with respect to this point.
	 *
	 * @return the comparator
	 */
	public Comparator<Point2D> atan2Order() {
		return new Atan2Order();
	}

	/**
	 * Compares two points by distance to this point.
	 *
	 * @return the comparator
	 */
	public Comparator<Point2D> distanceToOrder() {
		return new DistanceToOrder();
	}

	// compare points according to their x-coordinate
	private static class XOrder implements Comparator<Point2D> {
		public int compare(Point2D p, Point2D q) {
			if(p!=null && q!=null) {
				return Double.compare(p.x, q.x);
			}
			return 100;
		}
	}

	// compare points according to their y-coordinate
	private static class YOrder implements Comparator<Point2D> {
		public int compare(Point2D p, Point2D q) {
			if(p!=null && q!=null) {
				return Double.compare(p.y, q.y);
			}
			return 100;
		}
	}

	// compare points according to their polar radius
	private static class ROrder implements Comparator<Point2D> {
		public int compare(Point2D p, Point2D q) {
			double delta = (p.x*p.x + p.y*p.y) - (q.x*q.x + q.y*q.y);
			if (delta < 0) return -1;
			if (delta > 0) return +1;
			return 0;
		}
	}

	// compare other points relative to atan2 angle (bewteen -pi/2 and pi/2) they make with this Point
	private class Atan2Order implements Comparator<Point2D> {
		public int compare(Point2D q1, Point2D q2) {
			double angle1 = angleTo(q1);
			double angle2 = angleTo(q2);
			if      (angle1 < angle2) return -1;
			else if (angle1 > angle2) return +1;
			else                      return  0;
		}
	}

	// compare other points relative to polar angle (between 0 and 2pi) they make with this Point
	private class PolarOrder implements Comparator<Point2D> {
		public int compare(Point2D q1, Point2D q2) {
			double dx1 = q1.x - x;
			double dy1 = q1.y - y;
			double dx2 = q2.x - x;
			double dy2 = q2.y - y;

			if      (dy1 >= 0 && dy2 < 0) return -1;    // q1 above; q2 below
			else if (dy2 >= 0 && dy1 < 0) return +1;    // q1 below; q2 above
			else if (dy1 == 0 && dy2 == 0) {            // 3-collinear and horizontal
				if      (dx1 >= 0 && dx2 < 0) return -1;
				else if (dx2 >= 0 && dx1 < 0) return +1;
				else                          return  0;
			}
			else return -ccw(Point2D.this, q1, q2);     // both above or below

			// Note: ccw() recomputes dx1, dy1, dx2, and dy2
		}
	}

	// compare points according to their distance to this point
	private class DistanceToOrder implements Comparator<Point2D> {
		public int compare(Point2D p, Point2D q) {
			double dist1 = distanceSquaredTo(p);
			double dist2 = distanceSquaredTo(q);
			if      (dist1 < dist2) return -1;
			else if (dist1 > dist2) return +1;
			else                    return  0;
		}
	}


	/**
	 * Compares this point to the specified point.
	 *
	 * @param  other the other point
	 * @return {@code true} if this point equals {@code other};
	 *         {@code false} otherwise
	 */
	@Override
	public boolean equals(Object other) {
		if (other == this) return true;
		if (other == null) return false;
		if (other.getClass() != this.getClass()) return false;
		Point2D that = (Point2D) other;
		return this.x == that.x && this.y == that.y;
	}

	/**
	 * Returns an integer hash code for this point.
	 * @return an integer hash code for this point
	 */
	@Override
	public int hashCode() {
		int hashX = ((Double) x).hashCode();
		int hashY = ((Double) y).hashCode();
		return 31*hashX + hashY;
	}

}