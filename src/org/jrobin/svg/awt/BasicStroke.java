package org.jrobin.svg.awt;

public class BasicStroke implements Stroke{

	private float width;
	
	public String toString(){
		return "Stroke :"+width;
	}

	public BasicStroke(int i) {
		this.width = i;
	}

	public BasicStroke(float width) {
		this.width = width;
	}

	public float getWidth() {
			return width;
	}

}