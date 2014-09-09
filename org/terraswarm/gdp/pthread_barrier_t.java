package org.terraswarm.gdp;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Union;
/**
 * <i>native declaration : /usr/include/bits/pthreadtypes.h:97</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class pthread_barrier_t extends Union {
	/** C type : char[32] */
	public byte[] __size = new byte[32];
	public NativeLong __align;
	public pthread_barrier_t() {
		super();
	}
	public pthread_barrier_t(NativeLong __align) {
		super();
		this.__align = __align;
		setType(NativeLong.class);
	}
	/** @param __size C type : char[32] */
	public pthread_barrier_t(byte __size[]) {
		super();
		if ((__size.length != this.__size.length)) 
			throw new IllegalArgumentException("Wrong array size !");
		this.__size = __size;
		setType(byte[].class);
	}
	public pthread_barrier_t(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends pthread_barrier_t implements com.sun.jna.Structure.ByReference {
		
	};
	public static class ByValue extends pthread_barrier_t implements com.sun.jna.Structure.ByValue {
		
	};
}