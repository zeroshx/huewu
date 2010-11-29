package org.devtcg.tools.logcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.os.HandlerThread;

public abstract class LogcatProcessor extends HandlerThread
{
	/* TODO: Support logcat filtering. */
	private ArrayList<String> LOGCAT_CMDS = new ArrayList<String>();
	private static final int BUFFER_SIZE = 1024;

	private int mLines = 0;
	protected Process mLogcatProc = null;

	public LogcatProcessor(String name) {
		super(name);
		LOGCAT_CMDS.add("logcat");
		LOGCAT_CMDS.add("-v");
		LOGCAT_CMDS.add("brief");
	}

	public void addFilter(String filter){
		synchronized (LOGCAT_CMDS) {
			LOGCAT_CMDS.add(filter);
		}
	}
	
	public void clearFilters(){
		synchronized (LOGCAT_CMDS) {
			LOGCAT_CMDS.clear();
			LOGCAT_CMDS.add("logcat");
			LOGCAT_CMDS.add("-v");
			LOGCAT_CMDS.add("brief");
		}
	}
	
	public String getFilterString(){
		String filter = "";
		synchronized (LOGCAT_CMDS) {
			if(LOGCAT_CMDS.size() > 3){
				for(int i = 3; i <LOGCAT_CMDS.size(); ++i){
					filter += " " + LOGCAT_CMDS.get(i);
				}
			}
		}
		return filter;
	}

	public void run(){
		try	{
			synchronized (LOGCAT_CMDS) {
				String[] cmds = new String[LOGCAT_CMDS.size()];
				LOGCAT_CMDS.toArray(cmds);
				mLogcatProc = Runtime.getRuntime().exec(cmds);
			}
		}
		catch (IOException e){
			onError("Can't start " + LOGCAT_CMDS.get(0), e);
			return;
		}

		BufferedReader reader = null;

		try	{
			reader = new BufferedReader(
					new InputStreamReader(mLogcatProc.getInputStream()), BUFFER_SIZE);
			String line;
			while ((line = reader.readLine()) != null){
				onNewline(line);
				mLines++;
			}
		} catch (IOException e){
			e.printStackTrace();
			onError("Error reading from logcat process", e);
		} finally	{
			if (reader != null)
				try { reader.close(); } catch (IOException e) {}
				stopCatter();
		}
	}

	public void stopCatter(){
		if (mLogcatProc == null)
			return;

		mLogcatProc.destroy();
		mLogcatProc = null;
	}

	public int getLineCount(){
		return mLines;
	}

	public abstract void onError(String msg, Throwable e);
	public abstract void onNewline(String line);
}//end of class
