
#include <limits.h>
#include <math.h> //for PI
#include "avcodec.h"
#include "dsputil.h"
#include "mpegvideo.h"

//#include <assert.h>

static void copy_picture(Picture *dst, Picture *src)
{
    *dst = *src;
}

static int alloc_picture(MpegEncContext *s, Picture *pic, int shared)
{
    const int big_mb_num= s->mb_stride*(s->mb_height+1) + 1; //the +1 is needed so memset(,,stride*height) doesnt sig11
    const int mb_array_size= s->mb_stride*s->mb_height;
    const int b8_array_size= s->b8_stride*s->mb_height*2;
    const int b4_array_size= s->b4_stride*s->mb_height*4;
    int i;
    int r;
    r= avcodec_default_get_buffer(s->avctx, (AVFrame*)pic);

    if(r<0 || !pic->data[0])
        return -1;

    if(s->linesize && (s->linesize != pic->linesize[0] || s->uvlinesize != pic->linesize[1]))
        return -1;

    if(pic->linesize[1] != pic->linesize[2])
        return -1;

    s->linesize  = pic->linesize[0];
    s->uvlinesize= pic->linesize[1];

    if(pic->qscale_table==NULL)
	{
        CHECKED_ALLOCZ(pic->qscale_table , mb_array_size * sizeof(uint8_t));
        CHECKED_ALLOCZ(pic->mb_type_base , big_mb_num    * sizeof(uint32_t));
        pic->mb_type= pic->mb_type_base + s->mb_stride+1;

        for(i=0; i<2; i++)
		{
            CHECKED_ALLOCZ(pic->motion_val_base[i], 2 * (b4_array_size+2)  * sizeof(int16_t));
            pic->motion_val[i]= pic->motion_val_base[i]+2;
            CHECKED_ALLOCZ(pic->ref_index[i] , b8_array_size * sizeof(uint8_t));
        }
    }

    return 0;
fail: //for the CHECKED_ALLOCZ macro
    return -1;
}

static void free_picture(MpegEncContext *s, Picture *pic)
{
    int i;

    if(pic->data[0] )
	{
        avcodec_default_release_buffer(s->avctx, (AVFrame*)pic);
    }

    av_freep(&pic->qscale_table);
    av_freep(&pic->mb_type_base);

    pic->mb_type= NULL;
    for(i=0; i<2; i++)
	{
        av_freep(&pic->motion_val_base[i]);
        av_freep(&pic->ref_index[i]);
    }
}

void MPV_common_end(MpegEncContext *s)
{
	int i;

	av_freep(&s->cbp_table);

	if(s->picture)
	{
		for(i=0; i<MAX_PICTURE_COUNT; i++)
		{
			free_picture(s, &s->picture[i]);
		}
	}

	av_freep(&s->picture);

	avcodec_default_free_buffers(s->avctx);
	s->context_initialized = 0;
	s->last_picture_ptr= NULL;
	s->next_picture_ptr= NULL;
	s->current_picture_ptr= NULL;
}

int MPV_common_init(MpegEncContext *s)
{
    int y_size, c_size, yc_size, mb_array_size, mv_table_size;

    dsputil_init(&s->dsp, s->avctx);

    s->mb_width  = (s->width  + 15) / 16;
    s->mb_height = (s->height + 15) / 16;
    s->mb_stride = s->mb_width + 1;
    s->b8_stride = s->mb_width*2 + 1;
    s->b4_stride = s->mb_width*4 + 1;
    mb_array_size= s->mb_height * s->mb_stride;
    mv_table_size= (s->mb_height+2) * s->mb_stride + 1;

    /* set default edge pos, will be overriden in decode_header if needed */
    s->h_edge_pos= s->mb_width*16;
    s->v_edge_pos= s->mb_height*16;

    s->mb_num = s->mb_width * s->mb_height;

    y_size = s->b8_stride * (2 * s->mb_height + 1);
    c_size = s->mb_stride * (s->mb_height + 1);
    yc_size = y_size + 2 * c_size;

    s->avctx->coded_frame= (AVFrame*)&s->current_picture;

    s->picture= av_mallocz( MAX_PICTURE_COUNT * sizeof(Picture));

    s->context_initialized = 1;

    return 0;
}

