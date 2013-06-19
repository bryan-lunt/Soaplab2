package edu.rice.dcatest.pbsrunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.soaplab.services.JobState;

public class PBSUtils {

	private static Pattern job_status_pattern;
	private static Map<String,JobState> pbs_s_to_s_s;
	
	static{
		job_status_pattern = Pattern.compile("job_state = (.*?)\n");
		
		pbs_s_to_s_s = new HashMap<String,JobState>();
		pbs_s_to_s_s.put('Q', JobState.CREATED);
		pbs_s_to_s_s.put('R', JobState.RUNNING);
		pbs_s_to_s_s.put('C', JobState.COMPLETED);
		pbs_s_to_s_s.put('E', JobState.TERMINATED_BY_ERROR);
		pbs_s_to_s_s.put('U', JobState.UNKNOWN);
	}
	
	public static JobState get_job_status(String jobid){
		String ret = "U";
		String info = runQstat(jobid);
		Matcher myMatcher = job_status_pattern.matcher(info);
		if(myMatcher.find()){
			ret = myMatcher.group(1);
		}
		
		if(pbs_s_to_s_s.containsKey(ret))
			return pbs_s_to_s_s.get(ret);
		else
			return JobState.UNKNOWN;
	}
	
	public static String runQstat(String jobid){
		try{
		ProcessBuilder pb = new ProcessBuilder();
		pb.command().add("qstat");
		pb.command().add("-f");
		if(jobid != null){
			pb.command().add(jobid);
		}
		Process qstat = pb.start();
		
		int qstatExitCode = -324;
		while (true) {
		    try {
		    	qstatExitCode = qstat.waitFor();//waiting for qsub to finish
		    	break;
		    } catch (InterruptedException e) {/*Don't care*/}
		}
		
		String qstatret = readAll(qstat.getInputStream());
		return qstatret;
		}catch(Exception e){}
		return "";
	}
	
	
	public static String readAll(InputStream s){
		String retstr = "";
		try{
			StringBuilder outBuilder = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(s));
			String tmp = br.readLine();
			while(tmp != null){
				outBuilder.append(tmp.trim());
				tmp = br.readLine();
			}
			retstr = outBuilder.toString();
		}catch(Exception e){e.printStackTrace();}
		return retstr;
	}
	
	public static ProcessBuilder createQsubProccessBuilder(ProcessBuilder pb, File jobDir,File stdout2Report, File stderr2Report) throws java.io.IOException {
		 /*Copy the ProcessBuilder*/
	    ProcessBuilder qsubpb = new ProcessBuilder();
	   //Don't copy the command! Just the environment and directory and stuff!
	    qsubpb.environment().putAll(pb.environment());
	    qsubpb.directory(pb.directory());
	    qsubpb.redirectErrorStream(pb.redirectErrorStream());
	    
	    //Setup the qsub command
	    qsubpb.command().clear();
	    qsubpb.command().add("/usr/bin/qsub");
	    
	    qsubpb.command().add("-d" + jobDir.getAbsolutePath());
	    
	    qsubpb.command().add("-o" + stdout2Report.getAbsolutePath());
	    
	    qsubpb.command().add("-e" + stderr2Report.getAbsolutePath());
	    
	    qsubpb.command().add("-V");//Because we used a copied process builder, it now gets all the environment variables it would have gotten anyway.
	    
	    qsubpb.command().add("__thescript");//Script file to qsub...
	    
	    
	    //create the script
	    File script_file = new File(jobDir,"__thescript");
	    PrintWriter fw = new PrintWriter(new BufferedWriter(new FileWriter(script_file.getAbsoluteFile())));

	    List<String> theCommand = pb.command();
	    
	    for(int i = 0;i<theCommand.size();i++){
	    	fw.print(theCommand.get(i));
	    	fw.print(" ");
	    }
	    fw.println("");
	    fw.flush();
	    fw.close();
	    //done creating script
	    
	    return qsubpb;
	}
}
