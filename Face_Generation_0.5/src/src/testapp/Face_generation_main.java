package src.testapp;

import java.util.ArrayList;

import javax.media.opengl.GL;

import processing.core.*;

import processing.core.PFont;
import processing.net.Client;
import saito.objloader.*;
import damkjer.ocd.*;
import netP5.*;
import org.openkinect.*;
import org.openkinect.processing.Kinect;
import oscP5.*;
import codeanticode.glgraphics.*;
import controlP5.*;

//we need distance values to change the growth of the face
//face needs to be slightly more detailed
//growing process needs to be slightly different
//we need something around the faces 


public class Face_generation_main extends PApplet
{
	
	public static void main(String _args[]) 
	{
		PApplet.main(new String[] { src.testapp.Face_generation_main.class.getName() });
	}
		
//	OscP5 oscP5;
//	NetAddress myRemoteLocation;
	Client myClient;
	String kinectData;
	float distance;
	Kinect kinect;
	
	// Size of kinect image
	int w = 640;
	int h = 480;

		
	// We'll use a lookup table so that we don't have to repeat the math over and over
	float[] depthLookUp = new float[2048];
	
	OBJModel[] models = new OBJModel[5];
	float rotX, rotY;
	boolean clickFlag;
	boolean stormActive; 
	boolean firstCheck, secondCheck, thirdCheck, fourthCheck;
	float scaleValue, detailValue;
	int NUM_EXTRA_PARTICLES;
	int knCounter = -1;
	boolean knOff = false;
	boolean test = false;
	int id;

	ParticleSystem ps; 
	PFont mFont;

	
	
	GLGraphics pgl;
	GLGraphicsOffScreen offscreen;
	GL gl;

	GLTexture srcTex, bloomMask, destTex;
	GLTexture tex0, tex2, tex4, tex8, tex16;
	GLTextureFilter extractBloom, blur, blend4, toneMap;

	Camera cam;


	ControlP5 controlP5;
	ControlWindow controlWindow;
	
	Knob axisX;
	Knob axisY;
	Knob axisZ;
	
	float fx = 0.008f;
	float fy = 1.00f;
	float xValue = 180;
	float yValue = 180;
	float zValue = 180;
	float kinectThreshold = 1.8f;
	float kinectX = 0;
	float kinectY = 0;
	float kinectDistance = -1;
	float kinectTilt = 12;
	float kdb;
	float brightnessCoef = 0.79f;
	
