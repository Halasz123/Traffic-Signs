package com.trafficsigns.ui.network.classifiers;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import com.trafficsigns.ui.constant.Network;

import org.tensorflow.lite.Interpreter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * @author Halász Botond
 * @since 10/05/2021
 *
 * Classifies images with Tensorflow Lite.
 * */
public class ImageClassifier {

  private static final String TAG = "TfLiteCameraDemo";
  private static final String MODEL_PATH = "KagleModel100EPIL.tflite";
  private static final String LABEL_PATH = Network.CLASSIFICATION_LABELS_FILE_NAME;
  private static final int RESULTS_TO_SHOW = 1;
  private static final int DIM_BATCH_SIZE = 1;
  private static final int DIM_PIXEL_SIZE = 3;
  public static final int DIM_IMG_SIZE_X = 30;
  public static final int DIM_IMG_SIZE_Y = 30;
  private static final int IMAGE_MEAN = 0;
  private static final float IMAGE_STD = 255.0f;


  private final int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

  private Interpreter tfliteInterpreter;
  private final List<String> labelList;
  private final ByteBuffer imgDataByteBuffer;
  private final float[][] labelProbArray ;
  /** multi-stage low pass filter **/
  private float[][] filterLabelProbArray = null;
  private static final int FILTER_STAGES = 3;
  private static final float FILTER_FACTOR = 0.4f;

  private final PriorityQueue<Map.Entry<String, Float>> sortedLabels =
          new PriorityQueue<>(RESULTS_TO_SHOW, (o1, o2) -> (o1.getValue()).compareTo(o2.getValue()));

  /** Initializes an {@code ImageClassifier}. */
  public ImageClassifier(Activity activity) throws IOException {
    tfliteInterpreter = new Interpreter(loadModelFile(activity));
    labelList = loadLabelList(activity);
    imgDataByteBuffer = ByteBuffer.allocateDirect( 4 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
    imgDataByteBuffer.order(ByteOrder.nativeOrder());
    labelProbArray = new float[1][labelList.size()];
    filterLabelProbArray = new float[FILTER_STAGES][labelList.size()];
    Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");
  }

  public String classifyFrame(Bitmap bitmap) {
    if (tfliteInterpreter == null) {
      Log.e(TAG, "Image classifier has not been initialized; Skipped.");
      return "Uninitialized Classifier.";
    }
    convertBitmapToByteBuffer(bitmap);
    long startTime = SystemClock.uptimeMillis();
    tfliteInterpreter.run(imgDataByteBuffer, labelProbArray);
    long endTime = SystemClock.uptimeMillis();
    Log.d(TAG, "Time: " + Long.toString(endTime - startTime));

    applyFilter();
    return printTopKLabels();
  }

  void applyFilter(){
    int num_labels =  labelList.size();

    for(int j=0; j<num_labels; ++j){
      filterLabelProbArray[0][j] += FILTER_FACTOR*(labelProbArray[0][j] -
                                                   filterLabelProbArray[0][j]);
    }
    for (int i=1; i<FILTER_STAGES; ++i){
      for(int j=0; j<num_labels; ++j){
        filterLabelProbArray[i][j] += FILTER_FACTOR*(
                filterLabelProbArray[i-1][j] -
                filterLabelProbArray[i][j]);
      }
    }
    for(int j=0; j<num_labels; ++j){
      labelProbArray[0][j] = filterLabelProbArray[FILTER_STAGES-1][j];
    }
  }

  /** Closes tflite to release resources. */
  public void close() {
    tfliteInterpreter.close();
    tfliteInterpreter = null;
  }

  /** Reads label list from Assets. */
  private List<String> loadLabelList(Activity activity) throws IOException {
    List<String> labelList = new ArrayList<String>();
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(activity.getAssets().open(LABEL_PATH)));
    String line;
    while ((line = reader.readLine()) != null) {
      labelList.add(line);
    }
    reader.close();
    return labelList;
  }

  /** Memory-map the model file in Assets. */
  private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
    AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
    FileChannel fileChannel = inputStream.getChannel();
    long startOffset = fileDescriptor.getStartOffset();
    long declaredLength = fileDescriptor.getDeclaredLength();
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
  }

  /** Writes Image data into a {@code ByteBuffer}. */
  private void convertBitmapToByteBuffer(Bitmap bitmap) {
    if (imgDataByteBuffer == null || bitmap == null) {
      return;
    }
    imgDataByteBuffer.rewind();
    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    int pixel = 0;
    long startTime = SystemClock.uptimeMillis();
    for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
      for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
        final int val = intValues[pixel++];
        imgDataByteBuffer.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
        imgDataByteBuffer.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
        imgDataByteBuffer.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
      }
    }
    long endTime = SystemClock.uptimeMillis();
    Log.d(TAG, "Timecost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
  }

  /** Prints top-K labels, to be shown in UI as the results. */
  private String printTopKLabels() {
    for (int i = 0; i < labelList.size(); ++i) {
      sortedLabels.add(
          new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i]));
      if (sortedLabels.size() > RESULTS_TO_SHOW) {
        sortedLabels.poll();
      }
    }
    String textToShow = "";
    final int size = sortedLabels.size();
    for (int i = 0; i < size; ++i) {
      Map.Entry<String, Float> label = sortedLabels.poll();
      textToShow = String.format(Locale.ROOT,"%s|%4.2f",label.getKey(),label.getValue());
    }
    return textToShow;
  }

}
