package camera;

public class SnapShot
{
//private final static String SNAPSHOT_COMMAND = "raspistill -rot 180 --width 200 --height 150 --timeout 1 --output snap" + i + ".jpg --nopreview";
//private final static String SNAPSHOT_COMMAND = "fswebcam snap" + i + ".jpg";
  
  public static void main(String[] args) throws Exception
  {
    Runtime rt = Runtime.getRuntime();
    for (int i=0; i<10; i++)
    {
      long before = System.currentTimeMillis();
      Process snap = rt.exec("fswebcam snap" + i + ".jpg");
      snap.waitFor();
      long after = System.currentTimeMillis();
      System.out.println("Snapshot #" + i + " done in " + Long.toString(after - before) + " ms.");
      // Detect brightest spot here
      // TODO Analyze image here
    }
  }
}
