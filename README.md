# Open Source Face Landmark

### 介绍
良心级开源人脸标定算法,人脸美颜,美妆,配合式活体检测,人脸校准的预处理步骤.该项目Windows工程基于传统的SDM算法,通过修改开源代码,精简保留测试部分代码,优化代码结构.
Android代码基于深度学习,我们设计了高效的网络模型,该模型鲁棒性较好,支持多人脸跟踪.目前深度学习算法在人脸标定方向取得了良好的效果,该项目旨在提供一种较为简单易用的实现方式.


#### 相关资源 

+ [参考开源SDM算法](https://github.com/chengzhengxin/sdm)。
+ [基于CNN的人脸标定](https://github.com/lsy17096535/face-landmark)。


### 更新
+ 提交基于深度学习人脸标定android代码，速度快，可商用，arm 1.2GHz 单张人脸7ms (2018.08.14)
+ [基于深度学习106点人脸标定,代码即将提交](https://github.com/zeusees/HyperLandmark/blob/master/resource/106points.mp4)
+ 增加多人脸跟踪

### 识别APP

- 体验 Android APP：[https://fir.im/HyperLandmark](https://fir.im/HyperLandmark)

### TODO

+ 完善移动端版本(Android版已经提交)
+ SIMD指令优化hog特征提取速度
+ 丰富数据,提交一版106点标定模型(已经提交)


###  依赖

+ Windows demo 依赖 OpenCV
+ Android 可直接使用

### 测试样例

![image](./images/res1.png)

