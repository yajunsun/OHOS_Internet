
/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>

#include "common.h"
#include "avcodec.h"

struct AVCodec *codec=NULL;			  // Codec
struct AVCodecContext *c=NULL;		  // Codec Context
struct AVFrame *picture=NULL;		  // Frame	
		
int iWidth=0;
int iHeight=0;
	
int *colortab=NULL;
int *u_b_tab=NULL;
int *u_g_tab=NULL;
int *v_g_tab=NULL;
int *v_r_tab=NULL;

//short *tmp_pic=NULL;

unsigned int *rgb_2_pix=NULL;
unsigned int *r_2_pix=NULL;
unsigned int *g_2_pix=NULL;
unsigned int *b_2_pix=NULL;
		
void DeleteYUVTab()
{
//	av_free(tmp_pic);
	
	av_free(colortab);
	av_free(rgb_2_pix);
}

void CreateYUVTab_16()
{
	int i;
	int u, v;
	
//	tmp_pic = (short*)av_malloc(iWidth*iHeight*2); // ���� iWidth * iHeight * 16bits

	colortab = (int *)av_malloc(4*256*sizeof(int));
	u_b_tab = &colortab[0*256];
	u_g_tab = &colortab[1*256];
	v_g_tab = &colortab[2*256];
	v_r_tab = &colortab[3*256];

	for (i=0; i<256; i++)
	{
		u = v = (i-128);

		u_b_tab[i] = (int) ( 1.772 * u);
		u_g_tab[i] = (int) ( 0.34414 * u);
		v_g_tab[i] = (int) ( 0.71414 * v); 
		v_r_tab[i] = (int) ( 1.402 * v);
	}

	rgb_2_pix = (unsigned int *)av_malloc(3*768*sizeof(unsigned int));

	r_2_pix = &rgb_2_pix[0*768];
	g_2_pix = &rgb_2_pix[1*768];
	b_2_pix = &rgb_2_pix[2*768];

	for(i=0; i<256; i++)
	{
		r_2_pix[i] = 0;
		g_2_pix[i] = 0;
		b_2_pix[i] = 0;
	}

	for(i=0; i<256; i++)
	{
		r_2_pix[i+256] = (i & 0xF8) << 8;
		g_2_pix[i+256] = (i & 0xFC) << 3;
		b_2_pix[i+256] = (i ) >> 3;
	}

	for(i=0; i<256; i++)
	{
		r_2_pix[i+512] = 0xF8 << 8;
		g_2_pix[i+512] = 0xFC << 3;
		b_2_pix[i+512] = 0x1F;
	}

	r_2_pix += 256;
	g_2_pix += 256;
	b_2_pix += 256;
}

void DisplayYUV_16(unsigned int *pdst1, unsigned char *y, unsigned char *u, unsigned char *v, int width, int height, int src_ystride, int src_uvstride, int dst_ystride)
{
	int i, j;
	int r, g, b, rgb;

	int yy, ub, ug, vg, vr;

	unsigned char* yoff;
	unsigned char* uoff;
	unsigned char* voff;
	
	unsigned int* pdst=pdst1;

	int width2 = width/2;
	int height2 = height/2;
	
	if(width2>iWidth/2)
	{
		width2=iWidth/2;

		y+=(width-iWidth)/4*2;
		u+=(width-iWidth)/4;
		v+=(width-iWidth)/4;
	}

	if(height2>iHeight)
		height2=iHeight;

	for(j=0; j<height2; j++) // һ��2x2���ĸ�����
	{
		yoff = y + j * 2 * src_ystride;
		uoff = u + j * src_uvstride;
		voff = v + j * src_uvstride;

		for(i=0; i<width2; i++)
		{
			yy  = *(yoff+(i<<1));
			ub = u_b_tab[*(uoff+i)];
			ug = u_g_tab[*(uoff+i)];
			vg = v_g_tab[*(voff+i)];
			vr = v_r_tab[*(voff+i)];

			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			rgb = r_2_pix[r] + g_2_pix[g] + b_2_pix[b];

			yy = *(yoff+(i<<1)+1);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			pdst[(j*dst_ystride+i)] = (rgb)+((r_2_pix[r] + g_2_pix[g] + b_2_pix[b])<<16);

			yy = *(yoff+(i<<1)+src_ystride);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			rgb = r_2_pix[r] + g_2_pix[g] + b_2_pix[b];

			yy = *(yoff+(i<<1)+src_ystride+1);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			pdst [((2*j+1)*dst_ystride+i*2)>>1] = (rgb)+((r_2_pix[r] + g_2_pix[g] + b_2_pix[b])<<16);
		}
	}
}

//====================================================

/*
 * Class:     h264_com_VView
 * Method:    InitDecoder
 * Signature: ()I
 */
jint Java_h264_com_VView_InitDecoder(JNIEnv* env, jobject thiz, jint width, jint height)
{
	iWidth = width;
	iHeight = height;

	CreateYUVTab_16();
	
	c = avcodec_alloc_context(); 

	avcodec_open(c); 

	picture  = avcodec_alloc_frame();//picture= malloc(sizeof(AVFrame));
		
	return 1;
}

/*
 * Class:     h264_com_VView
 * Method:    UninitDecoder
 * Signature: ()I
 */
jint Java_h264_com_VView_UninitDecoder(JNIEnv* env, jobject thiz)
{
	if(c)
	{
		decode_end(c);
	    free(c->priv_data);

		free(c);
		c = NULL;
	}

	if(picture)
	{
		free(picture);
		picture = NULL;
	}

	DeleteYUVTab();
	
	return 1;	
}

/*
 * Class:     h264_com_VView
 * Method:    DecoderNal
 * Signature: ([B[I)I
 */
jint Java_h264_com_VView_DecoderNal(JNIEnv* env, jobject thiz, jbyteArray in, jint nalLen, jbyteArray out)
{
	int i;
	int imod;
	int got_picture;

	jbyte * Buf = (jbyte*)(*env)->GetByteArrayElements(env, in, 0);
	jbyte * Pixel= (jbyte*)(*env)->GetByteArrayElements(env, out, 0);
	
	int consumed_bytes = decode_frame(c, picture, &got_picture, Buf, nalLen); 

	if(consumed_bytes > 0)
	{
		DisplayYUV_16((int*)Pixel, picture->data[0], picture->data[1], picture->data[2], c->width, c->height, picture->linesize[0], picture->linesize[1], iWidth);	
/*
		for(i=0; i<c->height; i++)
			fwrite(picture->data[0] + i * picture->linesize[0], 1, c->width, outf);

		for(i=0; i<c->height/2; i++)
			fwrite(picture->data[1] + i * picture->linesize[1], 1, c->width/2, outf);

		for(i=0; i<c->height/2; i++)
			fwrite(picture->data[2] + i * picture->linesize[2], 1, c->width/2, outf);
// */
	}
	
    (*env)->ReleaseByteArrayElements(env, in, Buf, 0);    
    (*env)->ReleaseByteArrayElements(env, out, Pixel, 0); 

	return consumed_bytes;	
}
