package src.testapp;

import org.apache.tools.ant.types.Assertions.EnabledAssertion;
import org.openkinect.*;
import org.openkinect.processing.Kinect;

import processing.core.PApplet;
import processing.core.PVector;

public class Proximity {
	
	float distance;
	Kinect kinect;
	PApplet parent;
	
	int w = 640;
	int h = 480;


	// We'll use a lookup table so that we don't have to repeat the math over and over
	float[] depthLookUp = new float[2048];

	
	Proximity(PApplet p)
	{
		parent = p;
		kinect = new Kinect(p);
		  kinect.start();
		  p.println( kinect.toString());
		  kinect.enableDepth(true);
		  
		  // We don't need the grayscale image in this example
		  // so this makes it more efficient
		  kinect.processDepthImage(false);
		  for (int i = 0; i < depthLookUp.length; i++) {
			    depthLookUp[i] = rawDepthToMeters(i);
		  }

	}
	
	void getDistance()
	{
		PVector subjectPosition = new PVector(0,0,0);
		int[] depth = kinect.getRawDepth();
		
		
	}
	// These functions come from: http://graphics.stanford.edu/~mdfisher/Kinect.html
	float rawDepthToMeters(int depthValue) {
		  if (depthValue < 2047) {
		    return (float)(1.0 / ((double)(depthValue) * -0.0030711016 + 3.3309495161));
		  }
		  return 0.0f;
		}

		PVector depthToWorld(int x, int y, int depthValue) {

		  final double fx_d = 1.0 / 5.9421434211923247e+02;
		  final double fy_d = 1.0 / 5.9104053696870778e+02;
		  final double cx_d = 3.3930780975300314e+02;
		  final double cy_d = 2.4273913761751615e+02;

		  PVector result = new PVector();
		  double depth =  depthLookUp[depthValue];//rawDepthToMeters(depthValue);
		  result.x = (float)((x - cx_d) * depth * fx_d);
		  result.y = (float)((y - cy_d) * depth * fy_d);
		  result.z = (float)(depth);
		  return result;
		}
}
