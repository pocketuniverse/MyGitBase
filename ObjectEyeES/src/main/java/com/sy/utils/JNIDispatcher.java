package com.sy.utils;

import com.Face.JNIInterface;

public class JNIDispatcher {
	//private int GPUIndex = 3;
	private JNIInterface jni;
	private static final JNIDispatcher instance = new JNIDispatcher();
	public static JNIDispatcher getInstance(){
		return instance;
	}
	private JNIDispatcher(){
		//this.jni = new JNIInterface(GPUIndex);
		this.jni = new JNIInterface();
	}
	public JNIInterface getJNI(){
		return jni;
	}
	public void releaseJNI() {
		
	}
}
