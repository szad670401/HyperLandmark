package zeusees.tracking;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.abs;


public class FaceTracking {

    static {
        System.loadLibrary("zeuseesTracking-lib");
    }

    public native static void update(byte[] data, int height, int width, long session);

    public native static void initTracking(byte[] data, int height, int width, long session);

    public native static long createSession(String modelPath);

    public native static void releaseSession(long session);

    public native static int getTrackingNum(long session);

    public native static int[] getTrackingLandmarkByIndex(int index, long session);

    public native static int[] getTrackingLocationByIndex(int index, long session);

    public native static int getTrackingIDByIndex(int index, long session);

    public native static float[] getEulerAngleByIndex(int index, long session);


    private long session;
    private List<Face> faces;
    private int tracking_seq = 0;


    public void FaceTrackingInit(String pathModel,byte[] data, int height, int width) {
        session = createSession(pathModel);
        faces = new ArrayList<Face>();
        initTracking(data, height, width, session);
    }

    private static class FaceTrackingHolder {
        private static final FaceTracking instance = new FaceTracking();
    }

    public static FaceTracking getInstance() {
        return FaceTrackingHolder.instance;
    }


    public void relesaseJni() {
        releaseSession(session);
    }

    public void Update(byte[] data, int height, int width, boolean stablize) {
        long t0 = System.currentTimeMillis();
        update(data, height, width, session);
        long diff = System.currentTimeMillis() - t0;
        Log.d("update _time", diff + "ms");
        int numsFace = getTrackingNum(session);
        List<Face> _faces = new ArrayList<Face>();
        for (int i = 0; i < numsFace; i++) {
            int ID_GET = -1;
            int flag = -1;
            int[] faceRect = getTrackingLocationByIndex(i, session);
            int id = getTrackingIDByIndex(i, session);
            int[] landmarks = getTrackingLandmarkByIndex(i, session);
            float[] attitudes = getEulerAngleByIndex(i, session);
            if (tracking_seq > 0 && stablize) {
                ID_GET = findIdFace(faces, id);
                if (ID_GET != -1) {
                    boolean res = postProcess(faces.get(ID_GET).landmarks, landmarks);
                    if (res)
                        flag = -2;
                }
            }
            Face face = new Face(faceRect[0], faceRect[1], faceRect[2], faceRect[3], landmarks, id);
            face.pitch = attitudes[0];
            face.yaw = attitudes[1];
            face.roll = attitudes[2];
            if (flag == -2)
                face.isStable = false;
            _faces.add(face);
        }
        faces.clear();
        faces = _faces;
        tracking_seq += 1;
    }

    public boolean postProcess(int[] landmark_prev, int[] landmark_curr) {
        int diff = 0;
        for (int i = 0; i < 106 * 2; i++) {
            diff += abs(landmark_curr[i] - landmark_prev[i]);
        }
        if (diff < 2.0 * 106 * 2) {
            for (int j = 0; j < 106 * 2; j++) {
                landmark_curr[j] = (landmark_curr[j] + landmark_prev[j]) / 2;
            }
            return true;
        }
        return false;
    }

    public int findIdFace(List<Face> faces, int targetID) {
        for (int i = 0; i < faces.size(); i++) {
            if (faces.get(i).ID == targetID)
                return i;
        }
        return -1;
    }


    public List<Face> getTrackingInfo() {
        return faces;

    }
}