	@SuppressWarnings("deprecation")
	public void setup()
	{
		myClient = new Client(this,"127.0.0.1", 4321);
//		oscP5 = new OscP5(this, 4321);
//		myRemoteLocation = new NetAddress("127.0.0.1",4321);
		controlP5 = new ControlP5(this);
		  
		controlP5.setAutoDraw(false);
		controlWindow = controlP5.addControlWindow("controlP5window",100,100,400,200);
		controlWindow.hideCoordinates();
		controlWindow.setBackground(color(0));
		 
		Controller expSlider = controlP5.addSlider("Exposure",0,1,0,40,10,100,14);
		Controller briSlider = controlP5.addSlider("Brightness", 0,1, 0, 40, 30,100, 14);
		Controller knctSlider = controlP5.addSlider("Kinect",1,5,0,40,50,100,14);
		Controller knctTiltSlider = controlP5.addSlider("Tilt", 10, 30, 0, 40, 70, 100, 14);
		Controller brightnessSlider = controlP5.addSlider("Bri_Coeff", 0.1f, 1.0f, 0, 40, 90,100,14);
		axisX = controlP5.addKnob("axisX", 0,360,40,110,40);
		axisY = controlP5.addKnob("axisY", 0,360,100,110,40);
		axisZ = controlP5.addKnob("axisZ", 0, 360, 160, 110, 40);
		  
		expSlider.setId(1); expSlider.setWindow(controlWindow);
		briSlider.setId(2); briSlider.setWindow(controlWindow);
	    knctSlider.setId(5); knctSlider.setWindow(controlWindow);
	    knctTiltSlider.setId(7); knctTiltSlider.setWindow(controlWindow);
	    brightnessSlider.setId(8); brightnessSlider.setWindow(controlWindow);
	    
		axisX.setId(3); axisX.setWindow(controlWindow);
		axisY.setId(4); axisY.setWindow(controlWindow);
		axisZ.setId(6); axisZ.setWindow(controlWindow);
		expSlider.setWindow(controlWindow);
		briSlider.setWindow(controlWindow);
	    controlWindow.setTitle("Face Generator Parameters");
		 
	      
		stormActive = true;

		size(1024,768, GLConstants.GLGRAPHICS);
		hint( ENABLE_OPENGL_4X_SMOOTH );
		
		//GLGraphics Part
		// Loading required filters.
		extractBloom = new GLTextureFilter(this, "ExtractBloom.xml");
		blur = new GLTextureFilter(this, "Blur.xml");
		blend4 = new GLTextureFilter(this, "Blend4.xml");  
		toneMap = new GLTextureFilter(this, "ToneMap.xml");
		   

		destTex = new GLTexture(this, width, height);
		 
		// Initializing bloom mask and blur textures.
		bloomMask = new GLTexture(this, width, height, GLTexture.FLOAT);
		tex0 = new GLTexture(this, width, height, GLTexture.FLOAT);
		tex2 = new GLTexture(this, width / 2, height / 2, GLTexture.FLOAT);
		tex4 = new GLTexture(this, width / 4, height / 4, GLTexture.FLOAT);
		tex8 = new GLTexture(this, width / 8, height / 8, GLTexture.FLOAT);
		tex16 = new GLTexture(this, width / 16, height / 16, GLTexture.FLOAT);
		 
		cam = new Camera(this, 0, 0, 200);
		 
		offscreen = new GLGraphicsOffScreen(this, width, height, true, 4);  
		pgl = (GLGraphics) g;  
		gl = offscreen.gl;
		 //End of GLGraphics
		
		background(0);
		
		
		models[0] = new OBJModel(this, "alyson_laugh.obj", "absolute", TRIANGLES);
	    models[1] = new OBJModel(this, "alyson_scared.obj", "absolute", TRIANGLES);
		models[2] = new OBJModel(this, "ryan_laughD.obj", "absolute", TRIANGLES);
		models[3] = new OBJModel(this, "ryan_scaredD.obj", "absolute", TRIANGLES);
		models[4] = new OBJModel(this, "ryan_surprisedD.obj", "absolute", TRIANGLES);
		
		
		
		smooth();
		colorMode(HSB);
		strokeWeight(4);
		
		detailValue 		= 2;
		scaleValue  		= 10;
		ps 		    		= new ParticleSystem(this, scaleValue);
		NUM_EXTRA_PARTICLES = 4000;
		
		loadModels(models);
		
		initKinect();
	}
	
