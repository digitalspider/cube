package au.com.digitalspider.cube.service;

import java.util.List;

import au.com.digitalspider.cube.bean.CubeItem;
import au.com.digitalspider.cube.bean.CubeSpace;
import au.com.digitalspider.cube.bean.Orientation;

public interface CubingService {

	/**
	 * Use {@link #calculateCubeSpace(List, double, double, double, double, Orientation)} with default orientation
	 */
	public CubeSpace calculateCubeSpace(List<CubeItem> cubeList, double maxLength, double maxWidth, double maxHeight, double maxWeight) throws Exception;

	/**
	 * Calculates the optimal cube space required for a list of cubeItems
	 *
	 * @param cubeList list of {@link CubeItem} items to calculate
	 * @param maxLength the maximum length constraint
	 * @param maxWidth the maximum width constraint
	 * @param maxHeight the maximum height constraint
	 * @param maxWeight the maximum weight constraint
	 * @param orientation the orientation of the cubeItems, 0=any, 1=vertical, 2=horizontal (default)
	 * @return a {@link CubeSpace} with the width, length and height, and containing sub cubeSpaces and cubeItems
	 * @throws Exception if anything goes wrong
	 */
	public CubeSpace calculateCubeSpace(List<CubeItem> cubeList, double maxLength, double maxWidth, double maxHeight, double maxWeight, Orientation orientation) throws Exception;

}
