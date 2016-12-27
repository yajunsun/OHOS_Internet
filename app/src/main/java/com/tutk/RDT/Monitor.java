/******************************************************************************
 *                                                                            *
 * Copyright (c) 2011 by TUTK Co.LTD. All Rights Reserved.                    *
 *                                                                            *
 *                                                                            *
 * Class: CamMonitorView.java                                                 *
 *                                                                            *
 * Author: joshua ju                                                          *
 *                                                                            *
 * Date: 2011-05-14                                                           *
 *                                                                            *
 ******************************************************************************/

package com.tutk.RDT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.tutk.RDT.AVIOCTRLDEFs.SMsgAVIoctrlPtzCmd;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressLint("NewApi")
public class Monitor extends SurfaceView implements SurfaceHolder.Callback, IRegisterIOTCListener, OnTouchListener, GestureDetector.OnGestureListener {

	private static final float DEFAULT_MAX_ZOOM_SCALE = 2.0f;
	private static final int PTZ_SPEED = 8;
	private static final int PTZ_DELAY = 1500;
	private static final int FLING_MIN_DISTANCE = 100;
	private static final int FLING_MIN_VELOCITY = 0;
	//private static final int CHANNEL_LOST_FRAME_TIMEOUT = 5000; // ms

	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private int mPinchedMode = NONE;
	private PointF mStartPoint = new PointF();
	private PointF mMidPoint = new PointF();
	private PointF mMidPointForCanvas = new PointF();
	private float mOrigDist = 0f;
	private long mLastZoomTime;
	// private long mLastDragTime;
	private float mCurrentScale = 1.0f;
	private float mCurrentMaxScale = DEFAULT_MAX_ZOOM_SCALE;

	private GestureDetector mGestureDetector;
	private SurfaceHolder mSurHolder = null;

	private int vLeft, vTop, vRight, vBottom;

	private Rect mRectCanvas = new Rect(); // used for render image.
	private Rect mRectMonitor = new Rect(); // used for store size of monitor.

	private Bitmap mLastFrame;
	private Lock mLastFrameLock = new ReentrantLock();
	private Camera mCamera;
	private int mAVChannel = -1;
	
	private int mCurVideoWidth = 0;
	private int mCurVideoHeight = 0;

	private ThreadRender mThreadRender = null;

	public Monitor(Context context, AttributeSet attrs) {
		super(context, attrs);

		mSurHolder = getHolder();
		mSurHolder.addCallback(this);

		mGestureDetector = new GestureDetector((android.view.GestureDetector.OnGestureListener) this);
		this.setOnTouchListener(this);
		this.setLongClickable(true);
	}

	public void setMaxZoom(float value) {
		mCurrentMaxScale = value;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		synchronized (this) {

			mRectMonitor.set(0, 0, width, height);
			mRectCanvas.set(0, 0, width, height);

			if (mCurVideoWidth == 0 || mCurVideoHeight == 0) {
				if (height < width) { // landscape layout				
					mRectCanvas.right = 4 * height / 3;
					mRectCanvas.offset((width - mRectCanvas.right) / 2, 0);
				} else { // portrait layout
					mRectCanvas.bottom = 3 * width / 4;
					mRectCanvas.offset(0, (height - mRectCanvas.bottom) / 2);
				}
			} else {
				if ((mRectMonitor.bottom - mRectMonitor.top) < (mRectMonitor.right - mRectMonitor.left)) { // landscape layout
					Log.i("IOTCamera", "Landscape layout");
					double ratio = (double) mCurVideoWidth / mCurVideoHeight;
					mRectCanvas.right = (int) (mRectMonitor.bottom * ratio);
					mRectCanvas.offset((mRectMonitor.right - mRectCanvas.right) / 2, 0);
				} else { // portrait layout
					Log.i("IOTCamera", "Portrait layout");
					double ratio = (double) mCurVideoWidth / mCurVideoHeight;
					mRectCanvas.bottom = (int) (mRectMonitor.right / ratio);
					mRectCanvas.offset(0, (mRectMonitor.bottom - mRectCanvas.bottom) / 2);
				}
			}

			vLeft = mRectCanvas.left;
			vTop = mRectCanvas.top;
			vRight = mRectCanvas.right;
			vBottom = mRectCanvas.bottom;

			mCurrentScale = 1.0f;

			parseMidPoint(mMidPoint, vLeft, vTop, vRight, vBottom);
			parseMidPoint(mMidPointForCanvas, vLeft, vTop, vRight, vBottom);
		}

		// System.out.println("surfaceChanged -> l: " + vLeft + ", t: " + vTop + ", r: " + vRight + ", b: " + vBottom + ",  width: " + width + ", height: " + height);
	}

