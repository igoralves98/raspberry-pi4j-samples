package nmea;

import calculation.AstroComputer;
import calculation.SightReductionUtil;

import java.text.DecimalFormat;

import java.util.Calendar;
import java.util.TimeZone;

import ocss.nmea.api.NMEAClient;
import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.RMC;
import ocss.nmea.parser.StringParsers;

public class CustomNMEAReader extends NMEAClient
{
  private final static DecimalFormat DFH = new DecimalFormat("#0.00'\272'");
  private final static DecimalFormat DFZ = new DecimalFormat("#0.00'\272'");
  
  private static GeoPos prevPosition = null;
  private static long   prevDateTime = -1L;

  public CustomNMEAReader()
  {
    super();
  }
  
  public void dataDetectedEvent(NMEAEvent e)
  {
//  System.out.println("Received:" + e.getContent());
    manageData(e.getContent());
  }

  private static CustomNMEAReader customClient = null;  
  
  private static void manageData(String sentence)
  {
    boolean valid = StringParsers.validCheckSum(sentence);
    if (valid)
    {
      String id = sentence.substring(3, 6);
      if ("RMC".equals(id))
      {
     // System.out.println(line);
        RMC rmc = StringParsers.parseRMC(sentence);
     // System.out.println(rmc.toString());
        if (rmc.getRmcDate() != null && rmc.getGp() != null)
        {
          if ((prevDateTime == -1L || prevPosition == null) ||
              (prevDateTime != (rmc.getRmcDate().getTime() / 1000) || !rmc.getGp().equals(prevPosition)))
          {
            Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
            current.setTime(rmc.getRmcDate());
            AstroComputer.setDateTime(current.get(Calendar.YEAR), 
                                      current.get(Calendar.MONTH) + 1, 
                                      current.get(Calendar.DAY_OF_MONTH), 
                                      current.get(Calendar.HOUR_OF_DAY), // 12 - (int)Math.round(AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(ts.getTimeZone()))), 
                                      current.get(Calendar.MINUTE), 
                                      current.get(Calendar.SECOND));
            AstroComputer.calculate();
            SightReductionUtil sru = new SightReductionUtil(AstroComputer.getSunGHA(),
                                                            AstroComputer.getSunDecl(),
                                                            rmc.getGp().lat,
                                                            rmc.getGp().lng);
            sru.calculate();
            Double he = sru.getHe();
            Double  z = sru.getZ();
            System.out.println(current.getTime().toString() + ", He:" + DFH.format(he)+ ", Z:" + DFZ.format(z) + " (" + rmc.getGp().toString() + ")");
          }
          prevPosition = rmc.getGp();
          prevDateTime = (rmc.getRmcDate().getTime() / 1000);
        }
        else
        {
          if (rmc.getRmcDate() == null)
            System.out.println("... no Date in [" + sentence + "]");
          if (rmc.getGp() == null)
            System.out.println("... no Pos in [" + sentence + "]");
        }
      }
    }    
    else
      System.out.println("Invalid data [" + sentence + "]");
  }

  public static void main(String[] args)
  {
    System.setProperty("deltaT", "67.2810"); // 2014-Jan-01

    int br = 4800;
    System.out.println("CustomNMEAReader invoked with " + args.length + " Parameter(s).");
    for (String s : args)
    {
      System.out.println("CustomNMEAReader prm:" + s);
      try { br = Integer.parseInt(s); } catch (NumberFormatException nfe) {}
    }
    
    customClient = new CustomNMEAReader();
      
    Runtime.getRuntime().addShutdownHook(new Thread() 
      {
        public void run() 
        {
          System.out.println ("Shutting down nicely.");
          customClient.stopDataRead();
        }
      });    
    customClient.initClient();
//  customClient.setReader(new CustomReader(customClient.getListeners()));
    customClient.setReader(new CustomNMEASerialReader(customClient.getListeners(), br));
    customClient.startWorking();
  }

  private void stopDataRead()
  {
    if (customClient != null)
    {
      for (NMEAListener l : customClient.getListeners())
        l.stopReading(new NMEAEvent(this));
    }
  }
}