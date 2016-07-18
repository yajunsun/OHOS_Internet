
#include <string.h>

#include "common.h"
#include "avcodec.h"
#include "dsputil.h"

void put_pixels4_c(uint8_t *block, const uint8_t *pixels, int line_size, int h)
{
    int i;
    for(i=0; i<h; i++)
	{
		memcpy((block), (pixels), sizeof(pixels));
        pixels+=line_size;
        block +=line_size;
    }
}

void put_pixels8_c(uint8_t *block, const uint8_t *pixels, int line_size, int h)
{
    int i;
    for(i=0; i<h; i++)
	{
        memcpy((block  ), (pixels  ),sizeof(pixels));
        memcpy((block+4), (pixels+4),sizeof(pixels));
        pixels+=line_size;
        block +=line_size;
    }
}

void put_pixels16_c(uint8_t *block, const uint8_t *pixels, int line_size, int h)
{
	put_pixels8_c(block , pixels , line_size, h);
	put_pixels8_c(block+8, pixels+8, line_size, h);
}

void put_pixels4_l2(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int dst_stride, int src_stride1, int src_stride2, int h)
{
    int i;
	uint32_t a,b,c;
    for(i=0; i<h; i++)
	{
		memcpy(&a,&src1[i*src_stride1  ],sizeof(a));
		memcpy(&b,&src2[i*src_stride2  ],sizeof(b));
		c=rnd_avg32(a, b);
		memcpy(&dst[i*dst_stride  ],&c,sizeof(c));
    }
}

void put_pixels8_l2(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int dst_stride, int src_stride1, int src_stride2, int h)
{
    int i;
	uint32_t a,b,c;
    for(i=0; i<h; i++)
	{
		memcpy(&a,&src1[i*src_stride1  ],sizeof(a));
		memcpy(&b,&src2[i*src_stride2  ],sizeof(b));
		c=rnd_avg32(a, b);
		memcpy(&dst[i*dst_stride  ],&c,sizeof(c));

		memcpy(&a,&src1[i*src_stride1+4],sizeof(a));
		memcpy(&b,&src2[i*src_stride2+4],sizeof(b));
		c=rnd_avg32(a, b);
		memcpy(&dst[i*dst_stride+4],&c,sizeof(c));
    }
}

void put_pixels16_l2(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int dst_stride, int src_stride1, int src_stride2, int h)
{
	put_pixels8_l2(dst , src1 , src2 , dst_stride, src_stride1, src_stride2, h);
	put_pixels8_l2(dst+8, src1+8, src2+8, dst_stride, src_stride1, src_stride2, h);
}

void avg_pixels2_c(uint8_t *block, const uint8_t *pixels, int line_size, int h)
{
	int i;
	for(i=0; i<h; i++)
	{
		*((uint16_t*)(block )) = rnd_avg32(*((uint16_t*)(block )), ( *(pixels) | ((uint16_t)*(pixels+1) << 8) ));
		pixels+=line_size;
		block +=line_size;
	}
}

void avg_pixels4_c(uint8_t *block, const uint8_t *pixels, int line_size, int h)
{
    int i;
    for(i=0; i<h; i++)
	{
        *((uint32_t*)(block )) = rnd_avg32(*((uint32_t*)(block )), ( ((*(pixels))) | ((*(pixels+1))<< 8) | ((*(pixels+2))<< 16) | (*(pixels+3) <<24) ));
        pixels+=line_size;
        block +=line_size;
    }
}

void avg_pixels8_c(uint8_t *block, const uint8_t *pixels, int line_size, int h)
{
    int i;
    for(i=0; i<h; i++)
	{
        *((uint32_t*)(block )) = rnd_avg32(*((uint32_t*)(block )), ( ((*(pixels))) | ((*(pixels+1))<< 8) | ((*(pixels+2))<< 16) | (*(pixels+3) <<24) ));
        *((uint32_t*)(block+4)) = rnd_avg32(*((uint32_t*)(block+4)), ( ((*(pixels+4))) | ((*(pixels+4+1))<< 8) | ((*(pixels+4+2))<< 16) | (*(pixels+4+3) <<24) ));
        pixels+=line_size;
        block +=line_size;
    }
}

void avg_pixels16_c(uint8_t *block, const uint8_t *pixels, int line_size, int h)
{
	avg_pixels8_c(block , pixels , line_size, h);
	avg_pixels8_c(block+8, pixels+8, line_size, h);
}

void avg_pixels2_l2(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int dst_stride, int src_stride1, int src_stride2, int h)
{
	int i;
	for(i=0; i<h; i++)
	{
		uint32_t a,b;
		a= ( *(&src1[i*src_stride1 ]) | ((uint16_t)*(&src1[i*src_stride1 ]+1) << 8) );
		b= ( *(&src2[i*src_stride2 ]) | ((uint16_t)*(&src2[i*src_stride2 ]+1) << 8) );
		*((uint16_t*)&dst[i*dst_stride ]) = rnd_avg32(*((uint16_t*)&dst[i*dst_stride ]), rnd_avg32(a, b));
	}
}

void avg_pixels4_l2(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int dst_stride, int src_stride1, int src_stride2, int h)
{
    int i;
    for(i=0; i<h; i++)
	{
        uint32_t a,b;
        a= ( ((*(&src1[i*src_stride1 ]))) | ((*(&src1[i*src_stride1 ]+1))<< 8) | ((*(&src1[i*src_stride1 ]+2))<< 16) | (*(&src1[i*src_stride1 ]+3) <<24) );
        b= ( ((*(&src2[i*src_stride2 ]))) | ((*(&src2[i*src_stride2 ]+1))<< 8) | ((*(&src2[i*src_stride2 ]+2))<< 16) | (*(&src2[i*src_stride2 ]+3) <<24) );
        *((uint32_t*)&dst[i*dst_stride ]) = rnd_avg32(*((uint32_t*)&dst[i*dst_stride ]), rnd_avg32(a, b));
    }
}

void avg_pixels8_l2(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int dst_stride, int src_stride1, int src_stride2, int h)
{
    int i;
    for(i=0; i<h; i++)
	{
        uint32_t a,b;
        a= ( ((*(&src1[i*src_stride1 ]))) | ((*(&src1[i*src_stride1 ]+1))<< 8) | ((*(&src1[i*src_stride1 ]+2))<< 16) | (*(&src1[i*src_stride1 ]+3) <<24) );
        b= ( ((*(&src2[i*src_stride2 ]))) | ((*(&src2[i*src_stride2 ]+1))<< 8) | ((*(&src2[i*src_stride2 ]+2))<< 16) | (*(&src2[i*src_stride2 ]+3) <<24) );
        *((uint32_t*)&dst[i*dst_stride ]) = rnd_avg32(*((uint32_t*)&dst[i*dst_stride ]), rnd_avg32(a, b));
        a= ( ((*(&src1[i*src_stride1+4]))) | ((*(&src1[i*src_stride1+4]+1))<< 8) | ((*(&src1[i*src_stride1+4]+2))<< 16) | (*(&src1[i*src_stride1+4]+3) <<24) );
        b= ( ((*(&src2[i*src_stride2+4]))) | ((*(&src2[i*src_stride2+4]+1))<< 8) | ((*(&src2[i*src_stride2+4]+2))<< 16) | (*(&src2[i*src_stride2+4]+3) <<24) );
        *((uint32_t*)&dst[i*dst_stride+4]) = rnd_avg32(*((uint32_t*)&dst[i*dst_stride+4]), rnd_avg32(a, b));
    }
}

void avg_pixels16_l2(uint8_t *dst, const uint8_t *src1, const uint8_t *src2, int dst_stride, int src_stride1, int src_stride2, int h)
{
	avg_pixels8_l2(dst , src1 , src2 , dst_stride, src_stride1, src_stride2, h);
	avg_pixels8_l2(dst+8, src1+8, src2+8, dst_stride, src_stride1, src_stride2, h);
}

static void put_h264_chroma_mc2_c(uint8_t *dst, uint8_t *src, int stride, int h, int x, int y)
{
    const int A=(8-x)*(8-y);
    const int B=(  x)*(8-y);
    const int C=(8-x)*(  y);
    const int D=(  x)*(  y);
    int i;

    for(i=0; i<h; i++)
    {
        dst[0] = (A*src[0] + B*src[1] + C*src[stride+0] + D*src[stride+1] + 32) >> 6;
        dst[1] = (A*src[1] + B*src[2] + C*src[stride+1] + D*src[stride+2] + 32) >> 6;
        dst+= stride;
        src+= stride;
    }
}

static void put_h264_chroma_mc4_c(uint8_t *dst, uint8_t *src, int stride, int h, int x, int y)
{
    const int A=(8-x)*(8-y);
    const int B=(  x)*(8-y);
    const int C=(8-x)*(  y);
    const int D=(  x)*(  y);
    int i;

    for(i=0; i<h; i++)
    {
        dst[0] = (A*src[0] + B*src[1] + C*src[stride+0] + D*src[stride+1] + 32) >>6;
        dst[1] = (A*src[1] + B*src[2] + C*src[stride+1] + D*src[stride+2] + 32) >>6;
        dst[2] = (A*src[2] + B*src[3] + C*src[stride+2] + D*src[stride+3] + 32) >>6;
        dst[3] = (A*src[3] + B*src[4] + C*src[stride+3] + D*src[stride+4] + 32) >>6;
        dst+= stride;
        src+= stride;
    }
}

static void put_h264_chroma_mc8_c(uint8_t *dst, uint8_t *src, int stride, int h, int x, int y)
{
    const int A=(8-x)*(8-y);
    const int B=(  x)*(8-y);
    const int C=(8-x)*(  y);
    const int D=(  x)*(  y);
    int i;

    for(i=0; i<h; i++)
    {
        dst[0] = (A*src[0] + B*src[1] + C*src[stride+0] + D*src[stride+1] + 32) >>6;
        dst[1] = (A*src[1] + B*src[2] + C*src[stride+1] + D*src[stride+2] + 32) >>6;
        dst[2] = (A*src[2] + B*src[3] + C*src[stride+2] + D*src[stride+3] + 32) >>6;
        dst[3] = (A*src[3] + B*src[4] + C*src[stride+3] + D*src[stride+4] + 32) >>6;
        dst[4] = (A*src[4] + B*src[5] + C*src[stride+4] + D*src[stride+5] + 32) >>6;
        dst[5] = (A*src[5] + B*src[6] + C*src[stride+5] + D*src[stride+6] + 32) >>6;
        dst[6] = (A*src[6] + B*src[7] + C*src[stride+6] + D*src[stride+7] + 32) >>6;
        dst[7] = (A*src[7] + B*src[8] + C*src[stride+7] + D*src[stride+8] + 32) >>6;
        dst+= stride;
        src+= stride;
    }
}

static void avg_h264_chroma_mc2_c(uint8_t *dst, uint8_t *src, int stride, int h, int x, int y)
{
    const int A=(8-x)*(8-y);
    const int B=(  x)*(8-y);
    const int C=(8-x)*(  y);
    const int D=(  x)*(  y);
    int i;

    for(i=0; i<h; i++)
    {
        dst[0] = (dst[0] + ((A*src[0] + B*src[1] + C*src[stride+0] + D*src[stride+1] + 32)>>6)+1)>>1;
        dst[1] = (dst[1] + ((A*src[1] + B*src[2] + C*src[stride+1] + D*src[stride+2] + 32)>>6)+1)>>1;
        dst+= stride;
        src+= stride;
    }
}

