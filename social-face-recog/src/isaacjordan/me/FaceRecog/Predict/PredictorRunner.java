package isaacjordan.me.FaceRecog.Predict;

public class PredictorRunner {
  public static void main(String[] args) {
    Thread t = new Thread(new Recogniser());
    t.start();
    
  }
}