	public void draw()
	{
		background(0);
		
		srcTex = offscreen.getTexture();
		 
		offscreen.beginDraw();
		offscreen.background(0);
		       
		cam.circle(radians(1));    
		cam.feed();
		
		offscreen.beginGL();
		gl.glPushMatrix();
		ps.update(stormActive, xValue, yValue, zValue, kinectX, kinectY);
		gl.glPopMatrix();
		offscreen.endGL();
		     	 
		offscreen.endDraw();
		// Extracting the bright regions from input texture.
		extractBloom.setParameterValue("bright_threshold", fx);
		extractBloom.apply(srcTex, tex0);

		// Downsampling with blur.
		tex0.filter(blur, tex2);
		tex2.filter(blur, tex4);    
		tex4.filter(blur, tex8);    
		tex8.filter(blur, tex16);    
		   
	    // Blending downsampled textures.
	    blend4.apply(new GLTexture[]{tex2, tex4, tex8, tex16}, new GLTexture[]{bloomMask});
		   
		// Final tone mapping into destination texture.
		toneMap.setParameterValue("exposure", fy);
		toneMap.setParameterValue("bright", fx);
		   
		toneMap.apply(new GLTexture[]{srcTex, bloomMask}, new GLTexture[]{destTex});
		   
		image(destTex, 0, 0, width, height);		
		
		kinect.tilt(kinectTilt);
		kinectDistance = getCenterDistance();
		kdb = kinectDistance;
		getKinectData();
		
		if(kinectDistance<kinectThreshold && kinectDistance>0.6)
		{			
			stormActive = false;
			try
			{				
				kinectData = id+" "+kinectX+" "+" "+kinectY+" "+kinectDistance;
				fy = scaleBrightness(kdb);
				
				myClient.write(kinectData);
				
//				OscMessage myMessage = new OscMessage("/test");
//				myMessage.add(kinectData);
//				oscP5.send(myMessage, myRemoteLocation); 
				

		    }
			catch(Exception ex)
			{
				println("Server not running! "+ex.toString());
			}
		}
		else if(kinectDistance>kinectThreshold)
		{
			stormActive = true;
			//ps.restore();
			id = (int)random(0,5);
			ps.changeDestinations(id);			
		}		
		
	}	
	float getCenterDistance()
	{
		int[] depth = kinect.getRawDepth();
		float distance = 10000;
		for (int x = 0; x < w; x ++) 
		{
			for (int y = 0; y < h; y ++)
			{
				int offset = x + y * w;
				int rawDepth = depth[153920];
				distance = rawDepthToMeters(rawDepth);
			}
		}
		//println(distance);
		return distance;
	}
	void getKinectData()
	{

		int allX = 0;
		int allY = 0;
		int all  = 0;
		float di =-2;
		
		int[] depth = kinect.getRawDepth();
		
		for (int x = 0; x < w; x ++) 
		{
			for (int y = 0; y < h; y ++)
			{
				int offset = x + y * w;
				int rawDepth = depth[offset];
				di = rawDepthToMeters(rawDepth);
				
				if (di < kinectThreshold && di!=0)
				{
					allX += x;
					allY += y;
					kinectDistance += di;
					all++;
				}
			}
						
		}
		if (all != 0)
		{
			
			kinectX  = allX/all;
			kinectY  = allY/all;
			kinectX *=1.6f;
			kinectY *=1.6f;
			kinectX  = kinectX - width/2.0f;
			kinectY  = kinectY - height/2.0f;			

			kinectDistance = kinectDistance/all;	
			//println("get ="+kinectDistance);
		}
		
	}		
	
	public void initKinect()
	{
		try
		{
			kinect = new Kinect(this);
			kinect.start();
			kinect.enableDepth(true);
			kinect.processDepthImage(false);
			kinect.tilt(kinectTilt);
			stroke(255);
			
			// Lookup table for all possible depth values (0 - 2047)
			
			for (int i = 0; i < depthLookUp.length; i++) 
			{
			    depthLookUp[i] = rawDepthToMeters(i);
			}
			
			println("Kinect initialized! or that's what we think...");
		}
		catch(Exception ex)
		{			
			println("Kinect Error: "+ex.toString());
		}
	}
	public PVector genSP(PVector avgPos)
	{
		PVector startingPosition;
		int x =(int) random(0,3);
		PVector additive = new PVector((random(0,200)),(random(0,200)),(random(0,200)));
		startingPosition = PVector.add(additive, avgPos);			
		
		return startingPosition;
	}
	public void mousePressed()
	{			
		 saveFrame("capture-####.png"); 
		 println("Image saved");
	}
	
	float rawDepthToMeters(int depthValue) 
	{
		if (depthValue < 2047)
		{
			return (float)(1.0 / ((double)(depthValue) * -0.0030711016 + 3.3309495161));
		}
		return 0.0f;
	}
	
