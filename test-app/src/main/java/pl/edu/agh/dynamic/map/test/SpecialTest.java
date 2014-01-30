package pl.edu.agh.dynamic.map.test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpecialTest extends AbstractTest {

	@Override
	protected String name() {
		return "SPECIAL";
	}

	@Override
	protected void test() throws SQLException {
		
		
		StringBuilder all = new StringBuilder();
		all.append("\n\n\n\n\n\n\n");
		int startId = 6056;
		List<Integer> steps =Arrays.asList( new Integer[]{1,5,10,20,50,100,150,200,300,500,800});
		int count = 0;
		StringBuilder sb = new StringBuilder(
				"SELECT DISTINCT ON (label) label, time_measured,value FROM param_monitoringparametervalue v JOIN param_monitoringparameterinstance i ON (v.instance=i.id) JOIN param_instanceassignment a ON (i.assignment=a.id) JOIN param_instanceassignment_sm_lane l ON (l.param_instanceassignment=a.id) WHERE sm_lane IN (");
		
		while (count<=800) {
			if(count > 0){
				sb.append(",");
			}
			sb.append(startId++);
			count++;
			if(steps.contains(count)){
				String query = sb.toString()+") ORDER BY label, time_measured DESC;";
				log.info("--------------------------------"+count+"-------------------------");
				log.info(query);
				PreparedStatement statement = rdnrDao.getConnection().prepareStatement(query);

				start();
				ResultSet rs = statement.executeQuery();
				long ms = stop();
				long mcount = 0l;
				while(rs.next()){
					mcount++;
				}
				all.append(count);
				all.append(";");
				all.append(ms);
				all.append(";");
				all.append(mcount);
				all.append(";\n");
			}
		}
		log.fatal(all.toString());

	}

}
