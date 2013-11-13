package com.departmentofdigitalwizardry.morsetts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.speech.tts.SynthesisCallback;
import android.util.Log;
import android.content.res.*;

public class MorseEngine {
	
	public class Tone {
		int SampleRate;
		int Duration;
		byte[] Sound;
	}
	
	public class Time {
		
		private int factor;
		
		public Time() {
			this.factor = 25;
		}
		public Time(int factor) {
			this.factor = factor;
		}
		
		public int Dit() {
			return factor * 1;
		}
		
		public int Dash() {
			return factor * 3;
		}
		
		// For between tones 
		public int ShortBreak() {
			return factor * 1;
		}
		
		// For between letters
		public int LetterBreak() {
			return factor * 3;
		}
		
		// For between words, 7x dot (6x to account for short breaks after every tone)
		public int LongBreak() {
			return factor * 6;
		}
	}
	
	private Time time;
	
	public MorseEngine() {
		this.time = new Time();
	}
	
	private char[] AsciiToMorse(char[] input) {
		
		Map<Character, String> morseCode = new HashMap<Character, String>() {{
			put('A', ".-");
			put('B', "-...");
			put('C', "-.-.");
			put('D', "-..");
			put('E', ".");
			put('F', "..-.");
			put('G', "--.");
			put('H', "....");
			put('I', "..");
			put('J', ".---");
			put('K', "-.-");
			put('L', ".-..");
			put('M', "--");
			put('N', "-.");
			put('O', "---");
			put('P', ".--.");
			put('Q', "--.-");
			put('R', ".-.");
			put('S', "...");
			put('T', "-");
			put('U', "..-");
			put('V', "...-");
			put('W', ".--");
			put('X', "-..-");
			put('Y', "-.--");
			put('Z', "--..");
			put('1', ".----");
			put('2', "..---");
			put('3', "...--");
			put('4', "....-");
			put('5', ".....");
			put('6', "-....");
			put('7', "--...");
			put('8', "---..");
			put('9', "----.");
			put('0', "-----");
			put(' ', " "); // Short break pass-through
		}};
		
		char[] output = new char[] {};
		for (int i = 0; i < input.length; i++) {
			char upperInputItem = Character.toUpperCase(input[i]);
			if(morseCode.containsKey(upperInputItem)) {
				char[] morse = (morseCode.get(upperInputItem) + "_").toCharArray();
				char[] newOutput = new char[output.length + morse.length];						// Create a new array with sum of existing and morse arrays
				System.arraycopy(output, 0, newOutput, 0, output.length);						// Copy existing array into new array
				System.arraycopy(morse, 0, newOutput, output.length > 0 ? output.length : 0, morse.length);	// Copy morse array into new array
				output = newOutput;																// Set output
			}
		}
		return output;
	}
	
	private int GetToneDuration(char input) {
		
		Map<Character, Integer> durations = new HashMap<Character, Integer>() {{
			put('.', time.Dit());
			put('-', time.Dash());
			put(' ', time.LongBreak());
			put('_', time.LetterBreak());
		}};
		
		return durations.get(input);
		
	}
	
	private Tone GenerateTone(int duration, int sampleRate, double frequency) {
		/*
		 *	See http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android
		 */
		 
		final int samples = (int)Math.ceil((duration / 1000D) * (double)sampleRate);
		double sample[] = new double[samples];
		
		
		for (int i = 0; i < samples; i++) {
			sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/frequency));
		}
		
		int index = 0;
		byte generatedSound[] = new byte[2 * samples];
		for (final double dVal : sample) {	
			final short val = (short)(dVal * 32767);
			generatedSound[index++] = (byte)(val & 0x00ff);
			generatedSound[index++] = (byte)((val & 0xff00) >>> 8);
		}
		
		Tone tone = this.new Tone();
		tone.SampleRate = sampleRate;
		tone.Sound = generatedSound;
		tone.Duration = duration;
		
		return tone;
		
	}
	
	public synchronized void TextToTones(String text, SynthesisCallback callback, AssetManager assetManager) {
		
		final double FREQUENCY = 1000;
		final int SAMPLE_RATE = 8000;
		
		int totalBytes = 0;
		
		//int lastLength = 0;
		// Map ASCII characters to Mores strings
		char[] morseChars = AsciiToMorse(text.toCharArray());
		
		// Map Morse characters to tones
		List<Tone> tones = new ArrayList<Tone>();
		for (int i = 0; i < morseChars.length; i++) {
		
			int duration = GetToneDuration(morseChars[i]);	
			
			Tone tone = GenerateTone(duration, SAMPLE_RATE, (morseChars[i] == ' ' | morseChars[i] == '_') ? 0 : FREQUENCY);
			tones.add(tone);
			totalBytes += tone.Sound.length;
			Tone rest = GenerateTone(time.ShortBreak(), SAMPLE_RATE, 0); // Add letter spacing
			tones.add(rest);
			totalBytes += rest.Sound.length;
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(totalBytes);
		for (int j = 0; j < tones.size(); j++)
		{
			Tone tone = tones.get(j);
			buffer.put(tone.Sound);			
		}
		buffer.rewind();
		
		while (buffer.hasRemaining()) {
			int chunk = Math.min(callback.getMaxBufferSize(), buffer.remaining());
			byte[] reader = new byte[chunk];
			buffer.get(reader);
			callback.audioAvailable(reader, 0, chunk);
		}
		
		//callback.audioAvailable(tone.Sound, lastLength, tone.Sound.length);
		//lastLength += tone.Sound.length;
		
		//return tones.toArray(new Tone[tones.size()]);
		
	}
	
	public void PlaySound(Tone tone) {
		
		final AudioTrack audioTrack = new AudioTrack(
			AudioManager.STREAM_MUSIC,
			tone.SampleRate,
			AudioFormat.CHANNEL_CONFIGURATION_MONO,
			AudioFormat.ENCODING_PCM_16BIT,
			tone.Sound.length,
			AudioTrack.MODE_STATIC
		);
		
		audioTrack.write(tone.Sound, 0, tone.Sound.length);
		
		audioTrack.play();
		
	}
	
}
