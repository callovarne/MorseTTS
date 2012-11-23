package com.departmentofdigitalwizardry.morsetts;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;

public class CheckVoiceData extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);	
		
		int result = android.speech.tts.TextToSpeech.Engine.CHECK_VOICE_DATA_PASS;
		final Intent data = new Intent();
		
		ArrayList<String> availableVoices = new ArrayList<String>();
		availableVoices.add("eng-USA");
		
		ArrayList<String> unavailableVoices = new ArrayList<String>();
		
		data.putStringArrayListExtra(Engine.EXTRA_AVAILABLE_VOICES, availableVoices);
		data.putStringArrayListExtra(Engine.EXTRA_UNAVAILABLE_VOICES, unavailableVoices);
		
		setResult(result, data);
		
		finish();
		
	}
	
}
