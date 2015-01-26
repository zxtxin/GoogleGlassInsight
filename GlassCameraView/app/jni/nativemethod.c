#include "cn_edu_fudan_ee_cameraview_NativeMethod.h"
JNIEXPORT jboolean JNICALL Java_cn_edu_fudan_ee_cameraview_NativeMethod_CountAndSum
  (JNIEnv * env, jclass thiz, jbyteArray raw, jbyteArray prefixsum,jint pixelamounts,jint downsamplingfactor)
  {
	  unsigned int histogram[256]={0};
	  unsigned int i;
	  jbyte * nativeRaw=(*env)->GetByteArrayElements(env,raw,JNI_FALSE);
	  for(i=0;i<pixelamounts;i+=downsamplingfactor)
	  {
		  histogram[(unsigned char)nativeRaw[i]]++;
	  }
	  for(i=1;i<256;i++)
		  histogram[i]+=histogram[i-1];
	  jbyte* nativePrefixSum=(*env)->GetByteArrayElements(env,prefixsum,JNI_FALSE);
	  for(i=0;i<256;i++)
		  nativePrefixSum[i]= (jbyte)(histogram[i]*240/histogram[255]);
	  (*env)->ReleaseByteArrayElements(env, raw, nativeRaw, 0);
	  (*env)->ReleaseByteArrayElements(env, prefixsum, nativePrefixSum, 0);
	  return 0;
  }