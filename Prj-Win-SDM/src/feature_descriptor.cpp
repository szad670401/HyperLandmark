#include "feature_descriptor.h"


cv::Mat CalculateHogDescriptor(cv::Mat &src, cv::Mat shape, std::vector<int> LandmarkIndexs, std::vector<int> eyes_index, HoGParam mHoGParam){
	assert(shape.rows == 1 && eyes_index.size() == 4);

	cv::Mat grayImage;
	if (src.channels() == 1){
		grayImage = src;
	}
	else if (src.channels() == 3){
		cv::cvtColor(src, grayImage, CV_BGR2GRAY);
	}
	else if (src.channels() == 4){
		cv::cvtColor(src, grayImage, CV_RGBA2GRAY);
	}
	else{
		return cv::Mat();
	}

	// This is in pixels in the original image:
	int numLandmarks = shape.cols / 2;
	float lx = (shape.at<float>(eyes_index.at(0)) + shape.at<float>(eyes_index.at(1)))*0.5;
	float ly = (shape.at<float>(eyes_index.at(0) + numLandmarks) + shape.at<float>(eyes_index.at(1) + numLandmarks))*0.5;
	float rx = (shape.at<float>(eyes_index.at(2)) + shape.at<float>(eyes_index.at(3)))*0.5;
	float ry = (shape.at<float>(eyes_index.at(2) + numLandmarks) + shape.at<float>(eyes_index.at(3) + numLandmarks))*0.5;
	float distance = sqrt((rx - lx)*(rx - lx) + (ry - ly)*(ry - ly));
	int patch_width_half = std::round(mHoGParam.relative_patch_size * distance);

	cv::Mat hogDescriptors; // We'll get the dimensions later from vl_hog_get_*
	for (int i = 0; i < LandmarkIndexs.size(); i++) {
		int x = cvRound(shape.at<float>(LandmarkIndexs.at(i)));
		int y = cvRound(shape.at<float>(LandmarkIndexs.at(i) + numLandmarks));

		cv::Mat roiImg;
		if (x - patch_width_half < 0 || y - patch_width_half < 0 || x + patch_width_half >= grayImage.cols || y + patch_width_half >= grayImage.rows) {
			// The feature extraction location is too far near a border. We extend the
			// image (add a black canvas) and then extract from this larger image.
			int borderLeft = (x - patch_width_half) < 0 ? std::abs(x - patch_width_half) : 0; // x and y are patch-centers
			int borderTop = (y - patch_width_half) < 0 ? std::abs(y - patch_width_half) : 0;
			int borderRight = (x + patch_width_half) >= grayImage.cols ? std::abs(grayImage.cols - (x + patch_width_half)) : 0;
			int borderBottom = (y + patch_width_half) >= grayImage.rows ? std::abs(grayImage.rows - (y + patch_width_half)) : 0;
			cv::Mat extendedImage = grayImage.clone();
			cv::copyMakeBorder(extendedImage, extendedImage, borderTop, borderBottom, borderLeft, borderRight, cv::BORDER_CONSTANT, cv::Scalar(0));
			cv::Rect roi((x - patch_width_half) + borderLeft, (y - patch_width_half) + borderTop, patch_width_half * 2, patch_width_half * 2); // Rect: x y w h. x and y are top-left corner.
			roiImg = extendedImage(roi).clone(); // clone because we need a continuous memory block
		}
		else {
			cv::Rect roi(x - patch_width_half, y - patch_width_half, patch_width_half * 2, patch_width_half * 2); // x y w h. Rect: x and y are top-left corner. Our x and y are center. Convert.
			roiImg = grayImage(roi).clone(); // clone because we need a continuous memory block
		}
		// This has to be the same for each image, so each image's HOG descriptor will have the same dimensions, independent of the image's resolution
		int fixed_roi_size = mHoGParam.num_cells * mHoGParam.cell_size;
		cv::resize(roiImg, roiImg, cv::Size(fixed_roi_size, fixed_roi_size));

		roiImg.convertTo(roiImg, CV_32FC1); // vl_hog_put_image expects a float* (values 0.0f-255.0f)
		VlHog* hog = vl_hog_new(VlHogVariant::VlHogVariantUoctti, mHoGParam.num_bins, false); // transposed (=col-major) = false
		vl_hog_put_image(hog, (float*)roiImg.data, roiImg.cols, roiImg.rows, 1, mHoGParam.cell_size); // (the '1' is numChannels)
		int ww = static_cast<int>(vl_hog_get_width(hog)); // assert ww == hh == numCells
		int hh = static_cast<int>(vl_hog_get_height(hog));
		int dd = static_cast<int>(vl_hog_get_dimension(hog)); // assert ww=hogDim1, hh=hogDim2, dd=hogDim3
		cv::Mat hogArray(1, ww*hh*dd, CV_32FC1); // safer & same result. Don't use C-style memory management.
		vl_hog_extract(hog, hogArray.ptr<float>(0));
		vl_hog_delete(hog);
		cv::Mat hogDescriptor(hh*ww*dd, 1, CV_32FC1);
		// Stack the third dimensions of the HOG descriptor of this patch one after each other in a column-vector:
		for (int j = 0; j < dd; ++j) {
			cv::Mat hogFeatures(hh, ww, CV_32FC1, hogArray.ptr<float>(0) + j*ww*hh); // Creates the same array as in Matlab. I might have to check this again if hh!=ww (non-square)
			hogFeatures = hogFeatures.t(); // necessary because the Matlab reshape() takes column-wise from the matrix while the OpenCV reshape() takes row-wise.
			hogFeatures = hogFeatures.reshape(0, hh*ww); // make it to a column-vector
			cv::Mat currentDimSubMat = hogDescriptor.rowRange(j*ww*hh, j*ww*hh + ww*hh);
			hogFeatures.copyTo(currentDimSubMat);
		}
		hogDescriptor = hogDescriptor.t(); // now a row-vector
		hogDescriptors.push_back(hogDescriptor);
	}
	// concatenate all the descriptors for this sample vertically (into a row-vector):
	hogDescriptors = hogDescriptors.reshape(0, hogDescriptors.cols * hogDescriptors.rows).t();

	// add a bias row (affine part)
	cv::Mat bias = cv::Mat::ones(1, 1, CV_32FC1);
	cv::hconcat(hogDescriptors, bias, hogDescriptors);
	return hogDescriptors;
}