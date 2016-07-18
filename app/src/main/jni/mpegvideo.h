
#ifndef AVCODEC_MPEGVIDEO_H
#define AVCODEC_MPEGVIDEO_H

#include "dsputil.h"

#define EDGE_WIDTH 16

#define MAX_PICTURE_COUNT 15

#define I_TYPE 1
#define P_TYPE 2
#define B_TYPE 3
#define S_TYPE 4
#define SI_TYPE 5
#define SP_TYPE 6

#define MB_TYPE_INTRA MB_TYPE_INTRA4x4 //default mb_type if theres just one type
#define IS_INTRA4x4(a)   ((a)&MB_TYPE_INTRA4x4)
#define IS_INTRA16x16(a) ((a)&MB_TYPE_INTRA16x16)
#define IS_PCM(a)        ((a)&MB_TYPE_INTRA_PCM)
#define IS_INTRA(a)      ((a)&7)
#define IS_INTER(a)      ((a)&(MB_TYPE_16x16|MB_TYPE_16x8|MB_TYPE_8x16|MB_TYPE_8x8))
#define IS_SKIP(a)       ((a)&MB_TYPE_SKIP)
#define IS_INTRA_PCM(a)  ((a)&MB_TYPE_INTRA_PCM)
#define IS_INTERLACED(a) ((a)&MB_TYPE_INTERLACED)
#define IS_DIRECT(a)     ((a)&MB_TYPE_DIRECT2)
#define IS_GMC(a)        ((a)&MB_TYPE_GMC)
#define IS_16X16(a)      ((a)&MB_TYPE_16x16)
#define IS_16X8(a)       ((a)&MB_TYPE_16x8)
#define IS_8X16(a)       ((a)&MB_TYPE_8x16)
#define IS_8X8(a)        ((a)&MB_TYPE_8x8)
#define IS_SUB_8X8(a)    ((a)&MB_TYPE_16x16) //note reused
#define IS_SUB_8X4(a)    ((a)&MB_TYPE_16x8)  //note reused
#define IS_SUB_4X8(a)    ((a)&MB_TYPE_8x16)  //note reused
#define IS_SUB_4X4(a)    ((a)&MB_TYPE_8x8)   //note reused
#define IS_ACPRED(a)     ((a)&MB_TYPE_ACPRED)
#define IS_QUANT(a)      ((a)&MB_TYPE_QUANT)
#define IS_DIR(a, part, list) ((a) & (MB_TYPE_P0L0<<((part)+2*(list))))
#define USES_LIST(a, list) ((a) & ((MB_TYPE_P0L0|MB_TYPE_P1L0)<<(2*(list)))) ///< does this mb use listX, note doesnt work if subMBs
#define HAS_CBP(a)        ((a)&MB_TYPE_CBP)

#define VP_START            1          ///< current MB is the first after a resync marker
#define AC_ERROR            2
#define DC_ERROR            4
#define MV_ERROR            8
#define AC_END              16
#define DC_END              32
#define MV_END              64

#define PICT_TOP_FIELD     1
#define PICT_BOTTOM_FIELD  2
#define PICT_FRAME         3

typedef struct Picture
{
    uint8_t *data[4];
    int linesize[4];
    uint8_t *base[4];
    int key_frame;
    int pict_type;
    int reference;
    int8_t *qscale_table;
    int16_t (*motion_val[2])[2];
    uint32_t *mb_type;

    uint8_t *interpolated[3];
    int16_t (*motion_val_base[2])[2];
    int8_t *ref_index[2];
    uint32_t *mb_type_base;

    int field_poc[2];           ///< h264 top/bottom POC
    int poc;                    ///< h264 frame POC
    int frame_num;              ///< h264 frame_num
    int pic_id;                 ///< h264 pic_num or long_term_pic_idx
    int long_ref;               ///< 1->long term reference 0->short term reference

} Picture;

struct MpegEncContext;

typedef struct MpegEncContext
{
    AVCodecContext *avctx;
    int width, height;///< picture size. must be a multiple of 16
    int gop_size;
    int intra_only;   ///< if true, only intra pictures are generated

    int flags;        ///< AVCodecContext.flags (HQ, MV4, ...)

    /* sequence parameters */
    int context_initialized;
    int picture_number;       //FIXME remove, unclear definition

	int mb_width, mb_height;   ///< number of MBs horizontally & vertically
    int mb_stride;             ///< mb_width+1 used for some arrays to allow simple addressng of left & top MBs withoutt sig11
    int b8_stride;             ///< 2*mb_width+1 used for some 8x8 block arrays to allow simple addressng
    int b4_stride;             ///< 4*mb_width+1 used for some 4x4 block arrays to allow simple addressng
    int h_edge_pos, v_edge_pos;///< horizontal / vertical position of the right/bottom edge (pixel replicateion)
    int mb_num;                ///< number of MBs of a picture
    int linesize;              ///< line size, in bytes, may be different from width
    int uvlinesize;            ///< line size, for chroma in bytes, may be different from width
    Picture *picture;          ///< main picture buffer

    Picture current_picture;    ///< buffer to store the decompressed current picture

    Picture *last_picture_ptr;     ///< pointer to the previous picture.
    Picture *next_picture_ptr;     ///< pointer to the next picture (for bidir pred)
    Picture *current_picture_ptr;  ///< pointer to the current picture

#define PREV_PICT_TYPES_BUFFER_SIZE 256
    int mb_skiped;                ///< MUST BE SET only during DECODING

    uint8_t *cbp_table;           ///< used to store cbp, ac_pred for partitioned decoding

    int qscale;                 ///< QP

    int pict_type;              ///< I_TYPE, P_TYPE, B_TYPE, ...

    int unrestricted_mv;        ///< mv can point outside of the coded picture

    DSPContext dsp;             ///< pointers for accelerated dsp fucntions

    int hurry_up;     /**< when set to 1 during decoding, b frames will be skiped
                         when set to 2 idct/dequant will be skipped too */

    /* macroblock layer */
    int mb_x, mb_y;
    int mb_skip_run;
    int mb_intra;

    uint8_t *dest[3];

    /* mpeg4 specific */
    int data_partitioning;           ///< data partitioning flag from header
    int partitioned_frame;           ///< is current frame partitioned
    int low_delay;                   ///< no reordering needed / has no b-frames

    /* lavc specific stuff, used to workaround bugs in libavcodec */
    int ffmpeg_version;
    int lavc_build;

    /* decompression specific */
    GetBitContext gb;

    int broken_link;         ///< no_output_of_prior_pics_flag

    /* MPEG2 specific - I wish I had not to support this mess. */
    int progressive_sequence;
    int picture_structure;

    int progressive_frame;

} MpegEncContext;

int MPV_common_init(MpegEncContext *s);
void MPV_common_end(MpegEncContext *s);
int MPV_frame_start(MpegEncContext *s, AVCodecContext *avctx);
void MPV_frame_end(MpegEncContext *s);

int ff_find_unused_picture(MpegEncContext *s, int shared);

#endif
