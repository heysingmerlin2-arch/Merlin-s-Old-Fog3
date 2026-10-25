package dev.merlin.oldfog;

/** Per-frame flags shared between the fog mixins. */
public final class OldFogState {
	private OldFogState() {}

	/** true while the current frame's fog comes from the normal air (atmospheric) environment. */
	public static boolean atmospheric = false;
	/** true if we changed the shared colour's alpha and must restore it. */
	public static boolean alphaModified = false;
}