static void avg_h264_chroma_mc4_c(uint8_t *dst, uint8_t *src, int stride, int h, int x, int y)
{
    const int A=(8-x)*(8-y);
    const int B=(  x)*(8-y);
    const int C=(8-x)*(  y);
    const int D=(  x)*(  y);
    int i;

    for(i=0; i<h; i++)
    {
        dst[0] = (dst[0] + ((A*src[0] + B*src[1] + C*src[stride+0] + D*src[stride+1] + 32)>>6)+1)>>1;
        dst[1] = (dst[1] + ((A*src[1] + B*src[2] + C*src[stride+1] + D*src[stride+2] + 32)>>6)+1)>>1;
        dst[2] = (dst[2] + ((A*src[2] + B*src[3] + C*src[stride+2] + D*src[stride+3] + 32)>>6)+1)>>1;
        dst[3] = (dst[3] + ((A*src[3] + B*src[4] + C*src[stride+3] + D*src[stride+4] + 32)>>6)+1)>>1;
        dst+= stride;
        src+= stride;
    }
}

static void avg_h264_chroma_mc8_c(uint8_t *dst, uint8_t *src, int stride, int h, int x, int y)
{
    const int A=(8-x)*(8-y);
    const int B=(  x)*(8-y);
    const int C=(8-x)*(  y);
    const int D=(  x)*(  y);
    int i;

    for(i=0; i<h; i++)
    {
        dst[0] = (dst[0] + ((A*src[0] + B*src[1] + C*src[stride+0] + D*src[stride+1] + 32)>>6)+1)>>1;
        dst[1] = (dst[1] + ((A*src[1] + B*src[2] + C*src[stride+1] + D*src[stride+2] + 32)>>6)+1)>>1;
        dst[2] = (dst[2] + ((A*src[2] + B*src[3] + C*src[stride+2] + D*src[stride+3] + 32)>>6)+1)>>1;
        dst[3] = (dst[3] + ((A*src[3] + B*src[4] + C*src[stride+3] + D*src[stride+4] + 32)>>6)+1)>>1;
        dst[4] = (dst[4] + ((A*src[4] + B*src[5] + C*src[stride+4] + D*src[stride+5] + 32)>>6)+1)>>1;
        dst[5] = (dst[5] + ((A*src[5] + B*src[6] + C*src[stride+5] + D*src[stride+6] + 32)>>6)+1)>>1;
        dst[6] = (dst[6] + ((A*src[6] + B*src[7] + C*src[stride+6] + D*src[stride+7] + 32)>>6)+1)>>1;
        dst[7] = (dst[7] + ((A*src[7] + B*src[8] + C*src[stride+7] + D*src[stride+8] + 32)>>6)+1)>>1;
        dst+= stride;
        src+= stride;
    }
}

static __inline void copy_block4(uint8_t *dst, uint8_t *src, int dstStride, int srcStride, int h)
{
    int i;
    for(i=0; i<h; i++)
    {
		memcpy(dst,src,sizeof(src));
        dst+=dstStride;
        src+=srcStride;
    }
}

static __inline void copy_block8(uint8_t *dst, uint8_t *src, int dstStride, int srcStride, int h)
{
    int i;
    for(i=0; i<h; i++)
    {
		memcpy(dst,src,sizeof(src));
		memcpy(dst+4,src+4,sizeof(src));

        dst+=dstStride;
        src+=srcStride;
    }
}

static __inline void copy_block16(uint8_t *dst, uint8_t *src, int dstStride, int srcStride, int h)
{
    int i;
    for(i=0; i<h; i++)
    {
		memcpy(dst,src,sizeof(src));
		memcpy(dst+4,src+4,sizeof(src));
		memcpy(dst+8,src+8,sizeof(src));
		memcpy(dst+12,src+12,sizeof(src));

        dst+=dstStride;
        src+=srcStride;
    }
}

static void put_h264_qpel4_h_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride)
{
    const int h=4;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
    int i;
    for(i=0; i<h; i++)
    {
        dst[0] = cm[((src[0]+src[1])*20 - (src[-1]+src[2])*5 + (src[-2]+src[3])+16)>>5];
        dst[1] = cm[((src[1]+src[2])*20 - (src[0 ]+src[3])*5 + (src[-1]+src[4])+16)>>5];
        dst[2] = cm[((src[2]+src[3])*20 - (src[1 ]+src[4])*5 + (src[0 ]+src[5])+16)>>5];
        dst[3] = cm[((src[3]+src[4])*20 - (src[2 ]+src[5])*5 + (src[1 ]+src[6])+16)>>5];
        dst+=dstStride;
        src+=srcStride;
    }
}

static void put_h264_qpel4_v_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride)
{
    const int w=4;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
    int i;
    for(i=0; i<w; i++)
    {
        const int srcB= src[-2*srcStride];
        const int srcA= src[-1*srcStride];
        const int src0= src[0 *srcStride];
        const int src1= src[1 *srcStride];
        const int src2= src[2 *srcStride];
        const int src3= src[3 *srcStride];
        const int src4= src[4 *srcStride];
        const int src5= src[5 *srcStride];
        const int src6= src[6 *srcStride];
        dst[0*dstStride] = cm[((src0+src1)*20 - (srcA+src2)*5 + (srcB+src3)+16)>>5];
        dst[1*dstStride] = cm[((src1+src2)*20 - (src0+src3)*5 + (srcA+src4)+16)>>5];
        dst[2*dstStride] = cm[((src2+src3)*20 - (src1+src4)*5 + (src0+src5)+16)>>5];
        dst[3*dstStride] = cm[((src3+src4)*20 - (src2+src5)*5 + (src1+src6)+16)>>5];
        dst++;
        src++;
    }
}

static void put_h264_qpel4_hv_lowpass(uint8_t *dst, int16_t *tmp, uint8_t *src, int dstStride, int tmpStride, int srcStride)
{
    const int h=4;
    const int w=4;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
    int i;
    src -= 2*srcStride;
    for(i=0; i<h+5; i++)
    {
        tmp[0]= (src[0]+src[1])*20 - (src[-1]+src[2])*5 + (src[-2]+src[3]);
        tmp[1]= (src[1]+src[2])*20 - (src[0 ]+src[3])*5 + (src[-1]+src[4]);
        tmp[2]= (src[2]+src[3])*20 - (src[1 ]+src[4])*5 + (src[0 ]+src[5]);
        tmp[3]= (src[3]+src[4])*20 - (src[2 ]+src[5])*5 + (src[1 ]+src[6]);
        tmp+=tmpStride;
        src+=srcStride;
    }
    tmp -= tmpStride*(h+5-2);
    for(i=0; i<w; i++)
    {
        const int tmpB= tmp[-2*tmpStride];
        const int tmpA= tmp[-1*tmpStride];
        const int tmp0= tmp[0 *tmpStride];
        const int tmp1= tmp[1 *tmpStride];
        const int tmp2= tmp[2 *tmpStride];
        const int tmp3= tmp[3 *tmpStride];
        const int tmp4= tmp[4 *tmpStride];
        const int tmp5= tmp[5 *tmpStride];
        const int tmp6= tmp[6 *tmpStride];
        dst[0*dstStride] = cm[((tmp0+tmp1)*20 - (tmpA+tmp2)*5 + (tmpB+tmp3)+512)>>10];
        dst[1*dstStride] = cm[((tmp1+tmp2)*20 - (tmp0+tmp3)*5 + (tmpA+tmp4)+512)>>10];
        dst[2*dstStride] = cm[((tmp2+tmp3)*20 - (tmp1+tmp4)*5 + (tmp0+tmp5)+512)>>10];
        dst[3*dstStride] = cm[((tmp3+tmp4)*20 - (tmp2+tmp5)*5 + (tmp1+tmp6)+512)>>10];
        dst++;
        tmp++;
    }
}

static void put_h264_qpel8_h_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride)
{
    const int h=8;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
    int i;
    for(i=0; i<h; i++)
    {
        dst[0] = cm[((src[0]+src[1])*20 - (src[-1]+src[2])*5 + (src[-2]+src[3 ]) + 16)>>5];
        dst[1] = cm[((src[1]+src[2])*20 - (src[0 ]+src[3])*5 + (src[-1]+src[4 ]) + 16)>>5];
        dst[2] = cm[((src[2]+src[3])*20 - (src[1 ]+src[4])*5 + (src[0 ]+src[5 ]) + 16)>>5];
        dst[3] = cm[((src[3]+src[4])*20 - (src[2 ]+src[5])*5 + (src[1 ]+src[6 ]) + 16)>>5];
        dst[4] = cm[((src[4]+src[5])*20 - (src[3 ]+src[6])*5 + (src[2 ]+src[7 ]) + 16)>>5];
        dst[5] = cm[((src[5]+src[6])*20 - (src[4 ]+src[7])*5 + (src[3 ]+src[8 ]) + 16)>>5];
        dst[6] = cm[((src[6]+src[7])*20 - (src[5 ]+src[8])*5 + (src[4 ]+src[9 ]) + 16)>>5];
        dst[7] = cm[((src[7]+src[8])*20 - (src[6 ]+src[9])*5 + (src[5 ]+src[10]) + 16)>>5];
        dst+=dstStride;
        src+=srcStride;
    }
}

static void put_h264_qpel8_v_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride)
{
    const int w = 8;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
    char i;
    int srcB;
    int srcA;
    int src0;
    int src1;
    //int src[2 *srcStride];
    int src3;
    //int src[4 *srcStride];
    int src5;
    int src6;
    int src7;
    //int src[8 *srcStride];
    int src9;
    int src10;
    for (i = 0; i < w; i++)
    {
        srcB = src[ - 2 * srcStride];
        srcA = src[ - 1 * srcStride];
        src0 = src[0 *srcStride];
        src1 = src[1 *srcStride];
        //src[2 *srcStride]= src[2 *srcStride];
        src3 = src[3 *srcStride];
        //src[4 *srcStride]= src[4 *srcStride];
        src5 = src[5 *srcStride];
        src6 = src[6 *srcStride];
        src7 = src[7 *srcStride];
        //src[8 *srcStride]= src[8 *srcStride];
        src9 = src[9 *srcStride];
        src10 = src[10 *srcStride];

        dst[0 *dstStride] = cm[(((src0 + src1) *20-(srcA + src[2 *srcStride]) *5+(srcB + src3)) + 16) >> 5];
        dst[1 *dstStride] = cm[(((src1 + src[2 *srcStride]) *20-(src0 + src3) *5+(srcA + src[4 *srcStride])) + 16) >> 5];
        dst[2 *dstStride] = cm[(((src[2 *srcStride] + src3) *20-(src1 + src[4 *srcStride]) *5+(src0 + src5)) + 16) >> 5];
        dst[3 *dstStride] = cm[(((src3 + src[4 *srcStride]) *20-(src[2 *srcStride] + src5) *5+(src1 + src6)) + 16) >> 5];
        dst[4 *dstStride] = cm[(((src[4 *srcStride] + src5) *20-(src3 + src6) *5+(src[2 *srcStride] + src7)) + 16) >> 5];
        dst[5 *dstStride] = cm[(((src5 + src6) *20-(src[4 *srcStride] + src7) *5+(src3 + src[8 *srcStride])) + 16) >> 5];
        dst[6 *dstStride] = cm[(((src6 + src7) *20-(src5 + src[8 *srcStride]) *5+(src[4 *srcStride] + src9)) + 16) >> 5];
        dst[7 *dstStride] = cm[(((src7 + src[8 *srcStride]) *20-(src6 + src9) *5+(src5 + src10)) + 16) >> 5];

        dst++;
        src++;
    }
}
/*
static void put_h264_qpel8_v_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride)
{
    const int w=8;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
    int i;
    for(i=0; i<w; i++)
    {
        const int srcB= src[-2*srcStride];
        const int srcA= src[-1*srcStride];
        const int src0= src[0 *srcStride];
        const int src1= src[1 *srcStride];
        const int src2= src[2 *srcStride];
        const int src3= src[3 *srcStride];
        const int src4= src[4 *srcStride];
        const int src5= src[5 *srcStride];
        const int src6= src[6 *srcStride];
        const int src7= src[7 *srcStride];
        const int src8= src[8 *srcStride];
        const int src9= src[9 *srcStride];
        const int src10=src[10*srcStride];
        dst[0*dstStride] = cm[((src0+src1)*20 - (srcA+src2)*5 + (srcB+src3)+16)>>5];
        dst[1*dstStride] = cm[((src1+src2)*20 - (src0+src3)*5 + (srcA+src4)+16)>>5];
        dst[2*dstStride] = cm[((src2+src3)*20 - (src1+src4)*5 + (src0+src5)+16)>>5];
        dst[3*dstStride] = cm[((src3+src4)*20 - (src2+src5)*5 + (src1+src6)+16)>>5];
        dst[4*dstStride] = cm[((src4+src5)*20 - (src3+src6)*5 + (src2+src7)+16)>>5];
        dst[5*dstStride] = cm[((src5+src6)*20 - (src4+src7)*5 + (src3+src8)+16)>>5];
        dst[6*dstStride] = cm[((src6+src7)*20 - (src5+src8)*5 + (src4+src9)+16)>>5];
        dst[7*dstStride] = cm[((src7+src8)*20 - (src6+src9)*5 + (src5+src10)+16)>>5];
        dst++;
        src++;
    }
}
// */

