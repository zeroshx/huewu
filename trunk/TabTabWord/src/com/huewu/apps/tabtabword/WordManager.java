package com.huewu.apps.tabtabword;

import java.util.ArrayList;

import android.graphics.Rect;
import android.util.DisplayMetrics;

import com.huewu.apps.tabtabword.graphic.Meaning;
import com.huewu.apps.tabtabword.graphic.Word;

public class WordManager {

	//generate texture from given word.
	//managing texture cache.

	private Rect mRegion = null;
	private ArrayList<Word> mWordList = new ArrayList<Word>();
	private Meaning mMeaning = null;
	private int mCount = 0;

	public WordManager(final Rect rect, int count){
		mRegion = rect;
		mCount = count;
	}

	void generateWords(){
		mWordList = new ArrayList<Word>();
		String[] words = WordLoader.getRandomWords(mCount);
		String[] ms = WordLoader.getMeanings(words[0]);
		if(ms == null || ms.length == 0)
			generateWords();	//retry.
		else{
			mMeaning = new Meaning(ms[0], mRegion);
			for(String w : words){
				mWordList.add(new Word(w, mRegion));
			}
		}
	}
	
	void generateMeaning(){
		
		if(mWordList.size() == 0)
			generateWords();
		else{
			Word word = mWordList.get(0);
			if(word != null){
				String[] ms = WordLoader.getMeanings(word.getContent());
				mMeaning = new Meaning(ms[0], mRegion);
			}
		}
	}
	
	Meaning getMeaning(){
		return mMeaning;
	}
	
	ArrayList<Word> getWordList(){
		return mWordList;
	}

	public int count() {
		return mWordList.size();
	}
	
	public void checkTouch(float x, float y){
		
		Word w = mWordList.get(0);
		
		if(w.contains(x, y) == true){
			mWordList.remove(w);
			generateMeaning();
		}
	}
	
//	generateMeaning(){
//		
//	}


}//end of class
