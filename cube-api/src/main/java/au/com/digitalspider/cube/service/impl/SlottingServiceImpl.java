package au.com.digitalspider.cube.service.impl;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import au.com.digitalspider.cube.bean.CubeItem;
import au.com.digitalspider.cube.bean.CubeSpace;
import au.com.digitalspider.cube.service.CubingService;
import au.com.digitalspider.cube.service.SlottingService;

@Service
public class SlottingServiceImpl implements SlottingService {

	public static Logger LOG = Logger.getLogger(SlottingServiceImpl.class);

	private CubingService cubingService;

	@Override
	public CubeSpace findCubeSpace(List<CubeItem> spaces, List<CubeItem> items) throws Exception {
		LOG.info("slotProduct START. spaces="+spaces+", items="+items);
		LOG.debug("cubingService="+cubingService);


		// Find the optimal CubeSpace by sorting the spaces, and finding the first type available using cubingService.calculateCubeSpace()
		Collections.sort(spaces);
		CubeSpace optimalSpace = null;
		for (CubeItem cubeSpace : spaces) {
			try {
				optimalSpace = cubingService.calculateCubeSpace(items, cubeSpace.getLength(), cubeSpace.getWidth(), cubeSpace.getHeight(), cubeSpace.getWeight());
				break;
			} catch (Exception e) {
				LOG.warn("CubeSpace ["+cubeSpace+"] could not fit items="+items+". ERROR: " +e.getMessage());
			}
		}
		if (optimalSpace==null) {
			throw new Exception("Could not find any cube space for items="+items+"!");
		}
		LOG.info("Found optimal cubeSpace = "+optimalSpace);
		return optimalSpace;
	}

}