static void draw_edges_c(uint8_t *buf, int wrap, int width, int height, int w)
{
    uint8_t *ptr, *last_line;
    int i;

    last_line = buf + (height - 1) * wrap;
    for(i=0;i<w;i++)   /* top and bottom */
	{
        memcpy(buf - (i + 1) * wrap, buf, width);
        memcpy(last_line + (i + 1) * wrap, last_line, width);
    }

    ptr = buf;
    for(i=0;i<height;i++)    /* left and right */
	{
        memset(ptr - w, ptr[0], w);
        memset(ptr + width, ptr[width-1], w);
        ptr += wrap;
    }

    for(i=0;i<w;i++)    /* corners */
	{
        memset(buf - (i + 1) * wrap - w, buf[0], w); /* top left */
        memset(buf - (i + 1) * wrap + width, buf[width-1], w); /* top right */
        memset(last_line + (i + 1) * wrap - w, last_line[0], w); /* top left */
        memset(last_line + (i + 1) * wrap + width, last_line[width-1], w); /* top right */
    }
}

int ff_find_unused_picture(MpegEncContext *s, int shared)
{
    int i;

    for(i=0; i<MAX_PICTURE_COUNT; i++)
	{
        if(s->picture[i].data[0]==NULL)
			return i; //FIXME
    }
    for(i=0; i<MAX_PICTURE_COUNT; i++)
	{
        if(s->picture[i].data[0]==NULL)
			return i;
    }

    return -1;
}

int MPV_frame_start(MpegEncContext *s, AVCodecContext *avctx)
{
    int i;
    AVFrame *pic;
    s->mb_skiped = 0;

    if (s->pict_type != B_TYPE && s->last_picture_ptr && s->last_picture_ptr->data[0])
	{
        avcodec_default_release_buffer(avctx, (AVFrame*)s->last_picture_ptr);
    
        for(i=0; i<MAX_PICTURE_COUNT; i++)
		{
            if(s->picture[i].data[0] && &s->picture[i] != s->next_picture_ptr && s->picture[i].reference)
			{
                avcodec_default_release_buffer(avctx, (AVFrame*)&s->picture[i]);
            }
        }
    }

    for(i=0; i<MAX_PICTURE_COUNT; i++)
	{
        if(s->picture[i].data[0] && !s->picture[i].reference /*&& s->picture[i].type!=FF_BUFFER_TYPE_SHARED*/)
		{
            avcodec_default_release_buffer(s->avctx, (AVFrame*)&s->picture[i]);
        }
    }

    if(s->current_picture_ptr && s->current_picture_ptr->data[0]==NULL)
        pic= (AVFrame*)s->current_picture_ptr; //we allready have a unused image (maybe it was set before reading the header)
    else
	{
        i= ff_find_unused_picture(s, 0);
        pic= (AVFrame*)&s->picture[i];
    }

    pic->reference= s->pict_type != B_TYPE ? 3 : 0;

    if( alloc_picture(s, (Picture*)pic, 0) < 0)
        return -1;

    s->current_picture_ptr= (Picture*)pic;

    s->current_picture_ptr->pict_type= s->pict_type;
    s->current_picture_ptr->key_frame= s->pict_type == I_TYPE;

    copy_picture(&s->current_picture, s->current_picture_ptr);

    s->hurry_up= s->avctx->hurry_up;

    return 0;
}

void MPV_frame_end(MpegEncContext *s)
{
    if(s->unrestricted_mv && s->pict_type != B_TYPE && !s->intra_only )
	{
        draw_edges_c(s->current_picture.data[0], s->linesize  , s->h_edge_pos   , s->v_edge_pos   , EDGE_WIDTH  );
        draw_edges_c(s->current_picture.data[1], s->uvlinesize, s->h_edge_pos>>1, s->v_edge_pos>>1, EDGE_WIDTH/2);
        draw_edges_c(s->current_picture.data[2], s->uvlinesize, s->h_edge_pos>>1, s->v_edge_pos>>1, EDGE_WIDTH/2);
    } 
}

