# Mobile CV练习

## 参考资料

[1] Mobile vision youtube video: https://www.youtube.com/playlist?list=PL6v5F68v1ZZzTDq8VI9Jcmb0J99WRrYn4

[2] jni+CMake+Android Studio (in Chinese): https://juejin.im/post/5bb025db5188255c38537198

[3] opencv+jni+Android Studio: https://sriraghu.com/2017/03/11/opencv-in-android-an-introduction-part-1/

## Demo

一些配置及路径可能需要更改。另外，为了防止源码过大，jniLibs下库文件都删了。

### Demo1

使用相机及CANNY变换，参考资料[1]

<img src="https://github.com/gjgjh/OpenCVTest/blob/master/support_files/demo1.gif" width = 40% height = 40% />

### Demo2

调用JNI函数，参考资料[2]

### Demo3

JNI+第三方JSON库，参考资料[2]

### Demo4

JNI+OpenCV，参考资料[1], [3]

### Demo5

OPENCV文档纠正，个人练习project。目前只能手动画多边形，未来可以做一个shape detection。画多边形的顺序为左上、右上、右下、左下。目前代码中固定了纠正后比例（宽高比2:3），未来可以改进。

<img src="https://github.com/gjgjh/OpenCVTest/blob/master/support_files/demo5.gif" width = 40% height = 40% />
