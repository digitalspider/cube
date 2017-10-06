package au.com.digitalspider.service;

import java.util.List;

import au.com.digitalspider.bean.CubeItem;
import au.com.digitalspider.bean.CubeSpace;


public interface SlottingService {

	/**
	 * Find the smallest slot in the cubeSpace, which can take the given cubeItems.
	 * 
	 * @param spaces the cubeSpaces available
	 * @param items the items to slot
	 * @return the smallest CubeSpace that can take all the cubeItems
	 * @throws Exception
	 */
	public CubeSpace findCubeSpace(List<CubeItem> spaces, List<CubeItem> items) throws Exception;

}
