package cn.edu.fudan.ee.cameraview;

import android.hardware.Camera;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Created by zxtxin on 2014/12/3.
 */
public class RGBFilter extends FilterBase{
    private final int rgbProgram;
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer textureCoordinateBuffer;
    private final int positionAttr_rgb;
    private final int texCoordAttr_rgb;
    private final int tex0Uniform_rgb;
    private final int tex1Uniform_rgb;
    private int[] tex;

    public RGBFilter(OpenGLESSupervisor instance) {
        super(instance);
        rgbProgram = glInstance.buildShaderProgram(R.raw.rgb_filter_vertex, R.raw.rgb_filter_fragment);
        tex = glInstance.getTex();
        vertexBuffer = glInstance.getVertexBuffer();
        textureCoordinateBuffer = glInstance.getTextureCoordinatesBuffer();
        positionAttr_rgb = GLES20.glGetAttribLocation(rgbProgram, "vPosition");
        texCoordAttr_rgb = GLES20.glGetAttribLocation(rgbProgram, "inputTextureCoordinate");
        tex0Uniform_rgb = GLES20.glGetUniformLocation(rgbProgram, "u_Texture0");
        tex1Uniform_rgb = GLES20.glGetUniformLocation(rgbProgram, "u_Texture1");
    }

    @Override
    public void draw(ByteBuffer frameData, Camera.Size size, int pixelAmounts) {
        frameData.position(0);
        GLES20.glUseProgram(rgbProgram);
        GLES20.glVertexAttribPointer(positionAttr_rgb, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(positionAttr_rgb);
        GLES20.glVertexAttribPointer(texCoordAttr_rgb, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, textureCoordinateBuffer);
        GLES20.glEnableVertexAttribArray(texCoordAttr_rgb);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,tex[0]);
        GLES20.glTexImage2D( GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,size.width, size.height, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, frameData);
        GLES20.glUniform1i(tex0Uniform_rgb,0);
        frameData.position(pixelAmounts);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,tex[1]);
        GLES20.glTexImage2D( GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA,size.width/2, size.height/2, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE,frameData);
        GLES20.glUniform1i(tex1Uniform_rgb,1);

        // added
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0,4);

        // added
        GLES20.glViewport(viewOrigin_X, viewOrigin_Y, viewWidth, viewHeight);

        GLES20.glDisableVertexAttribArray(positionAttr_rgb);
        GLES20.glDisableVertexAttribArray(texCoordAttr_rgb);
    }
}
