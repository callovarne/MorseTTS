package com.departmentofdigitalwizardry.morsetts;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.speech.tts.SynthesisCallback;

public class MorseEngine {
	
	public class Tone {
		int SampleRate;
		int Duration;
		byte[] Sound;
	}
	
	private String AsciiToMorse(char input) {
		
		Map<Character, String> morse = new HashMap<Character, String>() {{
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
		
		String output = morse.get(Character.toUpperCase(input));
		if (output == null || output.isEmpty()) output = " ";
		
		return output;
	}
	
	private int GetToneDuration(char input) {
		
		final int DIT = 500;
		final int DASH = 1250;
		final int BREAK_SHORT = 300;
		//final int BREAK_LONG = 700;
		
		Map<Character, Integer> durations = new HashMap<Character, Integer>() {{
			put('.', DIT);
			put('-', DASH);
			put(' ', BREAK_SHORT);
		}};
		
		return durations.get(input);
		
	}
	
	private Tone GenerateTone(int duration, int sampleRate, double frequency) {
		/*
		 *	See http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android
		 */
		 
		final int samples = (int)Math.round((duration / 1000) * sampleRate);
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
	
	public void TextToTones(String text, SynthesisCallback callback) {
		
		final double FREQUENCY = 1000;
		final int SAMPLE_RATE = 44100;
		
		//List<Tone> tones = new ArrayList<Tone>();
		//int lastLength = 0;
		// Map ASCII characters to Mores strings
		char[] asciiChars = text.toCharArray();
		for (int i = 0; i < asciiChars.length; i++) {
			String morse = AsciiToMorse(asciiChars[i]);
			
			// Map Morse characters to tones
			char[] morseChars = morse.toCharArray();
			for (int j = 0; j < morseChars.length; j++) {
			
				int duration = GetToneDuration(morseChars[j]);	
				//tones.add(GenerateTone(duration, SAMPLE_RATE, FREQUENCY));
				Tone tone = GenerateTone(duration, SAMPLE_RATE, morseChars[j] == ' ' ? 0 : FREQUENCY);
				
				Tone rest = GenerateTone(1000, SAMPLE_RATE, 0);
				
				ByteBuffer buffer = ByteBuffer.allocate(tone.Sound.length + rest.Sound.length);
				buffer.put(tone.Sound);
				buffer.put(rest.Sound);
				buffer.rewind();
				while (buffer.hasRemaining()) {
					int chunk = Math.min(2, buffer.remaining());
					byte[] reader = new byte[chunk];
					buffer.get(reader);
					callback.audioAvailable(reader, 0, chunk);
				}
				//callback.audioAvailable(tone.Sound, lastLength, tone.Sound.length);
				//lastLength += tone.Sound.length;
			}
		
		}
		
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
