package au.com.digitalspider.bean;

import java.util.List;

import au.com.digitalspider.service.CubingService;

/**
 * JSON input defines the cube constraints, the list of {@link CubeItem} objects, and whether to populateProduct dimensions.
 * The processing is done by {@link CubingService#calculateCubeSpace}.
 * if populateProducts is true it assumes ids and quantities are provided, and dimensions will be populated using {@link #populateCubeItemDimensions(List)}
 *
 * The format of the input is:
 * <code>
 * {cubeSpaces: [{weight:0.281,length:17.8,width:11.1,height:3.2}], cubeItems: [{id:25845880,weight:0.500,length:24.4,width:16.8,height:2.0,quantity:1},{id:29854048,weight:0.028,length:22.9,width:15.2,height:2.5,quantity:3}]}
 * </code>
 */
public class CubingInput {

	private List<CubeItem> cubeSpaces;
	private List<CubeItem> cubeItems;

	public List<CubeItem> getCubeItems() {
		return cubeItems;
	}

	public void setCubeItems(List<CubeItem> cubeItems) {
		this.cubeItems = cubeItems;
	}

	public List<CubeItem> getCubeSpaces() {
		return cubeSpaces;
	}

	public void setCubeSpaces(List<CubeItem> cubeSpaces) {
		this.cubeSpaces = cubeSpaces;
	}
	
}
