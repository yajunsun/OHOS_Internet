
#ifndef COMMON_H
#define COMMON_H

#define WINCE
#define HAVE_AV_CONFIG_H
#define EMULATE_INTTYPES

#define inline __inline

#define ALT_BITSTREAM_READER

#define LIBMPEG2_BITSTREAM_READER_HACK //add BERO

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>

/*
typedef signed char  int8_t;
typedef signed short int16_t;
typedef signed int   int32_t;
typedef unsigned char  uint8_t;
typedef unsigned short uint16_t;
typedef unsigned int   uint32_t;

#ifdef WIN32
typedef signed __int64   int64_t;
typedef unsigned __int64 uint64_t;
#else
typedef signed long long   int64_t;
typedef unsigned long long uint64_t;
#endif
// */

#define ABS(a) ((a) >= 0 ? (a) : (-(a)))

#define FFMAX(a,b) ((a) > (b) ? (a) : (b))
#define FFMIN(a,b) ((a) > (b) ? (b) : (a))

#define FASTDIV(a,b)   ((a)/(b))

#define NEG_SSR32(a,s) ((( int32_t)(a))>>(32-(s)))
#define NEG_USR32(a,s) (((uint32_t)(a))>>(32-(s)))

typedef struct GetBitContext
{
    const uint8_t *buffer, *buffer_end;
    int index;
    int size_in_bits;
}GetBitContext;

#define VLC_TYPE int16_t

typedef struct VLC 
{
    int bits;
    VLC_TYPE (*table)[2]; ///< code, bits
    int table_size, table_allocated;
}VLC;

static inline int unaligned32_be(const void *v)
{
	const uint8_t *p=(uint8_t *)v;
	return (((p[0]<<8) | p[1])<<16) | (p[2]<<8) | (p[3]);
}

#define MIN_CACHE_BITS 25

#define OPEN_READER(name, gb)\
        int name##_index= (gb)->index;\
        int name##_cache= 0;

#define CLOSE_READER(name, gb)\
        (gb)->index= name##_index;

#define UPDATE_CACHE(name, gb)\
        name##_cache= unaligned32_be( ((uint8_t *)(gb)->buffer)+(name##_index>>3) ) << (name##_index&0x07);\

#define SKIP_CACHE(name, gb, num)\
        name##_cache <<= (num);

#define SKIP_COUNTER(name, gb, num)\
        name##_index += (num);

#define SKIP_BITS(name, gb, num)\
        {\
            SKIP_CACHE(name, gb, num)\
            SKIP_COUNTER(name, gb, num)\
        }

#define LAST_SKIP_BITS(name, gb, num) SKIP_COUNTER(name, gb, num)
#define LAST_SKIP_CACHE(name, gb, num) ;

#define SHOW_UBITS(name, gb, num) NEG_USR32(name##_cache, num)

#define SHOW_SBITS(name, gb, num) NEG_SSR32(name##_cache, num)

#define GET_CACHE(name, gb) ((uint32_t)name##_cache)

static inline int get_bits_count(GetBitContext *s)
{
    return s->index;
}

static inline unsigned int get_bits(GetBitContext *s, int n)
{
    register int tmp;
    OPEN_READER(re, s)
    UPDATE_CACHE(re, s)
    tmp= SHOW_UBITS(re, s, n);
    LAST_SKIP_BITS(re, s, n)
    CLOSE_READER(re, s)
    return tmp;
}

unsigned int get_bits_long(GetBitContext *s, int n);

static inline unsigned int show_bits(GetBitContext *s, int n)
{
    register int tmp;
    OPEN_READER(re, s)
    UPDATE_CACHE(re, s)
    tmp= SHOW_UBITS(re, s, n);
//    CLOSE_READER(re, s)
    return tmp;
}

unsigned int show_bits_long(GetBitContext *s, int n);

static inline void skip_bits(GetBitContext *s, int n)
{
    OPEN_READER(re, s)
    UPDATE_CACHE(re, s)
    LAST_SKIP_BITS(re, s, n)
    CLOSE_READER(re, s)
}

