package com.huewu.example.logreader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import com.huewu.apps.logtoaster.R;

/**
 * <p>
 * @file			LogUtils.java
 * @version			1.0
 * @date 			Nov. 11, 2010
 * @author 			huewu.yang
 * <p>
 *
 * <p>
 * This program is subject to copyright protection in accordance with the
 * applicable law. It must not, except where allowed by law, by any means or
 * in any form be reproduced, distributed or lent. Moreover, no part of the
 * program may be used, viewed, printed, disassembled or otherwise interfered
 * with in any form, except where allowed by law, without the express written
 * consent of the copyright holder.
 * <p>
 * <br>
 * All Rights Reserved.   
 */

public class LogUtils {

	private final static int MAX_ENTRIES = 100;

	//Small cache for performance and avoiding frequent GC calls.
	static LinkedHashMap<String, String[]> mTokenCache = new LinkedHashMap<String, String[]>(MAX_ENTRIES + 1){
		private static final long serialVersionUID = 1L;
		protected boolean removeEldestEntry(java.util.Map.Entry<String,String[]> eldest) {
			return size() > MAX_ENTRIES;    
		};
	};
	
	static ArrayList<File> mTempFiles = new ArrayList<File>();

	/**
	 * no public constructor
	 */
	private LogUtils(){
	}

	/**
	 * parse log string.
	 * @param log string to parse.
	 * @return string tokens. 
	 * token[0] : priority (V/D/I/W/E)
	 * token[1] : tag & process id
	 * token[2] : log content.
	 * Should check the number of returned tokens. There may be some logs that doesn't have all three tokens.
	 */
	public static String[] parseTokens(String log){

		String[] result = null;
		result = mTokenCache.get(log);

		if(	result != null )
			return result;	//hit!!

		int index1 = log.indexOf("/");
		int index2 = log.indexOf(":");
		if(index1 < 0 || index2 < 0){
			result = new String[]{log};
			mTokenCache.put(log, result);
			return result;
		} else {
			result = new String[]{
					log.substring(0, index1),
					log.substring(index1+1, index2+1),
					log.substring(index2+1).trim()};
			mTokenCache.put(log, result);
			return result;
		}
	}

	public static int parseLogLevel(String log){
		String[] tokens = parseTokens(log);

		if(tokens.length < 1)
			return Log.VERBOSE;	//invalid format log string. something wrong. 

		switch(tokens[0].charAt(0)){
		case 'V':
			return Log.VERBOSE;
		case 'D':
			return Log.DEBUG;
		case 'I':
			return Log.INFO;
		case 'W':
			return Log.WARN;
		case 'E':
			return Log.ERROR;
		default:
			return Log.VERBOSE;
		}		
	}

	public static void appendLog(String log, ArrayAdapter<String> adapter){
		//if 'error' log which occurred on the same process in a row, such logs are merged as one for user convenience. 

		String[] newLogTokens = parseTokens(log);
		if(newLogTokens.length != 3){
			return;
		}else if(adapter.getCount() == 0){
			adapter.add(log);
		}else{
			String lastLog = adapter.getItem(adapter.getCount() - 1);
			String[] oldLogTokens = parseTokens(lastLog);
			if(newLogTokens[0].equals("E") && newLogTokens[0].equals(oldLogTokens[0]) && newLogTokens[1].equals(oldLogTokens[1])){
				adapter.remove(lastLog);
				adapter.add(lastLog + "\n" + newLogTokens[2]);
			}else{
				adapter.add(log);
			}
		}
	}

	/**
	 * create log file. 
	 * @param seed
	 * @return log file. or null if an external storage doesn't mounted.
	 */
	public static File createLogFile(String seed){
		File dir = Environment.getExternalStorageDirectory();
		if(dir.exists() == true){
			String fileName = seed + "_" + System.currentTimeMillis() + ".log";
			return new File(dir, fileName);
		}else{
			return null;
		}
	}
	
	/**
	 * create temp log file.  
	 * @return log file. or null if an external storage doesn't mounted.
	 */	
	public static File createTempLogFile(){
		File dir = Environment.getExternalStorageDirectory();
		int i = 1;
		if(dir.exists() == true){
			while(true){
				String fileName = "created_by_logtoaster#" + i + ".log";
				File f = new File(dir, fileName);				
				if(f.exists() == true){
					++i;
					continue;
				}
				mTempFiles.add(f);
				return f;
			}
		}else{
			return null;
		}
	}
	
	/**
	 * delete all temp log files.
	 */
	public static void deleteTempLogFiles(){
		for(File f : mTempFiles)
			f.delete();
	}
	
	/**
	 * save one log.(one string)
	 * @param file
	 * @param log
	 * @return
	 */
	public static boolean saveLogFile(File file, String log){
		if(file == null || log.length() == 0)
			return false;
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(log);
			fw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * save whole logs.
	 * @param file
	 * @param logs
	 * @return
	 */
	public static boolean saveLogFile(File file, ArrayAdapter<String> logs){
		if(file == null || logs == null)
			return false;

		try {
			FileWriter fw = new FileWriter(file);
			for(int i = 0; i < logs.getCount(); ++i){
				fw.write((String)logs.getItem(i));
				fw.write('\n');
			}
			fw.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}//end of class
