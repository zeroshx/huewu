package com.huewu.apps.tabtabword;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.util.Log;

public class WordLoader {
	
	private static final String TAG = "WordLoader";
	//load word list from resource.
	
	static HashMap<String, ArrayList<String> > mWordList = new HashMap<String,ArrayList<String> >(); 
	
	public static void loadWords(Context context){
		try {
			
			InputStream fis = context.getResources().openRawResource(R.raw.word_list1);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, "utf-8"));
			
			String line = null;
			String word = null;
			String value = null;
			do{
				line = br.readLine();
				if(line == null)
					break;
				
//				Log.i(TAG, line);
				
				if(line.matches("[1-9].+") == true){
					//value.
					String[] ms = line.split("[1-9].+");
//					line.matches("[1-9].+[1-9])
//					line.replaceAll("[1~9]\\.", "");
//					value = line.replaceAll("[1~9]\\.", "");
//					Log.v(TAG, "\t" + line);
					
					ArrayList<String> meanings = mWordList.get(word);
					if(meanings != null)
						meanings.add(line);
					
					
				}else if(line.matches("[a-z].+") == true){
					word = line;
					mWordList.put(word, new ArrayList<String>());
				}
			}while(line != null);
			
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void dumpWordList(){
		
		for(Entry<String, ArrayList<String>> entry : mWordList.entrySet()){
			
			Log.v(TAG, entry.getKey());
			for(String s : entry.getValue()){
				Log.v(TAG, "\t" + s);
			}
		}
	}
	
	static String[] getRandomWords(int size){

		Set<String> set = mWordList.keySet();
		ArrayList<String> list = new ArrayList<String>(set);
		Collections.shuffle(list);
		List<String> temp = list.subList(0, (size > list.size()) ? list.size() : size);
		return temp.toArray(new String[0]);
	}
	
	static String[] getMeanings(String word){
		String[] result = new String[0]; 
		return mWordList.get(word).toArray(result);
	}

}//end of class
