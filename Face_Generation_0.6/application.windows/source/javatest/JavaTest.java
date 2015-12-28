package javatest;

import processing.core.*;
import processing.opengl.*;
import peasy.*;


public class JavaTest extends PApplet {

	PeasyCam cam;
	
	public static void main(String args[]) {
	    PApplet.main(new String[] { "--present", "JavaTest" });
	  }
	
	public void setup()
	{
	   size(600, 600, OPENGL);
	   frameRate(60);
	   cam = new PeasyCam(this,100);
	   background(0);

	    

	}

	public void draw()
	{
	  background(255);
	  
	  for(int i = 0; i < (360*12); i++)
	  {
	   pushMatrix();
	   translate(0,0,0);
	   rotateX(i/((float)0.25*PI));
	   rect(i,i,10,10);
	   popMatrix();
	   fill(0,150);
	  }
	  
	  for(int i = 0; i > (-360*12); i--)
	  {
	   pushMatrix();
	   translate(0,0,0);
	   rotateX(i/((float)0.10*PI));
	   rect(i,i,10,10);
	   popMatrix();
	   fill(0,150);
	  }
	  
	  
	  stroke(0,10);
	}
}