static void put_h264_qpel8_hv_lowpass(uint8_t *dst, int16_t *tmp, uint8_t *src, int dstStride, int tmpStride, int srcStride)
{
    const int h=8;
    const int w=8;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
    int i;
    src -= 2*srcStride;
    for(i=0; i<h+5; i++)
    {
        tmp[0]= (src[0]+src[1])*20 - (src[-1]+src[2])*5 + (src[-2]+src[3 ]);
        tmp[1]= (src[1]+src[2])*20 - (src[0 ]+src[3])*5 + (src[-1]+src[4 ]);
        tmp[2]= (src[2]+src[3])*20 - (src[1 ]+src[4])*5 + (src[0 ]+src[5 ]);
        tmp[3]= (src[3]+src[4])*20 - (src[2 ]+src[5])*5 + (src[1 ]+src[6 ]);
        tmp[4]= (src[4]+src[5])*20 - (src[3 ]+src[6])*5 + (src[2 ]+src[7 ]);
        tmp[5]= (src[5]+src[6])*20 - (src[4 ]+src[7])*5 + (src[3 ]+src[8 ]);
        tmp[6]= (src[6]+src[7])*20 - (src[5 ]+src[8])*5 + (src[4 ]+src[9 ]);
        tmp[7]= (src[7]+src[8])*20 - (src[6 ]+src[9])*5 + (src[5 ]+src[10]);
        tmp+=tmpStride;
        src+=srcStride;
    }
    tmp -= tmpStride*(h+5-2);
    for(i=0; i<w; i++)
    {
        const int tmpB= tmp[-2*tmpStride];
        const int tmpA= tmp[-1*tmpStride];
        const int tmp0= tmp[0 *tmpStride];
        const int tmp1= tmp[1 *tmpStride];
        const int tmp2= tmp[2 *tmpStride];
        const int tmp3= tmp[3 *tmpStride];
        const int tmp4= tmp[4 *tmpStride];
        const int tmp5= tmp[5 *tmpStride];
        const int tmp6= tmp[6 *tmpStride];
        const int tmp7= tmp[7 *tmpStride];
        const int tmp8= tmp[8 *tmpStride];
        const int tmp9= tmp[9 *tmpStride];
        const int tmp10=tmp[10*tmpStride];
        dst[0*dstStride] = cm[((tmp0+tmp1)*20 - (tmpA+tmp2)*5 + (tmpB+tmp3)+512)>>10];
        dst[1*dstStride] = cm[((tmp1+tmp2)*20 - (tmp0+tmp3)*5 + (tmpA+tmp4)+512)>>10];
        dst[2*dstStride] = cm[((tmp2+tmp3)*20 - (tmp1+tmp4)*5 + (tmp0+tmp5)+512)>>10];
        dst[3*dstStride] = cm[((tmp3+tmp4)*20 - (tmp2+tmp5)*5 + (tmp1+tmp6)+512)>>10];
        dst[4*dstStride] = cm[((tmp4+tmp5)*20 - (tmp3+tmp6)*5 + (tmp2+tmp7)+512)>>10];
        dst[5*dstStride] = cm[((tmp5+tmp6)*20 - (tmp4+tmp7)*5 + (tmp3+tmp8)+512)>>10];
        dst[6*dstStride] = cm[((tmp6+tmp7)*20 - (tmp5+tmp8)*5 + (tmp4+tmp9)+512)>>10];
        dst[7*dstStride] = cm[((tmp7+tmp8)*20 - (tmp6+tmp9)*5 + (tmp5+tmp10)+512)>>10];
        dst++;
        tmp++;
    }
}

static void put_h264_qpel16_h_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride)
{
    put_h264_qpel8_h_lowpass(dst  , src  , dstStride, srcStride);
    put_h264_qpel8_h_lowpass(dst+8, src+8, dstStride, srcStride);
    src += 8*srcStride;
    dst += 8*dstStride;
    put_h264_qpel8_h_lowpass(dst  , src  , dstStride, srcStride);
    put_h264_qpel8_h_lowpass(dst+8, src+8, dstStride, srcStride);
}

static void put_h264_qpel16_v_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride)
{
    put_h264_qpel8_v_lowpass(dst  , src  , dstStride, srcStride);
    put_h264_qpel8_v_lowpass(dst+8, src+8, dstStride, srcStride);
    src += 8*srcStride;
    dst += 8*dstStride;
    put_h264_qpel8_v_lowpass(dst  , src  , dstStride, srcStride);
    put_h264_qpel8_v_lowpass(dst+8, src+8, dstStride, srcStride);
}

static void put_h264_qpel16_hv_lowpass(uint8_t *dst, int16_t *tmp, uint8_t *src, int dstStride, int tmpStride, int srcStride)
{
    put_h264_qpel8_hv_lowpass(dst  , tmp  , src  , dstStride, tmpStride, srcStride);
    put_h264_qpel8_hv_lowpass(dst+8, tmp+8, src+8, dstStride, tmpStride, srcStride);
    src += 8*srcStride;
    tmp += 8*tmpStride; // 20080901
    dst += 8*dstStride;
    put_h264_qpel8_hv_lowpass(dst  , tmp  , src  , dstStride, tmpStride, srcStride);
    put_h264_qpel8_hv_lowpass(dst+8, tmp+8, src+8, dstStride, tmpStride, srcStride);
}

static void avg_h264_qpel4_h_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride)
{
    const int h=4;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
    int i;
    for(i=0; i<h; i++)
    {
        dst[0] = (dst[0] + cm[((src[0]+src[1])*20 - (src[-1]+src[2])*5 + (src[-2]+src[3])+16)>>5]+1)>>1;
        dst[1] = (dst[1] + cm[((src[1]+src[2])*20 - (src[0 ]+src[3])*5 + (src[-1]+src[4])+16)>>5]+1)>>1;
        dst[2] = (dst[2] + cm[((src[2]+src[3])*20 - (src[1 ]+src[4])*5 + (src[0 ]+src[5])+16)>>5]+1)>>1;
        dst[3] = (dst[3] + cm[((src[3]+src[4])*20 - (src[2 ]+src[5])*5 + (src[1 ]+src[6])+16)>>5]+1)>>1;
        dst+=dstStride;
        src+=srcStride;
    }
}

static void avg_h264_qpel4_v_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride)
{
    const int w=4;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
    int i;
    for(i=0; i<w; i++)
    {
        const int srcB= src[-2*srcStride];
        const int srcA= src[-1*srcStride];
        const int src0= src[0 *srcStride];
        const int src1= src[1 *srcStride];
        const int src2= src[2 *srcStride];
        const int src3= src[3 *srcStride];
        const int src4= src[4 *srcStride];
        const int src5= src[5 *srcStride];
        const int src6= src[6 *srcStride];
        dst[0*dstStride] = (dst[0*dstStride] + cm[((src0+src1)*20 - (srcA+src2)*5 + (srcB+src3)+16)>>5]+1)>>1;
        dst[1*dstStride] = (dst[1*dstStride] + cm[((src1+src2)*20 - (src0+src3)*5 + (srcA+src4)+16)>>5]+1)>>1;
        dst[2*dstStride] = (dst[2*dstStride] + cm[((src2+src3)*20 - (src1+src4)*5 + (src0+src5)+16)>>5]+1)>>1;
        dst[3*dstStride] = (dst[3*dstStride] + cm[((src3+src4)*20 - (src2+src5)*5 + (src1+src6)+16)>>5]+1)>>1;
        dst++;
        src++;
    }
}

static void avg_h264_qpel4_hv_lowpass(uint8_t *dst, int16_t *tmp, uint8_t *src, int dstStride, int tmpStride, int srcStride)
{
    const int h=4;
    const int w=4;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
    int i;
    src -= 2*srcStride;
    for(i=0; i<h+5; i++)
    {
        tmp[0]= (src[0]+src[1])*20 - (src[-1]+src[2])*5 + (src[-2]+src[3]);
        tmp[1]= (src[1]+src[2])*20 - (src[0 ]+src[3])*5 + (src[-1]+src[4]);
        tmp[2]= (src[2]+src[3])*20 - (src[1 ]+src[4])*5 + (src[0 ]+src[5]);
        tmp[3]= (src[3]+src[4])*20 - (src[2 ]+src[5])*5 + (src[1 ]+src[6]);
        tmp+=tmpStride;
        src+=srcStride;
    }
    tmp -= tmpStride*(h+5-2);
    for(i=0; i<w; i++)
    {
        const int tmpB= tmp[-2*tmpStride];
        const int tmpA= tmp[-1*tmpStride];
        const int tmp0= tmp[0 *tmpStride];
        const int tmp1= tmp[1 *tmpStride];
        const int tmp2= tmp[2 *tmpStride];
        const int tmp3= tmp[3 *tmpStride];
        const int tmp4= tmp[4 *tmpStride];
        const int tmp5= tmp[5 *tmpStride];
        const int tmp6= tmp[6 *tmpStride];
        dst[0*dstStride] = (dst[0*dstStride] + cm[((tmp0+tmp1)*20 - (tmpA+tmp2)*5 + (tmpB+tmp3)+512)>>10]+1)>>1;
        dst[1*dstStride] = (dst[1*dstStride] + cm[((tmp1+tmp2)*20 - (tmp0+tmp3)*5 + (tmpA+tmp4)+512)>>10]+1)>>1;
        dst[2*dstStride] = (dst[2*dstStride] + cm[((tmp2+tmp3)*20 - (tmp1+tmp4)*5 + (tmp0+tmp5)+512)>>10]+1)>>1;
        dst[3*dstStride] = (dst[3*dstStride] + cm[((tmp3+tmp4)*20 - (tmp2+tmp5)*5 + (tmp1+tmp6)+512)>>10]+1)>>1;

        dst++;
        tmp++;
    }
}

