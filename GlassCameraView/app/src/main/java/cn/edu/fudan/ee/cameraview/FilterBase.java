package cn.edu.fudan.ee.cameraview;

import android.hardware.Camera;

/**
 * Created by zxtxin on 2014/12/3.
 */
public abstract class FilterBase {
    protected OpenGLESSupervisor glInstance;
    protected int viewOrigin_X;
    protected int viewOrigin_Y;
    protected int viewWidth;
    protected int viewHeight;
    public int COORDS_PER_VERTEX = 2;
    public FilterBase(OpenGLESSupervisor instance) {
        this.glInstance = instance;
    }
    public void setViewPort(int x_origin, int y_origin, int width, int height) {
        this.viewOrigin_X = x_origin;
        this.viewOrigin_Y = y_origin;
        this.viewWidth = width;
        this.viewHeight = height;
    }
    abstract public void draw(byte[] frameData, Camera.Size size, int pixelAmounts);

}
