package au.com.digitalspider.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import au.com.digitalspider.bean.CubeItem;
import au.com.digitalspider.bean.CubeSpace;
import au.com.digitalspider.bean.Orientation;
import au.com.digitalspider.service.CubingService;

/**
 * Outstanding items:
 * <ul>
 * 	<li>Implementation is for width, length, and height. Weight is implemented, but works differently to the other dimensions and so not properly tested.</li>
 *  <li>Orientation.ANY is not implemented either.</li>
 * </ul>
 */
public class CubingServiceImpl implements CubingService {

	public static Logger LOG = Logger.getLogger(CubingServiceImpl.class);

	public List<CubeItem> parseCSVInput(String data) throws IOException {
		BufferedReader reader = new BufferedReader(new StringReader(data));
		String line;
		List<CubeItem> cubeList = new ArrayList<CubeItem>();
		while ((line = reader.readLine()) !=null) {
			if (line.trim().length()==0) {
				continue;
			}
			if (line.startsWith("PID")) {
				continue;
			}
			String[] lineParts = line.split(",");
			if (lineParts.length!=6) {
				continue;
			}
			String id = lineParts[0];
			double weight = Double.parseDouble(lineParts[1]);
			double l = Double.parseDouble(lineParts[2]);
			double w = Double.parseDouble(lineParts[3]);
			double h = Double.parseDouble(lineParts[4]);
			int quantity = Integer.parseInt(lineParts[5]);
			CubeItem cube = new CubeItem(id,l,w,h);
			cube.quantity = quantity;
			cube.weight = weight;
			cubeList.add(cube);
		}
		return cubeList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CubeSpace calculateCubeSpace(List<CubeItem> cubeList, double maxLength, double maxWidth, double maxHeight, double maxWeight) throws Exception {
		return calculateCubeSpace(cubeList,maxLength,maxWidth,maxHeight,maxWeight,null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CubeSpace calculateCubeSpace(List<CubeItem> cubeList, double maxLength, double maxWidth, double maxHeight, double maxWeight, Orientation orientation) throws Exception {
		Collections.sort(cubeList);

		if (orientation==null) {
			orientation = Orientation.HORIZONTAL;
		}

		CubeSpace cubeSpace = new CubeSpace(null);
		cubeSpace.maxWidth=maxWidth;
		cubeSpace.maxLength=maxLength;
		cubeSpace.maxHeight=maxHeight;
		cubeSpace.maxWeight=maxWeight;
		for (CubeItem cube : cubeList) {
			LOG.info(cubeSpace+". Adding "+cube);
			if (maxWeight>0 && cube.weight>cubeSpace.remainingWeight()) {
				throw new Exception (cubeSpace+". Cube exceeds weight constraints! "+cube);
			}
			switch(orientation) {
				case ANY:
					throw new Exception(orientation+" orientation not yet implemented!");
				case VERTICAL:
					cube = cube.orientateVertically();
					addCubeItemToCubeSpace(cube, cubeSpace, orientation);
					break;
				case HORIZONTAL:
					// Add each item individually
					int itemCount = cube.quantity;
					cube.quantity = 1;
					try {
						for (int i=0; i<itemCount; i++) {
							try {
								addCubeItemToCubeSpace(cube, cubeSpace, orientation);
							} catch (Exception e) {
								LOG.info(e);
								rearchitectureCubeSpace(cubeSpace);
								addCubeItemToCubeSpace(cube, cubeSpace, orientation);
							}
						}
					} finally {
						cube.quantity = itemCount;
					}
					break;
				default:
					throw new Exception("Unknown orientation: "+orientation);
			}
			LOG.info(cubeSpace+". Added "+cube);
		}
		return cubeSpace;
	}

	/**
	 * Recursively add the cube into the cubeSpace. First try to added to the deepest cubeSpace available, and then
	 * based on exceptions thrown, try to add it into any fitting cubeSpace.
	 *
	 * @param cube the cube to be added
	 * @param cubeSpace the cube space available
	 * @param orientation which orientation to use
	 * @throws Exception if the cube does not fit in
	 */
	void addCubeItemToCubeSpace(CubeItem cube, CubeSpace cubeSpace, Orientation orientation) throws Exception {
		if (cubeSpace.childCubeSpaceList.size()>0) {
			Iterator<CubeSpace> cubeSpaceItr = cubeSpace.childCubeSpaceList.iterator();
			while (cubeSpaceItr.hasNext()) {
				CubeSpace innerCubeSpace = cubeSpaceItr.next();
				try {
					try {
						if (innerCubeSpace.isFull()) {
							throw new Exception("This cube space is full. Try another one");
						}
						addCubeItemToCubeSpace(cube, innerCubeSpace, orientation);
						break;
					} catch (Exception e) {
						// Safe to ignore, unless this is the last space in the list
						if (!cubeSpaceItr.hasNext()) {
							LOG.info(e);
							CubeSpace newCubeSpace = rearchitectureCubeSpace(innerCubeSpace);
							addCubeItemToCubeSpace(cube, newCubeSpace, orientation);
						}
					}
				} catch (Exception e2) {
					if (cubeSpace.childCubeSpaceList.contains(innerCubeSpace)) {
						throw e2;
					} else {
						// CubeSpace was re-architectured while being inserted into.
						// Insert into the last available space
						CubeSpace newCubeSpace = cubeSpace.childCubeSpaceList.get(cubeSpace.childCubeSpaceList.size()-1);
						addCubeItemToCubeSpace(cube, newCubeSpace, orientation);
					}
				}
			}
			return;
		}
		switch (orientation) {
			case ANY:
				if (cube.longestSide()>cubeSpace.maxLength && cube.longestSide()>cubeSpace.maxWidth) {
					throw new Exception (cubeSpace+". Invalid cube exceeds constraints. Will never fit! "+cube);
				}
				break;
			case VERTICAL:
				if (cube.length>cubeSpace.maxLength) {
					throw new Exception (cubeSpace+". Cube exceeds length constraints! "+cube);
				}
				if (cube.width>cubeSpace.maxWidth) {
					throw new Exception (cubeSpace+". Cube exceeds width constraints! "+cube);
				}
				if (cube.height>cubeSpace.maxHeight) {
					throw new Exception (cubeSpace+". Cube exceeds height constraints! "+cube);
				}
				// initialize
				if (!cubeSpace.initialised) {
					cubeSpace.width=cube.width;
					cubeSpace.length=cube.length;
					cubeSpace.height=cube.height;
					cubeSpace.weight=cube.weight;
					cubeSpace.initialised=true;
					cubeSpace.cubeList.add(cube);
				} else {
					// if new cube is higher than current cube space, set new cubeSpace height
					if (cube.height > cubeSpace.height) {
						cubeSpace.height=cube.height;
					}
					// if new cube is longer than current cube space, set new cubeSpace length
					if (cube.length > cubeSpace.length) {
						cubeSpace.length=cube.length;
					}
					// add new cube width x cube qty, if possible
					if ((cube.width*cube.quantity)>cubeSpace.remainingWidth()) {
						throw new Exception(cubeSpace+". Cube exceeds width constraints! No more space available! "+cube);
					} else {
						cubeSpace.width += (cube.width*cube.quantity);
					}
					cubeSpace.cubeList.add(cube);
				}
				break;
			case HORIZONTAL:
				if (cube.length>cubeSpace.remainingLength() && cube.width>cubeSpace.remainingWidth()) {
					throw new Exception (cubeSpace+". Cube exceeds width and length constraints! No more space available! "+cube);
				}
				if (cube.height>cubeSpace.maxHeight) {
					if (cubeSpace.getRoot().remainingHeight()<cube.height) {
						throw new Exception (cubeSpace+". Cube exceeds height constraints! "+cube);
					}
					if (cubeSpace.getParent()!=null) {
						cubeSpace.getParent().raiseMaxHeight(cube.height);
					}
				}
				// if new cube can be added to cubeSpace by width
				if (cube.width <= cubeSpace.remainingWidth()) {
					cubeSpace.width+=cube.width;
					// Increase cubeSpace length, if necessary
					if (cube.length > cubeSpace.length) {
						// Check there is enough remaining length to increase;
						if (cube.length>cubeSpace.remainingLength()) {
							throw new Exception(cubeSpace+". Cube exceeds length constraints! No more space available! Need to go up! "+cube);
						}
						cubeSpace.length = cube.length;
					}
					// Increase cubeSpace height if necessary
					if (cube.height > cubeSpace.height) {
						cubeSpace.height = cube.height;
					}
					cubeSpace.cubeList.add(cube);
					break;
				}
				// cannot be added by width
				throw new Exception(cubeSpace+". Cube exceeds width constraints! No more space available! Need to go deeper! "+cube);
			default:
				throw new Exception("Unknown orientation: "+orientation);
		}
	}

	/**
	 * Re-architecture the given cube space, with at least some cubes in it, to break it into 4 cube spaces,
	 * a heightSpace and a heightRemainingSpace, and within the heightSpace, a lengthSpace, and a lengthRemainingSpace.
	 *
	 * @param cubeSpace the given cube space to re-architecture
	 * @return the next available cube space to try to insert into
	 * @throws Exception If the space cannot be re-architectured, or it has no cubes in it.
	 */
	CubeSpace rearchitectureCubeSpace(CubeSpace cubeSpace) throws Exception {
		// Get the remainingHeight before we change any items
		double remainingHeight = cubeSpace.remainingHeight();
		double remainingLength = cubeSpace.remainingLength();

		if (cubeSpace.cubeList.size()==0) {
			throw new Exception("Will not re-archirecture space with no cubes!");
		}

		boolean rearchitectureHeight = false;
		if (cubeSpace.orientation==Orientation.ANY || cubeSpace.orientation==Orientation.VERTICAL) {
			rearchitectureHeight = true;
		}

		CubeSpace workingSpace = cubeSpace;
		CubeSpace remainingSpace = null;

		if (rearchitectureHeight) {
			if (cubeSpace.getParent()!=null) {
				List<CubeSpace> siblingSpaces = cubeSpace.getParent().childCubeSpaceList;
				siblingSpaces.remove(siblingSpaces.size()-1);
				workingSpace = cubeSpace.getParent();
			}

			// Create height cubeSpace
			CubeSpace heightSpace = new CubeSpace(workingSpace, cubeSpace.maxWidth, cubeSpace.maxLength, cubeSpace.height, cubeSpace.remainingWeight());

			if (remainingHeight>0) {
				// Create remaining height cubeSpace
				CubeSpace remainingHeightSpace = new CubeSpace(workingSpace, cubeSpace.maxWidth, cubeSpace.maxLength, remainingHeight, cubeSpace.remainingWeight());
				remainingSpace = remainingHeightSpace;
			}

			workingSpace = heightSpace;
		} else {
			List<CubeSpace> siblingSpaces = cubeSpace.getParent().childCubeSpaceList;
			siblingSpaces.remove(siblingSpaces.size()-1);
			workingSpace = cubeSpace.getParent();
		}

		// Create selfSpace cubeSpace
		CubeSpace lengthSpace = new CubeSpace(workingSpace, cubeSpace.maxWidth, cubeSpace.length, cubeSpace.height, cubeSpace.remainingWeight());
		lengthSpace.width=cubeSpace.width;
		lengthSpace.length=cubeSpace.length;
		lengthSpace.height=cubeSpace.height;
		lengthSpace.weight=cubeSpace.weight;
		lengthSpace.cubeList.addAll(cubeSpace.cubeList);
		lengthSpace.initialised=true;

		if (remainingLength>0) {
			// Create remaining length cubeSpace
			CubeSpace remainingLengthSpace = new CubeSpace(workingSpace, cubeSpace.maxWidth, remainingLength, lengthSpace.height, cubeSpace.remainingWeight());
			remainingSpace = remainingLengthSpace;
		}

		// clear the parentCubeSpace
		cubeSpace.width=0;
		cubeSpace.length=0;
		cubeSpace.height=0;
		cubeSpace.weight=0;
		cubeSpace.cubeList.clear();

		return remainingSpace;
	}
}
