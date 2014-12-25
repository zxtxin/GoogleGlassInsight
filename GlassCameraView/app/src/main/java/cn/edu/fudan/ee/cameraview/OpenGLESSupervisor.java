package cn.edu.fudan.ee.cameraview;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glValidateProgram;

/**
 * Created by zxtxin on 2014/12/3.
 */
public class OpenGLESSupervisor {
    private Context mContext;
    private FloatBuffer vertexBuffer, textureCoordinatesBuffer;
    private int[] tex;
    private int[] fbo;
    static final float squareCoords[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f,  1.0f,
            -1.0f,  1.0f,
    };
    static final float textureVertices[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };
    public OpenGLESSupervisor(Context context) {
        mContext = context;
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords).position(0);
        ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureCoordinatesBuffer = bb2.asFloatBuffer();
        textureCoordinatesBuffer.put(textureVertices).position(0);
        tex = createTexture(4);
        fbo = createFrameBuffer(2);

    }
    public FloatBuffer getVertexBuffer(){return vertexBuffer;}
    public FloatBuffer getTextureCoordinatesBuffer(){return textureCoordinatesBuffer;}
    public int[] getTex(){return tex;}
    public int[] getFBO(){return fbo;}
    private int[] createTexture(int num){
        int[] texture = new int[num];
        GLES20.glGenTextures(num, texture, 0);
        for(int i = 0; i<num;i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[i]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return texture;
    }
    private int[] createFrameBuffer(int num) {
        int[] frameBuffer = new int[num];
        GLES20.glGenFramebuffers(num,frameBuffer,0);
        return frameBuffer;
    }
    private int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        Log.i("Results of compiling source", "\n" + shaderCode + "\n:" + glGetShaderInfoLog(shader));
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0)
        {
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }
    private int linkProgram(int vertexShader,int fragmentShader){
        int program = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(program, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(program, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(program);                  // creates OpenGL ES program executables
        Log.i("Results of linking program","\n"+glGetProgramInfoLog(program));
        // Get the link status.
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        // If the link failed, delete the program.
        if (linkStatus[0] == 0)
        {
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }
    public int buildShaderProgram(int vertex, int fragment) {
        String vertexShaderCode = TextResourceReader.readTextFileFromResource(mContext,vertex);
        String fragmentShaderCode = TextResourceReader.readTextFileFromResource(mContext,fragment);
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode);
        int program = linkProgram(vertexShader,fragmentShader);
        //       validateProgram(program);
        return program;
    }
    public boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS,
                validateStatus, 0);
        Log.v( "Results of validating program", + validateStatus[0]
                + "\nLog:" + glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }
}