static void avg_h264_qpel8_h_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride)
{
    const int h=8;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
    int i;
    for(i=0; i<h; i++)
    {
        dst[0] = (dst[0] + cm[((src[0]+src[1])*20 - (src[-1]+src[2])*5 + (src[-2]+src[3 ])+16)>>5]+1)>>1;
        dst[1] = (dst[1] + cm[((src[1]+src[2])*20 - (src[0 ]+src[3])*5 + (src[-1]+src[4 ])+16)>>5]+1)>>1;
        dst[2] = (dst[2] + cm[((src[2]+src[3])*20 - (src[1 ]+src[4])*5 + (src[0 ]+src[5 ])+16)>>5]+1)>>1;
        dst[3] = (dst[3] + cm[((src[3]+src[4])*20 - (src[2 ]+src[5])*5 + (src[1 ]+src[6 ])+16)>>5]+1)>>1;
        dst[4] = (dst[4] + cm[((src[4]+src[5])*20 - (src[3 ]+src[6])*5 + (src[2 ]+src[7 ])+16)>>5]+1)>>1;
        dst[5] = (dst[5] + cm[((src[5]+src[6])*20 - (src[4 ]+src[7])*5 + (src[3 ]+src[8 ])+16)>>5]+1)>>1;
        dst[6] = (dst[6] + cm[((src[6]+src[7])*20 - (src[5 ]+src[8])*5 + (src[4 ]+src[9 ])+16)>>5]+1)>>1;
        dst[7] = (dst[7] + cm[((src[7]+src[8])*20 - (src[6 ]+src[9])*5 + (src[5 ]+src[10])+16)>>5]+1)>>1;
        dst+=dstStride;
        src+=srcStride;
    }
}

static void avg_h264_qpel8_v_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride)
{
    const int w=8;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
    int i;
    for(i=0; i<w; i++)
    {
        const int srcB= src[-2*srcStride];
        const int srcA= src[-1*srcStride];
        const int src0= src[0 *srcStride];
        const int src1= src[1 *srcStride];
        const int src2= src[2 *srcStride];
        const int src3= src[3 *srcStride];
        const int src4= src[4 *srcStride];
        const int src5= src[5 *srcStride];
        const int src6= src[6 *srcStride];
        const int src7= src[7 *srcStride];
        const int src8= src[8 *srcStride];
        const int src9= src[9 *srcStride];
        const int src10=src[10*srcStride];
        dst[0*dstStride] = (dst[0*dstStride] + cm[((src0+src1)*20 - (srcA+src2)*5 + (srcB+src3)+16)>>5]+1)>>1;
        dst[1*dstStride] = (dst[1*dstStride] + cm[((src1+src2)*20 - (src0+src3)*5 + (srcA+src4)+16)>>5]+1)>>1;
        dst[2*dstStride] = (dst[2*dstStride] + cm[((src2+src3)*20 - (src1+src4)*5 + (src0+src5)+16)>>5]+1)>>1;
        dst[3*dstStride] = (dst[3*dstStride] + cm[((src3+src4)*20 - (src2+src5)*5 + (src1+src6)+16)>>5]+1)>>1;
        dst[4*dstStride] = (dst[4*dstStride] + cm[((src4+src5)*20 - (src3+src6)*5 + (src2+src7)+16)>>5]+1)>>1;
        dst[5*dstStride] = (dst[5*dstStride] + cm[((src5+src6)*20 - (src4+src7)*5 + (src3+src8)+16)>>5]+1)>>1;
        dst[6*dstStride] = (dst[6*dstStride] + cm[((src6+src7)*20 - (src5+src8)*5 + (src4+src9)+16)>>5]+1)>>1;
        dst[7*dstStride] = (dst[7*dstStride] + cm[((src7+src8)*20 - (src6+src9)*5 + (src5+src10)+16)>>5]+1)>>1;
        dst++;
        src++;
    }
}

static void avg_h264_qpel8_hv_lowpass(uint8_t *dst, int16_t *tmp, uint8_t *src, int dstStride, int tmpStride, int srcStride)
{
    const int h=8;
    const int w=8;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
    int i;
    src -= 2*srcStride;
    for(i=0; i<h+5; i++)
    {
        tmp[0]= (src[0]+src[1])*20 - (src[-1]+src[2])*5 + (src[-2]+src[3 ]);
        tmp[1]= (src[1]+src[2])*20 - (src[0 ]+src[3])*5 + (src[-1]+src[4 ]);
        tmp[2]= (src[2]+src[3])*20 - (src[1 ]+src[4])*5 + (src[0 ]+src[5 ]);
        tmp[3]= (src[3]+src[4])*20 - (src[2 ]+src[5])*5 + (src[1 ]+src[6 ]);
        tmp[4]= (src[4]+src[5])*20 - (src[3 ]+src[6])*5 + (src[2 ]+src[7 ]);
        tmp[5]= (src[5]+src[6])*20 - (src[4 ]+src[7])*5 + (src[3 ]+src[8 ]);
        tmp[6]= (src[6]+src[7])*20 - (src[5 ]+src[8])*5 + (src[4 ]+src[9 ]);
        tmp[7]= (src[7]+src[8])*20 - (src[6 ]+src[9])*5 + (src[5 ]+src[10]);
        tmp+=tmpStride;
        src+=srcStride;
    }
    tmp -= tmpStride*(h+5-2);
    for(i=0; i<w; i++)
    {
        const int tmpB= tmp[-2*tmpStride];
        const int tmpA= tmp[-1*tmpStride];
        const int tmp0= tmp[0 *tmpStride];
        const int tmp1= tmp[1 *tmpStride];
        const int tmp2= tmp[2 *tmpStride];
        const int tmp3= tmp[3 *tmpStride];
        const int tmp4= tmp[4 *tmpStride];
        const int tmp5= tmp[5 *tmpStride];
        const int tmp6= tmp[6 *tmpStride];
        const int tmp7= tmp[7 *tmpStride];
        const int tmp8= tmp[8 *tmpStride];
        const int tmp9= tmp[9 *tmpStride];
        const int tmp10=tmp[10*tmpStride];
        dst[0*dstStride] = (dst[0*dstStride] + cm[((tmp0+tmp1)*20 - (tmpA+tmp2)*5 + (tmpB+tmp3)+512)>>10]+1)>>1;
        dst[1*dstStride] = (dst[1*dstStride] + cm[((tmp1+tmp2)*20 - (tmp0+tmp3)*5 + (tmpA+tmp4)+512)>>10]+1)>>1;
        dst[2*dstStride] = (dst[2*dstStride] + cm[((tmp2+tmp3)*20 - (tmp1+tmp4)*5 + (tmp0+tmp5)+512)>>10]+1)>>1;
        dst[3*dstStride] = (dst[3*dstStride] + cm[((tmp3+tmp4)*20 - (tmp2+tmp5)*5 + (tmp1+tmp6)+512)>>10]+1)>>1;
        dst[4*dstStride] = (dst[4*dstStride] + cm[((tmp4+tmp5)*20 - (tmp3+tmp6)*5 + (tmp2+tmp7)+512)>>10]+1)>>1;
        dst[5*dstStride] = (dst[5*dstStride] + cm[((tmp5+tmp6)*20 - (tmp4+tmp7)*5 + (tmp3+tmp8)+512)>>10]+1)>>1;
        dst[6*dstStride] = (dst[6*dstStride] + cm[((tmp6+tmp7)*20 - (tmp5+tmp8)*5 + (tmp4+tmp9)+512)>>10]+1)>>1;
        dst[7*dstStride] = (dst[7*dstStride] + cm[((tmp7+tmp8)*20 - (tmp6+tmp9)*5 + (tmp5+tmp10)+512)>>10]+1)>>1;

        dst++;
        tmp++;
    }
}

static void avg_h264_qpel16_h_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride)
{
    avg_h264_qpel8_h_lowpass(dst  , src  , dstStride, srcStride);
    avg_h264_qpel8_h_lowpass(dst+8, src+8, dstStride, srcStride);
    src += 8*srcStride;
    dst += 8*dstStride;
    avg_h264_qpel8_h_lowpass(dst  , src  , dstStride, srcStride);
    avg_h264_qpel8_h_lowpass(dst+8, src+8, dstStride, srcStride);
}
static void avg_h264_qpel16_v_lowpass(uint8_t *dst, uint8_t *src, int dstStride, int srcStride)
{
    avg_h264_qpel8_v_lowpass(dst  , src  , dstStride, srcStride);
    avg_h264_qpel8_v_lowpass(dst+8, src+8, dstStride, srcStride);
    src += 8*srcStride;
    dst += 8*dstStride;
    avg_h264_qpel8_v_lowpass(dst  , src  , dstStride, srcStride);
    avg_h264_qpel8_v_lowpass(dst+8, src+8, dstStride, srcStride);
}

static void avg_h264_qpel16_hv_lowpass(uint8_t *dst, int16_t *tmp, uint8_t *src, int dstStride, int tmpStride, int srcStride)
{
    avg_h264_qpel8_hv_lowpass(dst  , tmp  , src  , dstStride, tmpStride, srcStride);
    avg_h264_qpel8_hv_lowpass(dst+8, tmp+8, src+8, dstStride, tmpStride, srcStride);
    src += 8*srcStride;
    tmp += 8*tmpStride;   // 20080901
    dst += 8*dstStride;
    avg_h264_qpel8_hv_lowpass(dst  , tmp  , src  , dstStride, tmpStride, srcStride);
    avg_h264_qpel8_hv_lowpass(dst+8, tmp+8, src+8, dstStride, tmpStride, srcStride);
}

//H264_MC(put_, 4)
static void put_h264_qpel4_mc00_c (uint8_t *dst, uint8_t *src, int stride)
{
    put_pixels4_c(dst, src, stride, 4);
}

static void put_h264_qpel4_mc10_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t half[4*4];
    put_h264_qpel4_h_lowpass(half, src, 4, stride);
    put_pixels4_l2(dst, src, half, stride, stride, 4, 4);
}

static void put_h264_qpel4_mc20_c(uint8_t *dst, uint8_t *src, int stride)
{
    put_h264_qpel4_h_lowpass(dst, src, stride, stride);
}

static void put_h264_qpel4_mc30_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t half[4*4];
    put_h264_qpel4_h_lowpass(half, src, 4, stride);
    put_pixels4_l2(dst, src+1, half, stride, stride, 4, 4);
}

static void put_h264_qpel4_mc01_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    uint8_t half[4*4];
    copy_block4(full, src - stride*2, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(half, full_mid, 4, 4);
    put_pixels4_l2(dst, full_mid, half, stride, 4, 4, 4);
}

static void put_h264_qpel4_mc02_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    copy_block4 (full, src - stride*2, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(dst, full_mid, stride, 4);
}

static void put_h264_qpel4_mc03_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    uint8_t half[4*4];
    copy_block4 (full, src - stride*2, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(half, full_mid, 4, 4);
    put_pixels4_l2(dst, full_mid+4, half, stride, 4, 4, 4);
}

static void put_h264_qpel4_mc11_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    uint8_t halfH[4*4];
    uint8_t halfV[4*4];
    put_h264_qpel4_h_lowpass(halfH, src, 4, stride);
    copy_block4 (full, src - stride*2, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(halfV, full_mid, 4, 4);
    put_pixels4_l2(dst, halfH, halfV, stride, 4, 4, 4);
}

static void put_h264_qpel4_mc31_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    uint8_t halfH[4*4];
    uint8_t halfV[4*4];
    put_h264_qpel4_h_lowpass(halfH, src, 4, stride);
    copy_block4 (full, src - stride*2 + 1, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(halfV, full_mid, 4, 4);
    put_pixels4_l2(dst, halfH, halfV, stride, 4, 4, 4);
}

static void put_h264_qpel4_mc13_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    uint8_t halfH[4*4];
    uint8_t halfV[4*4];
    put_h264_qpel4_h_lowpass(halfH, src + stride, 4, stride);
    copy_block4 (full, src - stride*2, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(halfV, full_mid, 4, 4);
    put_pixels4_l2(dst, halfH, halfV, stride, 4, 4, 4);
}

