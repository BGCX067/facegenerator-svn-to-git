package src.testapp;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.*;

public class ParticleSystem  
{
	ArrayList<Particle> particles;
	float destinationScale;
	PApplet parent;
	PGraphicsOpenGL pgl;
	PVector newLocation;
	int particleShape;
	float aspect, camZ;
	
	//Variables for frame buffer objects and etc.

    private Texture particleTexture;
    private BufferedImage texImage;
    private int NUM_EXTRA_PARTICLES;
    
    float vertexSize = 0.4f;
    
	GL gl;
	GLU glu;
	int FBOId;
	int textureId;
	int STEPCOUNTER = 0;
	int COLLAPSECOUNTER = 0;
	int TEXTURE_WIDTH = 1024;
	int TEXTURE_HEIGHT = 768;

	float kinectX;
	float kinectY;
	
	ParticleSystem( PApplet p, float destScale)
	{
		parent = p;
		destinationScale = destScale;
	    particles = new ArrayList();
	    aspect=(float)parent.width/(float)parent.height;
	    camZ=(float) ((parent.height/2.0) / PApplet.tan((float) (PConstants.PI*60.0/360.0)));
	    
        
	    initGL();
	}
	
	void addParticle(Particle p)
	{
		Particle temp = p;
	    particles.add(temp);
	    
 	}
	
	void initTexture()
	{
		try 
		{
			texImage = ImageIO.read(getClass().getResource("/data/pearl2.png"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		particleTexture = TextureIO.newTexture(texImage, true);
		particleTexture.setTexParameteri(GL.GL_TEXTURE_WRAP_R,GL.GL_REPEAT);    
		particleTexture.setTexParameteri(GL.GL_TEXTURE_WRAP_S,GL.GL_REPEAT);
		particleTexture.setTexParameteri(GL.GL_TEXTURE_WRAP_T,GL.GL_REPEAT);
		PApplet.println("Texture loaded");
	}
	
	
	void initGL()
	{
		 glu = new GLU();
         pgl = (PGraphicsOpenGL) parent.g;  // g may change
         gl = pgl.beginGL();
	     initFBO();

	     initTexture();     
	     gl.glEnable(GL.GL_TEXTURE_2D);							// Enable Texture Mapping 
         buildList();
	     gl.glDisable(GL.GL_DEPTH_TEST);
	     gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
	     
	     pgl.endGL();

	     		
	}
	void initFBO()
	{
		
		gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);
		gl.glEnable(GL.GL_COLOR_MATERIAL);
		
		gl.glClearColor(0,0,0,0);
		gl.glClearStencil(0);
		gl.glClearDepth(1.0f);
		gl.glDepthFunc(GL.GL_LEQUAL);
		
		/*
		 * Create a texture object
		 * -> textureID
		 */
		int[] tmp = new int[1];
		gl.glGenTextures(1, tmp, 0);
		textureId = tmp[0];
		gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_GENERATE_MIPMAP, GL.GL_TRUE); // automatic mipmap gen incl ni opengl 1.4
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, BufferUtil.newByteBuffer(TEXTURE_WIDTH*TEXTURE_HEIGHT*4));
		//gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA,width, height,0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, BufferUtil.newByteBuffer(width * height * 4));
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
		
		/*
		 * Create FBO (frame buffer object)
		 * -> FBOID
		 */
		tmp = new int[1];
		gl.glGenFramebuffersEXT(1, tmp, 0);
		FBOId = tmp[0];
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, FBOId);
		
		/*
		 * Renderbuffer object
		 */
		
