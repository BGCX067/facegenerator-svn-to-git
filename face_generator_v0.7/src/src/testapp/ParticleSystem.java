/*		Useful tutorials and examples
 * 		http://www.leolol.com/tutorials/graphics_tutorials.php
 * 		http://www.java-tips.org/other-api-tips/jogl/texture-mapping-nehe-tutorial-jogl-port.html
 * 		http://www.repeatwhiletrue.com/p5/P5JOGL.html
 * 		http://benhem.com/games/GLP5align/
 */

package src.testapp;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

import java.awt.image.BufferedImage;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.texture.*;

public class ParticleSystem  
{
	ArrayList particles;
	float destinationScale;
	PApplet parent;
	PGraphicsOpenGL pgl;
	int particleShape;
	float aspect, camZ;
	
	//Variables for frame buffer objects and etc.
	private int frameBufferObject1 = -1;
    private int frameBufferTexture1 = -1;

    private int frameBufferObject2 = -1;
    private int frameBufferTexture2 = -1;

    private Texture particleTexture;
    private BufferedImage texImage;
    private int texture;
    private int NUM_EXTRA_PARTICLES;

	GL gl;
	GLU glu;
	
	private String blurShaderSource =
        "const int MAX_KERNEL_SIZE = 25;" +
        "uniform sampler2D baseImage;" +
        "uniform vec2 offsets[MAX_KERNEL_SIZE];" +
        "uniform float kernelVals[MAX_KERNEL_SIZE];" +
        "" +
        "void main(void) {" +
        "    int i;" +
        "    vec4 sum = vec4(0.0);" +
        "" +
        "    for (i = 0; i < MAX_KERNEL_SIZE; i++) {" +
        "        vec4 tmp = texture2D(baseImage," +
        "                             gl_TexCoord[0].st + offsets[i]);" +
        "        sum += tmp * kernelVals[i];" +
        "    }" +
        "" +
        "    gl_FragColor = sum;" +
        "}";
    private int blurShader;
    
    private String brightPassShaderSource =
        "uniform sampler2D baseImage;" +
        "uniform float brightPassThreshold;" +
        "" +
        "void main(void) {" +
        "    vec3 luminanceVector = vec3(0.2125, 0.7154, 0.0721);" +
        "    vec4 sample = texture2D(baseImage, gl_TexCoord[0].st);" +
        "" +
        "    float luminance = dot(luminanceVector, sample.rgb);" +
      "    luminance = max(0.0, luminance - brightPassThreshold);" +
      "    sample.rgb *= sign(luminance);" +
      "    sample.a = 1.0;" +
        "" +
        "    gl_FragColor = sample;" +
        "}";
    private int brightPassShader;

    private float threshold = 0.3f;
    
    
    
	
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
		
