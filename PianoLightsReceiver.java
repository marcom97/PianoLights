
import com.philips.lighting.hue.sdk.wrapper.entertainment.TweenType;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.Animation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.ConstantAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.TweenAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.LightSourceEffect;

public class PianoLightsReceiver extends PianoReceiver {
	private static final float octaveWeight = 7f;
	private static final float[] weightsInOctave = {0.5f, 0.5f, 0.5f, 0.5f, 1, 0.5f,
													0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 1};
	private static final float minWeightedNote = weightedNote(21);
	private static final float maxWeightedNote = weightedNote(108);
	private static final float weightedNoteRange = maxWeightedNote - minWeightedNote;
	private static final float colorFactor = 360/weightedNoteRange;
	
	private static final float brightnessScale = 1f;
	
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}
	
	private static float weightedNote(int note) {
		return (note/12)*octaveWeight + noteWeightedInOctave(note);
	}
	
	private static float noteWeightedInOctave(int note) {
		int numInOctave = note % 12;
		
		float weightedNote = 0;
		while (numInOctave > 0) {
			numInOctave -= 1;
			weightedNote += weightsInOctave[numInOctave];
		}
		return weightedNote;
	}

	@Override
	protected void noteStart(byte note, byte velocity) {
		LightSourceEffect oldEffect = (LightSourceEffect) BridgeManager.getInstance().getEffect(Byte.toString(note));
		if (oldEffect == null) {
			Effect noteEffect = effectForNote(note, velocity);
			noteEffect.enable();
			BridgeManager.getInstance().addEffect(noteEffect);
		}
		else {
			Animation opacityAnimation = oldEffect.getIntensityAnimation();
			float velocityRate = velocity/127f * brightnessScale;
			
			if (opacityAnimation.getValue() < velocityRate) {
				Effect noteEffect = effectForNote(note, velocity);
				noteEffect.enable();
				BridgeManager.getInstance().replaceEffect(oldEffect, noteEffect);
			}
		}
	}
	
	private Effect effectForNote(byte note, byte velocity) {
//		byte noteNum = note.getNote();
//		byte velocity = note.getVelocity();
		
		ConstantAnimation x = new ConstantAnimation(0);
		ConstantAnimation y = new ConstantAnimation(0);
		ConstantAnimation radius = new ConstantAnimation(2);
		
		float weightedNote = weightedNote(note);
		float h = (weightedNote - minWeightedNote)*colorFactor;
		float hPrime = h/60;
		float Z = 1 - Math.abs(hPrime % 2 - 1);
		float C = 1/(1 + Z);
		float X = Z * C;
		System.out.println("weightedNote: " + weightedNote);
		System.out.println("minWeightedNote: " + minWeightedNote + ", colorFactor: " + colorFactor);
		System.out.println("h: " + h);
		System.out.println("hPrime: " + hPrime);
		System.out.println("Z: " + Z);
		System.out.println("C: " + C);
		System.out.println("X: " + X);

		float R = 0;
		float G = 0;
		float B = 0;
		if (0 <= hPrime && hPrime <= 1) {
			R = C;
			G = X;
		}
		else if (1 <= hPrime && hPrime <= 2) {
			R = X;
			G = C;
		}
		else if (2 <= hPrime && hPrime <= 3) {
			G = C;
			B = X;
		}
		else if (3 <= hPrime && hPrime <= 4) {
			G = X;
			B = C;
		}
		else if (4 <= hPrime && hPrime <= 5) {
			R = X;
			B = C;
		}
		else if (5 <= hPrime && hPrime <= 6) {
			R = C;
			B = X;
		}
		System.out.println("RGB: " + R + ", " + G + ", " + B);

		float velocityRate = velocity/127f;
		System.out.println("velocityRate: " + velocityRate);

		ConstantAnimation r = new ConstantAnimation(R);
		ConstantAnimation g = new ConstantAnimation(G);
		ConstantAnimation b = new ConstantAnimation(B);
		TweenAnimation intensity = new TweenAnimation(velocityRate*brightnessScale, 0, velocityRate*9000, TweenType.EaseInOutQuad);
		
		String noteStr = Byte.toString(note);
		LightSourceEffect noteEffect = new LightSourceEffect(noteStr, 0);
		noteEffect.setPositionAnimation(x, y);
		noteEffect.setRadiusAnimation(radius);
		noteEffect.setColorAnimation(r, g, b);
		noteEffect.setIntensityAnimation(intensity);
		noteEffect.setOpacityBoundToIntensity(true);
		
		return noteEffect;
	}

	@Override
	protected void noteEnd(byte note) {
		String noteStr = Byte.toString(note);

		Effect effect = BridgeManager.getInstance().getEffect(noteStr);
		if (effect == null) {
			return;
		}
		
		BridgeManager.getInstance().finishEffect(effect);
	}
}
