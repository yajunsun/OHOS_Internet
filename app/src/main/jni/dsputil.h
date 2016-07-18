
#ifndef DSPUTIL_H
#define DSPUTIL_H

typedef short DCTELEM;

void clear_blocks_c(DCTELEM *blocks);

void ff_h264_idct_add_c(uint8_t *dst, DCTELEM *block, int stride);
void ff_h264_idct_dc_add_c(uint8_t *dst, DCTELEM *block, int stride);

#define MAX_NEG_CROP 1024

extern const uint8_t cropTbl[256 + 2 * MAX_NEG_CROP];

typedef void (*qpel_mc_func)(uint8_t *dst/*align width (8 or 16)*/, uint8_t *src/*align 1*/, int stride);
typedef void (*h264_chroma_mc_func)(uint8_t *dst/*align 8*/, uint8_t *src/*align 1*/, int srcStride, int h, int x, int y);

typedef struct DSPContext
{
    h264_chroma_mc_func put_h264_chroma_pixels_tab[3];
    h264_chroma_mc_func avg_h264_chroma_pixels_tab[3];

    qpel_mc_func put_h264_qpel_pixels_tab[4][16];
    qpel_mc_func avg_h264_qpel_pixels_tab[4][16];

    void (*h264_v_loop_filter_luma)(uint8_t *pix, int stride, int alpha, int beta, int8_t *tc0);
    void (*h264_h_loop_filter_luma)(uint8_t *pix, int stride, int alpha, int beta, int8_t *tc0);
    void (*h264_v_loop_filter_chroma)(uint8_t *pix, int stride, int alpha, int beta, int8_t *tc0);
    void (*h264_h_loop_filter_chroma)(uint8_t *pix, int stride, int alpha, int beta, int8_t *tc0);
    void (*h264_v_loop_filter_chroma_intra)(uint8_t *pix, int stride, int alpha, int beta);
    void (*h264_h_loop_filter_chroma_intra)(uint8_t *pix, int stride, int alpha, int beta);

	void (*h264_idct_add)(uint8_t *dst, DCTELEM *block, int stride);
    void (*h264_idct_dc_add)(uint8_t *dst, DCTELEM *block, int stride);
}DSPContext;

void dsputil_static_init(void);
void dsputil_init(DSPContext* p, AVCodecContext *avctx);

#define BYTE_VEC32(c)   ((c)*0x01010101UL)

static inline uint32_t rnd_avg32(uint32_t a, uint32_t b)
{
    return (a | b) - (((a ^ b) & ~BYTE_VEC32(0x01)) >> 1);
}

#endif