static void put_h264_qpel4_mc33_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    uint8_t halfH[4*4];
    uint8_t halfV[4*4];
    put_h264_qpel4_h_lowpass(halfH, src + stride, 4, stride);
    copy_block4 (full, src - stride*2 + 1, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(halfV, full_mid, 4, 4);
    put_pixels4_l2(dst, halfH, halfV, stride, 4, 4, 4);
}

static void put_h264_qpel4_mc22_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[4*(4+5)];
    put_h264_qpel4_hv_lowpass(dst, tmp, src, stride, 4, stride);
}

static void put_h264_qpel4_mc21_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[4*(4+5)];
    uint8_t halfH[4*4];
    uint8_t halfHV[4*4];
    put_h264_qpel4_h_lowpass(halfH, src, 4, stride);
    put_h264_qpel4_hv_lowpass(halfHV, tmp, src, 4, 4, stride);
    put_pixels4_l2(dst, halfH, halfHV, stride, 4, 4, 4);
}

static void put_h264_qpel4_mc23_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[4*(4+5)];
    uint8_t halfH[4*4];
    uint8_t halfHV[4*4];
    put_h264_qpel4_h_lowpass(halfH, src + stride, 4, stride);
    put_h264_qpel4_hv_lowpass(halfHV, tmp, src, 4, 4, stride);
    put_pixels4_l2(dst, halfH, halfHV, stride, 4, 4, 4);
}

static void put_h264_qpel4_mc12_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    int16_t tmp[4*(4+5)];
    uint8_t halfV[4*4];
    uint8_t halfHV[4*4];
    copy_block4 (full, src - stride*2, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(halfV, full_mid, 4, 4);
    put_h264_qpel4_hv_lowpass(halfHV, tmp, src, 4, 4, stride);
    put_pixels4_l2(dst, halfV, halfHV, stride, 4, 4, 4);
}

static void put_h264_qpel4_mc32_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    int16_t tmp[4*(4+5)];
    uint8_t halfV[4*4];
    uint8_t halfHV[4*4];
    copy_block4 (full, src - stride*2 + 1, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(halfV, full_mid, 4, 4);
    put_h264_qpel4_hv_lowpass(halfHV, tmp, src, 4, 4, stride);
    put_pixels4_l2(dst, halfV, halfHV, stride, 4, 4, 4);
}

//H264_MC(put_, 8)
//*
static void put_h264_qpel8_mc00_c (uint8_t *dst, uint8_t *src, int stride)
{
    put_pixels8_c(dst, src, stride, 8);
}

static void put_h264_qpel8_mc10_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t half[8*8];
    put_h264_qpel8_h_lowpass(half, src, 8, stride);
    put_pixels8_l2(dst, src, half, stride, stride, 8, 8);
}

static void put_h264_qpel8_mc20_c(uint8_t *dst, uint8_t *src, int stride)
{
    put_h264_qpel8_h_lowpass(dst, src, stride, stride);
}

static void put_h264_qpel8_mc30_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t half[8*8];
    put_h264_qpel8_h_lowpass(half, src, 8, stride);
    put_pixels8_l2(dst, src+1, half, stride, stride, 8, 8);
}

static void put_h264_qpel8_mc01_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    uint8_t half[8*8];
    copy_block8 (full, src - stride*2, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(half, full_mid, 8, 8);
    put_pixels8_l2(dst, full_mid, half, stride, 8, 8, 8);
}

static void put_h264_qpel8_mc02_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    copy_block8 (full, src - stride*2, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(dst, full_mid, stride, 8);
}

static void put_h264_qpel8_mc03_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    uint8_t half[8*8];
    copy_block8 (full, src - stride*2, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(half, full_mid, 8, 8);
    put_pixels8_l2(dst, full_mid+8, half, stride, 8, 8, 8);
}

static void put_h264_qpel8_mc11_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    uint8_t halfH[8*8];
    uint8_t halfV[8*8];
    put_h264_qpel8_h_lowpass(halfH, src, 8, stride);
    copy_block8 (full, src - stride*2, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(halfV, full_mid, 8, 8);
    put_pixels8_l2(dst, halfH, halfV, stride, 8, 8, 8);
}

static void put_h264_qpel8_mc31_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    uint8_t halfH[8*8];
    uint8_t halfV[8*8];
    put_h264_qpel8_h_lowpass(halfH, src, 8, stride);
    copy_block8 (full, src - stride*2 + 1, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(halfV, full_mid, 8, 8);
    put_pixels8_l2(dst, halfH, halfV, stride, 8, 8, 8);
}

static void put_h264_qpel8_mc13_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    uint8_t halfH[8*8];
    uint8_t halfV[8*8];
    put_h264_qpel8_h_lowpass(halfH, src + stride, 8, stride);
    copy_block8 (full, src - stride*2, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(halfV, full_mid, 8, 8);
    put_pixels8_l2(dst, halfH, halfV, stride, 8, 8, 8);
}

static void put_h264_qpel8_mc33_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    uint8_t halfH[8*8];
    uint8_t halfV[8*8];
    put_h264_qpel8_h_lowpass(halfH, src + stride, 8, stride);
    copy_block8 (full, src - stride*2 + 1, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(halfV, full_mid, 8, 8);
    put_pixels8_l2(dst, halfH, halfV, stride, 8, 8, 8);
}

static void put_h264_qpel8_mc22_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[8*(8+5)];
    put_h264_qpel8_hv_lowpass(dst, tmp, src, stride, 8, stride);
}

static void put_h264_qpel8_mc21_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[8*(8+5)];
    uint8_t halfH[8*8];
    uint8_t halfHV[8*8];
    put_h264_qpel8_h_lowpass(halfH, src, 8, stride);
    put_h264_qpel8_hv_lowpass(halfHV, tmp, src, 8, 8, stride);
    put_pixels8_l2(dst, halfH, halfHV, stride, 8, 8, 8);
}

static void put_h264_qpel8_mc23_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[8*(8+5)];
    uint8_t halfH[8*8];
    uint8_t halfHV[8*8];
    put_h264_qpel8_h_lowpass(halfH, src + stride, 8, stride);
    put_h264_qpel8_hv_lowpass(halfHV, tmp, src, 8, 8, stride);
    put_pixels8_l2(dst, halfH, halfHV, stride, 8, 8, 8);
}

static void put_h264_qpel8_mc12_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    int16_t tmp[8*(8+5)];
    uint8_t halfV[8*8];
    uint8_t halfHV[8*8];
    copy_block8 (full, src - stride*2, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(halfV, full_mid, 8, 8);
    put_h264_qpel8_hv_lowpass(halfHV, tmp, src, 8, 8, stride);
    put_pixels8_l2(dst, halfV, halfHV, stride, 8, 8, 8);
}

static void put_h264_qpel8_mc32_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    int16_t tmp[8*(8+5)];
    uint8_t halfV[8*8];
    uint8_t halfHV[8*8];
    copy_block8 (full, src - stride*2 + 1, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(halfV, full_mid, 8, 8);
    put_h264_qpel8_hv_lowpass(halfHV, tmp, src, 8, 8, stride);
    put_pixels8_l2(dst, halfV, halfHV, stride, 8, 8, 8);
}
// */

//H264_MC(put_, 16)
//*
static void put_h264_qpel16_mc00_c (uint8_t *dst, uint8_t *src, int stride)
{
    put_pixels16_c(dst, src, stride, 16);
}

static void put_h264_qpel16_mc10_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t half[16*16];
    put_h264_qpel16_h_lowpass(half, src, 16, stride);
    put_pixels16_l2(dst, src, half, stride, stride, 16, 16);
}

static void put_h264_qpel16_mc20_c(uint8_t *dst, uint8_t *src, int stride)
{
    put_h264_qpel16_h_lowpass(dst, src, stride, stride);
}

static void put_h264_qpel16_mc30_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t half[16*16];
    put_h264_qpel16_h_lowpass(half, src, 16, stride);
    put_pixels16_l2(dst, src+1, half, stride, stride, 16, 16);
}

static void put_h264_qpel16_mc01_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    uint8_t half[16*16];
    copy_block16 (full, src - stride*2, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(half, full_mid, 16, 16);
    put_pixels16_l2(dst, full_mid, half, stride, 16, 16, 16);
}

static void put_h264_qpel16_mc02_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    copy_block16(full, src - stride*2, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(dst, full_mid, stride, 16);
}

static void put_h264_qpel16_mc03_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    uint8_t half[16*16];
    copy_block16 (full, src - stride*2, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(half, full_mid, 16, 16);
    put_pixels16_l2(dst, full_mid+16, half, stride, 16, 16, 16);
}

static void put_h264_qpel16_mc11_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    uint8_t halfH[16*16];
    uint8_t halfV[16*16];
    put_h264_qpel16_h_lowpass(halfH, src, 16, stride);
    copy_block16 (full, src - stride*2, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(halfV, full_mid, 16, 16);
    put_pixels16_l2(dst, halfH, halfV, stride, 16, 16, 16);
}

static void put_h264_qpel16_mc31_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    uint8_t halfH[16*16];
    uint8_t halfV[16*16];
    put_h264_qpel16_h_lowpass(halfH, src, 16, stride);
    copy_block16 (full, src - stride*2 + 1, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(halfV, full_mid, 16, 16);
    put_pixels16_l2(dst, halfH, halfV, stride, 16, 16, 16);
}

static void put_h264_qpel16_mc13_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    uint8_t halfH[16*16];
    uint8_t halfV[16*16];
    put_h264_qpel16_h_lowpass(halfH, src + stride, 16, stride);
    copy_block16 (full, src - stride*2, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(halfV, full_mid, 16, 16);
    put_pixels16_l2(dst, halfH, halfV, stride, 16, 16, 16);
}

static void put_h264_qpel16_mc33_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    uint8_t halfH[16*16];
    uint8_t halfV[16*16];
    put_h264_qpel16_h_lowpass(halfH, src + stride, 16, stride);
    copy_block16 (full, src - stride*2 + 1, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(halfV, full_mid, 16, 16);
    put_pixels16_l2(dst, halfH, halfV, stride, 16, 16, 16);
}

static void put_h264_qpel16_mc22_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[16*(16+5)];
    put_h264_qpel16_hv_lowpass(dst, tmp, src, stride, 16, stride);
}

static void put_h264_qpel16_mc21_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[16*(16+5)];
    uint8_t halfH[16*16];
    uint8_t halfHV[16*16];
    put_h264_qpel16_h_lowpass(halfH, src, 16, stride);
    put_h264_qpel16_hv_lowpass(halfHV, tmp, src, 16, 16, stride);
    put_pixels16_l2(dst, halfH, halfHV, stride, 16, 16, 16);
}

static void put_h264_qpel16_mc23_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[16*(16+5)];
    uint8_t halfH[16*16];
    uint8_t halfHV[16*16];
    put_h264_qpel16_h_lowpass(halfH, src + stride, 16, stride);
    put_h264_qpel16_hv_lowpass(halfHV, tmp, src, 16, 16, stride);
    put_pixels16_l2(dst, halfH, halfHV, stride, 16, 16, 16);
}

static void put_h264_qpel16_mc12_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    int16_t tmp[16*(16+5)];
    uint8_t halfV[16*16];
    uint8_t halfHV[16*16];
    copy_block16 (full, src - stride*2, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(halfV, full_mid, 16, 16);
    put_h264_qpel16_hv_lowpass(halfHV, tmp, src, 16, 16, stride);
    put_pixels16_l2(dst, halfV, halfHV, stride, 16, 16, 16);
}

