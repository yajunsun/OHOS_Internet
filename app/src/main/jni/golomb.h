
//#include <assert.h>

extern const uint8_t ff_golomb_vlc_len[512];
extern const uint8_t ff_ue_golomb_vlc_code[512];
extern const int8_t ff_se_golomb_vlc_code[512];

static inline int get_ue_golomb(GetBitContext *gb)
{
    unsigned int buf;
    int log;

    OPEN_READER(re, gb);
    UPDATE_CACHE(re, gb);
    buf=re_cache; //GET_CACHE(re, gb);

    if(buf >= (1<<27))
	{
        buf >>= 32 - 9;
        LAST_SKIP_BITS(re, gb, ff_golomb_vlc_len[buf]);
        CLOSE_READER(re, gb);

        return ff_ue_golomb_vlc_code[buf];
    }
	else
	{
        log= 2*av_log2(buf) - 31;
        buf>>= log;
        buf--;
        LAST_SKIP_BITS(re, gb, 32 - log);
        CLOSE_READER(re, gb);

        return buf;
    }
}

static inline int get_te0_golomb(GetBitContext *gb, int range)
{
//    assert(range >= 1);

    if(range==1)      return 0;
    else if(range==2) return get_bits1(gb)^1;
    else              return get_ue_golomb(gb);
}

static inline int get_se_golomb(GetBitContext *gb)
{
    unsigned int buf;
    int log;

    OPEN_READER(re, gb);
    UPDATE_CACHE(re, gb);
    buf=re_cache; //GET_CACHE(re, gb);

    if(buf >= (1<<27))
	{
        buf >>= 32 - 9;
        LAST_SKIP_BITS(re, gb, ff_golomb_vlc_len[buf]);
        CLOSE_READER(re, gb);

        return ff_se_golomb_vlc_code[buf];
    }
	else
	{
        log= 2*av_log2(buf) - 31;
        buf>>= log;

        LAST_SKIP_BITS(re, gb, 32 - log);
        CLOSE_READER(re, gb);

        if(buf&1) buf= -(buf>>1);
        else      buf=  (buf>>1);

        return buf;
    }
}