		tmp = new int[1];
		gl.glGenRenderbuffersEXT(1, tmp, 0);
		int renderId = tmp[0];
		gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, renderId);
		// allocate render storage
		int renderFormat = GL.GL_RGBA;
		//println("max renderbuffer size: "+GL.GL_MAX_RENDERBUFFER_SIZE_EXT);
		gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT, renderFormat, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		
		
		tmp = new int[1];
		gl.glGetRenderbufferParameterivEXT(GL.GL_RENDERBUFFER_EXT, GL.GL_RENDERBUFFER_WIDTH_EXT, tmp, 0);
		PApplet.println("rBuf width: "+tmp[0]);
		gl.glGetRenderbufferParameterivEXT(GL.GL_RENDERBUFFER_EXT, GL.GL_RENDERBUFFER_HEIGHT_EXT, tmp, 0);
		PApplet.println("rBuf height: "+tmp[0]);
		gl.glGetRenderbufferParameterivEXT(GL.GL_RENDERBUFFER_EXT, GL.GL_RENDERBUFFER_INTERNAL_FORMAT_EXT, tmp, 0);
		PApplet.println("rBuf format: "+tmp[0]);
		gl.glGetRenderbufferParameterivEXT(GL.GL_RENDERBUFFER_EXT, GL.GL_RENDERBUFFER_RED_SIZE_EXT, tmp, 0); // GREEN, BLUE, ALPHA, DEPTH, STENCIL
		PApplet.println("rBuf red size: "+tmp[0]);
		
		gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, 0);
		
		/*
		 * Attach image to FBO 
		 * attach a texture to FBO color attachment point
		 */
		int attachmentPoint = GL.GL_COLOR_ATTACHMENT0_EXT;
		int mipmapLevel = 0;//
		gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, attachmentPoint, GL.GL_TEXTURE_2D, textureId, mipmapLevel);
		
		/*
		 * Attach a renderbuffer to depth attachment point
		 */
		gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, GL.GL_RENDERBUFFER_EXT, renderId);

		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
		
		/*
		 * Check GL
		 */
		int status = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
		PApplet.println(status+" should be "+GL.GL_FRAMEBUFFER_COMPLETE_EXT);
		

		
	}
	
	void buildList()
	{
		particleShape = gl.glGenLists(1);
		gl.glNewList(particleShape, GL.GL_COMPILE);
		prepareParticleShape();
		gl.glEndList();
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		PApplet.println("display list created");

	}
	
	
	void prepareParticleShape()
	{
		gl.glBegin(GL.GL_TRIANGLES);							// Begin drawing triangle sides

		gl.glColor4f( 1.0f, 1.0f, 1.0f,  0.5f);					// Set colour to red

        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.0f, vertexSize, vertexSize);						// Top vertex
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-vertexSize,-vertexSize, 0.0f);						// Bottom left vertex
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( vertexSize,-vertexSize, 0.0f);						// Bottom right vertex


        gl.glColor4f( 1.0f, 1.0f, 1.0f,  0.5f);						// Set colour to green

		gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.0f, vertexSize, vertexSize);						// Top vertex
		gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-vertexSize,-vertexSize, vertexSize);						// Bottom left vertex
		gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( -vertexSize,-vertexSize, 0.0f);						// Bottom right vertex

		gl.glColor4f( 1.0f, 1.0f, 1.0f,  0.5f);						// Set colour to blue
		
		gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.0f, vertexSize, vertexSize);						// Top vertex
		gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-vertexSize,-vertexSize, vertexSize);						// Bottom left vertex
		gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( vertexSize,-vertexSize, vertexSize);						// Bottom right vertex


   	    gl.glColor4f( 1.0f, 1.0f, 1.0f,  0.5f);					// Set colour to purple
 
	    gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.0f, vertexSize, vertexSize);						// Top vertex
	    gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( vertexSize,-vertexSize, 0.0f);						// Bottom left vertex
	    gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( vertexSize,-vertexSize, vertexSize);						// Bottom right vertex


		gl.glEnd();												// Finish drawing triangle sides

		gl.glBegin(GL.GL_QUADS);									// Begin drawing square bottom

		gl.glColor4f( 1.0f, 1.0f, 1.0f,  0.5f);					// Set colour to yellow

		gl.glTexCoord2f(0.0f, 0.0f);gl.glVertex3f(-vertexSize,-vertexSize, 0.0f);						// Bottom left vertex
		gl.glTexCoord2f(0.0f, 1.0f);gl.glVertex3f(-vertexSize,-vertexSize, 2*vertexSize);						// Top left vertex
		gl.glTexCoord2f(1.0f, 0.0f);gl.glVertex3f( vertexSize,-vertexSize, 2*vertexSize);						// Bottom right vertex
		gl.glTexCoord2f(1.0f, 1.0f);gl.glVertex3f( vertexSize,-vertexSize, 0.0f);						// Top right vertex

	    gl.glEnd();							
		 
		// Finish drawing triangles
		PApplet.println("Shape prepared");
	}

	
	void renderParticle(PVector position)
	{
		gl.glPushMatrix();
		particleTexture.bind();
		gl.glTranslatef(position.x, position.y, position.z);
        gl.glCallList(particleShape);	        
        gl.glPopMatrix();	  

	}

	void update(boolean stormActive, float xValue, float yValue, float zValue, float kX, float kY)
	{
		kinectX = kX;
		kinectY = kY;
		
		gl.glMatrixMode(GL.GL_PROJECTION);  // tell GL that the camera is being set up now:
        gl.glLoadIdentity();
        glu.gluPerspective(180/3.0, aspect, camZ/10.0, camZ*10.0); // these commands are analogous to the p5
        glu.gluLookAt(0, 0, camZ, 0, 0, 0, 0, 1, 0);              // "perspective()" and "camera()," -- see reference
        
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        
        gl.glClearColor(0,0,0,1);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        
		gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_PIXEL_MODE_BIT); //V2
		gl.glDrawBuffer(GL.GL_BACK); //V2
		gl.glReadBuffer(GL.GL_BACK); //V2
		
        gl.glRotatef((float)0.0,-1,0,0); // perform the same rotations in the same order
        gl.glRotatef((float)0.0,0,1,0);   // the Y-axis is "backwards" in GL
        gl.glRotatef(xValue,1,0,0);
        gl.glRotatef(yValue,0,-1,0);
        gl.glRotated(zValue, 0,0,1);
        
        
        //storm should move around the face
        Particle p;
		Particle q = null;
        
        for(int j = particles.size()-1; j > particles.size()-getNumExtraParticles(); j-- )
		{			
			q = (Particle)particles.get(j);
			//512 384
			PVector newDestination = new PVector(512,-384,0);
			PVector newDestination2 = new PVector(-512,384,0);
			initGravitation(q.location, newDestination, q);
			initGravitation(q.location, newDestination2, q);
			renderParticle(q.location);
		}
        
        gl.glEnable(GL.GL_BLEND);
	    gl.glBlendEquation(GL.GL_FUNC_ADD);
	    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDepthMask(false);
		
        for(int i=particles.size()-getNumExtraParticles();i>1; i--)
	    {
			p = (Particle)particles.get(i);
	
			if(!stormActive)
			{				
				moveParticleDestination(p, q, i);
				p.update();				
				
			}
			else
			{
				PVector newDestination = q.location;
				initGravitation( p.location, newDestination, p);				
			}
			
	        renderParticle(p.location);
	    }
		
        gl.glPopMatrix();

	}
	
	public void initGravitation( PVector location, PVector destination, Particle p)
	{
		PVector start = location;
		PVector end   = destination;
		
		Particle z = new Particle(parent, start, end);
		
		p.gravitate(z);
		p.addPos();
	}
	
	//Check movement either in draw loop or own function
	void moveParticleDestination(Particle p, Particle q, int i)
	{
		if( STEPCOUNTER == 420 )
		{
			//remove for a different effect
			//particles.remove(p);
			//p.fixed = true;
			STEPCOUNTER = 0;
			//p.destination = p.location;
			p.destination = q.location;
			//particles.set(i, p);
			//restore();
			COLLAPSECOUNTER++;
			//parent.println(COLLAPSECOUNTER);
			
		}
		else if(!p.fixed)
		{						
			PVector mPosition = kinectTranslate();
			PVector trackAcc = PVector.sub(mPosition, p.location);
			trackAcc.mult(0.3f);
			p.location.add(PVector.mult(trackAcc, 0.2f));
			trackAcc.mult(0.0f);
			
			
		}
		
		STEPCOUNTER++;
		
	}
	
	//@SuppressWarnings("static-access")
	/*debug function
	public PVector mouseTranslate()
	{
		int xpoint = parent.mouseX-parent.width/2; 
		  int ypoint = parent.mouseY-parent.height/2; 
		  int zpoint = 500;
		  
		  // convert to polar coords
		  float r = parent.sqrt(xpoint*xpoint + ypoint*ypoint);
		  float theta = parent.atan2(ypoint, xpoint);
		  
		  // add the z-rotation
		  
		  // convert back to cartesian coords
		  xpoint = (int) (parent.round(r * parent.cos(theta)));
		  ypoint = (int) (parent.round(r * parent.sin(theta)));
		return new PVector(xpoint,ypoint,zpoint);
	}*/
	
	public PVector kinectTranslate()
	{		
		
		  float r = parent.sqrt(kinectX*kinectX + kinectY*kinectY);
		  float theta = parent.atan2(kinectY, kinectX);
		  
		  // add the z-rotation
		  
		  // convert back to cartesian coords
		  kinectX = (int) (parent.round(r * parent.cos(theta)));
		  kinectY = (int) (parent.round(r * parent.sin(theta)));		  
		 
		return new PVector(-kinectX,kinectY,500.0f);
	}
	
	public void changeDestinations(int id)
	{
		for(int i = 0; i < particles.size(); i++)
		{
			Particle p = particles.get(i);
			p.changeDestination(id);
		}
	}
	
	public void setParticleDestinations(int index, int index2, PVector destination)
	{
		Particle p;
		p = (Particle) particles.get(index2);
		p.destinations[index] = destination;
	}
	
	public int setNumExtraParticles(int NUM)
	{
		NUM_EXTRA_PARTICLES = NUM;
		return NUM_EXTRA_PARTICLES;
	}
	
	public int getNumExtraParticles()
	{
		return NUM_EXTRA_PARTICLES;
	}
	public int getSteps()
	{
		return STEPCOUNTER;
	}
}
