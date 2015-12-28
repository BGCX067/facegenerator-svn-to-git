package src.testapp;

import java.util.ArrayList;

import processing.core.*;

import peasy.*;
import processing.core.PFont;
import saito.objloader.*;
import controlP5.*;

//we need distance values to change the growth of the face
//face needs to be slightly more detailed
//growing process needs to be slightly different
//we need something around the faces 


public class TestApp extends PApplet
{
	
	public static void main(String _args[]) 
	{
		PApplet.main(new String[] { src.testapp.TestApp.class.getName() });
	}
	
	OBJModel model, model_2;
	float rotX, rotY;
	boolean clickFlag;
	boolean stormActive; 
	boolean firstCheck, secondCheck, thirdCheck, fourthCheck;
	float scaleValue, detailValue;
	int NUM_EXTRA_PARTICLES;

	ArrayList vertices, pattern;

	Proximity subjectTracker;
	ParticleSystem ps; 
	PFont mFont;

	PeasyCam cam;

	ControlP5 control;
	ControlWindow controlWindow;
	
	public void setup()
	{
		stormActive = true;
		//subjectTracker = new Proximity(this);
		mFont = createFont("ArialRoundedMTBold-36",48);
		size(640,480, OPENGL);
		background(0);
		frameRate(120);

		model   = new OBJModel(this, "ryan_scared.obj", "absolute", TRIANGLES);
		
		smooth();
		colorMode(HSB);
		strokeWeight(4);
		
		model.scale(400);
		model.translateToCenter();
		
		detailValue 		= 2;
		scaleValue  		= 10;
		vertices    		= new ArrayList();
		ps 		    		= new ParticleSystem(this, scaleValue);
		firstCheck  		= false;
		secondCheck 		= false;
		thirdCheck  		= false;
		fourthCheck 		= false;
		NUM_EXTRA_PARTICLES = 11;
		
		PVector averagePosition  = new PVector(0,0,0);
		for( int i = 0; i < model.getVertexCount(); i += detailValue)
		{
			PVector destinationPoint  = model.getVertex(i);
			vertices.add( destinationPoint );
			averagePosition.add( destinationPoint );
		}
		
		averagePosition.div(vertices.size());
		
		for(int i = 0; i < vertices.size(); i++)
		{
			PVector destination   = (PVector) vertices.get(i);
			Particle p  = new Particle( this, averagePosition, destination );
			ps.addParticle( p );
			
		}
		
		for( int i = 0; i < NUM_EXTRA_PARTICLES; i++  )
		{
			PVector position    = new PVector( random(40,-40), random(40,-40), 0);
			PVector destination = new PVector( 0, 0, 0 );
			Particle q  = new Particle( this, averagePosition, destination );
			
			ps.addParticle(q);
			ps.setNumExtraParticles(NUM_EXTRA_PARTICLES);
		}
		
		
		/*control = new ControlP5(this);
		controlWindow = control.addControlWindow("Particle Settings", 100, 100, 400, 200);*/

		//subjectTracker = new Proximity(this);
	  mFont = createFont("ArialRoundedMTBold-36",48);
	  //size(640,480, OPENGL);
	  //size(1440, 900, OPENGL);
	  hint(ENABLE_OPENGL_4X_SMOOTH);

	  background(0);
	  frameRate(120);
	 // model   = new OBJModel(this, "alyson_laugh.obj", "absolute", TRIANGLES);
	  model   = new OBJModel(this, "ryan_scared.obj", "absolute", TRIANGLES);
	  //model   = new OBJModel(this, "ryan_angry2.obj", "absolute", TRIANGLES);
	

	  
	/*	Controller first_check 		 = control.addToggle("firstCheck");
		Controller second_check 	 = control.addToggle("secondCheck");
		Controller third_check 		 = control.addToggle("thirdCheck");
		Controller fourth_check 	 = control.addToggle("fourthCheck");
		Controller firstPos	 		 = control.addSlider("frictionValue", 0,1,40,40,100,10);
		Controller secondPos 	 	 = control.addSlider("forceValue", 1, 10000, 40, 60, 100, 10);
		Controller thirdPos 		 = control.addSlider("minDistanceValue", 1, 500, 40, 80, 100, 10); 
		Controller fourthPos 	 	 = control.addSlider("forceValue", 1, 10000, 40, 60, 100, 10);
		
		firstPos.setWindow(controlWindow);
		secondPos.setWindow(controlWindow);
		thirdPos.setWindow(controlWindow);
		fourthPos.setWindow(controlWindow);
		
		first_check .setWindow(controlWindow);
		second_check .setWindow(controlWindow);
		third_check .setWindow(controlWindow);
		fourth_check .setWindow(controlWindow);*/
	}
	
	public void mouseDragged()
	{
	    rotX += (mouseX - pmouseX) * 0.01;
	    rotY -= (mouseY - pmouseY) * 0.01;
	}

	public void draw()
	{
		background(0);
		frame.setTitle(str((int)frameRate));
		ps.update(stormActive);
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
		stormActive = !stormActive;
	}
}