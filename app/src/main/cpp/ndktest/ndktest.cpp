//
// Created by gjh on 2019-06-10.
//

#include "ndktest.h"

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_opencvtest_NdkActivity_convertGray(JNIEnv *env, jclass type,
                                                          jlong matAddrImage, jlong matAddrGray) {

    Mat& image=*(Mat*)matAddrImage;
    Mat& gray=*(Mat*)matAddrGray;

    int conv;
    conv=toGray(image,gray);
    jint res=(jint)conv;
    return res;
}

int toGray(Mat image,Mat& gray){
    cvtColor(image,gray,CV_RGBA2GRAY);
    if(gray.rows==image.rows&&gray.cols==gray.cols)
        return 1;

    return 0;
}
