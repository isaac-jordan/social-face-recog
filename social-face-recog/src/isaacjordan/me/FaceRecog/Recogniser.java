package isaacjordan.me.FaceRecog;

public class Recogniser {
  public static void main(String[] args) {
    Thread t = new Thread(new GrabberShow());
    t.start();
    
  }
}