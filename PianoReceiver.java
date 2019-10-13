import java.util.BitSet;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public abstract class PianoReceiver implements Receiver {
	private static final byte SUSTAIN = 64;
	
	private BitSet currentNotes;
	private BitSet pressedNotes;
	private boolean sustain;
	
	public PianoReceiver() {
		currentNotes = new BitSet(128);
		pressedNotes = new BitSet(128);
	}

	@Override
	public void send(MidiMessage message, long timeStamp) {
		int status = message.getStatus();
		byte[] bytes = message.getMessage();
		
		switch (status) {
			case ShortMessage.NOTE_ON: {
				byte note = bytes[1];
				byte velocity = bytes[2];
								
				System.out.println("Note: " + note + ", ON, Velocity: " + velocity);
								
				currentNotes.set(note);
				pressedNotes.set(note);
				
				noteStart(note, velocity);
				break;
			}
			case ShortMessage.NOTE_OFF: {
				byte note = bytes[1];
				
				System.out.println(note + ", OFF");
				
				pressedNotes.clear(note);
				
				if (!sustain) {
					currentNotes.clear(note);
					noteEnd(note);
				}
				
				break;
			}
			case ShortMessage.CONTROL_CHANGE:
				switch (bytes[1]) {
					case SUSTAIN:
						if (bytes[2] <= 63) {
							System.out.println("Sustain: OFF");
							sustain =  false;
							
							endCurrentUnpressedNotes();
						}
						else {
							System.out.println("Sustain: ON");
							sustain = true;
						}
						break;
				}
		}
	}
	
	private void endCurrentUnpressedNotes() {
		BitSet currentUnpressedNotes = (BitSet) currentNotes.clone();
		currentUnpressedNotes.andNot(pressedNotes);
		
		for (byte i = (byte) currentUnpressedNotes.nextSetBit(0); 
				i >= 0; 
				i = (byte) currentUnpressedNotes.nextSetBit(i+1)) {
		     noteEnd(i);
		 }
		
		currentNotes.and(pressedNotes);
	}

	abstract protected void noteStart(byte note, byte velocity);
	abstract protected void noteEnd(byte note);
}
