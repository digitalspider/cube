package au.com.digitalspider.cube.bean;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class CubeSpace {
	public double x;
	public double y;
	public double z;
	public Orientation orientation;
	public double length;
	public double width;
	public double height;
	public double weight;
	public double maxLength;
	public double maxWidth;
	public double maxHeight;
	public double maxWeight;
	public boolean initialised = false;
	/**
	 * The childCubeSpaceList is constantly being rewritten as recursive calculations are processed, and as such requires a "thread-safe" implementation
	 */
	public List<CubeSpace> childCubeSpaceList = new CopyOnWriteArrayList<CubeSpace>();
	public List<CubeItem> cubeList = new ArrayList<CubeItem>();

	private CubeSpace parent = null;
	private CubeSpace root;

	public CubeSpace(CubeSpace parent) {
		this.parent = parent;
		if (root==null) {
			if (parent==null) {
				this.root = this;
				orientation = Orientation.ANY;
			} else {
				this.root = parent.root;
			}
		}
		if (parent!=null) {
			if (parent==root) {
				orientation = Orientation.VERTICAL;
			} else {
				orientation = Orientation.HORIZONTAL;
			}
			parent.childCubeSpaceList.add(this);
		}
	}

	public CubeSpace(CubeSpace parent, double maxWidth, double maxLength, double maxHeight) {
		this(parent);
		this.maxWidth = maxWidth;
		this.maxLength = maxLength;
		this.maxHeight = maxHeight;
	}

	public CubeSpace(CubeSpace parent, double maxWidth, double maxLength, double maxHeight, double maxWeight) {
		this(parent, maxWidth, maxLength, maxHeight);
		this.maxWeight = maxWeight;
	}

	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat("00.00");
		String volumeString = "volume="+df.format(getVolumePercent())+"%";
		return getClass().getSimpleName()+"["+orientation.toString().toUpperCase().charAt(0)+"]["+x+","+y+","+z+"] spaces="+getCubeSpaces().size()+" cubes="+getCubeList().size()+" l="+df.format(getTotalLength())+"/"+maxLength+", w="+df.format(getTotalWidth())+"/"+maxWidth+" h="+df.format(getTotalHeight())+"/"+maxHeight+" "+volumeString;
	}

	public double remainingLength() {
		return maxLength-getTotalLength();
	}
	public double remainingWidth() {
		return maxWidth-getTotalWidth();
	}
	public double remainingHeight() {
		return maxHeight-getTotalHeight();
	}
	public double remainingWeight() {
		return maxWeight-getTotalWeight();
	}

	public boolean isFull() {
		return (length==maxLength && width==maxWidth && height==maxHeight);
	}

	/**
	 * Recursively find the width of this cube space
	 */
	public double getTotalWidth() {
		double value = this.width;
		// if recursive
		if (childCubeSpaceList.size()>0) {
			double subValue = 0;
			for (CubeSpace cubeSpace : childCubeSpaceList) {
				subValue = cubeSpace.getTotalWidth();
				if (subValue>value) {
					value = subValue;
				}
			}
		}
		return value;
	}

	/**
	 * Recursively find the length of this cube space
	 */
	public double getTotalLength() {
		double value = this.length;
		// if recursive
		if (childCubeSpaceList.size()>0) {
			double subValue = 0;
			for (CubeSpace cubeSpace : childCubeSpaceList) {
				subValue = cubeSpace.getTotalLength();
				if (cubeSpace.orientation==Orientation.VERTICAL) {
					// If vertical find the greatest length
					if (subValue>value) {
						value = subValue;
					}
				} else {
					// else amongst the horizontal add all lengths together
					value += subValue;
				}
			}
		}
		return value;
	}

	/**
	 * Recursively find the height of this cube space
	 */
	public double getTotalHeight() {
		double value = this.height;
		if (value>0) {
			return value;
		}
		// if recursive
		if (childCubeSpaceList.size()>0) {
			double subValue = 0;
			for (CubeSpace cubeSpace : childCubeSpaceList) {
				subValue = cubeSpace.getTotalHeight();
				if (subValue>0) {
					if (cubeSpace.orientation==Orientation.VERTICAL) {
						// if vertical add all heights together
						value += subValue;
					} else {
						// else find the greatest height amongst the horizontal
						if (subValue>value) {
							value = subValue;
						}
					}
				}
			}
		}
		return value;
	}

	/**
	 * Recursively find the weight of this cube space
	 */
	public double getTotalWeight() {
		double value = 0;
		// if recursive
		if (childCubeSpaceList.size()>0) {
			for (CubeSpace cubeSpace : childCubeSpaceList) {
				value += cubeSpace.getTotalWeight();
			}
		}
		return value;
	}

	/**
	 * Recursively find the set of all CubeItems in the cubeSpace
	 */
	public Set<CubeItem> getCubeList() {
		Set<CubeItem> cubeListInternal = new HashSet<CubeItem>();
		// if recursive
		if (childCubeSpaceList.size()>0) {
			for (CubeSpace cubeSpace : childCubeSpaceList) {
				cubeListInternal.addAll(cubeSpace.getCubeList());
			}
		} else {
			cubeListInternal.addAll(cubeList);
		}
		return cubeListInternal;
	}

	/**
	 * Recursively find the set of all CubeItems in the cubeSpace
	 */
	public Set<CubeSpace> getCubeSpaces() {
		Set<CubeSpace> cubeSpaceList = new HashSet<CubeSpace>();
		// if recursive
		if (childCubeSpaceList.size()>0) {
			for (CubeSpace cubeSpace : childCubeSpaceList) {
				cubeSpaceList.addAll(cubeSpace.getCubeSpaces());
			}
		}
		cubeSpaceList.add(this); // add yourself to the list
		return cubeSpaceList;
	}

	/**
	 * Recursively find all the volume used in all the child CubeSpaces
	 */
	private double getVolumeUsed() {
		double volume = 0;
		// if recursive
		if (childCubeSpaceList.size()>0) {
			for (CubeSpace childCubeSpace : childCubeSpaceList) {
				volume += childCubeSpace.getVolumeUsed();
			}
		} else {
			volume = length*width*height;
		}
		return volume;
	}

	/**
	 * Get the volume of space used by this cubeSpace
	 */
	public double getVolumePercent() {
		double volume = getVolumeUsed();
		double maxVolume = maxLength * maxWidth * maxHeight;
		double volumePercent = 0;
		if (maxVolume > 0) {
			volumePercent = volume * 100 / maxVolume;
		}
		return volumePercent;
	}

	public void raiseMaxHeight(double newHeight) {
		maxHeight=newHeight;
		// if recursive
		if (childCubeSpaceList.size()>0) {
			for (CubeSpace childCubeSpace : childCubeSpaceList) {
				childCubeSpace.raiseMaxHeight(newHeight);
			}
		}
	}

	public CubeSpace getParent() {
		return parent;
	}

	public CubeSpace getRoot() {
		return root;
	}
}