static void put_h264_qpel16_mc32_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    int16_t tmp[16*(16+5)];
    uint8_t halfV[16*16];
    uint8_t halfHV[16*16];
    copy_block16 (full, src - stride*2 + 1, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(halfV, full_mid, 16, 16);
    put_h264_qpel16_hv_lowpass(halfHV, tmp, src, 16, 16, stride);
    put_pixels16_l2(dst, halfV, halfHV, stride, 16, 16, 16);
}// */

//H264_MC(avg_, 4)
//*
static void avg_h264_qpel4_mc00_c (uint8_t *dst, uint8_t *src, int stride)
{
    avg_pixels4_c(dst, src, stride, 4);
}

static void avg_h264_qpel4_mc10_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t half[4*4];
    put_h264_qpel4_h_lowpass(half, src, 4, stride);
    avg_pixels4_l2(dst, src, half, stride, stride, 4, 4);
}

static void avg_h264_qpel4_mc20_c(uint8_t *dst, uint8_t *src, int stride)
{
    avg_h264_qpel4_h_lowpass(dst, src, stride, stride);
}

static void avg_h264_qpel4_mc30_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t half[4*4];
    put_h264_qpel4_h_lowpass(half, src, 4, stride);
    avg_pixels4_l2(dst, src+1, half, stride, stride, 4, 4);
}

static void avg_h264_qpel4_mc01_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    uint8_t half[4*4];
    copy_block4 (full, src - stride*2, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(half, full_mid, 4, 4);
    avg_pixels4_l2(dst, full_mid, half, stride, 4, 4, 4);
}

static void avg_h264_qpel4_mc02_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    copy_block4 (full, src - stride*2, 4,  stride, 4 + 5);
    avg_h264_qpel4_v_lowpass(dst, full_mid, stride, 4);
}

static void avg_h264_qpel4_mc03_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    uint8_t half[4*4];
    copy_block4 (full, src - stride*2, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(half, full_mid, 4, 4);
    avg_pixels4_l2(dst, full_mid+4, half, stride, 4, 4, 4);
}

static void avg_h264_qpel4_mc11_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    uint8_t halfH[4*4];
    uint8_t halfV[4*4];
    put_h264_qpel4_h_lowpass(halfH, src, 4, stride);
    copy_block4 (full, src - stride*2, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(halfV, full_mid, 4, 4);
    avg_pixels4_l2(dst, halfH, halfV, stride, 4, 4, 4);
}

static void avg_h264_qpel4_mc31_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    uint8_t halfH[4*4];
    uint8_t halfV[4*4];
    put_h264_qpel4_h_lowpass(halfH, src, 4, stride);
    copy_block4 (full, src - stride*2 + 1, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(halfV, full_mid, 4, 4);
    avg_pixels4_l2(dst, halfH, halfV, stride, 4, 4, 4);
}

static void avg_h264_qpel4_mc13_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    uint8_t halfH[4*4];
    uint8_t halfV[4*4];
    put_h264_qpel4_h_lowpass(halfH, src + stride, 4, stride);
    copy_block4 (full, src - stride*2, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(halfV, full_mid, 4, 4);
    avg_pixels4_l2(dst, halfH, halfV, stride, 4, 4, 4);
}

static void avg_h264_qpel4_mc33_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    uint8_t halfH[4*4];
    uint8_t halfV[4*4];
    put_h264_qpel4_h_lowpass(halfH, src + stride, 4, stride);
    copy_block4 (full, src - stride*2 + 1, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(halfV, full_mid, 4, 4);
    avg_pixels4_l2(dst, halfH, halfV, stride, 4, 4, 4);
}

static void avg_h264_qpel4_mc22_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[4*(4+5)];
    avg_h264_qpel4_hv_lowpass(dst, tmp, src, stride, 4, stride);
}

static void avg_h264_qpel4_mc21_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[4*(4+5)];
    uint8_t halfH[4*4];
    uint8_t halfHV[4*4];
    put_h264_qpel4_h_lowpass(halfH, src, 4, stride);
    put_h264_qpel4_hv_lowpass(halfHV, tmp, src, 4, 4, stride);
    avg_pixels4_l2(dst, halfH, halfHV, stride, 4, 4, 4);
}

static void avg_h264_qpel4_mc23_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[4*(4+5)];
    uint8_t halfH[4*4];
    uint8_t halfHV[4*4];
    put_h264_qpel4_h_lowpass(halfH, src + stride, 4, stride);
    put_h264_qpel4_hv_lowpass(halfHV, tmp, src, 4, 4, stride);
    avg_pixels4_l2(dst, halfH, halfHV, stride, 4, 4, 4);
}

static void avg_h264_qpel4_mc12_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    int16_t tmp[4*(4+5)];
    uint8_t halfV[4*4];
    uint8_t halfHV[4*4];
    copy_block4 (full, src - stride*2, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(halfV, full_mid, 4, 4);
    put_h264_qpel4_hv_lowpass(halfHV, tmp, src, 4, 4, stride);
    avg_pixels4_l2(dst, halfV, halfHV, stride, 4, 4, 4);
}

static void avg_h264_qpel4_mc32_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[4*(4+5)];
    uint8_t * const full_mid= full + 4*2;
    int16_t tmp[4*(4+5)];
    uint8_t halfV[4*4];
    uint8_t halfHV[4*4];
    copy_block4 (full, src - stride*2 + 1, 4,  stride, 4 + 5);
    put_h264_qpel4_v_lowpass(halfV, full_mid, 4, 4);
    put_h264_qpel4_hv_lowpass(halfHV, tmp, src, 4, 4, stride);
    avg_pixels4_l2(dst, halfV, halfHV, stride, 4, 4, 4);
} // */

//H264_MC(avg_, 8)
//*
static void avg_h264_qpel8_mc00_c (uint8_t *dst, uint8_t *src, int stride)
{
    avg_pixels8_c(dst, src, stride, 8);
}

static void avg_h264_qpel8_mc10_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t half[8*8];
    put_h264_qpel8_h_lowpass(half, src, 8, stride);
    avg_pixels8_l2(dst, src, half, stride, stride, 8, 8);
}

static void avg_h264_qpel8_mc20_c(uint8_t *dst, uint8_t *src, int stride)
{
    avg_h264_qpel8_h_lowpass(dst, src, stride, stride);
}

static void avg_h264_qpel8_mc30_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t half[8*8];
    put_h264_qpel8_h_lowpass(half, src, 8, stride);
    avg_pixels8_l2(dst, src+1, half, stride, stride, 8, 8);
}

static void avg_h264_qpel8_mc01_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    uint8_t half[8*8];
    copy_block8 (full, src - stride*2, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(half, full_mid, 8, 8);
    avg_pixels8_l2(dst, full_mid, half, stride, 8, 8, 8);
}

static void avg_h264_qpel8_mc02_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    copy_block8 (full, src - stride*2, 8,  stride, 8 + 5);
    avg_h264_qpel8_v_lowpass(dst, full_mid, stride, 8);
}

static void avg_h264_qpel8_mc03_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    uint8_t half[8*8];
    copy_block8 (full, src - stride*2, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(half, full_mid, 8, 8);
    avg_pixels8_l2(dst, full_mid+8, half, stride, 8, 8, 8);
}

static void avg_h264_qpel8_mc11_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    uint8_t halfH[8*8];
    uint8_t halfV[8*8];
    put_h264_qpel8_h_lowpass(halfH, src, 8, stride);
    copy_block8 (full, src - stride*2, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(halfV, full_mid, 8, 8);
    avg_pixels8_l2(dst, halfH, halfV, stride, 8, 8, 8);
}

static void avg_h264_qpel8_mc31_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    uint8_t halfH[8*8];
    uint8_t halfV[8*8];
    put_h264_qpel8_h_lowpass(halfH, src, 8, stride);
    copy_block8 (full, src - stride*2 + 1, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(halfV, full_mid, 8, 8);
    avg_pixels8_l2(dst, halfH, halfV, stride, 8, 8, 8);
}

static void avg_h264_qpel8_mc13_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    uint8_t halfH[8*8];
    uint8_t halfV[8*8];
    put_h264_qpel8_h_lowpass(halfH, src + stride, 8, stride);
    copy_block8 (full, src - stride*2, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(halfV, full_mid, 8, 8);
    avg_pixels8_l2(dst, halfH, halfV, stride, 8, 8, 8);
}

static void avg_h264_qpel8_mc33_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    uint8_t halfH[8*8];
    uint8_t halfV[8*8];
    put_h264_qpel8_h_lowpass(halfH, src + stride, 8, stride);
    copy_block8 (full, src - stride*2 + 1, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(halfV, full_mid, 8, 8);
    avg_pixels8_l2(dst, halfH, halfV, stride, 8, 8, 8);
}

static void avg_h264_qpel8_mc22_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[8*(8+5)];
    avg_h264_qpel8_hv_lowpass(dst, tmp, src, stride, 8, stride);
}

static void avg_h264_qpel8_mc21_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[8*(8+5)];
    uint8_t halfH[8*8];
    uint8_t halfHV[8*8];
    put_h264_qpel8_h_lowpass(halfH, src, 8, stride);
    put_h264_qpel8_hv_lowpass(halfHV, tmp, src, 8, 8, stride);
    avg_pixels8_l2(dst, halfH, halfHV, stride, 8, 8, 8);
}

static void avg_h264_qpel8_mc23_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[8*(8+5)];
    uint8_t halfH[8*8];
    uint8_t halfHV[8*8];
    put_h264_qpel8_h_lowpass(halfH, src + stride, 8, stride);
    put_h264_qpel8_hv_lowpass(halfHV, tmp, src, 8, 8, stride);
    avg_pixels8_l2(dst, halfH, halfHV, stride, 8, 8, 8);
}

static void avg_h264_qpel8_mc12_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    int16_t tmp[8*(8+5)];
    uint8_t halfV[8*8];
    uint8_t halfHV[8*8];
    copy_block8 (full, src - stride*2, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(halfV, full_mid, 8, 8);
    put_h264_qpel8_hv_lowpass(halfHV, tmp, src, 8, 8, stride);
    avg_pixels8_l2(dst, halfV, halfHV, stride, 8, 8, 8);
}

static void avg_h264_qpel8_mc32_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[8*(8+5)];
    uint8_t * const full_mid= full + 8*2;
    int16_t tmp[8*(8+5)];
    uint8_t halfV[8*8];
    uint8_t halfHV[8*8];
    copy_block8 (full, src - stride*2 + 1, 8,  stride, 8 + 5);
    put_h264_qpel8_v_lowpass(halfV, full_mid, 8, 8);
    put_h264_qpel8_hv_lowpass(halfHV, tmp, src, 8, 8, stride);
    avg_pixels8_l2(dst, halfV, halfHV, stride, 8, 8, 8);
}// */

//H264_MC(avg_, 16)
//*
static void avg_h264_qpel16_mc00_c (uint8_t *dst, uint8_t *src, int stride)
{
    avg_pixels16_c(dst, src, stride, 16);
}

static void avg_h264_qpel16_mc10_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t half[16*16];
    put_h264_qpel16_h_lowpass(half, src, 16, stride);
    avg_pixels16_l2(dst, src, half, stride, stride, 16, 16);
}

static void avg_h264_qpel16_mc20_c(uint8_t *dst, uint8_t *src, int stride)
{
    avg_h264_qpel16_h_lowpass(dst, src, stride, stride);
}

static void avg_h264_qpel16_mc30_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t half[16*16];
    put_h264_qpel16_h_lowpass(half, src, 16, stride);
    avg_pixels16_l2(dst, src+1, half, stride, stride, 16, 16);
}

static void avg_h264_qpel16_mc01_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    uint8_t half[16*16];
    copy_block16 (full, src - stride*2, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(half, full_mid, 16, 16);
    avg_pixels16_l2(dst, full_mid, half, stride, 16, 16, 16);
}

