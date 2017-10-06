package au.com.digitalspider.cube.bean;

public enum Orientation {
	ANY(0), VERTICAL(1), HORIZONTAL(2);

	private int intValue;
	private Orientation(int orientation) {
		this.intValue = orientation;
	}
	public int getIntValue() {
		return intValue;
	}
	public static Orientation parseInt(int intValue) throws Exception {
		for (Orientation orientation : Orientation.values()) {
			if (orientation.getIntValue()==intValue) {
				return orientation;
			}
		}
		throw new Exception("No orientation exists with int value: "+intValue);
	}
}
