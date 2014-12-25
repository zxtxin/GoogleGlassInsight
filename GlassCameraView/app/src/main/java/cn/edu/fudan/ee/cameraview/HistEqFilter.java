package cn.edu.fudan.ee.cameraview;

import android.hardware.Camera;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * Created by zxtxin on 2014/12/3.
 */
public class HistEqFilter extends FilterBase {
    private final int histGenProgram;
    private final int prefixSumProgram;
    private final int histRemapProgram;
    private final int pointAttr_HistGen;
    private final int positionAttr_PrefixSum;
    private final int texCoordAttr_PrefixSum;
    private final int histogramTextureUniform_PrefixSum;
    private final int niUniform_PrefixSum;
    private final int positionAttr_HistRe;
    private final int texCoordAttr_HistRe;
    private final int tex0Uniform_HistRe;
    private final int tex1Uniform_HistRe;
    private final int tex2Uniform_HistRe;
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer textureCoordinateBuffer;
    private int[] tex;
    private int[] fbo;
    private int downsamplingFactor = 31;
    private int[] histogramBin = new int[256];
    private ByteBuffer prefixSumBuffer;


    public HistEqFilter(OpenGLESSupervisor instance) {
        super(instance);
        histGenProgram = glInstance.buildShaderProgram(R.raw.histo_gen_vertex,R.raw.histo_gen_fragment);
        prefixSumProgram = glInstance.buildShaderProgram(R.raw.prefix_sum_vertex,R.raw.prefix_sum_fragment);
        histRemapProgram = glInstance.buildShaderProgram(R.raw.histo_remapping_vertex,R.raw.histo_remapping_fragment);
        tex = glInstance.getTex();
        fbo = glInstance.getFBO();
        vertexBuffer = glInstance.getVertexBuffer();
        textureCoordinateBuffer = glInstance.getTextureCoordinatesBuffer();
        pointAttr_HistGen = GLES20.glGetAttribLocation(histGenProgram, "pixel_point");

        positionAttr_PrefixSum = GLES20.glGetAttribLocation(prefixSumProgram, "vPosition");
        texCoordAttr_PrefixSum = GLES20.glGetAttribLocation(prefixSumProgram, "inputTextureCoordinate");
        histogramTextureUniform_PrefixSum = GLES20.glGetUniformLocation(prefixSumProgram, "u_Texture2");
        niUniform_PrefixSum = GLES20.glGetUniformLocation(prefixSumProgram,"Ni");

        positionAttr_HistRe = GLES20.glGetAttribLocation(histRemapProgram, "vPosition");
        texCoordAttr_HistRe = GLES20.glGetAttribLocation(histRemapProgram, "inputTextureCoordinate");
        tex0Uniform_HistRe = GLES20.glGetUniformLocation(histRemapProgram, "u_Texture0");
        tex1Uniform_HistRe = GLES20.glGetUniformLocation(histRemapProgram, "u_Texture1");
        tex2Uniform_HistRe = GLES20.glGetUniformLocation(histRemapProgram, "u_Texture2");
        prefixSumBuffer = ByteBuffer.allocateDirect(256);
    }

    @Override
    public void draw(ByteBuffer frameData, Camera.Size size, int pixelAmounts) {
//        long startTime, time1, time2, time3;
//        startTime=System.nanoTime();// 获取开始时间
        GenHist(frameData,pixelAmounts);
//        time1=System.nanoTime();// 获取结束时间
//        Log.i("draw1：", (time1 - startTime) + "ns");
        HistPrefixSum();
//        time2=System.nanoTime();// 获取结束时间
//        Log.i("draw2：", (time2 - time1) + "ns");
        HistRemap(frameData,size,pixelAmounts);
//        time3=System.nanoTime();// 获取结束时间
//        Log.i("draw3：", (time3 - time2) + "ns");
    }
    private void GenHist(ByteBuffer frameData, int pixelAmounts) {
        Arrays.fill(histogramBin, 0);
        int i;
        for(i = 0 ; i<pixelAmounts;i+=downsamplingFactor)
        {
            histogramBin[frameData.get(i)&0xff]++;
        }
/*        GLES20.glViewport(0, 0, 256, 1);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(histGenProgram);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 256, 1, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, tex[0], 0);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
        frameData.position(0);
        GLES20.glVertexAttribPointer(pointAttr_HistGen, 1, GLES20.GL_UNSIGNED_BYTE, false, downsamplingFactor - 1, frameData);
        GLES20.glEnableVertexAttribArray(pointAttr_HistGen);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, pixelAmounts / downsamplingFactor);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisableVertexAttribArray(pointAttr_HistGen);*/
    }
    private void HistPrefixSum() {
        int i;
        for(i=1; i<256;i++)
        {
            histogramBin[i] += histogramBin[i-1];
        }
        prefixSumBuffer.position(0);
        for(i=0; i<256;i++)
        {
            prefixSumBuffer.put((byte)(histogramBin[i]*255/histogramBin[255]));
        }
/*        GLES20.glUseProgram(prefixSumProgram);
        GLES20.glVertexAttribPointer(positionAttr_PrefixSum, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(positionAttr_PrefixSum);
        GLES20.glVertexAttribPointer(texCoordAttr_PrefixSum, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, textureCoordinateBuffer);
        GLES20.glEnableVertexAttribArray(texCoordAttr_PrefixSum);
        int i;
        int n = 1;
        for(i=0;i<4;i++) {
            GLES20.glViewport(0,0,256,1);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[1]);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[1]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 256, 1, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, tex[1], 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
            GLES20.glUniform1i(histogramTextureUniform_PrefixSum,0);
            GLES20.glUniform1i(niUniform_PrefixSum, n);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
            n <<= 1;
            GLES20.glViewport(0,0,256,1);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 256, 1, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, tex[0], 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[1]);
            GLES20.glUniform1i(histogramTextureUniform_PrefixSum, 0);
            GLES20.glUniform1i(niUniform_PrefixSum,n);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
            n <<= 1;
        }
        GLES20.glDisableVertexAttribArray(positionAttr_PrefixSum);
        GLES20.glDisableVertexAttribArray(texCoordAttr_PrefixSum);*/
    }
    private void HistRemap(ByteBuffer frameData, Camera.Size size, int pixelAmounts) {
        GLES20.glUseProgram(histRemapProgram);
        GLES20.glViewport(viewOrigin_X, viewOrigin_Y, viewWidth, viewHeight);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[2]);
        frameData.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, size.width, size.height, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, frameData);
        GLES20.glUniform1i(tex0Uniform_HistRe, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[3]);
        frameData.position(pixelAmounts);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, size.width / 2, size.height / 2, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, frameData);
        GLES20.glUniform1i(tex1Uniform_HistRe, 1);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
        prefixSumBuffer.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,256,1,0,GLES20.GL_LUMINANCE,GLES20.GL_UNSIGNED_BYTE,prefixSumBuffer);
        GLES20.glUniform1i(tex2Uniform_HistRe, 2);

        GLES20.glVertexAttribPointer(positionAttr_HistRe, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(positionAttr_HistRe);
        GLES20.glVertexAttribPointer(texCoordAttr_HistRe, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, textureCoordinateBuffer);
        GLES20.glEnableVertexAttribArray(texCoordAttr_HistRe);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
        GLES20.glDisableVertexAttribArray(positionAttr_HistRe);
        GLES20.glDisableVertexAttribArray(texCoordAttr_HistRe);
    }

}
