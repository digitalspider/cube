package au.com.digitalspider.cube.bean;

import java.util.Comparator;

/**
 * A class with a length, width, height, weight and quantity to represent a rectangular prism.
 */
public class CubeItem implements Comparable<CubeItem> {
	/**
	 * A unique identifier for this CubeItem
	 */
	public String id;
	/**
	 * The weight of the cube item. Only values>0 will not used in calculations
	 */
	public double weight;
	/**
	 * The length of the cube item. Only values>0 will not used in calculations. When representing a book lying flat, it is the length from the spine across the book.
	 */
	public double length;
	/**
	 * The width of the cube item. Only values>0 will not used in calculations. When representing a book lying flat, it is the width of the spine of the book.
	 */
	public double width;
	/**
	 * The height of the cube item. Only values>0 will not used in calculations. When representing a book lying flat, it is the length of the spine of the book.
	 */
	public double height;
	/**
	 * The quantity of the cube item. Defaults to 1.
	 */
	public int quantity=1;

	/**
	 * Instantiate a new CubeItem with the given id. Length,width,height and weight all 0. Quantity=1.
	 */
	public CubeItem(String id) {
		this(id,0,0,0,0,1);
	}

	/**
	 * Instantiate a new CubeItem with the given id,length,width,height. Weight=0. Quantity=1.
	 */
	public CubeItem(String id, double length,double width, double height) {
		this(id,length,width,height,0,1);
	}

	/**
	 * Instantiate a new CubeItem with the given id,length,width,height, and weight. Quantity=1.
	 */
	public CubeItem(String id, double length,double width, double height, double weight) {
		this(id,length,width,height,weight,1);
	}

	/**
	 * Instantiate a new CubeItem with the given id,length,width,height,weight, and quantity.
	 */
	public CubeItem(String id, double length,double width, double height, double weight, int quantity) {
		this.id = id;
		this.length = length;
		this.width = width;
		this.height = height;
		this.weight = weight;
		this.quantity = quantity;
	}

	/**
	 * Show the CubeItem[id] (length,width,height) qty=<qty>.
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName()+"["+id+"]=("+length+","+width+","+height+") qty="+quantity;
	}

	/**
	 * Return the longest side of this cube item.
	 * @return
	 */
	public double longestSide() {
		double maxLW = Math.max(length, width);
		return Math.max(maxLW,height);
	}

	/**
	 * Return the shortest side of this cube item.
	 * @return
	 */
	public double shortestSide() {
		double minLW = Math.min(length, width);
		return Math.min(minLW,height);
	}

	/**
	 * Return the second shortest side of this cube item.
	 * @return
	 */
	public double secondLongestSide() {
		double maxLW = Math.max(length, width);
		if (maxLW==longestSide()) {
			return Math.max(Math.min(length, width), height);
		} else {
			return Math.max(length, width);
		}
	}

	/**
	 * Rotate this item so that width becomes height, height becomes length, and length becomes width
	 * @return
	 */
	public CubeItem orientateVertically() {
		CubeItem cube = new CubeItem(id, this.width, this.height, this.length);
		cube.weight = this.weight;
		cube.quantity = this.quantity;
		return cube;
	}

	/**
	 * Compare a CubeItem based on it's heights, returning the highest first. If both heights are equal, use the
	 * {@link LongestSideComparator} to differentiate them.
	 */
	@Override
	public int compareTo(CubeItem cubeRHS) {
		if (this.height>cubeRHS.height) {
			return -1;
		}
		if (this.height<cubeRHS.height) {
			return 1;
		}
		return new LongestSideComparator().compare(this, cubeRHS);
	}

	/**
	 * Compare two CubeItems based on their longest side, returning the longest first.
	 * If both sides are equal, then based on their {@link CubeItem#secondLongestSide()}.
	 */
	public static class LongestSideComparator implements Comparator<CubeItem> {

		@Override
		public int compare(CubeItem cubeLHS, CubeItem cubeRHS) {
			if (cubeLHS.longestSide()>cubeRHS.longestSide()) {
				return -1;
			}
			if (cubeLHS.longestSide()<cubeRHS.longestSide()) {
				return 1;
			}
			// longestSides are equal
			if (cubeLHS.secondLongestSide()>cubeRHS.secondLongestSide()) {
				return -1;
			}
			if (cubeLHS.secondLongestSide()<cubeRHS.secondLongestSide()) {
				return 1;
			}
			// longestSides and secondLongest sides are equal
			if (cubeLHS.shortestSide()>cubeRHS.shortestSide()) {
				return -1;
			}
			if (cubeLHS.shortestSide()<cubeRHS.shortestSide()) {
				return 1;
			}
			return 0;
		}

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
