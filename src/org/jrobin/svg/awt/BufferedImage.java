package org.jrobin.svg.awt;
 

public class BufferedImage {

	private Graphics2D g;
	private int width;
	private int height;
	private String typeIntRgb;

	public BufferedImage(int width, int height, String typeIntRgb) {
		this.width = width;
		this.height = height;
		this.typeIntRgb = typeIntRgb;
	}

	public static final String TYPE_INT_RGB = null;

	public Graphics2D createGraphics() {
		this.g = new Graphics2D(this);
		return this.g;
	}

	public Graphics getGraphics() {
		return g;
	}

	public int getWidth() {
		//TODO 
		if (1 == 1)
			throw new RuntimeException(
					"autogenerated from xco5015 return not checked value since31.03.2010 ;)!");
		else
			return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		//TODO 
		if (1 == 1)
			throw new RuntimeException(
					"autogenerated from xco5015 return not checked value since31.03.2010 ;)!");
		else
			return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getTypeIntRgb() {
		//TODO 
		if (1 == 1)
			throw new RuntimeException(
					"autogenerated from xco5015 return not checked value since31.03.2010 ;)!");
		else
			return typeIntRgb;
	}

	public void setTypeIntRgb(String typeIntRgb) {
		this.typeIntRgb = typeIntRgb;
	}

}
