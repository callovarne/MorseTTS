package com.departmentofdigitalwizardry.morsetts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

public class GetSampleText extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		final int result = TextToSpeech.LANG_AVAILABLE;
		final Intent data = new Intent();
		
		data.putExtra("sampleText", "This is a test. Pretty cool eh.");
		
		setResult(result, data);		
		
		finish();
		
	}
	
}