static void avg_h264_qpel16_mc02_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    copy_block16 (full, src - stride*2, 16,  stride, 16 + 5);
    avg_h264_qpel16_v_lowpass(dst, full_mid, stride, 16);
}

static void avg_h264_qpel16_mc03_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    uint8_t half[16*16];
    copy_block16 (full, src - stride*2, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(half, full_mid, 16, 16);
    avg_pixels16_l2(dst, full_mid+16, half, stride, 16, 16, 16);
}

static void avg_h264_qpel16_mc11_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    uint8_t halfH[16*16];
    uint8_t halfV[16*16];
    put_h264_qpel16_h_lowpass(halfH, src, 16, stride);
    copy_block16 (full, src - stride*2, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(halfV, full_mid, 16, 16);
    avg_pixels16_l2(dst, halfH, halfV, stride, 16, 16, 16);
}

static void avg_h264_qpel16_mc31_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    uint8_t halfH[16*16];
    uint8_t halfV[16*16];
    put_h264_qpel16_h_lowpass(halfH, src, 16, stride);
    copy_block16 (full, src - stride*2 + 1, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(halfV, full_mid, 16, 16);
    avg_pixels16_l2(dst, halfH, halfV, stride, 16, 16, 16);
}

static void avg_h264_qpel16_mc13_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    uint8_t halfH[16*16];
    uint8_t halfV[16*16];
    put_h264_qpel16_h_lowpass(halfH, src + stride, 16, stride);
    copy_block16 (full, src - stride*2, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(halfV, full_mid, 16, 16);
    avg_pixels16_l2(dst, halfH, halfV, stride, 16, 16, 16);
}

static void avg_h264_qpel16_mc33_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    uint8_t halfH[16*16];
    uint8_t halfV[16*16];
    put_h264_qpel16_h_lowpass(halfH, src + stride, 16, stride);
    copy_block16 (full, src - stride*2 + 1, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(halfV, full_mid, 16, 16);
    avg_pixels16_l2(dst, halfH, halfV, stride, 16, 16, 16);
}

static void avg_h264_qpel16_mc22_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[16*(16+5)];
    avg_h264_qpel16_hv_lowpass(dst, tmp, src, stride, 16, stride);
}

static void avg_h264_qpel16_mc21_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[16*(16+5)];
    uint8_t halfH[16*16];
    uint8_t halfHV[16*16];
    put_h264_qpel16_h_lowpass(halfH, src, 16, stride);
    put_h264_qpel16_hv_lowpass(halfHV, tmp, src, 16, 16, stride);
    avg_pixels16_l2(dst, halfH, halfHV, stride, 16, 16, 16);
}

static void avg_h264_qpel16_mc23_c(uint8_t *dst, uint8_t *src, int stride)
{
    int16_t tmp[16*(16+5)];
    uint8_t halfH[16*16];
    uint8_t halfHV[16*16];
    put_h264_qpel16_h_lowpass(halfH, src + stride, 16, stride);
    put_h264_qpel16_hv_lowpass(halfHV, tmp, src, 16, 16, stride);
    avg_pixels16_l2(dst, halfH, halfHV, stride, 16, 16, 16);
}

static void avg_h264_qpel16_mc12_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    int16_t tmp[16*(16+5)];
    uint8_t halfV[16*16];
    uint8_t halfHV[16*16];
    copy_block16 (full, src - stride*2, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(halfV, full_mid, 16, 16);
    put_h264_qpel16_hv_lowpass(halfHV, tmp, src, 16, 16, stride);
    avg_pixels16_l2(dst, halfV, halfHV, stride, 16, 16, 16);
}

static void avg_h264_qpel16_mc32_c(uint8_t *dst, uint8_t *src, int stride)
{
    uint8_t full[16*(16+5)];
    uint8_t * const full_mid= full + 16*2;
    int16_t tmp[16*(16+5)];
    uint8_t halfV[16*16];
    uint8_t halfHV[16*16];
    copy_block16 (full, src - stride*2 + 1, 16,  stride, 16 + 5);
    put_h264_qpel16_v_lowpass(halfV, full_mid, 16, 16);
    put_h264_qpel16_hv_lowpass(halfHV, tmp, src, 16, 16, stride);
    avg_pixels16_l2(dst, halfV, halfHV, stride, 16, 16, 16);
}
// */
//----------------------------------------------------------------------

static inline void h264_loop_filter_luma_c(uint8_t *pix, int xstride, int ystride, int alpha, int beta, int8_t *tc0)
{
    int i, d;
    for( i = 0; i < 4; i++ )
	{
        if( tc0[i] < 0 )
		{
            pix += 4*ystride;
            continue;
        }

        for( d = 0; d < 4; d++ )
		{
            const int p0 = pix[-1*xstride];
            const int p1 = pix[-2*xstride];
            const int p2 = pix[-3*xstride];
            const int q0 = pix[0];
            const int q1 = pix[1*xstride];
            const int q2 = pix[2*xstride];

            if( ABS( p0 - q0 ) < alpha && ABS( p1 - p0 ) < beta && ABS( q1 - q0 ) < beta )
			{
                int tc = tc0[i];
                int i_delta;

                if( ABS( p2 - p0 ) < beta )
				{
                    pix[-2*xstride] = p1 + clip( (( p2 + ( ( p0 + q0 + 1 ) >> 1 ) ) >> 1) - p1, -tc0[i], tc0[i] );
                    tc++;
                }

                if( ABS( q2 - q0 ) < beta )
				{
                    pix[   xstride] = q1 + clip( (( q2 + ( ( p0 + q0 + 1 ) >> 1 ) ) >> 1) - q1, -tc0[i], tc0[i] );
                    tc++;
                }

                i_delta = clip( (((q0 - p0 ) << 2) + (p1 - q1) + 4) >> 3, -tc, tc );
                pix[-xstride] = clip_uint8( p0 + i_delta );    /* p0' */
                pix[0]        = clip_uint8( q0 - i_delta );    /* q0' */
            }
            pix += ystride;
        }
    }
}

static void h264_h_loop_filter_luma_c(uint8_t *pix, int stride, int alpha, int beta, int8_t *tc0)
{
    h264_loop_filter_luma_c(pix, 1, stride, alpha, beta, tc0);
}

static void h264_v_loop_filter_luma_c(uint8_t *pix, int stride, int alpha, int beta, int8_t *tc0)
{
    h264_loop_filter_luma_c(pix, stride, 1, alpha, beta, tc0);
}

static inline void h264_loop_filter_chroma_c(uint8_t *pix, int xstride, int ystride, int alpha, int beta, int8_t *tc0)
{
    int i, d;
    for( i = 0; i < 4; i++ )
	{
        const int tc = tc0[i];
        if( tc <= 0 )
		{
            pix += 2*ystride;
            continue;
        }

        for( d = 0; d < 2; d++ )
		{
            const int p0 = pix[-1*xstride];
            const int p1 = pix[-2*xstride];
            const int q0 = pix[0];
            const int q1 = pix[1*xstride];

            if( ABS( p0 - q0 ) < alpha && ABS( p1 - p0 ) < beta && ABS( q1 - q0 ) < beta )
			{
                int delta = clip( (((q0 - p0 ) << 2) + (p1 - q1) + 4) >> 3, -tc, tc );

                pix[-xstride] = clip_uint8( p0 + delta );    /* p0' */
                pix[0]        = clip_uint8( q0 - delta );    /* q0' */
            }
            pix += ystride;
        }
    }
}

static void h264_v_loop_filter_chroma_c(uint8_t *pix, int stride, int alpha, int beta, int8_t *tc0)
{
    h264_loop_filter_chroma_c(pix, stride, 1, alpha, beta, tc0);
}

static void h264_h_loop_filter_chroma_c(uint8_t *pix, int stride, int alpha, int beta, int8_t *tc0)
{
    h264_loop_filter_chroma_c(pix, 1, stride, alpha, beta, tc0);
}

static inline void h264_loop_filter_chroma_intra_c(uint8_t *pix, int xstride, int ystride, int alpha, int beta)
{
    int d;
    for( d = 0; d < 8; d++ )
	{
        const int p0 = pix[-1*xstride];
        const int p1 = pix[-2*xstride];
        const int q0 = pix[0];
        const int q1 = pix[1*xstride];

        if(ABS( p0 - q0 ) < alpha && ABS( p1 - p0 ) < beta && ABS( q1 - q0 ) < beta )
		{
            pix[-xstride] = ( 2*p1 + p0 + q1 + 2 ) >> 2;   /* p0' */
            pix[0]        = ( 2*q1 + q0 + p1 + 2 ) >> 2;   /* q0' */
        }
        pix += ystride;
    }
}

static void h264_v_loop_filter_chroma_intra_c(uint8_t *pix, int stride, int alpha, int beta)
{
    h264_loop_filter_chroma_intra_c(pix, stride, 1, alpha, beta);
}

static void h264_h_loop_filter_chroma_intra_c(uint8_t *pix, int stride, int alpha, int beta)
{
    h264_loop_filter_chroma_intra_c(pix, 1, stride, alpha, beta);
}

//=======================================================================================

static inline void idct_internal(uint8_t *dst, DCTELEM *block, int stride, int block_stride, int shift, int add)
{
    int i;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;

    block[0] += 1<<(shift-1);

    for(i=0; i<4; i++)
	{
        const int z0=  block[0 + block_stride*i]     +  block[2 + block_stride*i];
        const int z1=  block[0 + block_stride*i]     -  block[2 + block_stride*i];
        const int z2= (block[1 + block_stride*i]>>1) -  block[3 + block_stride*i];
        const int z3=  block[1 + block_stride*i]     + (block[3 + block_stride*i]>>1);

        block[0 + block_stride*i]= z0 + z3;
        block[1 + block_stride*i]= z1 + z2;
        block[2 + block_stride*i]= z1 - z2;
        block[3 + block_stride*i]= z0 - z3;
    }

    for(i=0; i<4; i++)
	{
        const int z0=  block[i + block_stride*0]     +  block[i + block_stride*2];
        const int z1=  block[i + block_stride*0]     -  block[i + block_stride*2];
        const int z2= (block[i + block_stride*1]>>1) -  block[i + block_stride*3];
        const int z3=  block[i + block_stride*1]     + (block[i + block_stride*3]>>1);

        dst[i + 0*stride]= cm[ add*dst[i + 0*stride] + ((z0 + z3) >> shift) ];
        dst[i + 1*stride]= cm[ add*dst[i + 1*stride] + ((z1 + z2) >> shift) ];
        dst[i + 2*stride]= cm[ add*dst[i + 2*stride] + ((z1 - z2) >> shift) ];
        dst[i + 3*stride]= cm[ add*dst[i + 3*stride] + ((z0 - z3) >> shift) ];
    }
}

void ff_h264_idct_add_c(uint8_t *dst, DCTELEM *block, int stride)
{
    idct_internal(dst, block, stride, 4, 6, 1);
}

void ff_h264_idct_dc_add_c(uint8_t *dst, DCTELEM *block, int stride)
{
    int i, j;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
    int dc = (block[0] + 32) >> 6;
    for( j = 0; j < 4; j++ )
    {
        for( i = 0; i < 4; i++ )
            dst[i] = cm[ dst[i] + dc ];
        dst += stride;
    }
}

void clear_blocks_c(DCTELEM *blocks)
{
    memset(blocks, 0, sizeof(DCTELEM)*6*64);
}

void dsputil_static_init(void)
{
/*
    int i=sizeof(cropTbl);

	memset(cropTbl, 0, sizeof(cropTbl));

    for(i=0;i<256;i++)
		cropTbl[i + MAX_NEG_CROP] = i;

    for(i=0;i<MAX_NEG_CROP;i++)
	{
        cropTbl[i] = 0;
        cropTbl[i + MAX_NEG_CROP + 256] = 255;
    }
// */
}

