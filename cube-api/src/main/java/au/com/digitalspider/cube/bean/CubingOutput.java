package au.com.digitalspider.cube.bean;

public class CubingOutput {

	private CubeSpace cubeSpace;
	
	public String getResult() {
		return cubeSpace.toString();
	}

	public CubingOutput setCubeSpace(CubeSpace cubeSpace) {
		this.cubeSpace = cubeSpace;
		return this;
	}
}
