#include "helper.h"


cv::Mat align_mean(cv::Mat mean, cv::Rect facebox, float scaling_x, float scaling_y, float translation_x, float translation_y)
{
	using cv::Mat;
	// Initial estimate x_0: Center the mean face at the [-0.5, 0.5] x [-0.5, 0.5] square (assuming the face-box is that square)
	// More precise: Take the mean as it is (assume it is in a space [-0.5, 0.5] x [-0.5, 0.5]), and just place it in the face-box as
	// if the box is [-0.5, 0.5] x [-0.5, 0.5]. (i.e. the mean coordinates get upscaled)
	Mat aligned_mean = mean.clone();
	Mat aligned_mean_x = aligned_mean.colRange(0, aligned_mean.cols / 2);
	Mat aligned_mean_y = aligned_mean.colRange(aligned_mean.cols / 2, aligned_mean.cols);
	aligned_mean_x = (aligned_mean_x*scaling_x + 0.5f + translation_x) * facebox.width + facebox.x;
	aligned_mean_y = (aligned_mean_y*scaling_y + 0.3f + translation_y) * facebox.height + facebox.y;
	return aligned_mean;
}


