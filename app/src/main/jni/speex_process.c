#include <io.h>
#include <fcntl.h>
#include "speex_echo.h"
#include "speex_preprocess.h"
#include "speex_process.h"

static SpeexEchoState* m_pSET		= 0;
static SpeexPreprocessState* m_pSPS = 0;

JNIEXPORT jint JNICALL Java_com_speex_speexprocess_Speex_1init(JNIEnv *env, jobject obj, jint frame_size, jint filter_length, jint sample_rate)
{
	int tmp = sample_rate;
	m_pSET = (*env)->speex_echo_state_init(frame_size, filter_length);
	m_pSPS = (*env)->speex_preprocess_state_init(frame_size, tmp);
	(*env)->speex_echo_ctl(m_pSET, SPEEX_ECHO_SET_SAMPLING_RATE, &tmp);
	return 0;
}

JNIEXPORT jint JNICALL Java_com_speex_speexprocess_Speex_1exit
		(JNIEnv *env, jobject obj)
{
	(*env)->speex_echo_state_destroy(m_pSET);
	(*env)->speex_preprocess_state_destroy(m_pSPS);
	return 0;
}


JNIEXPORT jint JNICALL Java_com_speex_speexprocess_Speex_1process
		(JNIEnv *env, jobject obj, jbyteArray net_buf, jbyteArray mic_buf, jbyteArray out_buf, jbyteArray noise)
{
	(*env)->speex_echo_cancel(m_pSET,
				(const spx_int16_t*)net_buf, 
				(const spx_int16_t*)mic_buf, 
				(spx_int16_t*)out_buf, 
				noise);
	(*env)->speex_preprocess(m_pSPS, (spx_int16_t*)out_buf, noise);
	return 0;
}



