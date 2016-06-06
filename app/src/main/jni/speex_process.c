#include <io.h>
#include <fcntl.h>
#include "speex_echo.h"
#include "speex_preprocess.h"
#include "speex_process.h"


static SpeexEchoState* m_pSET		= 0;
static SpeexPreprocessState* m_pSPS = 0;
char* m_noise;

JNIEXPORT jint JNICALL Java_com_speex_speexprocess_Speex_1init(JNIEnv *env, jobject obj, jint frame_size, jint filter_length, jint sample_rate)
{
	int tmp = sample_rate;
	m_pSET = speex_echo_state_init(frame_size, filter_length);
	m_pSPS = speex_preprocess_state_init(frame_size, tmp);
	speex_echo_ctl(m_pSET, SPEEX_ECHO_SET_SAMPLING_RATE, &tmp);
	m_noise = (char*) malloc(frame_size);
	return 0;

}

JNIEXPORT jint JNICALL Java_com_speex_speexprocess_Speex_1exit
		(JNIEnv *env, jobject obj)
{
	speex_echo_state_destroy(m_pSET);
	speex_preprocess_state_destroy(m_pSPS);
	if(m_noise){
		free(m_noise);
		m_noise = 0;
	}
	return 0;
}


JNIEXPORT jint JNICALL Java_com_speex_speexprocess_Speex_1process
		(JNIEnv *env, jobject obj, jbyteArray net_buf, jbyteArray mic_buf, jbyteArray out_buf)
{
	out_buf=net_buf;
	jbyte* netBuffer = (*env)->GetByteArrayElements(env,net_buf, 0);//(jbyte *)
	jbyte* micBuffer = (*env)->GetByteArrayElements(env,mic_buf, 0);
	jbyte* outBuffer = (*env)->GetByteArrayElements(env,out_buf, 0);
	speex_echo_cancel(m_pSET,
					  (const spx_int16_t*)netBuffer,
					  (const spx_int16_t*)micBuffer,
					  (spx_int16_t*)outBuffer,
					  0);
//	speex_echo_cancel(m_pSET,
//					  (const spx_int16_t*)netBuffer,
//					  (const spx_int16_t*)micBuffer,
//					  (spx_int16_t*)outBuffer,
//					  m_noise);
	speex_preprocess(m_pSPS, (spx_int16_t*)outBuffer, m_noise);
//	(*env)->ReleaseByteArrayElements(env,net_buf,netBuffer,0) ;
//	(*env)->ReleaseByteArrayElements(env,mic_buf,micBuffer,0) ;
//	(*env)->ReleaseByteArrayElements(env,out_buf,outBuffer,0) ;

	return 3;
}