	public void surfaceCreated(SurfaceHolder holder) {
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	public void attachCamera(Camera camera, int avChannel) {

		mCamera = camera;
		mCamera.registerIOTCListener(this);
		mAVChannel = avChannel;

		if (mThreadRender == null) {
			mThreadRender = new ThreadRender();
			mThreadRender.start();
		}
	}

	public void deattachCamera() {

		mAVChannel = -1;

		if (mCamera != null) {
			mCamera.unregisterIOTCListener(this);
			mCamera = null;
		}

		if (mThreadRender != null) {
			mThreadRender.stopThread();
			
			try {
				mThreadRender.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			mThreadRender = null;
		}
	}

	public void receiveFrameData(Camera camera, int avChannel, Bitmap bmp) {

		// synchronized (Monitor.this) {
		// Fixed by CA
		
		//synchronized (mLastFrameLock) {
			if (mAVChannel == avChannel) {
				mLastFrame = bmp;
				
				if (bmp.getWidth() > 0 && bmp.getHeight() > 0 && 
					(bmp.getWidth() != mCurVideoWidth || bmp.getHeight() != mCurVideoHeight)) {
					
					mCurVideoWidth = bmp.getWidth();
					mCurVideoHeight = bmp.getHeight();
					
					mRectCanvas.set(0, 0, mRectMonitor.right, mRectMonitor.bottom);

					if ((mRectMonitor.bottom - mRectMonitor.top) < (mRectMonitor.right - mRectMonitor.left)) { // landscape layout
						Log.i("IOTCamera", "Landscape layout");
						double ratio = (double) mCurVideoWidth / mCurVideoHeight;
						mRectCanvas.right = (int) (mRectMonitor.bottom * ratio);
						mRectCanvas.offset((mRectMonitor.right - mRectCanvas.right) / 2, 0);
					} else { // portrait layout
						Log.i("IOTCamera", "Portrait layout");
						double ratio = (double) mCurVideoWidth / mCurVideoHeight;
						mRectCanvas.bottom = (int) (mRectMonitor.right / ratio);
						mRectCanvas.offset(0, (mRectMonitor.bottom - mRectCanvas.bottom) / 2);
					}

					vLeft = mRectCanvas.left;
					vTop = mRectCanvas.top;
					vRight = mRectCanvas.right;
					vBottom = mRectCanvas.bottom;

					mCurrentScale = 1.0f;

					parseMidPoint(mMidPoint, vLeft, vTop, vRight, vBottom);
					parseMidPoint(mMidPointForCanvas, vLeft, vTop, vRight, vBottom);

					Log.i("IOTCamera", "Change canvas size (" + (mRectCanvas.right - mRectCanvas.left) + ", " + (mRectCanvas.bottom - mRectCanvas.top) + ")");
				}
			}
		//}
		// }

		// System.out.println("ready to render");
	}

	public void receiveFrameInfo(Camera camera, int sessionChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

	}

	public void receiveChannelInfo(Camera camera, int sessionChannel, int resultCode) {

	}

	public void receiveSessionInfo(Camera camera, int resultCode) {

	}

	public void receiveIOCtrlData(Camera camera, int sessionChannel, int avIOCtrlMsgType, byte[] data) {

	}

	@SuppressLint("NewApi")
	public boolean onTouch(View view, MotionEvent event) {
		mGestureDetector.onTouchEvent(event);

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:

			if (mRectCanvas.left != vLeft || mRectCanvas.top != vTop || mRectCanvas.right != vRight || mRectCanvas.bottom != vBottom) {
				mPinchedMode = DRAG;
				mStartPoint.set(event.getX(), event.getY());
			}

			break;

		case MotionEvent.ACTION_POINTER_DOWN:

			float dist = spacing(event);

			if (dist > 10f) {

				mPinchedMode = ZOOM;
				mOrigDist = dist;

				System.out.println("Action_Pointer_Down -> origDist(" + mOrigDist + ")");
			}

			break;

		case MotionEvent.ACTION_MOVE:

			if (mPinchedMode == DRAG) {

				if (System.currentTimeMillis() - mLastZoomTime < 33)
					return true;

				PointF currentPoint = new PointF();
				currentPoint.set(event.getX(), event.getY());

				int offsetX = (int) currentPoint.x - (int) mStartPoint.x;
				int offsetY = (int) currentPoint.y - (int) mStartPoint.y;

				mStartPoint = currentPoint;

				Rect rect = new Rect();
				rect.set(mRectCanvas);
				rect.offset(offsetX, offsetY);

				int width = rect.right - rect.left;
				int height = rect.bottom - rect.top;

				if ((mRectMonitor.bottom - mRectMonitor.top) > (mRectMonitor.right - mRectMonitor.left)) {

					if (rect.left > mRectMonitor.left) {
						rect.left = mRectMonitor.left;
						rect.right = rect.left + width;
					}

					if (rect.top > mRectMonitor.top) {
						rect.top = mRectCanvas.top;
						rect.bottom = rect.top + height;
					}

					if (rect.right < mRectMonitor.right) {
						rect.right = mRectMonitor.right;
						rect.left = rect.right - width;
					}

					if (rect.bottom < mRectMonitor.bottom) {
						rect.bottom = mRectCanvas.bottom;
						rect.top = rect.bottom - height;
					}

				} else {

					if (rect.left > mRectMonitor.left) {
						rect.left = mRectCanvas.left;
						rect.right = rect.left + width;
					}

					if (rect.top > mRectMonitor.top) {
						rect.top = mRectMonitor.top;
						rect.bottom = rect.top + height;
					}

					if (rect.right < mRectMonitor.right) {
						rect.right = mRectCanvas.right;
						rect.left = rect.right - width;
					}

					if (rect.bottom < mRectMonitor.bottom) {
						rect.bottom = mRectMonitor.bottom;
						rect.top = rect.bottom - height;
					}
				}

				System.out.println("offset (" + offsetX + ", " + offsetY + "), after offset rect = (" + rect.left + ", " + rect.top + ", " + rect.right + ", " + rect.bottom + ")");

				mRectCanvas.set(rect);
				// mLastDragTime = System.currentTimeMillis();

			} else if (mPinchedMode == ZOOM) {

				if (System.currentTimeMillis() - mLastZoomTime < 33)
					return true;

				if (event.getPointerCount() == 1)
					return true;

				float newDist = spacing(event);
				float scale = newDist / mOrigDist;
				mCurrentScale *= scale;

				mOrigDist = newDist;

				if (mCurrentScale > mCurrentMaxScale) {
					mCurrentScale = mCurrentMaxScale;
					return true;
				}

				if (mCurrentScale < 1.0f) {
					mCurrentScale = 1.0f;
				}

				System.out.println("newDist(" + newDist + ") / origDist(" + mOrigDist + ") = zoom scale(" + mCurrentScale + ")");

				int maxWidth = (vRight - vLeft) * 3;
				int maxHeight = (vBottom - vTop) * 3;

				int scaledWidth = (int) ((float) (vRight - vLeft) * mCurrentScale);
				int scaledHeight = (int) ((float) (vBottom - vTop) * mCurrentScale);
				int origWidth = vRight - vLeft;
				int origHeight = vBottom - vTop;

				int l = (int) ((mRectMonitor.width() / 2) - (((mRectMonitor.width() / 2) - mRectCanvas.left) * scale));
				int t = (int) ((mRectMonitor.height() / 2) - (((mRectMonitor.height() / 2) - mRectCanvas.top) * scale));
				int r = l + scaledWidth;
				int b = t + scaledHeight;

				if (scaledWidth <= origWidth || scaledHeight <= origHeight) {
					l = vLeft;
					t = vTop;
					r = vRight;
					b = vBottom;
				} else if ((scaledWidth >= maxWidth) || (scaledHeight >= maxHeight)) {
					l = mRectCanvas.left;
					t = mRectCanvas.top;
					r = l + maxWidth;
					b = t + maxHeight;
				}

				mRectCanvas.set(l, t, r, b);

				System.out.println("zoom -> l: " + l + ", t: " + t + ", r: " + r + ", b: " + b + ",  width: " + scaledWidth + ", height: " + scaledHeight);
				mLastZoomTime = System.currentTimeMillis();
			}

			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:

			if (mCurrentScale == 1.0f) {
				mPinchedMode = NONE;
			}

			break;
		}

		return true;
	}

	public boolean onDown(MotionEvent e) {
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

		if (mRectCanvas.left != vLeft || mRectCanvas.top != vTop || mRectCanvas.right != vRight || mRectCanvas.bottom != vBottom)
			return false;

		System.out.println("velocityX: " + Math.abs(velocityX) + ", velocityY: " + Math.abs(velocityY));

		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {

			// Fling left
			if (mCamera != null && mAVChannel >= 0)
				mCamera.sendIOCtrl(mAVChannel, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND, SMsgAVIoctrlPtzCmd.parseContent((byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_RIGHT, (byte) PTZ_SPEED, (byte) 0, (byte) 0, (byte) 0, (byte) 0));

		} else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			// Fling right

			if (mCamera != null && mAVChannel >= 0)
				mCamera.sendIOCtrl(mAVChannel, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND, SMsgAVIoctrlPtzCmd.parseContent((byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_LEFT, (byte) PTZ_SPEED, (byte) 0, (byte) 0, (byte) 0, (byte) 0));

		} else if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_MIN_VELOCITY) {

			// Fling left
			if (mCamera != null && mAVChannel >= 0)
				mCamera.sendIOCtrl(mAVChannel, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND, SMsgAVIoctrlPtzCmd.parseContent((byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_DOWN, (byte) PTZ_SPEED, (byte) 0, (byte) 0, (byte) 0, (byte) 0));

		} else if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_MIN_VELOCITY) {

			// Fling right
			if (mCamera != null && mAVChannel >= 0)
				mCamera.sendIOCtrl(mAVChannel, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND, SMsgAVIoctrlPtzCmd.parseContent((byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_UP, (byte) PTZ_SPEED, (byte) 0, (byte) 0, (byte) 0, (byte) 0));
		}

		new Handler().postDelayed(new Runnable() {

			public void run() {

				if (mCamera != null && mAVChannel >= 0)
					mCamera.sendIOCtrl(mAVChannel, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND, SMsgAVIoctrlPtzCmd.parseContent((byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_STOP, (byte) PTZ_SPEED, (byte) 0, (byte) 0, (byte) 0, (byte) 0));
			}

		}, PTZ_DELAY);

		return false;
	}

	public void onLongPress(MotionEvent e) {
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return true;
	}

	public void onShowPress(MotionEvent e) {

	}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@SuppressLint("FloatMath")
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	// Calculate the mid point of the first two fingers 
	/*
	private void parseMidPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
	*/

	private void parseMidPoint(PointF point, float left, float top, float right, float bottom) {
		point.set((left + right) / 2, (top + bottom) / 2);
	}

	private class ThreadRender extends Thread {

		private boolean mIsRunningThread = false;
		private Object mWaitObjectForStopThread = new Object();

		public void stopThread() {
			mIsRunningThread = false;
			try {
				mWaitObjectForStopThread.notify();
			} catch (Exception e) {
			}
		}

		@Override
		public void run() {

			mIsRunningThread = true;
			Canvas videoCanvas = null ;

			while (mIsRunningThread) {

				// Fixed by CA
				//synchronized (mLastFrameLock) {
					if (mLastFrame != null && !mLastFrame.isRecycled()) {
						try{
							videoCanvas = mSurHolder.lockCanvas();	
							// synchronized (Monitor.this) {
							if (videoCanvas != null){
								// Fixed by CA
								// should edit videoCanvas before unlock it.
								videoCanvas.drawColor(Color.BLACK);
								videoCanvas.drawBitmap(mLastFrame, null, mRectCanvas, null);
							}
						} finally{
							if (videoCanvas != null)
								mSurHolder.unlockCanvasAndPost(videoCanvas);
							videoCanvas = null ;
						}
					}
				//}
				
				try {
					synchronized (mWaitObjectForStopThread) {
						mWaitObjectForStopThread.wait(33);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			System.out.println("===ThreadRender exit===");
		}
	}
}
