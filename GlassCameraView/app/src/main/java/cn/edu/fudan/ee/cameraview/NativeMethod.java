package cn.edu.fudan.ee.cameraview;

/**
 * Created by zxtxin on 2015/1/21.
 */
public class NativeMethod {
    public static native boolean CountAndSum(byte[] raw,byte[] prefixsum,int pixelAmounts,int downSamplingFactor);
}