unsigned int get_bits1(GetBitContext *s);

static inline unsigned int show_bits1(GetBitContext *s)
{
    return show_bits(s, 1);
}

static inline void skip_bits1(GetBitContext *s)
{
    skip_bits(s, 1);
}

static inline void init_get_bits(GetBitContext *s, const uint8_t *buffer, int bit_size)
{
    const int buffer_size= (bit_size+7)>>3;

    s->buffer= buffer;
    s->size_in_bits= bit_size;
    s->buffer_end= buffer + buffer_size;
    s->index=0;

    {
        OPEN_READER(re, s)
        UPDATE_CACHE(re, s)
        UPDATE_CACHE(re, s)
        CLOSE_READER(re, s)
    }
}

int check_marker(GetBitContext *s, const char *msg);
void align_get_bits(GetBitContext *s);
int init_vlc(VLC *vlc, int nb_bits, int nb_codes,
             const void *bits, int bits_wrap, int bits_size,
             const void *codes, int codes_wrap, int codes_size);
void free_vlc(VLC *vlc);

/**
 *
 * if the vlc code is invalid and max_depth=1 than no bits will be removed
 * if the vlc code is invalid and max_depth>1 than the number of bits removed
 * is undefined
 */
#define GET_VLC(code, name, gb, table, bits, max_depth)\
{\
    int n, index, nb_bits;\
\
    index= SHOW_UBITS(name, gb, bits);\
    code = table[index][0];\
    n    = table[index][1];\
\
    if(max_depth > 1 && n < 0){\
        LAST_SKIP_BITS(name, gb, bits)\
        UPDATE_CACHE(name, gb)\
\
        nb_bits = -n;\
\
        index= SHOW_UBITS(name, gb, nb_bits) + code;\
        code = table[index][0];\
        n    = table[index][1];\
        if(max_depth > 2 && n < 0){\
            LAST_SKIP_BITS(name, gb, nb_bits)\
            UPDATE_CACHE(name, gb)\
\
            nb_bits = -n;\
\
            index= SHOW_UBITS(name, gb, nb_bits) + code;\
            code = table[index][0];\
            n    = table[index][1];\
        }\
    }\
    SKIP_BITS(name, gb, n)\
}

static inline int get_vlc2(GetBitContext *s, VLC_TYPE (*table)[2], int bits, int max_depth)
{
    int code;

    OPEN_READER(re, s)
    UPDATE_CACHE(re, s)

    GET_VLC(code, re, s, table, bits, max_depth)

    CLOSE_READER(re, s)
    return code;
}

extern const uint8_t ff_log2_tab[256];

static inline int av_log2(unsigned int v)
{
    int n;

    n = 0;
    if (v & 0xffff0000)
	{
        v >>= 16;
        n += 16;
    }
    if (v & 0xff00)
	{
        v >>= 8;
        n += 8;
    }
    n += ff_log2_tab[v];

    return n;
}

static inline int mid_pred(int a, int b, int c)
{
    if(a>b)
	{
        if(c>b)
		{
            if(c>a) b=a;
            else    b=c;
        }
    }
	else
	{
        if(b>c)
		{
            if(c>a) b=c;
            else    b=a;
        }
    }
    return b;
}

static inline int clip(int a, int amin, int amax)
{
    if (a < amin)
        return amin;
    else if (a > amax)
        return amax;
    else
        return a;
}

static inline int clip_uint8(int a)
{
    if (a&(~255)) return (-a)>>31;
    else          return a;
}

#define COPY3_IF_LT(x,y,a,b,c,d)\
if((y)<(x)){\
     (x)=(y);\
     (a)=(b);\
     (c)=(d);\
}

#define CLAMP_TO_8BIT(d) ((d > 0xff) ? 0xff : (d < 0) ? 0 : d)

#define CHECKED_ALLOCZ(p, size)\
{\
    p= av_mallocz(size);\
    if(p==NULL && (size)!=0){\
        goto fail;\
    }\
}

#endif
