#pragma once
#ifndef __HELPER_H_
#define __HELPER_H_

#include <iostream>
#include <iostream>
#include <fstream>

#include "opencv2/opencv.hpp"
#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/objdetect/objdetect.hpp"

#include "cereal/cereal.hpp"
#include "cereal/types/string.hpp"
#include "cereal/types/vector.hpp"
#include "cereal/archives/binary.hpp"
#include "cereal_extension/mat_cerealisation.hpp"


template<class T = int>
cv::Rect_<T> get_enclosing_bbox(cv::Mat landmarks)
{
	auto num_landmarks = landmarks.cols / 2;
	double min_x_val, max_x_val, min_y_val, max_y_val;
	cv::minMaxLoc(landmarks.colRange(0, num_landmarks), &min_x_val, &max_x_val);
	cv::minMaxLoc(landmarks.colRange(num_landmarks, landmarks.cols), &min_y_val, &max_y_val);
	double width = max_x_val - min_x_val;
	double height = max_y_val - min_y_val;
	return cv::Rect_<T>(min_x_val, min_y_val, width, height);
	//    return cv::Rect_<T>(min_x_val, min_y_val, width, height);
}

cv::Mat align_mean(cv::Mat mean, cv::Rect facebox, float scaling_x = 1.0f, float scaling_y = 1.0f, float translation_x = 0.0f, float translation_y = 0.0f);


#endif