	PVector depthToWorld(int x, int y, int depthValue) 
	{
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
	
	public void stop()
	{
		try
		{
			kinect.quit();
			super.stop();
		}
		catch(Exception ex)
		{
			println("Error: "+ex.toString());	
		}
	}
	
	public void init()
	{
		frame.removeNotify();
		frame.setUndecorated(true);
		
		frame.setLocation(1680, 0);
		super.init();
		
	}
	
	public void loadModels(OBJModel[] models)
	{
		Particle p;
		PVector[] destinations = new PVector[5];
		ArrayList[] vertices = new ArrayList[5];
		
		for(int i = 0; i < models.length; i++ )
		{	
			models[i].scale(430);
			models[i].translateToCenter();
			
			vertices[i] = new ArrayList();

			println( models[i].getVertexCount());
			
			for( int j = 0; j < models[i].getVertexCount(); j += detailValue)
			{
				PVector destinationPoint  = models[i].getVertex(j);
				vertices[i].add( destinationPoint );
				
			}
			if(i==0)
			{
				for(int l = 0; l < vertices[i].size(); l++)
				{
					destinations[i]  = (PVector) vertices[i].get(l);
					PVector position    = new PVector( random(400,-400), random(400,-400), 0);
					p  = new Particle( this, genSP(position), destinations);
					ps.addParticle( p );						
				}
				
				for( int m = 0; m < NUM_EXTRA_PARTICLES; m++  )
				{
					PVector position    = new PVector( random(400,-400), random(400,-400), 0);
					PVector destination = new PVector( 0, 0, random(40,-40) );
					Particle q  = new Particle( this, genSP(position), destination );
							
					ps.addParticle(q);
					ps.setNumExtraParticles(NUM_EXTRA_PARTICLES);
				}
				
			}
			else
			{
				for(int l = 0; l < vertices[i].size(); l++)
				{
					destinations[i] = (PVector)vertices[i].get(l);
					ps.setParticleDestinations(i,l, (PVector) vertices[i].get(l));
				}
			}
				 
		}
	}
	
	public void keyPressed() {
		  if(key==CODED) {
		    if(keyCode==LEFT) frame.setLocation(
		      frame.getLocation().x-20,
		      frame.getLocation().y);
		    if(keyCode==RIGHT) frame.setLocation(
		      frame.getLocation().x+20,
		      frame.getLocation().y);
		    if(keyCode==UP) frame.setLocation(
		      frame.getLocation().x,
		      frame.getLocation().y-20);
		    if(keyCode==DOWN) frame.setLocation(
		      frame.getLocation().x,
		      frame.getLocation().y+20);
		    }
		  if(key=='a')
			  {
			  	frame.setLocation(frame.getLocation().x-1, frame.getLocation().y);
			  }
		  if(key=='d')
		  {
			  	frame.setLocation(frame.getLocation().x+1, frame.getLocation().y);
			  }
		  if(key=='w')
		  {
			  	frame.setLocation(frame.getLocation().x, frame.getLocation().y-1);
			  }
		  if(key=='s')
		  {
			  	frame.setLocation(frame.getLocation().x, frame.getLocation().y+1);
			  }
		  if(key=='f'){
			  stormActive = true;
			  println("Storm is "+stormActive);			 
		  }
		  if(key=='F'){
			  stormActive = false;
			  println("Storm is "+stormActive);
		  }
					  
	}
	public float  scaleBrightness(float kDist)
	{
		float briValue = 1.0f;
		briValue = fy;
		float scaledDistance = kinectDistance;
		
		if(kinectDistance<kinectThreshold && briValue<=1 && briValue>0 && kinectDistance>0)
		{
			
			briValue = scaledDistance * brightnessCoef;
			//println("brightness = "+briValue);print("\tDistance="+kinectDistance);
				
		}
		else
		{
			briValue =  1.0f;

		}
		return briValue;
	}
	public void controlEvent(ControlEvent theEvent) {
		  switch(theEvent.controller().id()) {
		    case(1): // Exposure
		    	fx = theEvent.controller().value();
		    break;
		    case(2):  // Brightness
		    	fy = theEvent.controller().value();   
		    break;  
		    case(3): // X - Axis
		    	xValue = theEvent.controller().value();
		    break;
		    case(4):
		    	yValue = theEvent.controller().value();
		    break;
		    case(5):
		    	kinectThreshold = theEvent.controller().value();
		    break;
		    case(6):
		    	zValue = theEvent.controller().value();
		    break;
		    case(7):
		    	kinectTilt = theEvent.controller().value();
		    break;
		    case(8):
		    	brightnessCoef = theEvent.controller().value();
		    break;
		  }
		}
	/* incoming osc message are forwarded to the oscEvent method. */
	void oscEvent(OscMessage theOscMessage) {
	  /* print the address pattern and the typetag of the received OscMessage */
	  print("### received an osc message.");
	  print(" addrpattern: "+theOscMessage.addrPattern());
	  println(" typetag: "+theOscMessage.typetag());
	}
}