package pl.edu.agh.dynamic.map.log;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Khajiit
 * Date: 27.01.14
 * Time: 21:34
 * To change this template use File | Settings | File Templates.
 */
public class StatsCollector {

    private Map<String, StatsCollectorEntry> statsMap = new HashMap<String, StatsCollectorEntry>();

    public void startActivity(String activity) {
        if(statsMap.get(activity) == null) {
            StatsCollectorEntry statsEntry = new StatsCollectorEntry();
            statsMap.put(activity, statsEntry);
        }
        statsMap.get(activity).startDate = new Date();
    }

    public void finishActivity(String activity) {
        statsMap.get(activity).endDate = new Date();
    }

    public String getLogFor(String activity) {
        StatsCollectorEntry statsEntry = statsMap.get(activity);
        StringBuilder logStr = new StringBuilder();
        if(statsEntry != null) {
            logStr.append("Activity: ");
            logStr.append(activity);
            logStr.append(" finished. \n");
        	logStr.append("###########################################################");
            logStr.append("Total time: ");
            logStr.append(statsEntry.endDate.getTime() - statsEntry.startDate.getTime());
            logStr.append("ms");
        	logStr.append("###########################################################");
        }

        return logStr.toString();
    }

    private class StatsCollectorEntry {
        Date startDate = null;
        Date endDate = null;
    }
}
