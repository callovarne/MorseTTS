package com.departmentofdigitalwizardry.morsetts;

import java.util.*;
import android.speech.tts.*;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeechService;
import com.departmentofdigitalwizardry.morsetts.MorseEngine.Tone;

public class MorseTTSService extends TextToSpeechService {
	
	private static final String DEFAULT_COUNTRY = "USA";
	private static final String DEFAULT_LANG = "eng";
	private static final String DEFAULT_VARIANT = "";
	
	private SynthesisCallback callback;
	private MorseEngine engine;
	
	@Override
	public void onCreate() {
		
		// Initialize engine
		initializeEngine();
		
		// Calls onIsLanguageAvailable(); must run after engine initialization
		super.onCreate();
		
	}
	
	private void initializeEngine() {
		if (engine != null) {
			//engine.stop();
			engine = null;
		}
		engine = new MorseEngine();
	}
	
	@Override
	protected String[] onGetLanguage() {
		
		return new String[] {DEFAULT_LANG, DEFAULT_COUNTRY, DEFAULT_VARIANT};
		
	}

	@Override
	protected int onIsLanguageAvailable(String lang, String country, String variant) {
		
		boolean match = lang.equalsIgnoreCase(DEFAULT_LANG);
		if (match) {
			return TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE;
		} else {
			return TextToSpeech.LANG_NOT_SUPPORTED;
		}
		
	}
	
	@Override
	protected int onLoadLanguage(String lang, String country, String variant) {
		
		return this.onIsLanguageAvailable(lang, country, variant);
		
	}
	
	@Override
	protected synchronized void onSynthesizeText(SynthesisRequest request, SynthesisCallback callback) {

		// Get request params
		String language = request.getLanguage();
		String country = request.getCountry();
		String variant = request.getVariant();
		String text = request.getText();
		
		this.callback = callback;
		
		callback.start(44100, AudioFormat.ENCODING_PCM_16BIT, 1);
		
		engine.TextToTones(text, callback);
//		// Get tones
//		MorseEngine.Tone[] tones = engine.TextToTones(text);
//		
//		// Synthesize audio
//		
//		// Combine tones.Sound byte arrays into one byte array
//		List<Byte> combined = new ArrayList<Byte>();
//		for (MorseEngine.Tone tone: tones) {
//			//engine.PlaySound(tone);
//			//combined.addAll(Arrays.<Byte>asList(tone.Sound.));
//			for (byte bytes : tone.Sound) {
//				combined.add(bytes);
//			}
//		}
//		
//		byte[] output = new byte[combined.size()];
//		for (int i = 0; i < combined.size(); i++) {
//			output[i] = combined.get(i);			
//		}
//		
//		callback.audioAvailable(output, 0, output.length);
		
		// Not sure if these are required
		
		//callback.audioAvailable(new byte[] {}, 0, 1);
		callback.done();
		
	}
	
	@Override
	protected void onStop() {
		// engine.stop()
	}
	
}
