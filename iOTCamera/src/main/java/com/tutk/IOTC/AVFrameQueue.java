/******************************************************************************
 *                                                                            *
 * Copyright (c) 2011 by TUTK Co.LTD. All Rights Reserved.                    *
 *                                                                            *
 *                                                                            *
 * Class: Fifo.java                                                           *
 *                                                                            *
 * Author: joshua ju                                                          *
 *                                                                            *
 * Date: 2011-05-14                                                           *
 *                                                                            *
 ******************************************************************************/

package com.tutk.IOTC;

import java.util.LinkedList;

class AVFrameQueue {

	private volatile LinkedList<AVFrame> listData = new LinkedList<AVFrame>();
	private volatile int mSize = 0;

	public synchronized int getCount() {
		return mSize;
	}

	public synchronized void addLast(AVFrame node) {

		if (mSize > 1500) {

			boolean bFirst = true;

			while (true) {
				if(listData.isEmpty())	
					break ;
				AVFrame frame = listData.get(0);

				if (bFirst) {

					if (frame.isIFrame())
						System.out.println("drop I frame");
					else
						System.out.println("drop p frame");

					listData.removeFirst();
					mSize--;

				} else {

					if (frame.isIFrame())
						break;
					else {
						System.out.println("drop p frame");
						listData.removeFirst();
						mSize--;
					}
				}

				bFirst = false;
			}
		}

		listData.addLast(node);
		mSize++;
	}

	public synchronized AVFrame removeHead() {

		if (mSize == 0)
			return null;
		else {
			AVFrame frame = listData.removeFirst();
			mSize--;
			return frame;
		}
	}

	public synchronized void removeAll() {

		if (!listData.isEmpty())
			listData.clear();

		mSize = 0;
	}
	
	public synchronized boolean isFirstIFrame() {
		return listData != null && !listData.isEmpty() && listData.get(0).isIFrame();
	}
}
