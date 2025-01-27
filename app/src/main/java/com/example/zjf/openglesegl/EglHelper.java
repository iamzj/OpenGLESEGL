package com.example.zjf.openglesegl;

import android.view.Surface;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;

public class EglHelper {
	EGL10 mEgl;
	EGLDisplay mEglDisplay;
	EGLContext mEglContext;
	EGLSurface mEglSurface;
	//private int mEGLContextClientVersion;
	public void initEgl(Surface surface, EGLContext eglContext){
		//1、得到Egl实例
		mEgl = (EGL10) EGLContext.getEGL();
		//2、得到默认的显示设备（就是窗口）
		mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

		if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
			throw new RuntimeException("eglGetDisplay failed");
		}

		//3、初始化默认显示设备
		int[] version = new int[2];
		if(!mEgl.eglInitialize(mEglDisplay, version)) {
			throw new RuntimeException("eglInitialize failed");
		}


		//4、设置显示设备的属性
		int[] attrbutes = new int[]{
				EGL10.EGL_RED_SIZE,8,
				EGL10.EGL_GREEN_SIZE,8,
				EGL10.EGL_BLUE_SIZE,8,
				EGL10.EGL_ALPHA_SIZE,8,
				EGL10.EGL_DEPTH_SIZE,8,
				EGL10.EGL_STENCIL_SIZE,8,
				EGL10.EGL_RENDERABLE_TYPE,4,//写死的
				EGL10.EGL_NONE};

		int[]num_config = new int[1];

		if (!mEgl.eglChooseConfig(mEglDisplay,attrbutes,null,1,num_config)) {
			throw new IllegalArgumentException("eglChooseConfig failed");
		}

		int numConfigs = num_config[0];
		if (numConfigs <= 0) {
			throw new IllegalArgumentException(
					"No configs match configSpec");
		}


		//5、从系统中获取对应属性的配置
		EGLConfig[] configs = new EGLConfig[numConfigs];
		if (!mEgl.eglChooseConfig(mEglDisplay, attrbutes, configs, numConfigs,
				num_config)) {
			throw new IllegalArgumentException("eglChooseConfig#2 failed");
		}

		//6、创建EglContext
		int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION,2 /*mEGLContextClientVersion*/,
				EGL10.EGL_NONE };
		if (eglContext != null) {
			//如果
			mEglContext = mEgl.eglCreateContext(mEglDisplay,configs[0],eglContext,attrib_list);
		} else {
			mEglContext = mEgl.eglCreateContext(mEglDisplay,configs[0],EGL10.EGL_NO_CONTEXT,attrib_list);
		}
		
		//7、创建渲染的Surface
//        	 mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay,eglConfigs[0],surface,attrib_list );//绘制无效,会一直打印libEGL: eglSwapBuffersWithDamageKHR:1370 error 300d (EGL_BAD_SURFACE)
        	int[] surfaceAttr = {EGL10.EGL_NONE};
        	mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay,eglConfigs[0],surface,surfaceAttr );//解决绘制无效,一直打印libEGL: eglSwapBuffersWithDamageKHR:1370 error 300d (EGL_BAD_SURFACE)


		//8、绑定EglContext和Surface到显示设备中
		if (!mEgl.eglMakeCurrent(mEglDisplay,mEglSurface,mEglSurface,mEglContext)){
			throw new RuntimeException("eglMakeCurrent fail");
		}
	}

	//刷新数据
	public boolean swapBuffers(){
		if (mEgl != null) {
			return mEgl.eglSwapBuffers(mEglDisplay,mEglSurface);
		} else {
			throw new RuntimeException("egl is null");
		}
	}


	public EGLContext getEglContext() {
		return mEglContext;
	}

	public void destroyEgl(){
		if (mEgl != null) {
			mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
					EGL10.EGL_NO_SURFACE,
					EGL10.EGL_NO_CONTEXT);//解绑
			mEgl.eglDestroySurface(mEglDisplay,mEglSurface);
			mEglSurface = null;

			mEgl.eglDestroyContext(mEglDisplay,mEglContext);
			mEglContext = null;

			mEgl.eglTerminate(mEglDisplay);
			mEglDisplay = null;

			mEgl = null;
		}
	}
}
