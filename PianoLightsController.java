import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;

public class PianoLightsController {
	
	public void start() {
		Transmitter piano;
		try {
			piano = MidiSystem.getTransmitter();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
			return;
		}

		BridgeManager.getInstance().connect(new EntertainmentCallback() {
			public void didCreateEntertainment( ) {
				PianoLightsReceiver receiver = new PianoLightsReceiver();
				piano.setReceiver(receiver);
				System.out.println("Did Create Entertainment");
			}
		});
	}
}