//=======================================================================================

void dsputil_init(DSPContext* c, AVCodecContext *avctx)
{
    c->h264_idct_add= ff_h264_idct_add_c;
    c->h264_idct_dc_add= ff_h264_idct_dc_add_c;

    c->put_h264_qpel_pixels_tab[0][ 0] = put_h264_qpel16_mc00_c;
    c->put_h264_qpel_pixels_tab[0][ 1] = put_h264_qpel16_mc10_c;
    c->put_h264_qpel_pixels_tab[0][ 2] = put_h264_qpel16_mc20_c;
    c->put_h264_qpel_pixels_tab[0][ 3] = put_h264_qpel16_mc30_c;
    c->put_h264_qpel_pixels_tab[0][ 4] = put_h264_qpel16_mc01_c;
    c->put_h264_qpel_pixels_tab[0][ 5] = put_h264_qpel16_mc11_c;
    c->put_h264_qpel_pixels_tab[0][ 6] = put_h264_qpel16_mc21_c;
    c->put_h264_qpel_pixels_tab[0][ 7] = put_h264_qpel16_mc31_c;
    c->put_h264_qpel_pixels_tab[0][ 8] = put_h264_qpel16_mc02_c;
    c->put_h264_qpel_pixels_tab[0][ 9] = put_h264_qpel16_mc12_c;
    c->put_h264_qpel_pixels_tab[0][10] = put_h264_qpel16_mc22_c;
    c->put_h264_qpel_pixels_tab[0][11] = put_h264_qpel16_mc32_c;
    c->put_h264_qpel_pixels_tab[0][12] = put_h264_qpel16_mc03_c;
    c->put_h264_qpel_pixels_tab[0][13] = put_h264_qpel16_mc13_c;
    c->put_h264_qpel_pixels_tab[0][14] = put_h264_qpel16_mc23_c;
    c->put_h264_qpel_pixels_tab[0][15] = put_h264_qpel16_mc33_c;

    c->put_h264_qpel_pixels_tab[1][ 0] = put_h264_qpel8_mc00_c;
    c->put_h264_qpel_pixels_tab[1][ 1] = put_h264_qpel8_mc10_c;
    c->put_h264_qpel_pixels_tab[1][ 2] = put_h264_qpel8_mc20_c;
    c->put_h264_qpel_pixels_tab[1][ 3] = put_h264_qpel8_mc30_c;
    c->put_h264_qpel_pixels_tab[1][ 4] = put_h264_qpel8_mc01_c;
    c->put_h264_qpel_pixels_tab[1][ 5] = put_h264_qpel8_mc11_c;
    c->put_h264_qpel_pixels_tab[1][ 6] = put_h264_qpel8_mc21_c;
    c->put_h264_qpel_pixels_tab[1][ 7] = put_h264_qpel8_mc31_c;
    c->put_h264_qpel_pixels_tab[1][ 8] = put_h264_qpel8_mc02_c;
    c->put_h264_qpel_pixels_tab[1][ 9] = put_h264_qpel8_mc12_c;
    c->put_h264_qpel_pixels_tab[1][10] = put_h264_qpel8_mc22_c;
    c->put_h264_qpel_pixels_tab[1][11] = put_h264_qpel8_mc32_c;
    c->put_h264_qpel_pixels_tab[1][12] = put_h264_qpel8_mc03_c;
    c->put_h264_qpel_pixels_tab[1][13] = put_h264_qpel8_mc13_c;
    c->put_h264_qpel_pixels_tab[1][14] = put_h264_qpel8_mc23_c;
    c->put_h264_qpel_pixels_tab[1][15] = put_h264_qpel8_mc33_c;

    c->put_h264_qpel_pixels_tab[2][ 0] = put_h264_qpel4_mc00_c;
    c->put_h264_qpel_pixels_tab[2][ 1] = put_h264_qpel4_mc10_c;
    c->put_h264_qpel_pixels_tab[2][ 2] = put_h264_qpel4_mc20_c;
    c->put_h264_qpel_pixels_tab[2][ 3] = put_h264_qpel4_mc30_c;
    c->put_h264_qpel_pixels_tab[2][ 4] = put_h264_qpel4_mc01_c;
    c->put_h264_qpel_pixels_tab[2][ 5] = put_h264_qpel4_mc11_c;
    c->put_h264_qpel_pixels_tab[2][ 6] = put_h264_qpel4_mc21_c;
    c->put_h264_qpel_pixels_tab[2][ 7] = put_h264_qpel4_mc31_c;
    c->put_h264_qpel_pixels_tab[2][ 8] = put_h264_qpel4_mc02_c;
    c->put_h264_qpel_pixels_tab[2][ 9] = put_h264_qpel4_mc12_c;
    c->put_h264_qpel_pixels_tab[2][10] = put_h264_qpel4_mc22_c;
    c->put_h264_qpel_pixels_tab[2][11] = put_h264_qpel4_mc32_c;
    c->put_h264_qpel_pixels_tab[2][12] = put_h264_qpel4_mc03_c;
    c->put_h264_qpel_pixels_tab[2][13] = put_h264_qpel4_mc13_c;
    c->put_h264_qpel_pixels_tab[2][14] = put_h264_qpel4_mc23_c;
    c->put_h264_qpel_pixels_tab[2][15] = put_h264_qpel4_mc33_c;

    c->avg_h264_qpel_pixels_tab[0][ 0] = avg_h264_qpel16_mc00_c;
    c->avg_h264_qpel_pixels_tab[0][ 1] = avg_h264_qpel16_mc10_c;
    c->avg_h264_qpel_pixels_tab[0][ 2] = avg_h264_qpel16_mc20_c;
    c->avg_h264_qpel_pixels_tab[0][ 3] = avg_h264_qpel16_mc30_c;
    c->avg_h264_qpel_pixels_tab[0][ 4] = avg_h264_qpel16_mc01_c;
    c->avg_h264_qpel_pixels_tab[0][ 5] = avg_h264_qpel16_mc11_c;
    c->avg_h264_qpel_pixels_tab[0][ 6] = avg_h264_qpel16_mc21_c;
    c->avg_h264_qpel_pixels_tab[0][ 7] = avg_h264_qpel16_mc31_c;
    c->avg_h264_qpel_pixels_tab[0][ 8] = avg_h264_qpel16_mc02_c;
    c->avg_h264_qpel_pixels_tab[0][ 9] = avg_h264_qpel16_mc12_c;
    c->avg_h264_qpel_pixels_tab[0][10] = avg_h264_qpel16_mc22_c;
    c->avg_h264_qpel_pixels_tab[0][11] = avg_h264_qpel16_mc32_c;
    c->avg_h264_qpel_pixels_tab[0][12] = avg_h264_qpel16_mc03_c;
    c->avg_h264_qpel_pixels_tab[0][13] = avg_h264_qpel16_mc13_c;
    c->avg_h264_qpel_pixels_tab[0][14] = avg_h264_qpel16_mc23_c;
    c->avg_h264_qpel_pixels_tab[0][15] = avg_h264_qpel16_mc33_c;

    c->avg_h264_qpel_pixels_tab[1][ 0] = avg_h264_qpel8_mc00_c;
    c->avg_h264_qpel_pixels_tab[1][ 1] = avg_h264_qpel8_mc10_c;
    c->avg_h264_qpel_pixels_tab[1][ 2] = avg_h264_qpel8_mc20_c;
    c->avg_h264_qpel_pixels_tab[1][ 3] = avg_h264_qpel8_mc30_c;
    c->avg_h264_qpel_pixels_tab[1][ 4] = avg_h264_qpel8_mc01_c;
    c->avg_h264_qpel_pixels_tab[1][ 5] = avg_h264_qpel8_mc11_c;
    c->avg_h264_qpel_pixels_tab[1][ 6] = avg_h264_qpel8_mc21_c;
    c->avg_h264_qpel_pixels_tab[1][ 7] = avg_h264_qpel8_mc31_c;
    c->avg_h264_qpel_pixels_tab[1][ 8] = avg_h264_qpel8_mc02_c;
    c->avg_h264_qpel_pixels_tab[1][ 9] = avg_h264_qpel8_mc12_c;
    c->avg_h264_qpel_pixels_tab[1][10] = avg_h264_qpel8_mc22_c;
    c->avg_h264_qpel_pixels_tab[1][11] = avg_h264_qpel8_mc32_c;
    c->avg_h264_qpel_pixels_tab[1][12] = avg_h264_qpel8_mc03_c;
    c->avg_h264_qpel_pixels_tab[1][13] = avg_h264_qpel8_mc13_c;
    c->avg_h264_qpel_pixels_tab[1][14] = avg_h264_qpel8_mc23_c;
    c->avg_h264_qpel_pixels_tab[1][15] = avg_h264_qpel8_mc33_c;

    c->avg_h264_qpel_pixels_tab[2][ 0] = avg_h264_qpel4_mc00_c;
    c->avg_h264_qpel_pixels_tab[2][ 1] = avg_h264_qpel4_mc10_c;
    c->avg_h264_qpel_pixels_tab[2][ 2] = avg_h264_qpel4_mc20_c;
    c->avg_h264_qpel_pixels_tab[2][ 3] = avg_h264_qpel4_mc30_c;
    c->avg_h264_qpel_pixels_tab[2][ 4] = avg_h264_qpel4_mc01_c;
    c->avg_h264_qpel_pixels_tab[2][ 5] = avg_h264_qpel4_mc11_c;
    c->avg_h264_qpel_pixels_tab[2][ 6] = avg_h264_qpel4_mc21_c;
    c->avg_h264_qpel_pixels_tab[2][ 7] = avg_h264_qpel4_mc31_c;
    c->avg_h264_qpel_pixels_tab[2][ 8] = avg_h264_qpel4_mc02_c;
    c->avg_h264_qpel_pixels_tab[2][ 9] = avg_h264_qpel4_mc12_c;
    c->avg_h264_qpel_pixels_tab[2][10] = avg_h264_qpel4_mc22_c;
    c->avg_h264_qpel_pixels_tab[2][11] = avg_h264_qpel4_mc32_c;
    c->avg_h264_qpel_pixels_tab[2][12] = avg_h264_qpel4_mc03_c;
    c->avg_h264_qpel_pixels_tab[2][13] = avg_h264_qpel4_mc13_c;
    c->avg_h264_qpel_pixels_tab[2][14] = avg_h264_qpel4_mc23_c;
    c->avg_h264_qpel_pixels_tab[2][15] = avg_h264_qpel4_mc33_c;

    c->put_h264_chroma_pixels_tab[0]= put_h264_chroma_mc8_c;
    c->put_h264_chroma_pixels_tab[1]= put_h264_chroma_mc4_c;
    c->put_h264_chroma_pixels_tab[2]= put_h264_chroma_mc2_c;

    c->avg_h264_chroma_pixels_tab[0]= avg_h264_chroma_mc8_c;
    c->avg_h264_chroma_pixels_tab[1]= avg_h264_chroma_mc4_c;
    c->avg_h264_chroma_pixels_tab[2]= avg_h264_chroma_mc2_c;

    c->h264_v_loop_filter_luma= h264_v_loop_filter_luma_c;
    c->h264_h_loop_filter_luma= h264_h_loop_filter_luma_c;
    c->h264_v_loop_filter_chroma= h264_v_loop_filter_chroma_c;
    c->h264_h_loop_filter_chroma= h264_h_loop_filter_chroma_c;
    c->h264_v_loop_filter_chroma_intra= h264_v_loop_filter_chroma_intra_c;
    c->h264_h_loop_filter_chroma_intra= h264_h_loop_filter_chroma_intra_c;
}

