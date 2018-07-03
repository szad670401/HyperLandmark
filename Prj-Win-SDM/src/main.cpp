#include <vector>
#include <iostream>
#include <fstream>
#include "opencv2/opencv.hpp"
#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/objdetect/objdetect.hpp"

#include "ldmarkmodel.h"

using namespace std;
using namespace cv;


int main()
{

	ldmarkmodel modelt("model/haar_facedetection.xml");
    std::string modelFilePath = "model/landmark-model.bin";

    if(!load_ldmarkmodel(modelFilePath, modelt)){
        std::cout << "Modle Opening Failed." << std::endl;
        std::cin >> modelFilePath;
    }

    cv::VideoCapture mCamera(0);
    if(!mCamera.isOpened()){
        std::cout << "Camera Opening Failed..." << std::endl;
        return 0;
    }
    cv::Mat Image;
	std::vector<cv::Mat> current_shape(MAX_FACE_NUM);

    while(1){
        mCamera >> Image;
        modelt.track(Image, current_shape);
        cv::Vec3d eav;
        modelt.EstimateHeadPose(current_shape[0], eav);
        modelt.drawPose(Image, current_shape[0], 50);

		for (int i = 0; i < MAX_FACE_NUM; i++){
			if (!current_shape[i].empty()){

				int numLandmarks = current_shape[i].cols / 2;
				for (int j = 0; j < numLandmarks; j++){
					int x = current_shape[i].at<float>(j);
					int y = current_shape[i].at<float>(j + numLandmarks);

					cv::circle(Image, cv::Point(x, y), 2, cv::Scalar(0, 0, 255), -1);

				}
			}
		}

        cv::imshow("Camera", Image);
        if(27 == cv::waitKey(5)){
            mCamera.release();
            cv::destroyAllWindows();
            break;
        }
    }

    return 0;
}






