		 try {
	            texImage = ImageIO.read(getClass().getResource("/data/test.png"));
	        } catch (IOException e) {
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
         buildList();
	     initTexture();       
	     gl.glDisable(GL.GL_DEPTH_TEST);
	     gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
	     gl.glEnable(GL.GL_BLEND);
	     gl.glBlendEquation(GL.GL_FUNC_ADD);
	     gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

	     pgl.endGL();
	     gl.glEnable(GL.GL_TEXTURE_2D);							// Enable Texture Mapping 


	     		
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

		gl.glColor4f( 1.0f, 1.0f, 1.0f,  0.3f);					// Set colour to red
           gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.0f, 12.0f, 12.0f);						// Top vertex
           gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-12.0f,-12.0f, 0.0f);						// Bottom left vertex
           gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 12.0f,-12.0f, 0.0f);						// Bottom right vertex

           gl.glColor4f( 1.0f, 1.0f, 1.0f,  0.3f);						// Set colour to green
		   
		   gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.0f, 12.0f, 12.0f);						// Top vertex
		   gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-12.0f,-12.0f, 4.0f);						// Bottom left vertex
		   gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( -12.0f,-12.0f, 0.0f);						// Bottom right vertex

		   gl.glColor4f( 1.0f, 1.0f, 1.0f,  0.3f);						// Set colour to blue
		   
		   gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.0f, 12.0f, 12.0f);						// Top vertex
		   gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-12.0f,-12.0f, 4.0f);						// Bottom left vertex
		   gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 12.0f,-12.0f, 4.0f);						// Bottom right vertex

		   gl.glColor4f( 1.0f, 1.0f, 1.0f,  0.3f);					// Set colour to purple
		   gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.0f, 12.0f, 12.0f);						// Top vertex
		   gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 12.0f,-12.0f, 0.0f);						// Bottom left vertex
		   gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 12.0f,-12.0f, 4.0f);						// Bottom right vertex

		 gl.glEnd();												// Finish drawing triangle sides

		 gl.glBegin(GL.GL_QUADS);									// Begin drawing square bottom

		   gl.glColor4f( 1.0f, 1.0f, 1.0f,  0.3f);					// Set colour to yellow
		   gl.glVertex3f(-12.0f,-12.0f, 0.0f);						// Bottom left vertex
		   gl.glVertex3f(-12.0f,-12.0f, 24.0f);						// Top left vertex
		   gl.glVertex3f( 12.0f,-12.0f, 24.0f);						// Bottom right vertex
		   gl.glVertex3f( 12.0f,-12.0f, 0.0f);						// Top right vertex

		 gl.glEnd();							
		 
		 // Finish drawing triangles
		 PApplet.println("Shape prepared");
	}

	int createFragmentProgram(String[] fragmentShaderSource)
	{
		int fragmentProgram, fragmentShader;
        int[] success = new int[1];
        
        // create the shader object and compile the shader source code
        fragmentShader = gl.glCreateShaderObjectARB(GL.GL_FRAGMENT_SHADER_ARB);
        gl.glShaderSourceARB(fragmentShader, 1, fragmentShaderSource, null);
        gl.glCompileShader(fragmentShader);
        gl.glGetObjectParameterivARB(fragmentShader,
                GL.GL_OBJECT_COMPILE_STATUS_ARB,
                success, 0);
        // print the compiler messages, if necessary
        int[] infoLogLength = new int[1];
        int[] length = new int[1];
        gl.glGetObjectParameterivARB(fragmentShader,
                                     GL.GL_OBJECT_INFO_LOG_LENGTH_ARB,
                                     infoLogLength, 0);
        if (infoLogLength[0] > 1) {
            byte[] b = new byte[1024];
            gl.glGetInfoLogARB(fragmentShader, 1024, length, 0, b, 0);
            System.out.println("Fragment compile phase = " + new String(b, 0, length[0]));
        }

        if (success[0] == 0) {
            gl.glDeleteObjectARB(fragmentShader);
            return -1;
        }
        
        // create the program object and attach it to the shader
        fragmentProgram = gl.glCreateProgramObjectARB();
        gl.glAttachObjectARB(fragmentProgram, fragmentShader);
        
        // it is now safe to delete the shader object
        gl.glDeleteObjectARB(fragmentShader);
        
        // link the program
        gl.glLinkProgramARB(fragmentProgram);
        gl.glGetObjectParameterivARB(fragmentProgram,
                                     GL.GL_OBJECT_LINK_STATUS_ARB,
                                     success, 0);
        
        gl.glGetObjectParameterivARB(fragmentShader,
                GL.GL_OBJECT_INFO_LOG_LENGTH_ARB,
                infoLogLength, 0);
        
        if (infoLogLength[0] > 1) {
            byte[] b = new byte[1024];
            gl.glGetInfoLogARB(fragmentShader, 1024, length, 0, b, 0);
            System.out.println("Fragment link phase = " + new String(b, 0, length[0]));
        }

        if (success[0] == 0) {
            gl.glDeleteObjectARB(fragmentProgram);
            return -1;
        }
        
		return fragmentProgram;
		
	}
	
	void enableBlurFragmentProgram(int program,
            float textureWidth,
            float textureHeight) 
	{
		gl.glUseProgramObjectARB(program);

		int kernelWidth = 5;
		int kernelHeight = 5;

		float xoff = 1.0f / textureWidth;
		float yoff = 1.0f / textureHeight;

		float[] offsets = new float[kernelWidth * kernelHeight * 2];
		int offsetIndex = 0;

		for (int i = -kernelHeight / 2; i < kernelHeight / 2 + 1; i++) 
		{
			for (int j = -kernelWidth / 2; j < kernelWidth / 2 + 1; j++) {
				offsets[offsetIndex++] = j * xoff;
				offsets[offsetIndex++] = i * yoff;
			}
		}

		int loc = gl.glGetUniformLocationARB(program, "offsets");
		gl.glUniform2fv(loc, offsets.length, offsets, 0);

		float[] values = createGaussianBlurFilter(2);

		loc = gl.glGetUniformLocationARB(program, "kernelVals");
		gl.glUniform1fvARB(loc, values.length, values, 0);
	}
	
	float[] createGaussianBlurFilter(int radius) {
        if (radius < 1) {
            throw new IllegalArgumentException("Radius must be >= 1");
        }

        int size = radius * 2 + 1;
        float[] data = new float[size * size];

        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;

        int index = 0;
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                float distance = x * x + y * y;
                data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
                total += data[index];
                index++;
            }
        }

        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }

        return data;
    }
	
	void renderParticle(PVector position)
	{
		particleTexture.bind();
		particleTexture.enable();
		gl.glMatrixMode(GL.GL_PROJECTION);  // tell GL that the camera is being set up now:
        gl.glLoadIdentity();
        glu.gluPerspective(180/3.0, aspect, camZ/10.0, camZ*10.0); // these commands are analogous to the p5
        glu.gluLookAt(0, 0, camZ, 0, 0, 0, 0, 1, 0);              // "perspective()" and "camera()," -- see reference
        
        
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        
        gl.glRotatef((float)0.0,-1,0,0); // perform the same rotations in the same order
        gl.glRotatef((float)0.0,0,1,0);   // the Y-axis is "backwards" in GL
//        gl.glRotatef((float)parent.mouseX*360/(float)parent.width,0,0,-1);
        gl.glRotatef((float)parent.mouseX*360/(float)parent.width,0,1,0);
        gl.glRotatef((float)parent.mouseY*360/(float)parent.height,-1,0,0);
        
//		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);	  		
        gl.glTranslatef(position.x, position.y, position.z);
        gl.glCallList(particleShape);	        
        gl.glPopMatrix();	        
  		
    
        //gl.glFlush();
	}

	void update(boolean stormActive)
	{
		for(int i=particles.size()-getNumExtraParticles();i>1; i--)
	    {
			//parent.println(i);
			Particle p = (Particle)particles.get(i);
	
			if(!stormActive)
			{
				

				p.update();
			}
			else
			{
				PVector newDestination = new PVector(0,50,40);
				//PVector newDestination2 = new PVector(0,0,0);
				//PVector newDestination3 = new PVector(0,50,40);
				//PVector newDestination4 = new PVector(0,0,39);
				Particle Z = new Particle(parent, p.location,newDestination );
				//Particle A = new Particle(parent, p.location,newDestination2 );
				//Particle B = new Particle(parent, p.location,newDestination3 );
				//Particle C = new Particle(parent, p.location,newDestination4 );
				p.gravitate(Z);
				//p.gravitate(A);
				//p.gravitate(B);
				//p.gravitate(C);
				p.addPos();
				
			}
	        renderParticle(p.location);
	        
	        for(int j = particles.size()-1; j > particles.size()-getNumExtraParticles(); j-- )
			{					
				Particle q = (Particle)particles.get(j);
				q = (Particle)particles.get(j);
				PVector newDestination = new PVector(100,50,40);
				Particle A = new Particle(parent, q.location,newDestination );
			
				q.gravitate(A);
				q.addPos();
				renderParticle(q.location);
				

				q.update();	
			}
	    }
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
}
