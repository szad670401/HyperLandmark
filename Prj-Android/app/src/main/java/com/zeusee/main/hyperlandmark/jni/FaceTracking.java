package com.zeusee.main.hyperlandmark.jni;

import com.zeusee.main.hyperlandmark.CameraOverlap;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;


public class FaceTracking {

    static {
        System.loadLibrary("zeuseesTracking-lib");
    }

    private static class FaceTrackingHolder {
        private static final FaceTracking instance = new FaceTracking();
    }


    public static FaceTracking getInstance() {
        return FaceTrackingHolder.instance;
    }


    public native static void update(byte[] data, int height, int width, int angle, boolean mirror, long session);

    //scale  跟踪时缩小，对速度有要求可以考虑,1 为 原图， 2 为 缩小一般，以此类推
    public native static void initTracker(int height, int width, int scale, long session);

    public native static long createSession(String modelPath);

    public native static void releaseSession(long session);

    public native static int getTrackingNum(long session);

    public native static int[] getTrackingLandmarkByIndex(int index, long session);

    public native static int[] getTrackingLocationByIndex(int index, long session);

    public native static int[] getAttributeByIndex(int index, long session);

    public native static float[] getEulerAngleByIndex(int index, long session);

    public native static int getTrackingIDByIndex(int index, long session);

    private long session;
    private List<Face> faces;
    private int tracking_seq = 0;

    public FaceTracking() {
    }


//    public FaceTracking(String pathModel) {
//        session = createSession(pathModel);
//        faces = new ArrayList<Face>();
//    }

    public void release() {
        releaseSession(session);
    }


    public void FaceTrackingInit(String pathModel, int height, int width) {
        session = createSession(pathModel);
        faces = new ArrayList<Face>();
        initTracker(height, width, CameraOverlap.SCALLE_FACTOR, session);
    }

    public boolean postProcess(int[] landmark_prev, int[] landmark_curr) {
        int diff = 0;

        for (int i = 0; i < 106 * 2; i++) {
            diff += abs(landmark_curr[i] - landmark_prev[i]);

        }

        if (diff < 1.0 * 106 * 2) {
            for (int j = 0; j < 106 * 2; j++) {
                landmark_curr[j] = (landmark_curr[j] + landmark_prev[j]) / 2;
            }
            return true;
        } else if (diff < 2 * 106 * 2) {
            for (int j = 0; j < 106 * 2; j++) {
                landmark_curr[j] = (landmark_curr[j] + landmark_prev[j]) / 2;
            }
            return true;
        }
        return false;
    }

    public int find_id_face(List<Face> faces, int targetID) {
        for (int i = 0; i < faces.size(); i++) {
            if (faces.get(i).ID == targetID)
                return i;
        }
        return -1;
    }

    public void postProcess_aux(int[] landmark_prev, int[] landmark_curr) {

        for (int i = 0; i < 106 * 2; i++) {
            landmark_curr[i] = (landmark_curr[i]);

        }
    }


    public void Update(byte[] data, int height, int width) {
        update(data, height, width, 270, true, session);
        int numsFace = getTrackingNum(session);
        List<Face> _faces = new ArrayList<Face>();
        for (int i = 0; i < numsFace; i++) {
            int ID_GET = -1;
            int flag = -1;

            int[] faceRect = getTrackingLocationByIndex(i, session);
            int id = getTrackingIDByIndex(i, session);
//            Log.e("TAG","====id====="+id);
            int[] landmarks = getTrackingLandmarkByIndex(i, session);
            float[] attitudes = getEulerAngleByIndex(i, session);
            if (tracking_seq > 0) {
                ID_GET = find_id_face(faces, id);
                if (ID_GET != -1) {
                    boolean res = postProcess(faces.get(ID_GET).landmarks, landmarks);
                    if (res)
                        flag = -2;
                }
                if (ID_GET != -1) {
                    if (faces.get(ID_GET).isStable) {
                        postProcess_aux(faces.get(ID_GET).landmarks, landmarks);
                    }
                }
            }
            Face face = new Face(faceRect[0], faceRect[1], faceRect[2], faceRect[3], landmarks, id);
            face.pitch = attitudes[0];
            face.yaw = attitudes[1];
            face.roll = attitudes[2];
            if (flag == -2)
                face.isStable = true;
            else
                face.isStable = false;
            _faces.add(face);
//            faces.(i,face);
        }
        faces.clear();
        faces = _faces;
        tracking_seq += 1;

    }


    public List<Face> getTrackingInfo() {
        return faces;

    }
}
