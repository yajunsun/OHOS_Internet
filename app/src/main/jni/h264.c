
#include "common.h"
#include "avcodec.h"
#include "h264.h"

static void filter_mb( H264Context *h, int mb_x, int mb_y, uint8_t *img_y, uint8_t *img_cb, uint8_t *img_cr);

const uint8_t cropTbl[256 + 2 * MAX_NEG_CROP] = {
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
	0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
	0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F,
	0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F,
	0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
	0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F,
	0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F,
	0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F,
	0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0x8D, 0x8E, 0x8F,
	0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9A, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F,
	0xA0, 0xA1, 0xA2, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9, 0xAA, 0xAB, 0xAC, 0xAD, 0xAE, 0xAF,
	0xB0, 0xB1, 0xB2, 0xB3, 0xB4, 0xB5, 0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xBB, 0xBC, 0xBD, 0xBE, 0xBF,
	0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF,
	0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF,
	0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF,
	0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
	0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
};

static  uint32_t pack16to32(int a, int b)
{
#ifdef WORDS_BIGENDIAN
	return (b&0xFFFF) + (a<<16);
#else
	return (a&0xFFFF) + (b<<16);
#endif
}

static  void fill_rectangle(void *vp, int w, int h, int stride, uint32_t val, int size) //FIXME ensure this IS inlined
{
	uint8_t *p= (uint8_t*)vp;
	uint16_t temp16;
	uint32_t temp32;
	
    w      *= size;
    stride *= size;
	
    if(w==2 && h==2)
	{
		temp16=size==4 ? val : val*0x0101;
		memcpy(p+0,&temp16,sizeof(temp16));
		memcpy(p+stride,&temp16,sizeof(temp16));
    }
	else if(w==2 && h==4)
	{
		temp16=size==4 ? val : val*0x0101;
		memcpy(p+0*stride,&temp16,sizeof(temp16));
		memcpy(p+1*stride,&temp16,sizeof(temp16));
		memcpy(p+2*stride,&temp16,sizeof(temp16));
		memcpy(p+3*stride,&temp16,sizeof(temp16));
    }
	else if(w==4 && h==1)
	{
		temp32=size==4 ? val : val*0x01010101;
		memcpy(p+0*stride,&temp32,sizeof(temp32));
    }
	else if(w==4 && h==2)
	{
		temp32=size==4 ? val : val*0x01010101;
		memcpy(p+0*stride,&temp32,sizeof(temp32));
		memcpy(p+1*stride,&temp32,sizeof(temp32));
    }
	else if(w==4 && h==4)
	{
		temp32=size==4 ? val : val*0x01010101;
		memcpy((p + 0*stride),&temp32,sizeof(temp32));
		memcpy((p + 1*stride),&temp32,sizeof(temp32));
		memcpy((p + 2*stride),&temp32,sizeof(temp32));
		memcpy((p + 3*stride),&temp32,sizeof(temp32));
    }
	else if(w==8 && h==1)
	{
		temp32=size==4 ? val : val*0x01010101;
		memcpy(p+0,&temp32,sizeof(temp32));
		memcpy(p+4,&temp32,sizeof(temp32));
    }
	else if(w==8 && h==2)
	{
		temp32=size==4 ? val : val*0x01010101;
		memcpy(p+0+0*stride,&temp32,sizeof(temp32));
		memcpy(p+4+0*stride,&temp32,sizeof(temp32));
		memcpy(p+0+1*stride,&temp32,sizeof(temp32));
		memcpy(p+4+1*stride,&temp32,sizeof(temp32));
    }
	else if(w==8 && h==4)
	{
		temp32=size==4 ? val : val*0x01010101;
		memcpy(p+0*stride,&temp32,sizeof(temp32)); 		memcpy(p+0*stride+4,&temp32,sizeof(temp32));
		memcpy(p+1*stride,&temp32,sizeof(temp32)); 		memcpy(p+1*stride+4,&temp32,sizeof(temp32));
		memcpy(p+2*stride,&temp32,sizeof(temp32));		memcpy(p+2*stride+4,&temp32,sizeof(temp32));
		memcpy(p+3*stride,&temp32,sizeof(temp32));		memcpy(p+3*stride+4,&temp32,sizeof(temp32));
    }
	else if(w==16 && h==2)
	{
		temp32=size==4 ? val : val*0x01010101;
		memcpy(p+0+0*stride,&temp32,sizeof(temp32));	memcpy(p+0+0*stride+4,&temp32,sizeof(temp32));
		memcpy(p+8+0*stride,&temp32,sizeof(temp32));	memcpy(p+8+0*stride+4,&temp32,sizeof(temp32));
		memcpy(p+0+1*stride,&temp32,sizeof(temp32));	memcpy(p+0+1*stride+4,&temp32,sizeof(temp32));
		memcpy(p+8+1*stride,&temp32,sizeof(temp32));	memcpy(p+8+1*stride+4,&temp32,sizeof(temp32));
    }
	else if(w==16 && h==4)
	{
		temp32=size==4 ? val : val*0x01010101;
		memcpy(p+0+0*stride,&temp32,sizeof(temp32));	memcpy(p+0+0*stride+4,&temp32,sizeof(temp32));
		memcpy(p+8+0*stride,&temp32,sizeof(temp32));	memcpy(p+8+0*stride+4,&temp32,sizeof(temp32));
		memcpy(p+0+1*stride,&temp32,sizeof(temp32));	memcpy(p+0+1*stride+4,&temp32,sizeof(temp32));
		memcpy(p+8+1*stride,&temp32,sizeof(temp32));	memcpy(p+8+1*stride+4,&temp32,sizeof(temp32));
		memcpy(p+0+2*stride,&temp32,sizeof(temp32));	memcpy(p+0+2*stride+4,&temp32,sizeof(temp32));
		memcpy(p+8+2*stride,&temp32,sizeof(temp32));	memcpy(p+8+2*stride+4,&temp32,sizeof(temp32));
		memcpy(p+0+3*stride,&temp32,sizeof(temp32));	memcpy(p+0+3*stride+4,&temp32,sizeof(temp32));
		memcpy(p+8+3*stride,&temp32,sizeof(temp32));	memcpy(p+8+3*stride+4,&temp32,sizeof(temp32));
    }
//	else
//      assert(0);
}

static  void fill_caches(H264Context *h, int mb_type)
{
    MpegEncContext * const s = &h->s;
    const int mb_xy= s->mb_x + s->mb_y*s->mb_stride;
    int topleft_xy, top_xy, topright_xy, left_xy[2];
    int topleft_type, top_type, topright_type, left_type[2];
    int left_block[4];
    int i;
	
    if(h->sps.mb_aff)
	{
        topleft_xy = 0; /* avoid warning */
        top_xy = 0; /* avoid warning */
        topright_xy = 0; /* avoid warning */
    }
	else
	{
        topleft_xy = mb_xy-1 - s->mb_stride;
        top_xy     = mb_xy   - s->mb_stride;
        topright_xy= mb_xy+1 - s->mb_stride;
        left_xy[0]   = mb_xy-1;
        left_xy[1]   = mb_xy-1;
        left_block[0]= 0;
        left_block[1]= 1;
        left_block[2]= 2;
        left_block[3]= 3;
    }
	
    topleft_type = h->slice_table[topleft_xy ] == h->slice_num ? s->current_picture.mb_type[topleft_xy] : 0;
    top_type     = h->slice_table[top_xy     ] == h->slice_num ? s->current_picture.mb_type[top_xy]     : 0;
    topright_type= h->slice_table[topright_xy] == h->slice_num ? s->current_picture.mb_type[topright_xy]: 0;
    left_type[0] = h->slice_table[left_xy[0] ] == h->slice_num ? s->current_picture.mb_type[left_xy[0]] : 0;
    left_type[1] = h->slice_table[left_xy[1] ] == h->slice_num ? s->current_picture.mb_type[left_xy[1]] : 0;
	
    if(IS_INTRA(mb_type))
	{
        h->topleft_samples_available=
			h->top_samples_available=
			h->left_samples_available= 0xFFFF;
        h->topright_samples_available= 0xEEEA;
		
        if(!IS_INTRA(top_type) && (top_type==0 || h->pps.constrained_intra_pred))
		{
            h->topleft_samples_available= 0xB3FF;
            h->top_samples_available= 0x33FF;
            h->topright_samples_available= 0x26EA;
        }
        for(i=0; i<2; i++)
		{
            if(!IS_INTRA(left_type[i]) && (left_type[i]==0 || h->pps.constrained_intra_pred))
			{
                h->topleft_samples_available&= 0xDF5F;
                h->left_samples_available&= 0x5F5F;
            }
        }
		
        if(!IS_INTRA(topleft_type) && (topleft_type==0 || h->pps.constrained_intra_pred))
            h->topleft_samples_available&= 0x7FFF;
		
        if(!IS_INTRA(topright_type) && (topright_type==0 || h->pps.constrained_intra_pred))
            h->topright_samples_available&= 0xFBFF;
		
        if(IS_INTRA4x4(mb_type))
		{
            if(IS_INTRA4x4(top_type))
			{
                h->intra4x4_pred_mode_cache[4+8*0]= h->intra4x4_pred_mode[top_xy][4];
                h->intra4x4_pred_mode_cache[5+8*0]= h->intra4x4_pred_mode[top_xy][5];
                h->intra4x4_pred_mode_cache[6+8*0]= h->intra4x4_pred_mode[top_xy][6];
                h->intra4x4_pred_mode_cache[7+8*0]= h->intra4x4_pred_mode[top_xy][3];
            }
			else
			{
                int pred;
                if(IS_INTRA16x16(top_type) || (IS_INTER(top_type) && !h->pps.constrained_intra_pred))
                    pred= 2;
                else
                    pred= -1;
				
                h->intra4x4_pred_mode_cache[4+8*0]=
					h->intra4x4_pred_mode_cache[5+8*0]=
					h->intra4x4_pred_mode_cache[6+8*0]=
					h->intra4x4_pred_mode_cache[7+8*0]= pred;
            }
            for(i=0; i<2; i++)
			{
                if(IS_INTRA4x4(left_type[i]))
				{
                    h->intra4x4_pred_mode_cache[3+8*1 + 2*8*i]= h->intra4x4_pred_mode[left_xy[i]][left_block[0+2*i]];
                    h->intra4x4_pred_mode_cache[3+8*2 + 2*8*i]= h->intra4x4_pred_mode[left_xy[i]][left_block[1+2*i]];
                }
				else
				{
                    int pred;
                    if(IS_INTRA16x16(left_type[i]) || (IS_INTER(left_type[i]) && !h->pps.constrained_intra_pred))
                        pred= 2;
                    else
                        pred= -1;
					
                    h->intra4x4_pred_mode_cache[3+8*1 + 2*8*i]=
						h->intra4x4_pred_mode_cache[3+8*2 + 2*8*i]= pred;
                }
            }
        }
    }
	/*
	0 . T T. T T T T
	1 L . .L . . . .
	2 L . .L . . . .
	3 . T TL . . . .
	4 L . .L . . . .
	5 L . .. . . . .
	*/
    if(top_type)
	{
        h->non_zero_count_cache[4+8*0]= h->non_zero_count[top_xy][0];
        h->non_zero_count_cache[5+8*0]= h->non_zero_count[top_xy][1];
        h->non_zero_count_cache[6+8*0]= h->non_zero_count[top_xy][2];
        h->non_zero_count_cache[7+8*0]= h->non_zero_count[top_xy][3];
		
        h->non_zero_count_cache[1+8*0]= h->non_zero_count[top_xy][7];
        h->non_zero_count_cache[2+8*0]= h->non_zero_count[top_xy][8];
		
        h->non_zero_count_cache[1+8*3]= h->non_zero_count[top_xy][10];
        h->non_zero_count_cache[2+8*3]= h->non_zero_count[top_xy][11];
    }
	else
	{
        h->non_zero_count_cache[4+8*0]=
		h->non_zero_count_cache[5+8*0]=
		h->non_zero_count_cache[6+8*0]=
		h->non_zero_count_cache[7+8*0]=
		
		h->non_zero_count_cache[1+8*0]=
		h->non_zero_count_cache[2+8*0]=
		
		h->non_zero_count_cache[1+8*3]=
		h->non_zero_count_cache[2+8*3]= 64;
    }
	
    if(left_type[0])
	{
        h->non_zero_count_cache[3+8*1]= h->non_zero_count[left_xy[0]][6];
        h->non_zero_count_cache[3+8*2]= h->non_zero_count[left_xy[0]][5];
        h->non_zero_count_cache[0+8*1]= h->non_zero_count[left_xy[0]][9]; //FIXME left_block
        h->non_zero_count_cache[0+8*4]= h->non_zero_count[left_xy[0]][12];
    }
	else
	{
        h->non_zero_count_cache[3+8*1]=
		h->non_zero_count_cache[3+8*2]=
		h->non_zero_count_cache[0+8*1]=
		h->non_zero_count_cache[0+8*4]= 64;
    }
	
    if(left_type[1])
	{
        h->non_zero_count_cache[3+8*3]= h->non_zero_count[left_xy[1]][4];
        h->non_zero_count_cache[3+8*4]= h->non_zero_count[left_xy[1]][3];
        h->non_zero_count_cache[0+8*2]= h->non_zero_count[left_xy[1]][8];
        h->non_zero_count_cache[0+8*5]= h->non_zero_count[left_xy[1]][11];
    }
	else
	{
        h->non_zero_count_cache[3+8*3]=
		h->non_zero_count_cache[3+8*4]=
		h->non_zero_count_cache[0+8*2]=
		h->non_zero_count_cache[0+8*5]= 64;
    }
	
    if(IS_INTER(mb_type))
	{
        int list;
        for(list=0; list<2; list++)
		{
            if((!IS_8X8(mb_type)) && !USES_LIST(mb_type, list))
                continue; //FIXME direct mode ...
			
            if(IS_INTER(topleft_type))
			{
                const int b_xy = h->mb2b_xy[topleft_xy] + 3 + 3*h->b_stride;
                const int b8_xy= h->mb2b8_xy[topleft_xy] + 1 + h->b8_stride;
                *(uint32_t*)h->mv_cache[list][scan8[0] - 1 - 1*8]= *(uint32_t*)s->current_picture.motion_val[list][b_xy];
                h->ref_cache[list][scan8[0] - 1 - 1*8]= s->current_picture.ref_index[list][b8_xy];
            }
			else
			{
                *(uint32_t*)h->mv_cache[list][scan8[0] - 1 - 1*8]= 0;
                h->ref_cache[list][scan8[0] - 1 - 1*8]= topleft_type ? LIST_NOT_USED : PART_NOT_AVAILABLE;
            }
			
            if(IS_INTER(top_type))
			{
                const int b_xy= h->mb2b_xy[top_xy] + 3*h->b_stride;
                const int b8_xy= h->mb2b8_xy[top_xy] + h->b8_stride;
                *(uint32_t*)h->mv_cache[list][scan8[0] + 0 - 1*8]= *(uint32_t*)s->current_picture.motion_val[list][b_xy + 0];
                *(uint32_t*)h->mv_cache[list][scan8[0] + 1 - 1*8]= *(uint32_t*)s->current_picture.motion_val[list][b_xy + 1];
                *(uint32_t*)h->mv_cache[list][scan8[0] + 2 - 1*8]= *(uint32_t*)s->current_picture.motion_val[list][b_xy + 2];
                *(uint32_t*)h->mv_cache[list][scan8[0] + 3 - 1*8]= *(uint32_t*)s->current_picture.motion_val[list][b_xy + 3];
                h->ref_cache[list][scan8[0] + 0 - 1*8]=
				h->ref_cache[list][scan8[0] + 1 - 1*8]= s->current_picture.ref_index[list][b8_xy + 0];
                h->ref_cache[list][scan8[0] + 2 - 1*8]=
				h->ref_cache[list][scan8[0] + 3 - 1*8]= s->current_picture.ref_index[list][b8_xy + 1];
            }
			else
			{
                *(uint32_t*)h->mv_cache [list][scan8[0] + 0 - 1*8]=
				*(uint32_t*)h->mv_cache [list][scan8[0] + 1 - 1*8]=
				*(uint32_t*)h->mv_cache [list][scan8[0] + 2 - 1*8]=
				*(uint32_t*)h->mv_cache [list][scan8[0] + 3 - 1*8]= 0;
                *(uint32_t*)&h->ref_cache[list][scan8[0] + 0 - 1*8]= ((top_type ? LIST_NOT_USED : PART_NOT_AVAILABLE)&0xFF)*0x01010101;
            }
			
            if(IS_INTER(topright_type))
			{
                const int b_xy= h->mb2b_xy[topright_xy] + 3*h->b_stride;
                const int b8_xy= h->mb2b8_xy[topright_xy] + h->b8_stride;
                *(uint32_t*)h->mv_cache[list][scan8[0] + 4 - 1*8]= *(uint32_t*)s->current_picture.motion_val[list][b_xy];
                h->ref_cache[list][scan8[0] + 4 - 1*8]= s->current_picture.ref_index[list][b8_xy];
            }
			else
			{
                *(uint32_t*)h->mv_cache [list][scan8[0] + 4 - 1*8]= 0;
                h->ref_cache[list][scan8[0] + 4 - 1*8]= topright_type ? LIST_NOT_USED : PART_NOT_AVAILABLE;
            }
			
            //FIXME unify cleanup or sth
            if(IS_INTER(left_type[0]))
			{
                const int b_xy= h->mb2b_xy[left_xy[0]] + 3;
                const int b8_xy= h->mb2b8_xy[left_xy[0]] + 1;
                *(uint32_t*)h->mv_cache[list][scan8[0] - 1 + 0*8]= *(uint32_t*)s->current_picture.motion_val[list][b_xy + h->b_stride*left_block[0]];
                *(uint32_t*)h->mv_cache[list][scan8[0] - 1 + 1*8]= *(uint32_t*)s->current_picture.motion_val[list][b_xy + h->b_stride*left_block[1]];
                h->ref_cache[list][scan8[0] - 1 + 0*8]=
				h->ref_cache[list][scan8[0] - 1 + 1*8]= s->current_picture.ref_index[list][b8_xy + h->b8_stride*(left_block[0]>>1)];
            }
			else
			{
                *(uint32_t*)h->mv_cache [list][scan8[0] - 1 + 0*8]=
				*(uint32_t*)h->mv_cache [list][scan8[0] - 1 + 1*8]= 0;
                h->ref_cache[list][scan8[0] - 1 + 0*8]=
				h->ref_cache[list][scan8[0] - 1 + 1*8]= left_type[0] ? LIST_NOT_USED : PART_NOT_AVAILABLE;
            }
			
            if(IS_INTER(left_type[1]))
			{
                const int b_xy= h->mb2b_xy[left_xy[1]] + 3;
                const int b8_xy= h->mb2b8_xy[left_xy[1]] + 1;
                *(uint32_t*)h->mv_cache[list][scan8[0] - 1 + 2*8]= *(uint32_t*)s->current_picture.motion_val[list][b_xy + h->b_stride*left_block[2]];
                *(uint32_t*)h->mv_cache[list][scan8[0] - 1 + 3*8]= *(uint32_t*)s->current_picture.motion_val[list][b_xy + h->b_stride*left_block[3]];
                h->ref_cache[list][scan8[0] - 1 + 2*8]=
				h->ref_cache[list][scan8[0] - 1 + 3*8]= s->current_picture.ref_index[list][b8_xy + h->b8_stride*(left_block[2]>>1)];
            }
			else
			{
                *(uint32_t*)h->mv_cache [list][scan8[0] - 1 + 2*8]=
				*(uint32_t*)h->mv_cache [list][scan8[0] - 1 + 3*8]= 0;
                h->ref_cache[list][scan8[0] - 1 + 2*8]=
				h->ref_cache[list][scan8[0] - 1 + 3*8]= left_type[0] ? LIST_NOT_USED : PART_NOT_AVAILABLE;
            }
			
            h->ref_cache[list][scan8[5 ]+1] =
			h->ref_cache[list][scan8[7 ]+1] =
			h->ref_cache[list][scan8[13]+1] =  //FIXME remove past 3 (init somewher else)
			h->ref_cache[list][scan8[4 ]] =
			h->ref_cache[list][scan8[12]] = PART_NOT_AVAILABLE;
            *(uint32_t*)h->mv_cache [list][scan8[5 ]+1]=
			*(uint32_t*)h->mv_cache [list][scan8[7 ]+1]=
			*(uint32_t*)h->mv_cache [list][scan8[13]+1]= //FIXME remove past 3 (init somewher else)
			*(uint32_t*)h->mv_cache [list][scan8[4 ]]=
			*(uint32_t*)h->mv_cache [list][scan8[12]]= 0;
			
            if( h->pps.cabac )
			{
                /* XXX beurk, Load mvd */
                if(IS_INTER(topleft_type))
				{
                    const int b_xy = h->mb2b_xy[topleft_xy] + 3 + 3*h->b_stride;
                    *(uint32_t*)h->mvd_cache[list][scan8[0] - 1 - 1*8]= *(uint32_t*)h->mvd_table[list][b_xy];
                }
				else
				{
                    *(uint32_t*)h->mvd_cache[list][scan8[0] - 1 - 1*8]= 0;
                }
				
                if(IS_INTER(top_type))
				{
                    const int b_xy= h->mb2b_xy[top_xy] + 3*h->b_stride;
                    *(uint32_t*)h->mvd_cache[list][scan8[0] + 0 - 1*8]= *(uint32_t*)h->mvd_table[list][b_xy + 0];
                    *(uint32_t*)h->mvd_cache[list][scan8[0] + 1 - 1*8]= *(uint32_t*)h->mvd_table[list][b_xy + 1];
                    *(uint32_t*)h->mvd_cache[list][scan8[0] + 2 - 1*8]= *(uint32_t*)h->mvd_table[list][b_xy + 2];
                    *(uint32_t*)h->mvd_cache[list][scan8[0] + 3 - 1*8]= *(uint32_t*)h->mvd_table[list][b_xy + 3];
                }
				else
				{
                    *(uint32_t*)h->mvd_cache [list][scan8[0] + 0 - 1*8]=
					*(uint32_t*)h->mvd_cache [list][scan8[0] + 1 - 1*8]=
					*(uint32_t*)h->mvd_cache [list][scan8[0] + 2 - 1*8]=
					*(uint32_t*)h->mvd_cache [list][scan8[0] + 3 - 1*8]= 0;
                }
				
                if(IS_INTER(left_type[0]))
				{
                    const int b_xy= h->mb2b_xy[left_xy[0]] + 3;
                    *(uint32_t*)h->mvd_cache[list][scan8[0] - 1 + 0*8]= *(uint32_t*)h->mvd_table[list][b_xy + h->b_stride*left_block[0]];
                    *(uint32_t*)h->mvd_cache[list][scan8[0] - 1 + 1*8]= *(uint32_t*)h->mvd_table[list][b_xy + h->b_stride*left_block[1]];
                }
				else
				{
                    *(uint32_t*)h->mvd_cache [list][scan8[0] - 1 + 0*8]=
					*(uint32_t*)h->mvd_cache [list][scan8[0] - 1 + 1*8]= 0;
                }
				
                if(IS_INTER(left_type[1]))
				{
                    const int b_xy= h->mb2b_xy[left_xy[1]] + 3;
                    *(uint32_t*)h->mvd_cache[list][scan8[0] - 1 + 2*8]= *(uint32_t*)h->mvd_table[list][b_xy + h->b_stride*left_block[2]];
                    *(uint32_t*)h->mvd_cache[list][scan8[0] - 1 + 3*8]= *(uint32_t*)h->mvd_table[list][b_xy + h->b_stride*left_block[3]];
                }
				else
				{
                    *(uint32_t*)h->mvd_cache [list][scan8[0] - 1 + 2*8]=
					*(uint32_t*)h->mvd_cache [list][scan8[0] - 1 + 3*8]= 0;
                }
                *(uint32_t*)h->mvd_cache [list][scan8[5 ]+1]=
				*(uint32_t*)h->mvd_cache [list][scan8[7 ]+1]=
				*(uint32_t*)h->mvd_cache [list][scan8[13]+1]= //FIXME remove past 3 (init somewher else)
				*(uint32_t*)h->mvd_cache [list][scan8[4 ]]=
				*(uint32_t*)h->mvd_cache [list][scan8[12]]= 0;
            }
        }
    }
}

static  void write_back_intra_pred_mode(H264Context *h)
{
    MpegEncContext * const s = &h->s;
    const int mb_xy= s->mb_x + s->mb_y*s->mb_stride;
	
    h->intra4x4_pred_mode[mb_xy][0]= h->intra4x4_pred_mode_cache[7+8*1];
    h->intra4x4_pred_mode[mb_xy][1]= h->intra4x4_pred_mode_cache[7+8*2];
    h->intra4x4_pred_mode[mb_xy][2]= h->intra4x4_pred_mode_cache[7+8*3];
    h->intra4x4_pred_mode[mb_xy][3]= h->intra4x4_pred_mode_cache[7+8*4];
    h->intra4x4_pred_mode[mb_xy][4]= h->intra4x4_pred_mode_cache[4+8*4];
    h->intra4x4_pred_mode[mb_xy][5]= h->intra4x4_pred_mode_cache[5+8*4];
    h->intra4x4_pred_mode[mb_xy][6]= h->intra4x4_pred_mode_cache[6+8*4];
}

static  int check_intra4x4_pred_mode(H264Context *h)
{
    static const int8_t top [12]= {-1, 0,LEFT_DC_PRED,-1,-1,-1,-1,-1, 0};
    static const int8_t left[12]= { 0,-1, TOP_DC_PRED, 0,-1,-1,-1, 0,-1,DC_128_PRED};
    int i;
	
    if(!(h->top_samples_available&0x8000))
	{
        for(i=0; i<4; i++)
		{
            int status= top[ h->intra4x4_pred_mode_cache[scan8[0] + i] ];
            if(status<0)
			{
                return -1;
            } 
			else if(status)
			{
                h->intra4x4_pred_mode_cache[scan8[0] + i]= status;
            }
        }
    }
	
    if(!(h->left_samples_available&0x8000))
	{
        for(i=0; i<4; i++)
		{
            int status= left[ h->intra4x4_pred_mode_cache[scan8[0] + 8*i] ];
            if(status<0)
			{
                return -1;
            } 
			else if(status)
			{
                h->intra4x4_pred_mode_cache[scan8[0] + 8*i]= status;
            }
        }
    }
	
    return 0;
} 

static  int check_intra_pred_mode(H264Context *h, int mode)
{
    static const int8_t top [7]= {LEFT_DC_PRED8x8, 1,-1,-1};
    static const int8_t left[7]= { TOP_DC_PRED8x8,-1, 2,-1,DC_128_PRED8x8};
	
    if(!(h->top_samples_available&0x8000))
	{
        mode= top[ mode ];
        if(mode<0)
            return -1;
    }
	
    if(!(h->left_samples_available&0x8000))
	{
        mode= left[ mode ];
        if(mode<0)
            return -1;
    }
	
    return mode;
}

static  int pred_intra_mode(H264Context *h, int n)
{
    const int index8= scan8[n];
    const int left= h->intra4x4_pred_mode_cache[index8 - 1];
    const int top = h->intra4x4_pred_mode_cache[index8 - 8];
    const int min= FFMIN(left, top);
	
    if(min<0) return DC_PRED;
    else      return min;
}

static  void write_back_non_zero_count(H264Context *h)
{
    MpegEncContext * const s = &h->s;
    const int mb_xy= s->mb_x + s->mb_y*s->mb_stride;
	
    h->non_zero_count[mb_xy][0]= h->non_zero_count_cache[4+8*4];
    h->non_zero_count[mb_xy][1]= h->non_zero_count_cache[5+8*4];
    h->non_zero_count[mb_xy][2]= h->non_zero_count_cache[6+8*4];
    h->non_zero_count[mb_xy][3]= h->non_zero_count_cache[7+8*4];
    h->non_zero_count[mb_xy][4]= h->non_zero_count_cache[7+8*3];
    h->non_zero_count[mb_xy][5]= h->non_zero_count_cache[7+8*2];
    h->non_zero_count[mb_xy][6]= h->non_zero_count_cache[7+8*1];
	
    h->non_zero_count[mb_xy][7]= h->non_zero_count_cache[1+8*2];
    h->non_zero_count[mb_xy][8]= h->non_zero_count_cache[2+8*2];
    h->non_zero_count[mb_xy][9]= h->non_zero_count_cache[2+8*1];
	
    h->non_zero_count[mb_xy][10]=h->non_zero_count_cache[1+8*5];
    h->non_zero_count[mb_xy][11]=h->non_zero_count_cache[2+8*5];
    h->non_zero_count[mb_xy][12]=h->non_zero_count_cache[2+8*4];
}

static  int pred_non_zero_count(H264Context *h, int n)
{
    const int index8= scan8[n];
    const int left= h->non_zero_count_cache[index8 - 1];
    const int top = h->non_zero_count_cache[index8 - 8];
    int i= left + top;
	
    if(i<64) i= (i+1)>>1;
	
    return i&31;
}

static  int fetch_diagonal_mv(H264Context *h, const int16_t **C, int i, int list, int part_width)
{
    const int topright_ref= h->ref_cache[list][ i - 8 + part_width ];
	
    if(topright_ref != PART_NOT_AVAILABLE)
	{
        *C= h->mv_cache[list][ i - 8 + part_width ];
        return topright_ref;
    }
	else
	{
        *C= h->mv_cache[list][ i - 8 - 1 ];
        return h->ref_cache[list][ i - 8 - 1 ];
    }
}

static  void pred_motion(H264Context * const h, int n, int part_width, int list, int ref, int * const mx, int * const my)
{
    const int index8= scan8[n];
    const int top_ref=      h->ref_cache[list][ index8 - 8 ];
    const int left_ref=     h->ref_cache[list][ index8 - 1 ];
    const int16_t * const A= h->mv_cache[list][ index8 - 1 ];
    const int16_t * const B= h->mv_cache[list][ index8 - 8 ];
    const int16_t * C;
    int diagonal_ref, match_count;
	
//    assert(part_width==1 || part_width==2 || part_width==4);
	
	/* mv_cache
	B . . A T T T T
	U . . L . . , .
	U . . L . . . .
	U . . L . . , .
	. . . L . . . .*/
	
    diagonal_ref= fetch_diagonal_mv(h, &C, index8, list, part_width);
    match_count= (diagonal_ref==ref) + (top_ref==ref) + (left_ref==ref);
    if(match_count > 1)//most common
	{ 
        *mx= mid_pred(A[0], B[0], C[0]);
        *my= mid_pred(A[1], B[1], C[1]);
    }
	else if(match_count==1)
	{
        if(left_ref==ref)
		{
            *mx= A[0];
            *my= A[1];
        }
		else if(top_ref==ref)
		{
            *mx= B[0];
            *my= B[1];
        }
		else
		{
            *mx= C[0];
            *my= C[1];
        }
    }
	else
	{
        if(top_ref == PART_NOT_AVAILABLE && diagonal_ref == PART_NOT_AVAILABLE && left_ref != PART_NOT_AVAILABLE)
		{
            *mx= A[0];
            *my= A[1];
        }
		else
		{
            *mx= mid_pred(A[0], B[0], C[0]);
            *my= mid_pred(A[1], B[1], C[1]);
        }
    }
}

static  void pred_16x8_motion(H264Context * const h, int n, int list, int ref, int * const mx, int * const my)
{
    if(n==0)
	{
        const int top_ref=      h->ref_cache[list][ scan8[0] - 8 ];
        const int16_t * const B= h->mv_cache[list][ scan8[0] - 8 ];
		
        if(top_ref == ref)
		{
            *mx= B[0];
            *my= B[1];
            return;
        }
    }
	else
	{
        const int left_ref=     h->ref_cache[list][ scan8[8] - 1 ];
        const int16_t * const A= h->mv_cache[list][ scan8[8] - 1 ];
		
        if(left_ref == ref)
		{
            *mx= A[0];
            *my= A[1];
            return;
        }
    }
	
    pred_motion(h, n, 4, list, ref, mx, my);
}

static  void pred_8x16_motion(H264Context * const h, int n, int list, int ref, int * const mx, int * const my)
{
    if(n==0)
	{
        const int left_ref=      h->ref_cache[list][ scan8[0] - 1 ];
        const int16_t * const A=  h->mv_cache[list][ scan8[0] - 1 ];
		
        if(left_ref == ref)
		{
            *mx= A[0];
            *my= A[1];
            return;
        }
    }
	else
	{
        const int16_t * C;
        int diagonal_ref;
		
        diagonal_ref= fetch_diagonal_mv(h, &C, scan8[4], list, 2);
		
        if(diagonal_ref == ref)
		{
            *mx= C[0];
            *my= C[1];
            return;
        }
    }
	
    pred_motion(h, n, 2, list, ref, mx, my);
}

static  void pred_pskip_motion(H264Context * const h, int * const mx, int * const my)
{
    const int top_ref = h->ref_cache[0][ scan8[0] - 8 ];
    const int left_ref= h->ref_cache[0][ scan8[0] - 1 ];
	
    if(top_ref == PART_NOT_AVAILABLE || left_ref == PART_NOT_AVAILABLE
		|| (top_ref == 0  && *(uint32_t*)h->mv_cache[0][ scan8[0] - 8 ] == 0)
		|| (left_ref == 0 && *(uint32_t*)h->mv_cache[0][ scan8[0] - 1 ] == 0))
	{
        *mx = *my = 0;
        return;
    }
	
    pred_motion(h, 0, 4, 0, 0, mx, my);
	
    return;
}

static  void write_back_motion(H264Context *h, int mb_type)
{
    MpegEncContext * const s = &h->s;
    const int b_xy = 4*s->mb_x + 4*s->mb_y*h->b_stride;
    const int b8_xy= 2*s->mb_x + 2*s->mb_y*h->b8_stride;
    int list;
	
    for(list=0; list<2; list++)
	{
        int y;
        if((!IS_8X8(mb_type)) && !USES_LIST(mb_type, list))
		{
            if(1)
			{ 
                for(y=0; y<4; y++)
				{
                    *(uint64_t*)s->current_picture.motion_val[list][b_xy + 0 + y*h->b_stride]=
						*(uint64_t*)s->current_picture.motion_val[list][b_xy + 2 + y*h->b_stride]= 0;
                }
                if( h->pps.cabac )
				{
                    for(y=0; y<4; y++)
					{
                        *(uint64_t*)h->mvd_table[list][b_xy + 0 + y*h->b_stride]=
							*(uint64_t*)h->mvd_table[list][b_xy + 2 + y*h->b_stride]= 0;
                    }
                }
                for(y=0; y<2; y++)
				{
                    *(uint16_t*)s->current_picture.motion_val[list][b8_xy + y*h->b8_stride]= (LIST_NOT_USED&0xFF)*0x0101;
                }
            }
            continue; //FIXME direct mode ...
        }
		
        for(y=0; y<4; y++)
		{
            *(uint64_t*)s->current_picture.motion_val[list][b_xy + 0 + y*h->b_stride]= *(uint64_t*)h->mv_cache[list][scan8[0]+0 + 8*y];
            *(uint64_t*)s->current_picture.motion_val[list][b_xy + 2 + y*h->b_stride]= *(uint64_t*)h->mv_cache[list][scan8[0]+2 + 8*y];
        }
        if( h->pps.cabac ) 
		{
            for(y=0; y<4; y++)
			{
                *(uint64_t*)h->mvd_table[list][b_xy + 0 + y*h->b_stride]= *(uint64_t*)h->mvd_cache[list][scan8[0]+0 + 8*y];
                *(uint64_t*)h->mvd_table[list][b_xy + 2 + y*h->b_stride]= *(uint64_t*)h->mvd_cache[list][scan8[0]+2 + 8*y];
            }
        }
        for(y=0; y<2; y++)
		{
            s->current_picture.ref_index[list][b8_xy + 0 + y*h->b8_stride]= h->ref_cache[list][scan8[0]+0 + 16*y];
            s->current_picture.ref_index[list][b8_xy + 1 + y*h->b8_stride]= h->ref_cache[list][scan8[0]+2 + 16*y];
        }
    }
}

static uint8_t *decode_nal(H264Context *h, uint8_t *src, int *dst_length, int *consumed, int length)
{
    int i, si, di;
    uint8_t *dst;
	
    h->nal_ref_idc= src[0]>>5;
    h->nal_unit_type= src[0]&0x1F;
	
    src++; 
	length--;
	
    for(i=0; i+1<length; i+=2)
	{
        if(src[i]) continue;
        if(i>0 && src[i-1]==0) i--;
        if(i+2<length && src[i+1]==0 && src[i+2]<=3)
		{
            if(src[i+2]!=3)
                length=i;
			
            break;
        }
    }
	
    if(i>=length-1)
	{ //no escaped 0
        *dst_length= length;
        *consumed= length+1; //+1 for the header
        return src;
    }
	
    h->rbsp_buffer= av_fast_realloc(h->rbsp_buffer, &h->rbsp_buffer_size, length);
    dst= h->rbsp_buffer;
	
    si=di=0;
    while(si<length)
	{
        if(si+2<length && src[si]==0 && src[si+1]==0 && src[si+2]<=3)
		{
            if(src[si+2]==3)
			{ //escape
                dst[di++]= 0;
                dst[di++]= 0;
                si+=3;
            }else //next start code
                break;
        }
		
        dst[di++]= src[si++];
    }
	
    *dst_length= di;
    *consumed= si + 1;//+1 for the header
    return dst;
}

static int decode_rbsp_trailing(uint8_t *src)
{
    int v= *src;
    int r;
	
    for(r=1; r<9; r++)
	{
        if(v&1) return r;
        v>>=1;
    }
    return 0;
}

static void h264_luma_dc_dequant_idct_c(DCTELEM *block, int qp)
{
    const int qmul= dequant_coeff[qp][0];
#define stride 16
    int i;
    int temp[16]; //FIXME check if this is a good idea
    static const int x_offset[4]={0, 1*stride, 4* stride,  5*stride};
    static const int y_offset[4]={0, 2*stride, 8* stride, 10*stride};
	
    for(i=0; i<4; i++)
	{
        const int offset= y_offset[i];
        const int z0= block[offset+stride*0] + block[offset+stride*4];
        const int z1= block[offset+stride*0] - block[offset+stride*4];
        const int z2= block[offset+stride*1] - block[offset+stride*5];
        const int z3= block[offset+stride*1] + block[offset+stride*5];
		
        temp[4*i+0]= z0+z3;
        temp[4*i+1]= z1+z2;
        temp[4*i+2]= z1-z2;
        temp[4*i+3]= z0-z3;
    }
	
    for(i=0; i<4; i++)
	{
        const int offset= x_offset[i];
        const int z0= temp[4*0+i] + temp[4*2+i];
        const int z1= temp[4*0+i] - temp[4*2+i];
        const int z2= temp[4*1+i] - temp[4*3+i];
        const int z3= temp[4*1+i] + temp[4*3+i];
		
        block[stride*0 +offset]= ((z0 + z3)*qmul + 2)>>2; //FIXME think about merging this into decode_resdual
        block[stride*2 +offset]= ((z1 + z2)*qmul + 2)>>2;
        block[stride*8 +offset]= ((z1 - z2)*qmul + 2)>>2;
        block[stride*10+offset]= ((z0 - z3)*qmul + 2)>>2;
    }
}

#undef xStride
#undef stride

static void chroma_dc_dequant_idct_c(DCTELEM *block, int qp)
{
    const int qmul= dequant_coeff[qp][0];
    const int stride= 16*2;
    const int xStride= 16;
    int a,b,c,d,e;
	
    a= block[stride*0 + xStride*0];
    b= block[stride*0 + xStride*1];
    c= block[stride*1 + xStride*0];
    d= block[stride*1 + xStride*1];
	
    e= a-b;
    a= a+b;
    b= c-d;
    c= c+d;
	
    block[stride*0 + xStride*0]= ((a+c)*qmul + 0)>>1;
    block[stride*0 + xStride*1]= ((e+b)*qmul + 0)>>1;
    block[stride*1 + xStride*0]= ((a-c)*qmul + 0)>>1;
    block[stride*1 + xStride*1]= ((e-b)*qmul + 0)>>1;
}

static  int get_chroma_qp(H264Context *h, int qscale)
{
    return chroma_qp[clip(qscale + h->pps.chroma_qp_index_offset, 0, 51)];
}

static void h264_add_idct_c(uint8_t *dst, DCTELEM *block, int stride)
{
    int i;
    uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
	
    block[0] += 32;
	
    for(i=0; i<4; i++)
	{
        const int z0=  block[0 + 4*i]     +  block[2 + 4*i];
        const int z1=  block[0 + 4*i]     -  block[2 + 4*i];
        const int z2= (block[1 + 4*i]>>1) -  block[3 + 4*i];
        const int z3=  block[1 + 4*i]     + (block[3 + 4*i]>>1);
		
        block[0 + 4*i]= z0 + z3;
        block[1 + 4*i]= z1 + z2;
        block[2 + 4*i]= z1 - z2;
        block[3 + 4*i]= z0 - z3;
    }
	
    for(i=0; i<4; i++)
	{
        const int z0=  block[i + 4*0]     +  block[i + 4*2];
        const int z1=  block[i + 4*0]     -  block[i + 4*2];
        const int z2= (block[i + 4*1]>>1) -  block[i + 4*3];
        const int z3=  block[i + 4*1]     + (block[i + 4*3]>>1);
		
        dst[i + 0*stride]= cm[ dst[i + 0*stride] + ((z0 + z3) >> 6) ];
        dst[i + 1*stride]= cm[ dst[i + 1*stride] + ((z1 + z2) >> 6) ];
        dst[i + 2*stride]= cm[ dst[i + 2*stride] + ((z1 - z2) >> 6) ];
        dst[i + 3*stride]= cm[ dst[i + 3*stride] + ((z0 - z3) >> 6) ];
    }
}

static void pred4x4_vertical_c(uint8_t *src, uint8_t *topright, int stride)
{
    const uint32_t a= ((uint32_t*)(src-stride))[0];
    ((uint32_t*)(src+0*stride))[0]= a;
    ((uint32_t*)(src+1*stride))[0]= a;
    ((uint32_t*)(src+2*stride))[0]= a;
    ((uint32_t*)(src+3*stride))[0]= a;
}

static void pred4x4_horizontal_c(uint8_t *src, uint8_t *topright, int stride)
{
    ((uint32_t*)(src+0*stride))[0]= src[-1+0*stride]*0x01010101;
    ((uint32_t*)(src+1*stride))[0]= src[-1+1*stride]*0x01010101;
    ((uint32_t*)(src+2*stride))[0]= src[-1+2*stride]*0x01010101;
    ((uint32_t*)(src+3*stride))[0]= src[-1+3*stride]*0x01010101;
}

static void pred4x4_dc_c(uint8_t *src, uint8_t *topright, int stride)
{
    const int dc= (  src[-stride] + src[1-stride] + src[2-stride] + src[3-stride]
		+ src[-1+0*stride] + src[-1+1*stride] + src[-1+2*stride] + src[-1+3*stride] + 4) >>3;
	
	int dc1=dc*0x01010101;
	memcpy(src+0*stride,&dc1,sizeof(uint32_t));
	memcpy(src+1*stride,&dc1,sizeof(uint32_t));
	memcpy(src+2*stride,&dc1,sizeof(uint32_t));
	memcpy(src+3*stride,&dc1,sizeof(uint32_t));
}

static void pred4x4_left_dc_c(uint8_t *src, uint8_t *topright, int stride)
{
    const int dc= (  src[-1+0*stride] + src[-1+1*stride] + src[-1+2*stride] + src[-1+3*stride] + 2) >>2;
	int dc1=dc*0x01010101;
	
	memcpy(src+0*stride,&dc1,sizeof(uint32_t));
	memcpy(src+1*stride,&dc1,sizeof(uint32_t));
	memcpy(src+2*stride,&dc1,sizeof(uint32_t));
	memcpy(src+3*stride,&dc1,sizeof(uint32_t));
}

static void pred4x4_top_dc_c(uint8_t *src, uint8_t *topright, int stride)
{
    const int dc= (  src[-stride] + src[1-stride] + src[2-stride] + src[3-stride] + 2) >>2;
	int dc1=dc*0x01010101;
	
	memcpy(src+0*stride,&dc1,sizeof(uint32_t));
	memcpy(src+1*stride,&dc1,sizeof(uint32_t));
	memcpy(src+2*stride,&dc1,sizeof(uint32_t));
	memcpy(src+3*stride,&dc1,sizeof(uint32_t));
}

static void pred4x4_128_dc_c(uint8_t *src, uint8_t *topright, int stride)
{
    ((uint32_t*)(src+0*stride))[0]=
	((uint32_t*)(src+1*stride))[0]=
	((uint32_t*)(src+2*stride))[0]=
	((uint32_t*)(src+3*stride))[0]= 128U*0x01010101U;
}

#define LOAD_TOP_RIGHT_EDGE\
    const int t4= topright[0];\
    const int t5= topright[1];\
    const int t6= topright[2];\
    const int t7= topright[3];\
	
#define LOAD_LEFT_EDGE\
    const int l0= src[-1+0*stride];\
    const int l1= src[-1+1*stride];\
    const int l2= src[-1+2*stride];\
    const int l3= src[-1+3*stride];\
	
#define LOAD_TOP_EDGE\
    const int t0= src[ 0-1*stride];\
    const int t1= src[ 1-1*stride];\
    const int t2= src[ 2-1*stride];\
    const int t3= src[ 3-1*stride];\
	
static void pred4x4_down_right_c(uint8_t *src, uint8_t *topright, int stride)
{
    const int lt= src[-1-1*stride];
    LOAD_TOP_EDGE
	LOAD_LEFT_EDGE
		
	src[0+3*stride]=(l3 + 2*l2 + l1 + 2)>>2;
    src[0+2*stride]=
	src[1+3*stride]=(l2 + 2*l1 + l0 + 2)>>2;
    src[0+1*stride]=
	src[1+2*stride]=
	src[2+3*stride]=(l1 + 2*l0 + lt + 2)>>2;
    src[0+0*stride]=
	src[1+1*stride]=
	src[2+2*stride]=
	src[3+3*stride]=(l0 + 2*lt + t0 + 2)>>2;
    src[1+0*stride]=
	src[2+1*stride]=
	src[3+2*stride]=(lt + 2*t0 + t1 + 2)>>2;
    src[2+0*stride]=
	src[3+1*stride]=(t0 + 2*t1 + t2 + 2)>>2;
    src[3+0*stride]=(t1 + 2*t2 + t3 + 2)>>2;
}

static void pred4x4_down_left_c(uint8_t *src, uint8_t *topright, int stride)
{
    LOAD_TOP_EDGE
	LOAD_TOP_RIGHT_EDGE
		
	src[0+0*stride]=(t0 + t2 + 2*t1 + 2)>>2;
    src[1+0*stride]=
	src[0+1*stride]=(t1 + t3 + 2*t2 + 2)>>2;
    src[2+0*stride]=
	src[1+1*stride]=
	src[0+2*stride]=(t2 + t4 + 2*t3 + 2)>>2;
    src[3+0*stride]=
	src[2+1*stride]=
	src[1+2*stride]=
	src[0+3*stride]=(t3 + t5 + 2*t4 + 2)>>2;
    src[3+1*stride]=
	src[2+2*stride]=
	src[1+3*stride]=(t4 + t6 + 2*t5 + 2)>>2;
    src[3+2*stride]=
	src[2+3*stride]=(t5 + t7 + 2*t6 + 2)>>2;
    src[3+3*stride]=(t6 + 3*t7 + 2)>>2;
}

static void pred4x4_vertical_right_c(uint8_t *src, uint8_t *topright, int stride)
{
    const int lt= src[-1-1*stride];
    LOAD_TOP_EDGE
	LOAD_LEFT_EDGE
		
	src[0+0*stride]=
	src[1+2*stride]=(lt + t0 + 1)>>1;
    src[1+0*stride]=
	src[2+2*stride]=(t0 + t1 + 1)>>1;
    src[2+0*stride]=
	src[3+2*stride]=(t1 + t2 + 1)>>1;
    src[3+0*stride]=(t2 + t3 + 1)>>1;
    src[0+1*stride]=
	src[1+3*stride]=(l0 + 2*lt + t0 + 2)>>2;
    src[1+1*stride]=
	src[2+3*stride]=(lt + 2*t0 + t1 + 2)>>2;
    src[2+1*stride]=
	src[3+3*stride]=(t0 + 2*t1 + t2 + 2)>>2;
    src[3+1*stride]=(t1 + 2*t2 + t3 + 2)>>2;
    src[0+2*stride]=(lt + 2*l0 + l1 + 2)>>2;
    src[0+3*stride]=(l0 + 2*l1 + l2 + 2)>>2;
}

static void pred4x4_vertical_left_c(uint8_t *src, uint8_t *topright, int stride)
{
    LOAD_TOP_EDGE
	LOAD_TOP_RIGHT_EDGE
		
	src[0+0*stride]=(t0 + t1 + 1)>>1;
    src[1+0*stride]=
	src[0+2*stride]=(t1 + t2 + 1)>>1;
    src[2+0*stride]=
	src[1+2*stride]=(t2 + t3 + 1)>>1;
    src[3+0*stride]=
	src[2+2*stride]=(t3 + t4+ 1)>>1;
    src[3+2*stride]=(t4 + t5+ 1)>>1;
    src[0+1*stride]=(t0 + 2*t1 + t2 + 2)>>2;
    src[1+1*stride]=
	src[0+3*stride]=(t1 + 2*t2 + t3 + 2)>>2;
    src[2+1*stride]=
	src[1+3*stride]=(t2 + 2*t3 + t4 + 2)>>2;
    src[3+1*stride]=
	src[2+3*stride]=(t3 + 2*t4 + t5 + 2)>>2;
    src[3+3*stride]=(t4 + 2*t5 + t6 + 2)>>2;
}

static void pred4x4_horizontal_up_c(uint8_t *src, uint8_t *topright, int stride)
{
    LOAD_LEFT_EDGE
		
	src[0+0*stride]=(l0 + l1 + 1)>>1;
    src[1+0*stride]=(l0 + 2*l1 + l2 + 2)>>2;
    src[2+0*stride]=
	src[0+1*stride]=(l1 + l2 + 1)>>1;
    src[3+0*stride]=
	src[1+1*stride]=(l1 + 2*l2 + l3 + 2)>>2;
    src[2+1*stride]=
	src[0+2*stride]=(l2 + l3 + 1)>>1;
    src[3+1*stride]=
	src[1+2*stride]=(l2 + 2*l3 + l3 + 2)>>2;
    src[3+2*stride]=
	src[1+3*stride]=
	src[0+3*stride]=
	src[2+2*stride]=
	src[2+3*stride]=
	src[3+3*stride]=l3;
}

static void pred4x4_horizontal_down_c(uint8_t *src, uint8_t *topright, int stride)
{
    const int lt= src[-1-1*stride];
    LOAD_TOP_EDGE
	LOAD_LEFT_EDGE
		
	src[0+0*stride]=
	src[2+1*stride]=(lt + l0 + 1)>>1;
    src[1+0*stride]=
	src[3+1*stride]=(l0 + 2*lt + t0 + 2)>>2;
    src[2+0*stride]=(lt + 2*t0 + t1 + 2)>>2;
    src[3+0*stride]=(t0 + 2*t1 + t2 + 2)>>2;
    src[0+1*stride]=
	src[2+2*stride]=(l0 + l1 + 1)>>1;
    src[1+1*stride]=
	src[3+2*stride]=(lt + 2*l0 + l1 + 2)>>2;
    src[0+2*stride]=
	src[2+3*stride]=(l1 + l2+ 1)>>1;
    src[1+2*stride]=
	src[3+3*stride]=(l0 + 2*l1 + l2 + 2)>>2;
    src[0+3*stride]=(l2 + l3 + 1)>>1;
    src[1+3*stride]=(l1 + 2*l2 + l3 + 2)>>2;
}

static void pred16x16_vertical_c(uint8_t *src, int stride)
{
    int i;
    const uint32_t a= ((uint32_t*)(src-stride))[0];
    const uint32_t b= ((uint32_t*)(src-stride))[1];
    const uint32_t c= ((uint32_t*)(src-stride))[2];
    const uint32_t d= ((uint32_t*)(src-stride))[3];
	
    for(i=0; i<16; i++)
	{
        ((uint32_t*)(src+i*stride))[0]= a;
        ((uint32_t*)(src+i*stride))[1]= b;
        ((uint32_t*)(src+i*stride))[2]= c;
        ((uint32_t*)(src+i*stride))[3]= d;
    }
}

static void pred16x16_horizontal_c(uint8_t *src, int stride)
{
    int i;
	
    for(i=0; i<16; i++)
	{
        ((uint32_t*)(src+i*stride))[0]=
		((uint32_t*)(src+i*stride))[1]=
		((uint32_t*)(src+i*stride))[2]=
		((uint32_t*)(src+i*stride))[3]= src[-1+i*stride]*0x01010101;
    }
}

static void pred16x16_dc_c(uint8_t *src, int stride)
{
    int i, dc=0;
	
    for(i=0;i<16; i++)
	{
        dc+= src[-1+i*stride];
    }
	
    for(i=0;i<16; i++)
	{
        dc+= src[i-stride];
    }
	
    dc= 0x01010101*((dc + 16)>>5);
	
    for(i=0; i<16; i++)
	{
        ((uint32_t*)(src+i*stride))[0]=
		((uint32_t*)(src+i*stride))[1]=
		((uint32_t*)(src+i*stride))[2]=
		((uint32_t*)(src+i*stride))[3]= dc;
    }
}

static void pred16x16_left_dc_c(uint8_t *src, int stride)
{
    int i, dc=0;
	
    for(i=0;i<16; i++)
	{
        dc+= src[-1+i*stride];
    }
	
    dc= 0x01010101*((dc + 8)>>4);
	
    for(i=0; i<16; i++)
	{
        ((uint32_t*)(src+i*stride))[0]=
		((uint32_t*)(src+i*stride))[1]=
		((uint32_t*)(src+i*stride))[2]=
		((uint32_t*)(src+i*stride))[3]= dc;
    }
}

static void pred16x16_top_dc_c(uint8_t *src, int stride)
{
    int i, dc=0;
	
    for(i=0;i<16; i++)
	{
        dc+= src[i-stride];
    }
    dc= 0x01010101*((dc + 8)>>4);
	
    for(i=0; i<16; i++)
	{
        ((uint32_t*)(src+i*stride))[0]=
		((uint32_t*)(src+i*stride))[1]=
		((uint32_t*)(src+i*stride))[2]=
		((uint32_t*)(src+i*stride))[3]= dc;
    }
}

static void pred16x16_128_dc_c(uint8_t *src, int stride)
{
    int i;
	
    for(i=0; i<16; i++)
	{
        ((uint32_t*)(src+i*stride))[0]=
		((uint32_t*)(src+i*stride))[1]=
		((uint32_t*)(src+i*stride))[2]=
		((uint32_t*)(src+i*stride))[3]= 0x01010101U*128U;
    }
}

static  void pred16x16_plane_compat_c(uint8_t *src, int stride, const int svq3)
{
	int i, j, k;
	int a;
	uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
	const uint8_t * const src0 = src+7-stride;
	const uint8_t *src1 = src+8*stride-1;
	const uint8_t *src2 = src1-2*stride;      // == src+6*stride-1;
	int H = src0[1] - src0[-1];
	int V = src1[0] - src2[ 0];
	for(k=2; k<=8; ++k)
	{
		src1 += stride; src2 -= stride;
		H += k*(src0[k] - src0[-k]);
		V += k*(src1[0] - src2[ 0]);
	}
	if(svq3)
	{
		H = ( 5*(H/4) ) / 16;
		V = ( 5*(V/4) ) / 16;
		
		/* required for 100% accuracy */
		i = H; H = V; V = i;
	}
	else
	{
		H = ( 5*H+32 ) >> 6;
		V = ( 5*V+32 ) >> 6;
	}
	
	a = 16*(src1[0] + src2[16] + 1) - 7*(V+H);
	for(j=16; j>0; --j)
	{
		int b = a;
		a += V;
		for(i=-16; i<0; i+=4)
		{
			src[16+i] = cm[ (b    ) >> 5 ];
			src[17+i] = cm[ (b+  H) >> 5 ];
			src[18+i] = cm[ (b+2*H) >> 5 ];
			src[19+i] = cm[ (b+3*H) >> 5 ];
			b += 4*H;
		}
		src += stride;
	}
}

static void pred16x16_plane_c(uint8_t *src, int stride)
{
    pred16x16_plane_compat_c(src, stride, 0);
}

static void pred8x8_vertical_c(uint8_t *src, int stride)
{
    int i;
    const uint32_t a= ((uint32_t*)(src-stride))[0];
    const uint32_t b= ((uint32_t*)(src-stride))[1];
	
    for(i=0; i<8; i++)
	{
        ((uint32_t*)(src+i*stride))[0]= a;
        ((uint32_t*)(src+i*stride))[1]= b;
    }
}

static void pred8x8_horizontal_c(uint8_t *src, int stride)
{
    int i;
	
    for(i=0; i<8; i++)
	{
        ((uint32_t*)(src+i*stride))[0]=
		((uint32_t*)(src+i*stride))[1]= src[-1+i*stride]*0x01010101;
    }
}

static void pred8x8_128_dc_c(uint8_t *src, int stride)
{
    int i;
	
    for(i=0; i<4; i++)
	{
        ((uint32_t*)(src+i*stride))[0]=
		((uint32_t*)(src+i*stride))[1]= 0x01010101U*128U;
    }
    for(i=4; i<8; i++)
	{
        ((uint32_t*)(src+i*stride))[0]=
		((uint32_t*)(src+i*stride))[1]= 0x01010101U*128U;
    }
}

static void pred8x8_left_dc_c(uint8_t *src, int stride)
{
    int i;
    int dc0, dc2;
	
    dc0=dc2=0;
    for(i=0;i<4; i++)
	{
        dc0+= src[-1+i*stride];
        dc2+= src[-1+(i+4)*stride];
    }
    dc0= 0x01010101*((dc0 + 2)>>2);
    dc2= 0x01010101*((dc2 + 2)>>2);
	
    for(i=0; i<4; i++)
	{
        ((uint32_t*)(src+i*stride))[0]=
		((uint32_t*)(src+i*stride))[1]= dc0;
    }
    for(i=4; i<8; i++)
	{
        ((uint32_t*)(src+i*stride))[0]=
		((uint32_t*)(src+i*stride))[1]= dc2;
    }
}

static void pred8x8_top_dc_c(uint8_t *src, int stride)
{
    int i;
    int dc0, dc1;
	
    dc0=dc1=0;
    for(i=0;i<4; i++)
	{
        dc0+= src[i-stride];
        dc1+= src[4+i-stride];
    }
    dc0= 0x01010101*((dc0 + 2)>>2);
    dc1= 0x01010101*((dc1 + 2)>>2);
	
    for(i=0; i<4; i++)
	{
        ((uint32_t*)(src+i*stride))[0]= dc0;
        ((uint32_t*)(src+i*stride))[1]= dc1;
    }
    for(i=4; i<8; i++)
	{
        ((uint32_t*)(src+i*stride))[0]= dc0;
        ((uint32_t*)(src+i*stride))[1]= dc1;
    }
}

static void pred8x8_dc_c(uint8_t *src, int stride)
{
    int i;
    int dc0, dc1, dc2, dc3;
	
    dc0=dc1=dc2=0;
    for(i=0;i<4; i++)
	{
        dc0+= src[-1+i*stride] + src[i-stride];
        dc1+= src[4+i-stride];
        dc2+= src[-1+(i+4)*stride];
    }
    dc3= 0x01010101*((dc1 + dc2 + 4)>>3);
    dc0= 0x01010101*((dc0 + 4)>>3);
    dc1= 0x01010101*((dc1 + 2)>>2);
    dc2= 0x01010101*((dc2 + 2)>>2);
	
    for(i=0; i<4; i++)
	{
        ((uint32_t*)(src+i*stride))[0]= dc0;
        ((uint32_t*)(src+i*stride))[1]= dc1;
    }
    for(i=4; i<8; i++)
	{
        ((uint32_t*)(src+i*stride))[0]= dc2;
        ((uint32_t*)(src+i*stride))[1]= dc3;
    }
}

static void pred8x8_plane_c(uint8_t *src, int stride)
{
	int j, k;
	int a;
	uint8_t *cm = (uint8_t*)cropTbl + MAX_NEG_CROP;
	const uint8_t * const src0 = src+3-stride;
	const uint8_t *src1 = src+4*stride-1;
	const uint8_t *src2 = src1-2*stride;      // == src+2*stride-1;
	int H = src0[1] - src0[-1];
	int V = src1[0] - src2[ 0];
	for(k=2; k<=4; ++k)
	{
		src1 += stride; src2 -= stride;
		H += k*(src0[k] - src0[-k]);
		V += k*(src1[0] - src2[ 0]);
	}
	H = ( 17*H+16 ) >> 5;
	V = ( 17*V+16 ) >> 5;
	
	a = 16*(src1[0] + src2[8]+1) - 3*(V+H);
	for(j=8; j>0; --j)
	{
		int b = a;
		a += V;
		src[0] = cm[ (b    ) >> 5 ];
		src[1] = cm[ (b+  H) >> 5 ];
		src[2] = cm[ (b+2*H) >> 5 ];
		src[3] = cm[ (b+3*H) >> 5 ];
		src[4] = cm[ (b+4*H) >> 5 ];
		src[5] = cm[ (b+5*H) >> 5 ];
		src[6] = cm[ (b+6*H) >> 5 ];
		src[7] = cm[ (b+7*H) >> 5 ];
		src += stride;
	}
}

static  void mc_dir_part(H264Context *h, Picture *pic, int n, int square, int chroma_height, int delta, int list,
						 uint8_t *dest_y, uint8_t *dest_cb, uint8_t *dest_cr,
						 int src_x_offset, int src_y_offset,
						 qpel_mc_func *qpix_op, h264_chroma_mc_func chroma_op)
{
    MpegEncContext * const s = &h->s;
    const int mx= h->mv_cache[list][ scan8[n] ][0] + src_x_offset*8;
    const int my= h->mv_cache[list][ scan8[n] ][1] + src_y_offset*8;
    const int luma_xy= (mx&3) + ((my&3)<<2);
    uint8_t * src_y = pic->data[0] + (mx>>2) + (my>>2)*s->linesize;
    uint8_t * src_cb= pic->data[1] + (mx>>3) + (my>>3)*s->uvlinesize;
    uint8_t * src_cr= pic->data[2] + (mx>>3) + (my>>3)*s->uvlinesize;
	
    qpix_op[luma_xy](dest_y, src_y, s->linesize); //FIXME try variable height perhaps?
    if(!square){
        qpix_op[luma_xy](dest_y + delta, src_y + delta, s->linesize);
    }
	
    chroma_op(dest_cb, src_cb, s->uvlinesize, chroma_height, mx&7, my&7);
	
    chroma_op(dest_cr, src_cr, s->uvlinesize, chroma_height, mx&7, my&7);
}

static  void mc_part(H264Context *h, int n, int square, int chroma_height, int delta,
					 uint8_t *dest_y, uint8_t *dest_cb, uint8_t *dest_cr,
					 int x_offset, int y_offset,
					 qpel_mc_func *qpix_put, h264_chroma_mc_func chroma_put,
					 qpel_mc_func *qpix_avg, h264_chroma_mc_func chroma_avg,
					 int list0, int list1)
{
    MpegEncContext * const s = &h->s;
    qpel_mc_func *qpix_op=  qpix_put;
    h264_chroma_mc_func chroma_op= chroma_put;
	
    dest_y  += 2*x_offset + 2*y_offset*s->  linesize;
    dest_cb +=   x_offset +   y_offset*s->uvlinesize;
    dest_cr +=   x_offset +   y_offset*s->uvlinesize;
    x_offset += 8*s->mb_x;
    y_offset += 8*s->mb_y;
	
    if(list0)
	{
        mc_dir_part(h, &h->ref_list[0][ h->ref_cache[0][ scan8[n] ] ], n, square, chroma_height, delta, 0,
			dest_y, dest_cb, dest_cr, x_offset, y_offset,
			qpix_op, chroma_op);
		
        qpix_op=  qpix_avg;
        chroma_op= chroma_avg;
    }
	
    if(list1)
	{
        mc_dir_part(h, &h->ref_list[0][ h->ref_cache[0][ scan8[n] ] ], n, square, chroma_height, delta, 1,
			dest_y, dest_cb, dest_cr, x_offset, y_offset,
			qpix_op, chroma_op);
    }
}

static  void hl_motion(H264Context *h, uint8_t *dest_y, uint8_t *dest_cb, uint8_t *dest_cr,
					   qpel_mc_func (*qpix_put)[16], h264_chroma_mc_func (*chroma_put),
					   qpel_mc_func (*qpix_avg)[16], h264_chroma_mc_func (*chroma_avg))
{
    MpegEncContext * const s = &h->s;
    const int mb_xy= s->mb_x + s->mb_y*s->mb_stride;
    const int mb_type= s->current_picture.mb_type[mb_xy];
	
//    assert(IS_INTER(mb_type));
	
    if(IS_16X16(mb_type))
	{
        mc_part(h, 0, 1, 8, 0, dest_y, dest_cb, dest_cr, 0, 0,
			qpix_put[0], chroma_put[0], qpix_avg[0], chroma_avg[0],
			IS_DIR(mb_type, 0, 0), IS_DIR(mb_type, 0, 1));
    }
	else if(IS_16X8(mb_type))
	{
        mc_part(h, 0, 0, 4, 8, dest_y, dest_cb, dest_cr, 0, 0,
			qpix_put[1], chroma_put[0], qpix_avg[1], chroma_avg[0],
			IS_DIR(mb_type, 0, 0), IS_DIR(mb_type, 0, 1));
        mc_part(h, 8, 0, 4, 8, dest_y, dest_cb, dest_cr, 0, 4,
			qpix_put[1], chroma_put[0], qpix_avg[1], chroma_avg[0],
			IS_DIR(mb_type, 1, 0), IS_DIR(mb_type, 1, 1));
    }
	else if(IS_8X16(mb_type))
	{
        mc_part(h, 0, 0, 8, 8*s->linesize, dest_y, dest_cb, dest_cr, 0, 0,
			qpix_put[1], chroma_put[1], qpix_avg[1], chroma_avg[1],
			IS_DIR(mb_type, 0, 0), IS_DIR(mb_type, 0, 1));
        mc_part(h, 4, 0, 8, 8*s->linesize, dest_y, dest_cb, dest_cr, 4, 0,
			qpix_put[1], chroma_put[1], qpix_avg[1], chroma_avg[1],
			IS_DIR(mb_type, 1, 0), IS_DIR(mb_type, 1, 1));
    }
	else
	{
        int i;
		int sub_mb_type;
		int n;
		int x_offset;
		int y_offset;
		
//        assert(IS_8X8(mb_type));
		
        for(i=0; i<4; i++)
		{
            sub_mb_type= h->sub_mb_type[i];
            n= 4*i;
            x_offset= (i&1)<<2;
            y_offset= (i&2)<<1;
			
            if(IS_SUB_8X8(sub_mb_type))
			{
                mc_part(h, n, 1, 4, 0, dest_y, dest_cb, dest_cr, x_offset, y_offset,
                    qpix_put[1], chroma_put[1], qpix_avg[1], chroma_avg[1],
                    IS_DIR(sub_mb_type, 0, 0), IS_DIR(sub_mb_type, 0, 1));
            }
			else if(IS_SUB_8X4(sub_mb_type))
			{
                mc_part(h, n  , 0, 2, 4, dest_y, dest_cb, dest_cr, x_offset, y_offset,
                    qpix_put[2], chroma_put[1], qpix_avg[2], chroma_avg[1],
                    IS_DIR(sub_mb_type, 0, 0), IS_DIR(sub_mb_type, 0, 1));
                mc_part(h, n+2, 0, 2, 4, dest_y, dest_cb, dest_cr, x_offset, y_offset+2,
                    qpix_put[2], chroma_put[1], qpix_avg[2], chroma_avg[1],
                    IS_DIR(sub_mb_type, 0, 0), IS_DIR(sub_mb_type, 0, 1));
            }
			else if(IS_SUB_4X8(sub_mb_type))
			{
                mc_part(h, n  , 0, 4, 4*s->linesize, dest_y, dest_cb, dest_cr, x_offset, y_offset,
                    qpix_put[2], chroma_put[2], qpix_avg[2], chroma_avg[2],
                    IS_DIR(sub_mb_type, 0, 0), IS_DIR(sub_mb_type, 0, 1));
                mc_part(h, n+1, 0, 4, 4*s->linesize, dest_y, dest_cb, dest_cr, x_offset+2, y_offset,
                    qpix_put[2], chroma_put[2], qpix_avg[2], chroma_avg[2],
                    IS_DIR(sub_mb_type, 0, 0), IS_DIR(sub_mb_type, 0, 1));
            }
			else
			{
                int j;
//                assert(IS_SUB_4X4(sub_mb_type));
                for(j=0; j<4; j++)
				{
                    int sub_x_offset= x_offset + 2*(j&1);
                    int sub_y_offset= y_offset +   (j&2);
                    mc_part(h, n+j, 1, 2, 0, dest_y, dest_cb, dest_cr, sub_x_offset, sub_y_offset,
                        qpix_put[2], chroma_put[2], qpix_avg[2], chroma_avg[2],
                        IS_DIR(sub_mb_type, 0, 0), IS_DIR(sub_mb_type, 0, 1));
                }
            }
        }
    }
}

static void decode_init_vlc(H264Context *h)
{
    int i;
 	
    init_vlc(&(h->chroma_dc_coeff_token_vlc), CHROMA_DC_COEFF_TOKEN_VLC_BITS, 4*5,
		&chroma_dc_coeff_token_len [0], 1, 1,
		&chroma_dc_coeff_token_bits[0], 1, 1);
	
    for(i=0; i<4; i++)
	{
        init_vlc(&(h->coeff_token_vlc[i]), COEFF_TOKEN_VLC_BITS, 4*17,
			&coeff_token_len [i][0], 1, 1,
			&coeff_token_bits[i][0], 1, 1);
    }
	
    for(i=0; i<3; i++)
	{
        init_vlc(&(h->chroma_dc_total_zeros_vlc[i]), CHROMA_DC_TOTAL_ZEROS_VLC_BITS, 4,
			&chroma_dc_total_zeros_len [i][0], 1, 1,
			&chroma_dc_total_zeros_bits[i][0], 1, 1);
    }
    for(i=0; i<15; i++)
	{
        init_vlc(&(h->total_zeros_vlc[i]), TOTAL_ZEROS_VLC_BITS, 16,
			&total_zeros_len [i][0], 1, 1,
			&total_zeros_bits[i][0], 1, 1);
    }
	
    for(i=0; i<6; i++)
	{
        init_vlc(&(h->run_vlc[i]), RUN_VLC_BITS, 7,
			&run_len [i][0], 1, 1,
			&run_bits[i][0], 1, 1);
    }
    init_vlc(&(h->run7_vlc), RUN7_VLC_BITS, 16,
		&run_len [6][0], 1, 1,
		&run_bits[6][0], 1, 1);
}

static void decode_free_vlc(H264Context *h)
{
    int i;
	
    free_vlc(&(h->chroma_dc_coeff_token_vlc));
	
    for(i=0; i<4; i++)
        free_vlc(&(h->coeff_token_vlc[i]));
	
    for(i=0; i<3; i++)
        free_vlc(&(h->chroma_dc_total_zeros_vlc[i]));
	
    for(i=0; i<15; i++)
        free_vlc(&(h->total_zeros_vlc[i]));
	
    for(i=0; i<6; i++)
        free_vlc(&(h->run_vlc[i]));
	
    free_vlc(&(h->run7_vlc));
}

static void init_pred_ptrs(H264Context *h)
{
    h->pred4x4[VERT_PRED           ]= pred4x4_vertical_c;
    h->pred4x4[HOR_PRED            ]= pred4x4_horizontal_c;
    h->pred4x4[DC_PRED             ]= pred4x4_dc_c;
    h->pred4x4[DIAG_DOWN_LEFT_PRED ]= pred4x4_down_left_c;
    h->pred4x4[DIAG_DOWN_RIGHT_PRED]= pred4x4_down_right_c;
    h->pred4x4[VERT_RIGHT_PRED     ]= pred4x4_vertical_right_c;
    h->pred4x4[HOR_DOWN_PRED       ]= pred4x4_horizontal_down_c;
    h->pred4x4[VERT_LEFT_PRED      ]= pred4x4_vertical_left_c;
    h->pred4x4[HOR_UP_PRED         ]= pred4x4_horizontal_up_c;
    h->pred4x4[LEFT_DC_PRED        ]= pred4x4_left_dc_c;
    h->pred4x4[TOP_DC_PRED         ]= pred4x4_top_dc_c;
    h->pred4x4[DC_128_PRED         ]= pred4x4_128_dc_c;
	
    h->pred8x8[DC_PRED8x8     ]= pred8x8_dc_c;
    h->pred8x8[VERT_PRED8x8   ]= pred8x8_vertical_c;
    h->pred8x8[HOR_PRED8x8    ]= pred8x8_horizontal_c;
    h->pred8x8[PLANE_PRED8x8  ]= pred8x8_plane_c;
    h->pred8x8[LEFT_DC_PRED8x8]= pred8x8_left_dc_c;
    h->pred8x8[TOP_DC_PRED8x8 ]= pred8x8_top_dc_c;
    h->pred8x8[DC_128_PRED8x8 ]= pred8x8_128_dc_c;
	
    h->pred16x16[DC_PRED8x8     ]= pred16x16_dc_c;
    h->pred16x16[VERT_PRED8x8   ]= pred16x16_vertical_c;
    h->pred16x16[HOR_PRED8x8    ]= pred16x16_horizontal_c;
    h->pred16x16[PLANE_PRED8x8  ]= pred16x16_plane_c;
    h->pred16x16[LEFT_DC_PRED8x8]= pred16x16_left_dc_c;
    h->pred16x16[TOP_DC_PRED8x8 ]= pred16x16_top_dc_c;
    h->pred16x16[DC_128_PRED8x8 ]= pred16x16_128_dc_c;
}

static void free_tables(H264Context *h)
{
    av_freep(&h->intra4x4_pred_mode);
    av_freep(&h->chroma_pred_mode_table);
    av_freep(&h->cbp_table);
    av_freep(&h->mvd_table[0]);
    av_freep(&h->mvd_table[1]);
    av_freep(&h->non_zero_count);
    av_freep(&h->slice_table_base);
    av_freep(&h->top_border);
    h->slice_table= NULL;
	
    av_freep(&h->mb2b_xy);
    av_freep(&h->mb2b8_xy);
}

static int alloc_tables(H264Context *h)
{
	MpegEncContext * const s = &h->s;
	const int big_mb_num= s->mb_stride * (s->mb_height+1);
	int x,y;

	CHECKED_ALLOCZ(h->intra4x4_pred_mode, big_mb_num * 8  * sizeof(uint8_t));

	CHECKED_ALLOCZ(h->non_zero_count    , big_mb_num * 16 * sizeof(uint8_t));
	CHECKED_ALLOCZ(h->slice_table_base  , big_mb_num * sizeof(uint8_t));
	CHECKED_ALLOCZ(h->top_border       , s->mb_width * (16+8+8) * sizeof(uint8_t));

	if( h->pps.cabac )
	{
		CHECKED_ALLOCZ(h->chroma_pred_mode_table, big_mb_num * sizeof(uint8_t));
		CHECKED_ALLOCZ(h->cbp_table, big_mb_num * sizeof(uint16_t));
		CHECKED_ALLOCZ(h->mvd_table[0], 32*big_mb_num * sizeof(uint16_t));
		CHECKED_ALLOCZ(h->mvd_table[1], 32*big_mb_num * sizeof(uint16_t));
	}

	memset(h->slice_table_base, -1, big_mb_num  * sizeof(uint8_t));
	h->slice_table= h->slice_table_base + s->mb_stride + 1;

	CHECKED_ALLOCZ(h->mb2b_xy  , big_mb_num * sizeof(uint16_t));
	CHECKED_ALLOCZ(h->mb2b8_xy , big_mb_num * sizeof(uint16_t));
	for(y=0; y<s->mb_height; y++)
	{
		for(x=0; x<s->mb_width; x++)
		{
			const int mb_xy= x + y*s->mb_stride;
			const int b_xy = 4*x + 4*y*h->b_stride;
			const int b8_xy= 2*x + 2*y*h->b8_stride;

			h->mb2b_xy [mb_xy]= b_xy;
			h->mb2b8_xy[mb_xy]= b8_xy;
		}
	}

	return 0;

fail:
	free_tables(h);
	return -1;
}

int decode_init(AVCodecContext *avctx)
{
    H264Context *h= avctx->priv_data;
    MpegEncContext * const s = &h->s;
	
    s->progressive_frame= 1;
    s->progressive_sequence= 1;
    s->picture_structure= PICT_FRAME;
    s->picture_number = 0;
	
    s->low_delay= 1;
	
    s->avctx = avctx;
    s->width = s->avctx->width;
    s->height = s->avctx->height;
	
    s->unrestricted_mv=1;
	
    init_pred_ptrs(h);
	
    decode_init_vlc(h);
	
    return 0;
}

static void frame_start(H264Context *h)
{
    MpegEncContext * const s = &h->s;
    int i;
	
    MPV_frame_start(s, s->avctx);
    h->mmco_index=0;
	
//    assert(s->linesize && s->uvlinesize);
	
    for(i=0; i<16; i++)
	{
        h->block_offset[i]= 4*((scan8[i] - scan8[0])&7) + 4*s->linesize*((scan8[i] - scan8[0])>>3);
    }

    for(i=0; i<4; i++)
	{
        h->block_offset[16+i]=
		h->block_offset[20+i]= 4*((scan8[i] - scan8[0])&7) + 4*s->uvlinesize*((scan8[i] - scan8[0])>>3);
    }
}

static  void backup_mb_border(H264Context *h, uint8_t *src_y, uint8_t *src_cb, uint8_t *src_cr, int linesize, int uvlinesize)
{
    MpegEncContext * const s = &h->s;
    int i;
	
    src_y  -=   linesize;
    src_cb -= uvlinesize;
    src_cr -= uvlinesize;
	
    h->left_border[0]= h->top_border[s->mb_x][15];
    for(i=1; i<17; i++)
	{
        h->left_border[i]= src_y[15+i*  linesize];
    }
	
    *(uint64_t*)(h->top_border[s->mb_x]+0)= *(uint64_t*)(src_y +  16*linesize);
    *(uint64_t*)(h->top_border[s->mb_x]+8)= *(uint64_t*)(src_y +8+16*linesize);
	
    h->left_border[17  ]= h->top_border[s->mb_x][16+7];
    h->left_border[17+9]= h->top_border[s->mb_x][24+7];
    for(i=1; i<9; i++)
	{
        h->left_border[i+17  ]= src_cb[7+i*uvlinesize];
        h->left_border[i+17+9]= src_cr[7+i*uvlinesize];
    }
    *(uint64_t*)(h->top_border[s->mb_x]+16)= *(uint64_t*)(src_cb+8*uvlinesize);
    *(uint64_t*)(h->top_border[s->mb_x]+24)= *(uint64_t*)(src_cr+8*uvlinesize);
}

static  void xchg_mb_border(H264Context *h, uint8_t *src_y, uint8_t *src_cb, uint8_t *src_cr, int linesize, int uvlinesize, int xchg)
{
    MpegEncContext * const s = &h->s;
    int temp8, i;
    uint64_t temp64;
	
    src_y  -=   linesize + 1;
    src_cb -= uvlinesize + 1;
    src_cr -= uvlinesize + 1;
	
    for(i=0; i<17; i++)
	{
        XCHG(h->left_border[i     ], src_y [i*  linesize], temp8, xchg);
    }
	
    XCHG(*(uint64_t*)(h->top_border[s->mb_x]+0), *(uint64_t*)(src_y +1), temp64, xchg);
    XCHG(*(uint64_t*)(h->top_border[s->mb_x]+8), *(uint64_t*)(src_y +9), temp64, 1);

    for(i=0; i<9; i++)
	{
        XCHG(h->left_border[i+17  ], src_cb[i*uvlinesize], temp8, xchg);
        XCHG(h->left_border[i+17+9], src_cr[i*uvlinesize], temp8, xchg);
    }
    XCHG(*(uint64_t*)(h->top_border[s->mb_x]+16), *(uint64_t*)(src_cb+1), temp64, 1);
    XCHG(*(uint64_t*)(h->top_border[s->mb_x]+24), *(uint64_t*)(src_cr+1), temp64, 1);
}

static int fill_default_ref_list(H264Context *h)
{
    MpegEncContext * const s = &h->s;
    int i;
    Picture sorted_short_ref[16];
	
    if(h->slice_type==B_TYPE)
	{
        int out_i;
        int limit= -1;
		
        for(out_i=0; out_i<h->short_ref_count; out_i++)
		{
            int best_i=-1;
            int best_poc=-1;
			
            for(i=0; i<h->short_ref_count; i++)
			{
                const int poc= h->short_ref[i]->poc;
                if(poc > limit && poc < best_poc)
				{
                    best_poc= poc;
                    best_i= i;
                }
            }
			
//            assert(best_i != -1);
			
            limit= best_poc;
            sorted_short_ref[out_i]= *h->short_ref[best_i];
        }
    }
	
    if(s->picture_structure == PICT_FRAME)
	{
        if(h->slice_type==B_TYPE)
		{
            const int current_poc= s->current_picture_ptr->poc;
            int list;
			
            for(list=0; list<2; list++)
			{
                int index=0;
				
                for(i=0; i<h->short_ref_count && index < h->ref_count[list]; i++)
				{
                    const int i2= list ? h->short_ref_count - i - 1 : i;
                    const int poc= sorted_short_ref[i2].poc;
					
                    if(sorted_short_ref[i2].reference != 3) continue; //FIXME refernce field shit
					
                    if((list==1 && poc > current_poc) || (list==0 && poc < current_poc))
					{
                        h->default_ref_list[list][index  ]= sorted_short_ref[i2];
                        h->default_ref_list[list][index++].pic_id= sorted_short_ref[i2].frame_num;
                    }
                }
				
                for(i=0; i<h->long_ref_count && index < h->ref_count[ list ]; i++)
				{
                    if(h->long_ref[i]->reference != 3) continue;
					
                    h->default_ref_list[ list ][index  ]= *h->long_ref[i];
                    h->default_ref_list[ list ][index++].pic_id= i;;
                }
				
                if(h->long_ref_count > 1 && h->short_ref_count==0)
				{
                    Picture temp= h->default_ref_list[1][0];
                    h->default_ref_list[1][0] = h->default_ref_list[1][1];
                    h->default_ref_list[1][0] = temp;
                }
				
                if(index < h->ref_count[ list ])
                    memset(&h->default_ref_list[list][index], 0, sizeof(Picture)*(h->ref_count[ list ] - index));
            }
        }
		else
		{
            int index=0;
            for(i=0; i<h->short_ref_count && index < h->ref_count[0]; i++)
			{
                if(h->short_ref[i]->reference != 3) continue; //FIXME refernce field shit
                h->default_ref_list[0][index  ]= *h->short_ref[i];
                h->default_ref_list[0][index++].pic_id= h->short_ref[i]->frame_num;
            }
            for(i=0; i<h->long_ref_count && index < h->ref_count[0]; i++)
			{
                if(h->long_ref[i]->reference != 3) continue;
                h->default_ref_list[0][index  ]= *h->long_ref[i];
                h->default_ref_list[0][index++].pic_id= i;;
            }
            if(index < h->ref_count[0])
                memset(&h->default_ref_list[0][index], 0, sizeof(Picture)*(h->ref_count[0] - index));
        }
    }

    return 0;
}

static int decode_ref_pic_list_reordering(H264Context *h)
{
    MpegEncContext * const s = &h->s;
    int list;
	
    if(h->slice_type==I_TYPE || h->slice_type==SI_TYPE) return 0; //FIXME move beofre func
	
    for(list=0; list<2; list++)
	{
        memcpy(h->ref_list[list], h->default_ref_list[list], sizeof(Picture)*h->ref_count[list]);
		
        if(get_bits1(&s->gb))
		{
            int pred= h->curr_pic_num;
            int index;
			
            for(index=0; ; index++)
			{
                int reordering_of_pic_nums_idc= get_ue_golomb(&s->gb);
                int pic_id;
                int i;				
				
                if(index >= h->ref_count[list])
                    return -1;

                if(reordering_of_pic_nums_idc<3)
				{
                    if(reordering_of_pic_nums_idc<2)
					{
                        const int abs_diff_pic_num= get_ue_golomb(&s->gb) + 1;
						
                        if(abs_diff_pic_num >= h->max_pic_num)
                            return -1;
						
                        if(reordering_of_pic_nums_idc == 0) pred-= abs_diff_pic_num;
                        else                                pred+= abs_diff_pic_num;
                        pred &= h->max_pic_num - 1;
						
                        for(i= h->ref_count[list]-1; i>=index; i--)
						{
                            if(h->ref_list[list][i].pic_id == pred && h->ref_list[list][i].long_ref==0)
                                break;
                        }
                    }
					else
					{
                        pic_id= get_ue_golomb(&s->gb); //long_term_pic_idx
						
                        for(i= h->ref_count[list]-1; i>=index; i--)
						{
                            if(h->ref_list[list][i].pic_id == pic_id && h->ref_list[list][i].long_ref==1)
                                break;
                        }
                    }
					
                    if(i < index)
					{
                        memset(&h->ref_list[list][index], 0, sizeof(Picture)); //FIXME
                    }
					else if(i > index)
					{
                        Picture tmp= h->ref_list[list][i];
                        for(; i>index; i--)
						{
                            h->ref_list[list][i]= h->ref_list[list][i-1];
                        }
                        h->ref_list[list][index]= tmp;
                    }
                }
				else if(reordering_of_pic_nums_idc==3)
                    break;
                else
                    return -1;
            }
        }
		
        if(h->slice_type!=B_TYPE) break;
    }
    return 0;
}

static int pred_weight_table(H264Context *h)
{
    MpegEncContext * const s = &h->s;
    int list, i;
	
    h->luma_log2_weight_denom= get_ue_golomb(&s->gb);
    h->chroma_log2_weight_denom= get_ue_golomb(&s->gb);
	
    for(list=0; list<2; list++)
	{
        for(i=0; i<h->ref_count[list]; i++)
		{
            int luma_weight_flag, chroma_weight_flag;
			
            luma_weight_flag= get_bits1(&s->gb);
            if(luma_weight_flag)
			{
                h->luma_weight[list][i]= get_se_golomb(&s->gb);
                h->luma_offset[list][i]= get_se_golomb(&s->gb);
            }
			
            chroma_weight_flag= get_bits1(&s->gb);
            if(chroma_weight_flag)
			{
                int j;
                for(j=0; j<2; j++)
				{
                    h->chroma_weight[list][i][j]= get_se_golomb(&s->gb);
                    h->chroma_offset[list][i][j]= get_se_golomb(&s->gb);
                }
            }
        }
        if(h->slice_type != B_TYPE) break;
    }
    return 0;
}

static void idr(H264Context *h)
{
    int i;
	
    for(i=0; i<h->long_ref_count; i++)
	{
        h->long_ref[i]->reference=0;
        h->long_ref[i]= NULL;
    }
    h->long_ref_count=0;
	
    for(i=0; i<h->short_ref_count; i++)
	{
        h->short_ref[i]->reference=0;
        h->short_ref[i]= NULL;
    }
    h->short_ref_count=0;
}

static Picture * remove_short(H264Context *h, int frame_num)
{
    int i;
		
    for(i=0; i<h->short_ref_count; i++)
	{
        Picture *pic= h->short_ref[i];
		
        if(pic->frame_num == frame_num)
		{
            h->short_ref[i]= NULL;
            memmove(&h->short_ref[i], &h->short_ref[i+1], (h->short_ref_count - i - 1)*sizeof(Picture*));
            h->short_ref_count--;
            return pic;
        }
    }
    return NULL;
}

static Picture * remove_long(H264Context *h, int i)
{
    Picture *pic;
	
    if(i >= h->long_ref_count) return NULL;
    pic= h->long_ref[i];
    if(pic==NULL) return NULL;
	
    h->long_ref[i]= NULL;
    memmove(&h->long_ref[i], &h->long_ref[i+1], (h->long_ref_count - i - 1)*sizeof(Picture*));
    h->long_ref_count--;
	
    return pic;
}

static int execute_ref_pic_marking(H264Context *h, MMCO *mmco, int mmco_count)
{
    MpegEncContext * const s = &h->s;
    int i;
    int current_is_long=0;
    Picture *pic;	
	
    for(i=0; i<mmco_count; i++)
	{		
        switch(mmco[i].opcode)
		{
        case MMCO_SHORT2UNUSED:
            pic= remove_short(h, mmco[i].short_frame_num);
            if(pic==NULL) return -1;
            pic->reference= 0;
            break;
        case MMCO_SHORT2LONG:
            pic= remove_long(h, mmco[i].long_index);
            if(pic) pic->reference=0;
			
            h->long_ref[ mmco[i].long_index ]= remove_short(h, mmco[i].short_frame_num);
            h->long_ref[ mmco[i].long_index ]->long_ref=1;
            break;
        case MMCO_LONG2UNUSED:
            pic= remove_long(h, mmco[i].long_index);
            if(pic==NULL) return -1;
            pic->reference= 0;
            break;
        case MMCO_LONG:
            pic= remove_long(h, mmco[i].long_index);
            if(pic) pic->reference=0;
			
            h->long_ref[ mmco[i].long_index ]= s->current_picture_ptr;
            h->long_ref[ mmco[i].long_index ]->long_ref=1;
            h->long_ref_count++;
			
            current_is_long=1;
            break;
        case MMCO_SET_MAX_LONG:
//            assert(mmco[i].long_index <= 16);
            while(mmco[i].long_index < h->long_ref_count){
                pic= remove_long(h, mmco[i].long_index);
                pic->reference=0;
            }
            while(mmco[i].long_index > h->long_ref_count){
                h->long_ref[ h->long_ref_count++ ]= NULL;
            }
            break;
        case MMCO_RESET:
            while(h->short_ref_count)
			{
                pic= remove_short(h, h->short_ref[0]->frame_num);
                pic->reference=0;
            }
            while(h->long_ref_count)
			{
                pic= remove_long(h, h->long_ref_count-1);
                pic->reference=0;
            }
            break;
        default: 
//			assert(0);
			break;
        }
    }
	
    if(!current_is_long)
	{
        pic= remove_short(h, s->current_picture_ptr->frame_num);
        if(pic)
            pic->reference=0;
		
        if(h->short_ref_count)
            memmove(&h->short_ref[1], &h->short_ref[0], h->short_ref_count*sizeof(Picture*));
		
        h->short_ref[0]= s->current_picture_ptr;
        h->short_ref[0]->long_ref=0;
        h->short_ref_count++;
    }
	
    return 0;
}

static int decode_ref_pic_marking(H264Context *h)
{
    MpegEncContext * const s = &h->s;
    int i;
	
    if(h->nal_unit_type == NAL_IDR_SLICE)
	{
        s->broken_link= get_bits1(&s->gb) -1;
        h->mmco[0].long_index= get_bits1(&s->gb) - 1; // current_long_term_idx
        if(h->mmco[0].long_index == -1)
            h->mmco_index= 0;
        else
		{
            h->mmco[0].opcode= MMCO_LONG;
            h->mmco_index= 1;
        }
    }
	else
	{
        if(get_bits1(&s->gb))
		{ 
            for(i= h->mmco_index; i<MAX_MMCO_COUNT; i++)
			{
                MMCOOpcode opcode= get_ue_golomb(&s->gb);;
				
                h->mmco[i].opcode= opcode;
                if(opcode==MMCO_SHORT2UNUSED || opcode==MMCO_SHORT2LONG)
				{
                    h->mmco[i].short_frame_num= (h->frame_num - get_ue_golomb(&s->gb) - 1) & ((1<<h->sps.log2_max_frame_num)-1); //FIXME fields
                }
                if(opcode==MMCO_SHORT2LONG || opcode==MMCO_LONG2UNUSED || opcode==MMCO_LONG || opcode==MMCO_SET_MAX_LONG)
				{
                    h->mmco[i].long_index= get_ue_golomb(&s->gb);
                    if(h->mmco[i].long_index >= 16)
                        return -1;
                }
				
                if(opcode > MMCO_LONG)
                    return -1;
            }
            h->mmco_index= i;
        }
		else
		{
//            assert(h->long_ref_count + h->short_ref_count <= h->sps.ref_frame_count);
			
            if(h->long_ref_count + h->short_ref_count == h->sps.ref_frame_count)
			{ //FIXME fields
                h->mmco[0].opcode= MMCO_SHORT2UNUSED;
                h->mmco[0].short_frame_num= h->short_ref[ h->short_ref_count - 1 ]->frame_num;
                h->mmco_index= 1;
            }else
                h->mmco_index= 0;
        }
    }
	
    return 0;
}

static int init_poc(H264Context *h)
{
    MpegEncContext * const s = &h->s;
    const int max_frame_num= 1<<h->sps.log2_max_frame_num;
    int field_poc[2];
	
    if(h->nal_unit_type == NAL_IDR_SLICE)
	{
        h->frame_num_offset= 0;
    }
	else
	{
        if(h->frame_num < h->prev_frame_num)
            h->frame_num_offset= h->prev_frame_num_offset + max_frame_num;
        else
            h->frame_num_offset= h->prev_frame_num_offset;
    }
	
    if(h->sps.poc_type==0)
	{
        const int max_poc_lsb= 1<<h->sps.log2_max_poc_lsb;
		
        if     (h->poc_lsb < h->prev_poc_lsb && h->prev_poc_lsb - h->poc_lsb >= max_poc_lsb/2)
            h->poc_msb = h->prev_poc_msb + max_poc_lsb;
        else if(h->poc_lsb > h->prev_poc_lsb && h->prev_poc_lsb - h->poc_lsb < -max_poc_lsb/2)
            h->poc_msb = h->prev_poc_msb - max_poc_lsb;
        else
            h->poc_msb = h->prev_poc_msb;

        field_poc[0] = field_poc[1] = h->poc_msb + h->poc_lsb;
        if(s->picture_structure == PICT_FRAME)
            field_poc[1] += h->delta_poc_bottom;
    }
	else if(h->sps.poc_type==1)
	{
        int abs_frame_num, expected_delta_per_poc_cycle, expectedpoc;
        int i;
		
        if(h->sps.poc_cycle_length != 0)
            abs_frame_num = h->frame_num_offset + h->frame_num;
        else
            abs_frame_num = 0;
		
        if(h->nal_ref_idc==0 && abs_frame_num > 0)
            abs_frame_num--;
		
        expected_delta_per_poc_cycle = 0;
        for(i=0; i < h->sps.poc_cycle_length; i++)
            expected_delta_per_poc_cycle += h->sps.offset_for_ref_frame[ i ]; //FIXME integrate during sps parse
		
        if(abs_frame_num > 0)
		{
            int poc_cycle_cnt          = (abs_frame_num - 1) / h->sps.poc_cycle_length;
            int frame_num_in_poc_cycle = (abs_frame_num - 1) % h->sps.poc_cycle_length;
			
            expectedpoc = poc_cycle_cnt * expected_delta_per_poc_cycle;
            for(i = 0; i <= frame_num_in_poc_cycle; i++)
                expectedpoc = expectedpoc + h->sps.offset_for_ref_frame[ i ];
        } else
            expectedpoc = 0;
		
        if(h->nal_ref_idc == 0)
            expectedpoc = expectedpoc + h->sps.offset_for_non_ref_pic;
		
        field_poc[0] = expectedpoc + h->delta_poc[0];
        field_poc[1] = field_poc[0] + h->sps.offset_for_top_to_bottom_field;
		
        if(s->picture_structure == PICT_FRAME)
            field_poc[1] += h->delta_poc[1];
    }
	else
	{
        int poc;
        if(h->nal_unit_type == NAL_IDR_SLICE)
		{
            poc= 0;
        }
		else
		{
            if(h->nal_ref_idc) poc= 2*(h->frame_num_offset + h->frame_num);
            else               poc= 2*(h->frame_num_offset + h->frame_num) - 1;
        }
        field_poc[0]= poc;
        field_poc[1]= poc;
    }
	
    if(s->picture_structure != PICT_BOTTOM_FIELD)
        s->current_picture_ptr->field_poc[0]= field_poc[0];
    if(s->picture_structure != PICT_TOP_FIELD)
        s->current_picture_ptr->field_poc[1]= field_poc[1];
    if(s->picture_structure == PICT_FRAME) // FIXME field pix?
        s->current_picture_ptr->poc= FFMIN(field_poc[0], field_poc[1]);
	
    return 0;
}

static int decode_slice_header(H264Context *h)
{
    MpegEncContext * const s = &h->s;
	
	int first_mb_in_slice, pps_id; 
	
	int num_ref_idx_active_override_flag;
    static const uint8_t slice_type_map[5]= {P_TYPE, B_TYPE, I_TYPE, SP_TYPE, SI_TYPE};
	
    s->current_picture.reference= h->nal_ref_idc != 0;
	
	first_mb_in_slice= get_ue_golomb(&s->gb);
	
    h->slice_type= get_ue_golomb(&s->gb);
    if(h->slice_type > 9)
	{
    }
    if(h->slice_type > 4)
	{
        h->slice_type -= 5;
        h->slice_type_fixed=1;
    }
	else
	{
        h->slice_type_fixed=0;
	}
	
    h->slice_type= slice_type_map[ h->slice_type ];
	
    s->pict_type= h->slice_type; // to make a few old func happy, its wrong though
	
    pps_id= get_ue_golomb(&s->gb);
    if(pps_id>255)
        return -1;

    h->pps= h->pps_buffer[pps_id];
    if(h->pps.slice_group_count == 0)
        return -1;
	
    h->sps= h->sps_buffer[ h->pps.sps_id ];
    if(h->sps.log2_max_frame_num == 0)
        return -1;
	
    s->mb_width= h->sps.mb_width;
    s->mb_height= h->sps.mb_height;
	
    h->b_stride=  s->mb_width*4;
    h->b8_stride= s->mb_width*2;
	
    s->mb_x = first_mb_in_slice % s->mb_width;
    s->mb_y = first_mb_in_slice / s->mb_width; //FIXME AFFW
	
    s->width = 16*s->mb_width - 2*(h->sps.crop_left + h->sps.crop_right );
    if(h->sps.frame_mbs_only_flag)
	{
        s->height= 16*s->mb_height - 2*(h->sps.crop_top  + h->sps.crop_bottom);
	}
    else
	{
        s->height= 16*s->mb_height - 4*(h->sps.crop_top  + h->sps.crop_bottom); //FIXME recheck
	}
	
    if (s->context_initialized && ( s->width != s->avctx->width || s->height != s->avctx->height))
	{
        free_tables(h);
        MPV_common_end(s);
    }
    if (!s->context_initialized)
	{
        if (MPV_common_init(s) < 0)
            return -1;
		
        alloc_tables(h);
		
        s->avctx->width = s->width;
        s->avctx->height = s->height;
    }
	
    if(first_mb_in_slice == 0)
	{
        frame_start(h);
    }
	
    s->current_picture_ptr->frame_num= //FIXME frame_num cleanup
	h->frame_num= get_bits(&s->gb, h->sps.log2_max_frame_num);
	
    if(h->sps.frame_mbs_only_flag)
	{
        s->picture_structure= PICT_FRAME;
    }
	else
	{
        if(get_bits1(&s->gb)) //field_pic_flag
		{
            s->picture_structure= PICT_TOP_FIELD + get_bits1(&s->gb); //bottom_field_flag
		}
        else
		{
            s->picture_structure= PICT_FRAME;
		}
    }
	
    if(s->picture_structure==PICT_FRAME)
	{
        h->curr_pic_num=   h->frame_num;
        h->max_pic_num= 1<< h->sps.log2_max_frame_num;
    }
	else
	{
        h->curr_pic_num= 2*h->frame_num;
        h->max_pic_num= 1<<(h->sps.log2_max_frame_num + 1);
    }
	
    if(h->nal_unit_type == NAL_IDR_SLICE)
	{
        get_ue_golomb(&s->gb); /* idr_pic_id */
    }
	
    if(h->sps.poc_type==0)
	{
        h->poc_lsb= get_bits(&s->gb, h->sps.log2_max_poc_lsb);
		
        if(h->pps.pic_order_present==1 && s->picture_structure==PICT_FRAME)
		{
            h->delta_poc_bottom= get_se_golomb(&s->gb);
        }
    }
	
    if(h->sps.poc_type==1 && !h->sps.delta_pic_order_always_zero_flag)
	{
        h->delta_poc[0]= get_se_golomb(&s->gb);
		
        if(h->pps.pic_order_present==1 && s->picture_structure==PICT_FRAME)
		{
            h->delta_poc[1]= get_se_golomb(&s->gb);
		}
    }
	
    init_poc(h);
	
    if(h->pps.redundant_pic_cnt_present)
	{
        h->redundant_pic_count= get_ue_golomb(&s->gb);
    }
	
    h->ref_count[0]= h->pps.ref_count[0];
    h->ref_count[1]= h->pps.ref_count[1];
	
    if(h->slice_type == P_TYPE || h->slice_type == SP_TYPE || h->slice_type == B_TYPE)
	{
        if(h->slice_type == B_TYPE)
		{
            h->direct_spatial_mv_pred= get_bits1(&s->gb);
        }
        num_ref_idx_active_override_flag= get_bits1(&s->gb);
		
        if(num_ref_idx_active_override_flag)
		{
            h->ref_count[0]= get_ue_golomb(&s->gb) + 1;
            if(h->slice_type==B_TYPE)
			{
                h->ref_count[1]= get_ue_golomb(&s->gb) + 1;
			}
			
            if(h->ref_count[0] > 32 || h->ref_count[1] > 32)
                return -1;
        }
    }
	
    if(first_mb_in_slice == 0)
	{
        fill_default_ref_list(h);
    }
	
    decode_ref_pic_list_reordering(h);
	
    if(   (h->pps.weighted_pred          && (h->slice_type == P_TYPE || h->slice_type == SP_TYPE ))
		|| (h->pps.weighted_bipred_idc==1 && h->slice_type==B_TYPE ) )
	{
        pred_weight_table(h);
	}
	
    if(s->current_picture.reference)
	{
        decode_ref_pic_marking(h);
	}
	
    if( h->slice_type != I_TYPE && h->slice_type != SI_TYPE && h->pps.cabac )
	{
        h->cabac_init_idc = get_ue_golomb(&s->gb);
	}
	
    h->last_qscale_diff = 0;
    s->qscale = h->pps.init_qp + get_se_golomb(&s->gb);
    if(s->qscale<0 || s->qscale>51)
        return -1;

    if(h->slice_type == SP_TYPE)
        get_bits1(&s->gb); /* sp_for_switch_flag */

    if(h->slice_type==SP_TYPE || h->slice_type == SI_TYPE)
        get_se_golomb(&s->gb); /* slice_qs_delta */
	
    h->deblocking_filter = 1;
    h->slice_alpha_c0_offset = 0;
    h->slice_beta_offset = 0;
    if( h->pps.deblocking_filter_parameters_present )
	{
        h->deblocking_filter= get_ue_golomb(&s->gb);
        if(h->deblocking_filter < 2)
            h->deblocking_filter^= 1; // 1<->0
		
        if( h->deblocking_filter )
		{
            h->slice_alpha_c0_offset = get_se_golomb(&s->gb) << 1;
            h->slice_beta_offset = get_se_golomb(&s->gb) << 1;
        }
    }
	
    return 0;
}

static  int get_level_prefix(GetBitContext *gb)
{
    unsigned int buf;
    int log;
	
    OPEN_READER(re, gb);
    UPDATE_CACHE(re, gb);
    buf=re_cache; //GET_CACHE(re, gb);
	
    log= 32 - av_log2(buf);
	
    LAST_SKIP_BITS(re, gb, log);
    CLOSE_READER(re, gb);
	
    return log-1;
}

static int decode_residual(H264Context *h, GetBitContext *gb, DCTELEM *block, int n, const uint8_t *scantable, int qp, int max_coeff)
{
    const uint16_t *qmul= dequant_coeff[qp];
    static const int coeff_token_table_index[17]= {0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3};
    int level[16], run[16];
    int suffix_length, zeros_left, coeff_num, coeff_token, total_coeff, i, trailing_ones;
	
    if(n == CHROMA_DC_BLOCK_INDEX)
	{
        coeff_token= get_vlc2(gb, h->chroma_dc_coeff_token_vlc.table, CHROMA_DC_COEFF_TOKEN_VLC_BITS, 1);
        total_coeff= coeff_token>>2;
    }
	else
	{
        if(n == LUMA_DC_BLOCK_INDEX)
		{
            total_coeff= pred_non_zero_count(h, 0);
            coeff_token= get_vlc2(gb, h->coeff_token_vlc[ coeff_token_table_index[total_coeff] ].table, COEFF_TOKEN_VLC_BITS, 2);
            total_coeff= coeff_token>>2;
        }
		else
		{
            total_coeff= pred_non_zero_count(h, n);
            coeff_token= get_vlc2(gb, h->coeff_token_vlc[ coeff_token_table_index[total_coeff] ].table, COEFF_TOKEN_VLC_BITS, 2);
            total_coeff= coeff_token>>2;
            h->non_zero_count_cache[ scan8[n] ]= total_coeff;
        }
    }
	
    if(total_coeff==0)
        return 0;
	
    trailing_ones= coeff_token&3;
//    assert(total_coeff<=16);
	
    for(i=0; i<trailing_ones; i++)
	{
        level[i]= 1 - 2*get_bits1(gb);
    }
	
    suffix_length= total_coeff > 10 && trailing_ones < 3;
	
    for(; i<total_coeff; i++)
	{
        const int prefix= get_level_prefix(gb);
        int level_code, mask;
		
        if(prefix<14)
		{ 
            if(suffix_length)
                level_code= (prefix<<suffix_length) + get_bits(gb, suffix_length); //part
            else
                level_code= (prefix<<suffix_length); //part
        }
		else if(prefix==14)
		{
            if(suffix_length)
                level_code= (prefix<<suffix_length) + get_bits(gb, suffix_length); //part
            else
                level_code= prefix + get_bits(gb, 4); //part
        }
		else if(prefix==15)
		{
            level_code= (prefix<<suffix_length) + get_bits(gb, 12); //part
            if(suffix_length==0) level_code+=15; //FIXME doesnt make (much)sense
        }
		else
		{
            return -1;
        }
		
        if(i==trailing_ones && i<3) level_code+= 2; //FIXME split first iteration
		
        mask= -(level_code&1);
        level[i]= (((2+level_code)>>1) ^ mask) - mask;
		
        if(suffix_length==0) suffix_length=1; //FIXME split first iteration
		
#if 1
        if(ABS(level[i]) > (3<<(suffix_length-1)) && suffix_length<6) suffix_length++;
#else
        if((2+level_code)>>1) > (3<<(suffix_length-1)) && suffix_length<6) suffix_length++;
#endif
    }
	
    if(total_coeff == max_coeff)
        zeros_left=0;
    else
	{
        if(n == CHROMA_DC_BLOCK_INDEX)
            zeros_left= get_vlc2(gb, h->chroma_dc_total_zeros_vlc[ total_coeff-1 ].table, CHROMA_DC_TOTAL_ZEROS_VLC_BITS, 1);
        else
            zeros_left= get_vlc2(gb, h->total_zeros_vlc[ total_coeff-1 ].table, TOTAL_ZEROS_VLC_BITS, 1);
    }
	
    for(i=0; i<total_coeff-1; i++)
	{
        if(zeros_left <=0)
            break;
        else if(zeros_left < 7)
		{
            run[i]= get_vlc2(gb, h->run_vlc[zeros_left-1].table, RUN_VLC_BITS, 1);
        }
		else
		{
            run[i]= get_vlc2(gb, h->run7_vlc.table, RUN7_VLC_BITS, 2);
        }
        zeros_left -= run[i];
    }
	
    if(zeros_left<0)
        return -1;
	
    for(; i<total_coeff-1; i++){
        run[i]= 0;
    }
	
    run[i]= zeros_left;
	
    coeff_num=-1;
    if(n > 24)
	{
        for(i=total_coeff-1; i>=0; i--)
		{
            int j;
			
            coeff_num += run[i] + 1; //FIXME add 1 earlier ?
            j= scantable[ coeff_num ];
			
            block[j]= level[i];
        }
    }
	else
	{
        for(i=total_coeff-1; i>=0; i--)
		{
            int j;
			
            coeff_num += run[i] + 1; //FIXME add 1 earlier ?
            j= scantable[ coeff_num ];
			
            block[j]= level[i] * qmul[j];
        }
    }
    return 0;
}

static int decode_mb_cavlc(H264Context *h)
{
    MpegEncContext * const s = &h->s;
    const int mb_xy= s->mb_x + s->mb_y*s->mb_stride;
    int mb_type, partition_count, cbp;
	
    clear_blocks_c(h->mb); //FIXME avoid if allready clear (move after skip handlong?
	
    cbp = 0; 

    if(h->slice_type != I_TYPE && h->slice_type != SI_TYPE)
	{
        if(s->mb_skip_run==-1)
            s->mb_skip_run= get_ue_golomb(&s->gb);
		
        if (s->mb_skip_run--)
		{
            int mx, my;

            mb_type= MB_TYPE_16x16|MB_TYPE_P0L0|MB_TYPE_P1L0;
			
            memset(h->non_zero_count[mb_xy], 0, 16);
            memset(h->non_zero_count_cache + 8, 0, 8*5); //FIXME ugly, remove pfui
			
            if(h->sps.mb_aff && s->mb_skip_run==0 && (s->mb_y&1)==0)
			{
                h->mb_field_decoding_flag= get_bits1(&s->gb);
            }
			
            if(h->mb_field_decoding_flag)
                mb_type|= MB_TYPE_INTERLACED;
			
            fill_caches(h, mb_type); //FIXME check what is needed and what not ...
            pred_pskip_motion(h, &mx, &my);
            fill_rectangle(&h->ref_cache[0][scan8[0]], 4, 4, 8, 0, 1);
            fill_rectangle(  h->mv_cache[0][scan8[0]], 4, 4, 8, pack16to32(mx,my), 4);
            write_back_motion(h, mb_type);
			
            s->current_picture.mb_type[mb_xy]= mb_type; //FIXME SKIP type
            s->current_picture.qscale_table[mb_xy]= s->qscale;
            h->slice_table[ mb_xy ]= h->slice_num;
			
            return 0;
        }
    }
    if(h->sps.mb_aff /* && !field pic FIXME needed? */)
	{
        if((s->mb_y&1)==0)
            h->mb_field_decoding_flag = get_bits1(&s->gb);
    }else
        h->mb_field_decoding_flag=0; //FIXME som ed note ?!	
	
    mb_type= get_ue_golomb(&s->gb);
    if(h->slice_type == B_TYPE)
	{
        if(mb_type < 23)
		{
            partition_count= b_mb_type_info[mb_type].partition_count;
            mb_type=         b_mb_type_info[mb_type].type;
        }
		else
		{
            mb_type -= 23;
            goto decode_intra_mb;
        }
    }
	else if(h->slice_type == P_TYPE /*|| h->slice_type == SP_TYPE */)
	{
        if(mb_type < 5)
		{
            partition_count= p_mb_type_info[mb_type].partition_count;
            mb_type=         p_mb_type_info[mb_type].type;
        }
		else
		{
            mb_type -= 5;
            goto decode_intra_mb;
        }
    }
	else
	{
//		assert(h->slice_type == I_TYPE);
decode_intra_mb:
        if(mb_type > 25)
            return -1;

        partition_count=0;
        cbp= i_mb_type_info[mb_type].cbp;
        h->intra16x16_pred_mode= i_mb_type_info[mb_type].pred_mode;
        mb_type= i_mb_type_info[mb_type].type;
    }
	
    if(h->mb_field_decoding_flag)
        mb_type |= MB_TYPE_INTERLACED;
	
    s->current_picture.mb_type[mb_xy]= mb_type;
    h->slice_table[ mb_xy ]= h->slice_num;
	
    if(IS_INTRA_PCM(mb_type))
	{
        const uint8_t *ptr;
        int x, y;
		
        align_get_bits(&s->gb);
		
        ptr= s->gb.buffer + get_bits_count(&s->gb);
		
        for(y=0; y<16; y++)
		{
            const int index= 4*(y&3) + 64*(y>>2);
            for(x=0; x<16; x++)
			{
                h->mb[index + (x&3) + 16*(x>>2)]= *(ptr++);
            }
        }
        for(y=0; y<8; y++)
		{
            const int index= 256 + 4*(y&3) + 32*(y>>2);
            for(x=0; x<8; x++)
			{
                h->mb[index + (x&3) + 16*(x>>2)]= *(ptr++);
            }
        }
        for(y=0; y<8; y++)
		{
            const int index= 256 + 64 + 4*(y&3) + 32*(y>>2);
            for(x=0; x<8; x++)
			{
                h->mb[index + (x&3) + 16*(x>>2)]= *(ptr++);
            }
        }
		
        skip_bits(&s->gb, 384); //FIXME check /fix the bitstream readers
		
        memset(h->non_zero_count[mb_xy], 16, 16);
        s->current_picture.qscale_table[mb_xy]= s->qscale;
		
        return 0;
    }
	
    fill_caches(h, mb_type);
	
    //mb_pred
    if(IS_INTRA(mb_type))
	{
		if(IS_INTRA4x4(mb_type))
		{
			int i;
			
			for(i=0; i<16; i++)
			{
				const int mode_coded= !get_bits1(&s->gb);
				const int predicted_mode=  pred_intra_mode(h, i);
				int mode;
				
				if(mode_coded)
				{
					const int rem_mode= get_bits(&s->gb, 3);
					if(rem_mode<predicted_mode)
						mode= rem_mode;
					else
						mode= rem_mode + 1;
				}
				else
				{
					mode= predicted_mode;
				}
				
				h->intra4x4_pred_mode_cache[ scan8[i] ] = mode;
			}
			write_back_intra_pred_mode(h);
			if( check_intra4x4_pred_mode(h) < 0)
				return -1;
		}
		else
		{
			h->intra16x16_pred_mode= check_intra_pred_mode(h, h->intra16x16_pred_mode);
			if(h->intra16x16_pred_mode < 0)
				return -1;
		}
		h->chroma_pred_mode= get_ue_golomb(&s->gb);
		
		h->chroma_pred_mode= check_intra_pred_mode(h, h->chroma_pred_mode);
		if(h->chroma_pred_mode < 0)
			return -1;
    }
	else if(partition_count==4)
	{
        int i, j, sub_partition_count[4], list, ref[2][4];
		
        if(h->slice_type == B_TYPE)
		{
            for(i=0; i<4; i++){
                h->sub_mb_type[i]= get_ue_golomb(&s->gb);
                if(h->sub_mb_type[i] >=13)
                    return -1;

                sub_partition_count[i]= b_sub_mb_type_info[ h->sub_mb_type[i] ].partition_count;
                h->sub_mb_type[i]=      b_sub_mb_type_info[ h->sub_mb_type[i] ].type;
            }
        }
		else
		{
//            assert(h->slice_type == P_TYPE || h->slice_type == SP_TYPE); //FIXME SP correct ?
            for(i=0; i<4; i++)
			{
                h->sub_mb_type[i]= get_ue_golomb(&s->gb);
                if(h->sub_mb_type[i] >=4)
                    return -1;

                sub_partition_count[i]= p_sub_mb_type_info[ h->sub_mb_type[i] ].partition_count;
                h->sub_mb_type[i]=      p_sub_mb_type_info[ h->sub_mb_type[i] ].type;
            }
        }
		
        for(list=0; list<2; list++)
		{
            const int ref_count= IS_REF0(mb_type) ? 1 : h->ref_count[list];
            if(ref_count == 0) continue;
            for(i=0; i<4; i++)
			{
                if(IS_DIR(h->sub_mb_type[i], 0, list) && !IS_DIRECT(h->sub_mb_type[i]))
				{
                    ref[list][i] = get_te0_golomb(&s->gb, ref_count); //FIXME init to 0 before and skip?
                }
				else
				{
                    ref[list][i] = -1;
                }
            }
        }
		
        for(list=0; list<2; list++)
		{
            const int ref_count= IS_REF0(mb_type) ? 1 : h->ref_count[list];
            if(ref_count == 0) continue;
			
            for(i=0; i<4; i++)
			{
                h->ref_cache[list][ scan8[4*i]   ]=h->ref_cache[list][ scan8[4*i]+1 ]=
				h->ref_cache[list][ scan8[4*i]+8 ]=h->ref_cache[list][ scan8[4*i]+9 ]= ref[list][i];
				
                if(IS_DIR(h->sub_mb_type[i], 0, list) && !IS_DIRECT(h->sub_mb_type[i]))
				{
                    const int sub_mb_type= h->sub_mb_type[i];
                    const int block_width= (sub_mb_type & (MB_TYPE_16x16|MB_TYPE_16x8)) ? 2 : 1;
                    for(j=0; j<sub_partition_count[i]; j++)
					{
                        int mx, my;
                        const int index= 4*i + block_width*j;
                        int16_t (* mv_cache)[2]= &h->mv_cache[list][ scan8[index] ];
                        pred_motion(h, index, block_width, list, h->ref_cache[list][ scan8[index] ], &mx, &my);
                        mx += get_se_golomb(&s->gb);
                        my += get_se_golomb(&s->gb);
						
                        if(IS_SUB_8X8(sub_mb_type))
						{
                            mv_cache[ 0 ][0]= mv_cache[ 1 ][0]=
							mv_cache[ 8 ][0]= mv_cache[ 9 ][0]= mx;
                            mv_cache[ 0 ][1]= mv_cache[ 1 ][1]=
							mv_cache[ 8 ][1]= mv_cache[ 9 ][1]= my;
                        }
						else if(IS_SUB_8X4(sub_mb_type))
						{
                            mv_cache[ 0 ][0]= mv_cache[ 1 ][0]= mx;
                            mv_cache[ 0 ][1]= mv_cache[ 1 ][1]= my;
                        }
						else if(IS_SUB_4X8(sub_mb_type))
						{
                            mv_cache[ 0 ][0]= mv_cache[ 8 ][0]= mx;
                            mv_cache[ 0 ][1]= mv_cache[ 8 ][1]= my;
                        }
						else
						{
//                            assert(IS_SUB_4X4(sub_mb_type));
                            mv_cache[ 0 ][0]= mx;
                            mv_cache[ 0 ][1]= my;
                        }
                    }
                }
				else
				{
                    uint32_t *p= (uint32_t *)&h->mv_cache[list][ scan8[4*i] ][0];
                    p[0] = p[1]= p[8] = p[9]= 0;
                }
            }
        }
    }
	else if(!IS_DIRECT(mb_type))
	{
        int list, mx, my, i;

        if(IS_16X16(mb_type))
		{
            for(list=0; list<2; list++)
			{
                if(h->ref_count[0]>0)
				{
                    if(IS_DIR(mb_type, 0, list))
					{
                        const int val= get_te0_golomb(&s->gb, h->ref_count[list]);
                        fill_rectangle(&h->ref_cache[list][ scan8[0] ], 4, 4, 8, val, 1);
                    }
                }
            }

            for(list=0; list<2; list++)
			{
                if(IS_DIR(mb_type, 0, list))
				{
                    pred_motion(h, 0, 4, list, h->ref_cache[list][ scan8[0] ], &mx, &my);
                    mx += get_se_golomb(&s->gb);
                    my += get_se_golomb(&s->gb);
					
                    fill_rectangle(h->mv_cache[list][ scan8[0] ], 4, 4, 8, pack16to32(mx,my), 4);
                }
            }
        }
        else if(IS_16X8(mb_type))
		{
            for(list=0; list<2; list++)
			{
                if(h->ref_count[list]>0)
				{
                    for(i=0; i<2; i++)
					{
                        if(IS_DIR(mb_type, i, list))
						{
                            const int val= get_te0_golomb(&s->gb, h->ref_count[list]);
                            fill_rectangle(&h->ref_cache[list][ scan8[0] + 16*i ], 4, 2, 8, val, 1);
                        }
                    }
                }
            }
            for(list=0; list<2; list++)
			{
                for(i=0; i<2; i++)
				{
                    if(IS_DIR(mb_type, i, list))
					{
                        pred_16x8_motion(h, 8*i, list, h->ref_cache[list][scan8[0] + 16*i], &mx, &my);
                        mx += get_se_golomb(&s->gb);
                        my += get_se_golomb(&s->gb);
						
                        fill_rectangle(h->mv_cache[list][ scan8[0] + 16*i ], 4, 2, 8, pack16to32(mx,my), 4);
                    }
                }
            }
        }
		else
		{
//            assert(IS_8X16(mb_type));
            for(list=0; list<2; list++)
			{
                if(h->ref_count[list]>0)
				{
                    for(i=0; i<2; i++)
					{
                        if(IS_DIR(mb_type, i, list))
						{ //FIXME optimize
                            const int val= get_te0_golomb(&s->gb, h->ref_count[list]);
                            fill_rectangle(&h->ref_cache[list][ scan8[0] + 2*i ], 2, 4, 8, val, 1);
                        }
                    }
                }
            }
            for(list=0; list<2; list++)
			{
                for(i=0; i<2; i++)
				{
                    if(IS_DIR(mb_type, i, list))
					{
                        pred_8x16_motion(h, i*4, list, h->ref_cache[list][ scan8[0] + 2*i ], &mx, &my);
                        mx += get_se_golomb(&s->gb);
                        my += get_se_golomb(&s->gb);
						
                        fill_rectangle(h->mv_cache[list][ scan8[0] + 2*i ], 2, 4, 8, pack16to32(mx,my), 4);
                    }
                }
            }
        }
    }
	
    if(IS_INTER(mb_type))
        write_back_motion(h, mb_type);
	
    if(!IS_INTRA16x16(mb_type))
	{
        cbp= get_ue_golomb(&s->gb);
        if(cbp > 47)
            return -1;
		
        if(IS_INTRA4x4(mb_type))
            cbp= golomb_to_intra4x4_cbp[cbp];
        else
            cbp= golomb_to_inter_cbp[cbp];
    }
	
    if(cbp || IS_INTRA16x16(mb_type))
	{
        int i8x8, i4x4, chroma_idx;
        int chroma_qp, dquant;
        GetBitContext *gb= IS_INTRA(mb_type) ? h->intra_gb_ptr : h->inter_gb_ptr;
        const uint8_t *scan, *dc_scan;
		
        if(IS_INTERLACED(mb_type))
		{
            scan= field_scan;
            dc_scan= luma_dc_field_scan;
        }
		else
		{
            scan= zigzag_scan;
            dc_scan= luma_dc_zigzag_scan;
        }
		
        dquant= get_se_golomb(&s->gb);
		
        if( dquant > 25 || dquant < -26 )
            return -1;
		
        s->qscale += dquant;
        if(((unsigned)s->qscale) > 51){
            if(s->qscale<0) s->qscale+= 52;
            else            s->qscale-= 52;
        }
		
        h->chroma_qp= chroma_qp= get_chroma_qp(h, s->qscale);
        if(IS_INTRA16x16(mb_type))
		{
            if( decode_residual(h, h->intra_gb_ptr, h->mb, LUMA_DC_BLOCK_INDEX, dc_scan, s->qscale, 16) < 0)
                return -1; 
			
//            assert((cbp&15) == 0 || (cbp&15) == 15);
			
            if(cbp&15)
			{
                for(i8x8=0; i8x8<4; i8x8++)
				{
                    for(i4x4=0; i4x4<4; i4x4++)
					{
                        const int index= i4x4 + 4*i8x8;
                        if( decode_residual(h, h->intra_gb_ptr, h->mb + 16*index, index, scan + 1, s->qscale, 15) < 0 )
                            return -1;
                    }
                }
            }
			else
			{
                fill_rectangle(&h->non_zero_count_cache[scan8[0]], 4, 4, 8, 0, 1);
            }
        }
		else
		{
            for(i8x8=0; i8x8<4; i8x8++)
			{
                if(cbp & (1<<i8x8))
				{
                    for(i4x4=0; i4x4<4; i4x4++)
					{
                        const int index= i4x4 + 4*i8x8;
						
                        if( decode_residual(h, gb, h->mb + 16*index, index, scan, s->qscale, 16) <0 )
                            return -1;
                    }
                }
				else
				{
                    uint8_t * const nnz= &h->non_zero_count_cache[ scan8[4*i8x8] ];
                    nnz[0] = nnz[1] = nnz[8] = nnz[9] = 0;
                }
            }
        }
		
        if(cbp&0x30)
		{
            for(chroma_idx=0; chroma_idx<2; chroma_idx++)
                if( decode_residual(h, gb, h->mb + 256 + 16*4*chroma_idx, CHROMA_DC_BLOCK_INDEX, chroma_dc_scan, chroma_qp, 4) < 0)
                    return -1;
        }
		
        if(cbp&0x20)
		{
            for(chroma_idx=0; chroma_idx<2; chroma_idx++)
			{
                for(i4x4=0; i4x4<4; i4x4++)
				{
                    const int index= 16 + 4*chroma_idx + i4x4;
                    if( decode_residual(h, gb, h->mb + 16*index, index, scan + 1, chroma_qp, 15) < 0)
                        return -1;
                }
            }
        }
		else
		{
            uint8_t * const nnz= &h->non_zero_count_cache[0];
            nnz[ scan8[16]+0 ] = nnz[ scan8[16]+1 ] =nnz[ scan8[16]+8 ] =nnz[ scan8[16]+9 ] =
			nnz[ scan8[20]+0 ] = nnz[ scan8[20]+1 ] =nnz[ scan8[20]+8 ] =nnz[ scan8[20]+9 ] = 0;
        }
    }
	else
	{
        uint8_t * const nnz= &h->non_zero_count_cache[0];
        fill_rectangle(&nnz[scan8[0]], 4, 4, 8, 0, 1);
        nnz[ scan8[16]+0 ] = nnz[ scan8[16]+1 ] =nnz[ scan8[16]+8 ] =nnz[ scan8[16]+9 ] =
		nnz[ scan8[20]+0 ] = nnz[ scan8[20]+1 ] =nnz[ scan8[20]+8 ] =nnz[ scan8[20]+9 ] = 0;
    }
    s->current_picture.qscale_table[mb_xy]= s->qscale;
    write_back_non_zero_count(h);
	
    return 0;
}

static int decode_cabac_mb_type( H264Context *h ) 
{
    MpegEncContext * const s = &h->s;
	
    if( h->slice_type == I_TYPE )
	{
        const int mb_xy= s->mb_x + s->mb_y*s->mb_stride;
        int ctx = 0;
        int mb_type;
		
        if( s->mb_x > 0 && !IS_INTRA4x4( s->current_picture.mb_type[mb_xy-1] ) )
            ctx++;
        if( s->mb_y > 0 && !IS_INTRA4x4( s->current_picture.mb_type[mb_xy-s->mb_stride] ) )
            ctx++;
		
        if( get_cabac( &h->cabac, &h->cabac_state[3+ctx] ) == 0 )
            return 0;   /* I4x4 */
		
        if( get_cabac_terminate( &h->cabac ) )
            return 25;  /* PCM */
		
        mb_type = 1;    /* I16x16 */
        if( get_cabac( &h->cabac, &h->cabac_state[3+3] ) )
            mb_type += 12;  /* cbp_luma != 0 */
		
        if( get_cabac( &h->cabac, &h->cabac_state[3+4] ) ) {
            if( get_cabac( &h->cabac, &h->cabac_state[3+5] ) )
                mb_type += 4 * 2;   /* cbp_chroma == 2 */
            else
                mb_type += 4 * 1;   /* cbp_chroma == 1 */
        }
        if( get_cabac( &h->cabac, &h->cabac_state[3+6] ) )
            mb_type += 2;
        if( get_cabac( &h->cabac, &h->cabac_state[3+7] ) )
            mb_type += 1;
        return mb_type;		
    }
	else if( h->slice_type == P_TYPE )
	{
        if( get_cabac( &h->cabac, &h->cabac_state[14] ) == 0 )
		{
            /* P-type */
            if( get_cabac( &h->cabac, &h->cabac_state[15] ) == 0 )
			{
                if( get_cabac( &h->cabac, &h->cabac_state[16] ) == 0 )
                    return 0; /* P_L0_D16x16; */
                else
                    return 3; /* P_8x8; */
            }
			else
			{
                if( get_cabac( &h->cabac, &h->cabac_state[17] ) == 0 )
                    return 2; /* P_L0_D8x16; */
                else
                    return 1; /* P_L0_D16x8; */
            }
        }
		else 
		{
            int mb_type;
            /* I-type */
            if( get_cabac( &h->cabac, &h->cabac_state[17] ) == 0 )
                return 5+0; /* I_4x4 */
            if( get_cabac_terminate( &h->cabac ) )
                return 5+25; /*I_PCM */
            mb_type = 5+1;    /* I16x16 */
            if( get_cabac( &h->cabac, &h->cabac_state[17+1] ) )
                mb_type += 12;  /* cbp_luma != 0 */
			
            if( get_cabac( &h->cabac, &h->cabac_state[17+2] ) )
			{
                if( get_cabac( &h->cabac, &h->cabac_state[17+2] ) )
                    mb_type += 4 * 2;   /* cbp_chroma == 2 */
                else
                    mb_type += 4 * 1;   /* cbp_chroma == 1 */
            }
            if( get_cabac( &h->cabac, &h->cabac_state[17+3] ) )
                mb_type += 2;
            if( get_cabac( &h->cabac, &h->cabac_state[17+3] ) )
                mb_type += 1;
			
            return mb_type;
        }
    }
	else 
	{
        return -1;
    }
}

static int decode_cabac_mb_skip( H264Context *h)
{
    MpegEncContext * const s = &h->s;
    const int mb_xy = s->mb_x + s->mb_y*s->mb_stride;
    const int mba_xy = mb_xy - 1;
    const int mbb_xy = mb_xy - s->mb_stride;
    int ctx = 0;
	
    if( s->mb_x > 0 && !IS_SKIP( s->current_picture.mb_type[mba_xy] ) )
        ctx++;
    if( s->mb_y > 0 && !IS_SKIP( s->current_picture.mb_type[mbb_xy] ) )
        ctx++;
	
    if( h->slice_type == P_TYPE || h->slice_type == SP_TYPE)
        return get_cabac( &h->cabac, &h->cabac_state[11+ctx] );
    else /* B-frame */
        return get_cabac( &h->cabac, &h->cabac_state[24+ctx] );
}

static int decode_cabac_mb_intra4x4_pred_mode( H264Context *h, int pred_mode )
{
    int mode = 0;
	
    if( get_cabac( &h->cabac, &h->cabac_state[68] ) )
        return pred_mode;
	
    if( get_cabac( &h->cabac, &h->cabac_state[69] ) )
        mode += 1;
    if( get_cabac( &h->cabac, &h->cabac_state[69] ) )
        mode += 2;
    if( get_cabac( &h->cabac, &h->cabac_state[69] ) )
        mode += 4;
    if( mode >= pred_mode )
        return mode + 1;
    else
        return mode;
}

static int decode_cabac_mb_chroma_pre_mode( H264Context *h)
{
    MpegEncContext * const s = &h->s;
    const int mb_xy = s->mb_x + s->mb_y*s->mb_stride;
    const int mba_xy = mb_xy - 1;
    const int mbb_xy = mb_xy - s->mb_stride;
	
    int ctx = 0;
	
    if( s->mb_x > 0 &&
        ( IS_INTRA4x4( s->current_picture.mb_type[mba_xy] ) || IS_INTRA16x16( s->current_picture.mb_type[mba_xy] ) ) &&
        h->chroma_pred_mode_table[mba_xy] != 0 ) {
        ctx++;
    }
    if( s->mb_y > 0 &&
        ( IS_INTRA4x4( s->current_picture.mb_type[mbb_xy] ) || IS_INTRA16x16( s->current_picture.mb_type[mbb_xy] ) ) &&
        h->chroma_pred_mode_table[mbb_xy] != 0 ) {
        ctx++;
    }
	
    if( get_cabac( &h->cabac, &h->cabac_state[64+ctx] ) == 0 )
        return 0;
	
    if( get_cabac( &h->cabac, &h->cabac_state[64+3] ) == 0 )
        return 1;
    if( get_cabac( &h->cabac, &h->cabac_state[64+3] ) == 0 )
        return 2;
    else
        return 3;
}

static const uint8_t block_idx_x[16] = {
    0, 1, 0, 1, 2, 3, 2, 3, 0, 1, 0, 1, 2, 3, 2, 3
};
static const uint8_t block_idx_y[16] = {
    0, 0, 1, 1, 0, 0, 1, 1, 2, 2, 3, 3, 2, 2, 3, 3
};
static const uint8_t block_idx_xy[4][4] = {
    { 0, 2, 8,  10},
    { 1, 3, 9,  11},
    { 4, 6, 12, 14},
    { 5, 7, 13, 15}
};

static int decode_cabac_mb_cbp_luma( H264Context *h)
{
    MpegEncContext * const s = &h->s;
    const int mb_xy = s->mb_x + s->mb_y*s->mb_stride;
	
    int cbp = 0;
    int i8x8;
	
    h->cbp_table[mb_xy] = 0;  /* FIXME aaahahahah beurk */
	
    for( i8x8 = 0; i8x8 < 4; i8x8++ )
	{
        int mba_xy = -1;
        int mbb_xy = -1;
        int x, y;
        int ctx = 0;
		
        x = block_idx_x[4*i8x8];
        y = block_idx_y[4*i8x8];
		
        if( x > 0 )
            mba_xy = mb_xy;
        else if( s->mb_x > 0 )
            mba_xy = mb_xy - 1;
		
        if( y > 0 )
            mbb_xy = mb_xy;
        else if( s->mb_y > 0 )
            mbb_xy = mb_xy - s->mb_stride;
		
        if( mba_xy >= 0 )
		{
            int i8x8a = block_idx_xy[(x-1)&0x03][y]/4;
            if( IS_SKIP( s->current_picture.mb_type[mba_xy] ) || ((h->cbp_table[mba_xy] >> i8x8a)&0x01) == 0 )
                ctx++;
        }
		
        if( mbb_xy >= 0 )
		{
            int i8x8b = block_idx_xy[x][(y-1)&0x03]/4;
            if( IS_SKIP( s->current_picture.mb_type[mbb_xy] ) || ((h->cbp_table[mbb_xy] >> i8x8b)&0x01) == 0 )
                ctx += 2;
        }
		
        if( get_cabac( &h->cabac, &h->cabac_state[73 + ctx] ) )
		{
            cbp |= 1 << i8x8;
            h->cbp_table[mb_xy] = cbp;  /* FIXME aaahahahah beurk */
        }
    }
    return cbp;
}

static int decode_cabac_mb_cbp_chroma( H264Context *h)
{
    MpegEncContext * const s = &h->s;
    const int mb_xy = s->mb_x + s->mb_y*s->mb_stride;
    int ctx;
    int cbp_a, cbp_b;
	
    if( s->mb_x > 0 && !IS_SKIP( s->current_picture.mb_type[mb_xy-1] ) )
        cbp_a = (h->cbp_table[mb_xy-1]>>4)&0x03;
    else
        cbp_a = -1;
	
    if( s->mb_y > 0 && !IS_SKIP( s->current_picture.mb_type[mb_xy-s->mb_stride] ) )
        cbp_b = (h->cbp_table[mb_xy-s->mb_stride]>>4)&0x03;
    else
        cbp_b = -1;
	
    ctx = 0;
    if( cbp_a > 0 ) ctx++;
    if( cbp_b > 0 ) ctx += 2;
    if( get_cabac( &h->cabac, &h->cabac_state[77 + ctx] ) == 0 )
        return 0;
	
    ctx = 4;
    if( cbp_a == 2 ) ctx++;
    if( cbp_b == 2 ) ctx += 2;
    if( get_cabac( &h->cabac, &h->cabac_state[77 + ctx] ) )
        return 2;
    else
        return 1;
}

static int decode_cabac_mb_dqp( H264Context *h) 
{
    MpegEncContext * const s = &h->s;
    int mbn_xy;
    int   ctx = 0;
    int   val = 0;
	
    if( s->mb_x > 0 )
        mbn_xy = s->mb_x + s->mb_y*s->mb_stride - 1;
    else
        mbn_xy = s->mb_width - 1 + (s->mb_y-1)*s->mb_stride;
	
    if( mbn_xy >= 0 && h->last_qscale_diff != 0 && ( IS_INTRA16x16(s->current_picture.mb_type[mbn_xy] ) || (h->cbp_table[mbn_xy]&0x3f) ) )
        ctx++;
	
    while( get_cabac( &h->cabac, &h->cabac_state[60 + ctx] ) )
	{
        if( ctx < 2 )
            ctx = 2;
        else
            ctx = 3;
        val++;
    }
	
    if( val&0x01 )
        return (val + 1)/2;
    else
        return -(val + 1)/2;
}

static int decode_cabac_mb_sub_type( H264Context *h )
{
    if( get_cabac( &h->cabac, &h->cabac_state[21] ) )
        return 0;   /* 8x8 */
    if( !get_cabac( &h->cabac, &h->cabac_state[22] ) )
        return 1;   /* 8x4 */
    if( get_cabac( &h->cabac, &h->cabac_state[23] ) )
        return 2;   /* 4x8 */
    return 3;       /* 4x4 */
}

static int decode_cabac_mb_ref( H264Context *h, int list, int n )
{
    int refa = h->ref_cache[list][scan8[n] - 1];
    int refb = h->ref_cache[list][scan8[n] - 8];
    int ref  = 0;
    int ctx  = 0;
	
    if( refa > 0 )
        ctx++;
    if( refb > 0 )
        ctx += 2;
	
    while( get_cabac( &h->cabac, &h->cabac_state[54+ctx] ) )
	{
        ref++;
        if( ctx < 4 )
            ctx = 4;
        else
            ctx = 5;
    }
    return ref;
}

static int decode_cabac_mb_mvd( H264Context *h, int list, int n, int l )
{
    int amvd = abs( h->mvd_cache[list][scan8[n] - 1][l] ) +
		abs( h->mvd_cache[list][scan8[n] - 8][l] );
    int ctxbase = (l == 0) ? 40 : 47;
    int ctx;
    int mvd = 0;
	
    if( amvd < 3 )
        ctx = 0;
    else if( amvd > 32 )
        ctx = 2;
    else
        ctx = 1;
	
    while( mvd < 9 && get_cabac( &h->cabac, &h->cabac_state[ctxbase+ctx] ) )
	{
        mvd++;
        if( ctx < 3 )
            ctx = 3;
        else if( ctx < 6 )
            ctx++;
    }
	
    if( mvd >= 9 ) 
	{
        int k = 3;
        while( get_cabac_bypass( &h->cabac ) )
		{
            mvd += 1 << k;
            k++;
        }
        while( k-- )
		{
            if( get_cabac_bypass( &h->cabac ) )
                mvd += 1 << k;
        }
    }
    if( mvd != 0 && get_cabac_bypass( &h->cabac ) )
        return -mvd;
    return mvd;
}

static int get_cabac_cbf_ctx( H264Context *h, int cat, int idx )
{
    MpegEncContext * const s = &h->s;
    const int mb_xy  = s->mb_x + s->mb_y*s->mb_stride;
    int mba_xy = -1;
    int mbb_xy = -1;
	
    int nza = -1;
    int nzb = -1;
    int ctx = 0;
	
    if( cat == 0 )
	{
        if( s->mb_x > 0 )
		{
            mba_xy = mb_xy - 1;
            if( IS_INTRA16x16(s->current_picture.mb_type[mba_xy] ) )
				nza = h->cbp_table[mba_xy]&0x100;
        }
        if( s->mb_y > 0 )
		{
            mbb_xy = mb_xy - s->mb_stride;
            if( IS_INTRA16x16(s->current_picture.mb_type[mbb_xy] ) )
				nzb = h->cbp_table[mbb_xy]&0x100;
        }
    } 
	else if( cat == 1 || cat == 2 )
	{
        int i8x8a, i8x8b;
        int x, y;
		
        x = block_idx_x[idx];
        y = block_idx_y[idx];
		
        if( x > 0 )
            mba_xy = mb_xy;
        else if( s->mb_x > 0 )
            mba_xy = mb_xy - 1;
		
        if( y > 0 )
            mbb_xy = mb_xy;
        else if( s->mb_y > 0 )
            mbb_xy = mb_xy - s->mb_stride;
		
        if( mba_xy >= 0 )
		{
            i8x8a = block_idx_xy[(x-1)&0x03][y]/4;
			
            if( !IS_SKIP(s->current_picture.mb_type[mba_xy] ) &&
                !IS_INTRA_PCM(s->current_picture.mb_type[mba_xy] ) &&
                ((h->cbp_table[mba_xy]&0x0f)>>i8x8a))
                nza = h->non_zero_count_cache[scan8[idx] - 1];
        }
		
        if( mbb_xy >= 0 )
		{
            i8x8b = block_idx_xy[x][(y-1)&0x03]/4;
			
            if( !IS_SKIP(s->current_picture.mb_type[mbb_xy] ) &&
                !IS_INTRA_PCM(s->current_picture.mb_type[mbb_xy] ) &&
                ((h->cbp_table[mbb_xy]&0x0f)>>i8x8b))
                nzb = h->non_zero_count_cache[scan8[idx] - 8];
        }
    }
	else if( cat == 3 )
	{
        if( s->mb_x > 0 )
		{
            mba_xy = mb_xy - 1;
			
            if( !IS_SKIP(s->current_picture.mb_type[mba_xy] ) &&
                !IS_INTRA_PCM(s->current_picture.mb_type[mba_xy] ) &&
                (h->cbp_table[mba_xy]&0x30) )
                nza = (h->cbp_table[mba_xy]>>(6+idx))&0x01;
        }
        if( s->mb_y > 0 )
		{
            mbb_xy = mb_xy - s->mb_stride;
			
            if( !IS_SKIP(s->current_picture.mb_type[mbb_xy] ) &&
                !IS_INTRA_PCM(s->current_picture.mb_type[mbb_xy] ) &&
                (h->cbp_table[mbb_xy]&0x30) )
                nzb = (h->cbp_table[mbb_xy]>>(6+idx))&0x01;
        }
    }
	else if( cat == 4 )
	{
        int idxc = idx % 4 ;
        if( idxc == 1 || idxc == 3 )
            mba_xy = mb_xy;
        else if( s->mb_x > 0 )
            mba_xy = mb_xy -1;
		
        if( idxc == 2 || idxc == 3 )
            mbb_xy = mb_xy;
        else if( s->mb_y > 0 )
            mbb_xy = mb_xy - s->mb_stride;
		
        if( mba_xy >= 0 &&
            !IS_SKIP(s->current_picture.mb_type[mba_xy] ) &&
            !IS_INTRA_PCM(s->current_picture.mb_type[mba_xy] ) &&
            (h->cbp_table[mba_xy]&0x30) == 0x20 )
            nza = h->non_zero_count_cache[scan8[16+idx] - 1];
		
        if( mbb_xy >= 0 &&
            !IS_SKIP(s->current_picture.mb_type[mbb_xy] ) &&
            !IS_INTRA_PCM(s->current_picture.mb_type[mbb_xy] ) &&
            (h->cbp_table[mbb_xy]&0x30) == 0x20 )
            nzb = h->non_zero_count_cache[scan8[16+idx] - 8];
    }
	
    if( ( mba_xy < 0 && IS_INTRA( s->current_picture.mb_type[mb_xy] ) ) ||
        ( mba_xy >= 0 && IS_INTRA_PCM(s->current_picture.mb_type[mba_xy] ) ) ||
		nza > 0 )
        ctx++;
	
    if( ( mbb_xy < 0 && IS_INTRA( s->current_picture.mb_type[mb_xy] ) ) ||
        ( mbb_xy >= 0 && IS_INTRA_PCM(s->current_picture.mb_type[mbb_xy] ) ) ||
		nzb > 0 )
        ctx += 2;
	
    return ctx + 4 * cat;
}

static int decode_cabac_residual( H264Context *h, DCTELEM *block, int cat, int n, const uint8_t *scantable, int qp, int max_coeff)
{
    const int mb_xy  = h->s.mb_x + h->s.mb_y*h->s.mb_stride;
    const uint16_t *qmul= dequant_coeff[qp];
    static const int significant_coeff_flag_offset[5] = { 0, 15, 29, 44, 47 };
    static const int last_significant_coeff_flag_offset[5] = { 0, 15, 29, 44, 47 };
    static const int coeff_abs_level_m1_offset[5] = { 0, 10, 20, 30, 39 };
	
    int coeff[16];
	
    int last = 0;
    int coeff_count = 0;
    int nz[16] = {0};
    int i;
	
    int abslevel1 = 0;
    int abslevelgt1 = 0;
	
    /* cat: 0-> DC 16x16  n = 0
	*      1-> AC 16x16  n = luma4x4idx
	*      2-> Luma4x4   n = luma4x4idx
	*      3-> DC Chroma n = iCbCr
	*      4-> AC Chroma n = 4 * iCbCr + chroma4x4idx
	*/
	
    /* read coded block flag */
    if( get_cabac( &h->cabac, &h->cabac_state[85 + get_cabac_cbf_ctx( h, cat, n ) ] ) == 0 ) 
	{
        if( cat == 1 || cat == 2 )
            h->non_zero_count_cache[scan8[n]] = 0;
        else if( cat == 4 )
            h->non_zero_count_cache[scan8[16+n]] = 0;
		
        return 0;
    }
	
    while( last < max_coeff - 1 ) 
	{
        int ctx = FFMIN( last, max_coeff - 2 );
		
        if( get_cabac( &h->cabac, &h->cabac_state[105+significant_coeff_flag_offset[cat]+ctx] ) == 0 ) 
		{
            nz[last++] = 0;
        }
        else
		{
            nz[last++] = 1;
            coeff_count++;
            if( get_cabac( &h->cabac, &h->cabac_state[166+last_significant_coeff_flag_offset[cat]+ctx] ) )
			{
                while( last < max_coeff ) 
				{
                    nz[last++] = 0;
                }
                break;
            }
        }
    }

    if( last == max_coeff -1 )
	{
        nz[last++] = 1;
        coeff_count++;
    }
	
    if( cat == 0 && coeff_count > 0 )
        h->cbp_table[mb_xy] |= 0x100;
    else if( cat == 1 || cat == 2 )
        h->non_zero_count_cache[scan8[n]] = coeff_count;
    else if( cat == 3 && coeff_count > 0 )
        h->cbp_table[mb_xy] |= 0x40 << n;
    else if( cat == 4 )
        h->non_zero_count_cache[scan8[16+n]] = coeff_count;
	
    for( i = coeff_count - 1; i >= 0; i-- )
	{
        int coeff_abs_m1;
		
        int ctx = (abslevelgt1 != 0 ? 0 : FFMIN( 4, abslevel1 + 1 )) + coeff_abs_level_m1_offset[cat];
		
        if( get_cabac( &h->cabac, &h->cabac_state[227+ctx] ) == 0 )
		{
            coeff_abs_m1 = 0;
        }
		else
		{
            coeff_abs_m1 = 1;
            ctx = 5 + FFMIN( 4, abslevelgt1 ) + coeff_abs_level_m1_offset[cat];
            while( coeff_abs_m1 < 14 && get_cabac( &h->cabac, &h->cabac_state[227+ctx] ) ) 
			{
                coeff_abs_m1++;
            }
        }
		
        if( coeff_abs_m1 >= 14 )
		{
            int j = 0;
            while( get_cabac_bypass( &h->cabac ) )
			{
                coeff_abs_m1 += 1 << j;
                j++;
            }
			
            while( j-- )
			{
                if( get_cabac_bypass( &h->cabac ) )
                    coeff_abs_m1 += 1 << j ;
            }
        }
        if( get_cabac_bypass( &h->cabac ) )
            coeff[i] = -1 *( coeff_abs_m1 + 1 );
        else
            coeff[i] = coeff_abs_m1 + 1;
		
        if( coeff_abs_m1 == 0 )
            abslevel1++;
        else
            abslevelgt1++;
    }
	
    if( cat == 0 || cat == 3 ) 
	{ /* DC */
        int j;
        for( i = 0, j = 0; j < coeff_count; i++ ) 
		{
            if( nz[i] )
			{
                block[scantable[i]] = coeff[j];				
                j++;
            }
        }		
    }
	else 
	{ /* AC */
        int j;
        for( i = 0, j = 0; j < coeff_count; i++ )
		{
            if( nz[i] ) 
			{
                block[scantable[i]] = coeff[j] * qmul[scantable[i]];				
                j++;
            }
        }
    }
    return 0;
}

static int decode_mb_cabac(H264Context *h)
{
    MpegEncContext * const s = &h->s;
    const int mb_xy= s->mb_x + s->mb_y*s->mb_stride;
    int mb_type, partition_count, cbp = 0;
	
    clear_blocks_c(h->mb); //FIXME avoid if allready clear (move after skip handlong?)
	
    if( h->slice_type == B_TYPE )
        return -1;

    if( h->sps.mb_aff )
        return -1;
	
    if( h->slice_type != I_TYPE && h->slice_type != SI_TYPE )
	{
        /* read skip flags */
        if( decode_cabac_mb_skip( h ) ) 
		{
            int mx, my;
			
            /* skip mb */
            mb_type= MB_TYPE_16x16|MB_TYPE_P0L0|MB_TYPE_P1L0|MB_TYPE_SKIP;
			
            memset(h->non_zero_count[mb_xy], 0, 16);
            memset(h->non_zero_count_cache + 8, 0, 8*5); //FIXME ugly, remove pfui
			
            fill_caches(h, mb_type); //FIXME check what is needed and what not ...
            pred_pskip_motion(h, &mx, &my);
            fill_rectangle(&h->ref_cache[0][scan8[0]], 4, 4, 8, 0, 1);
            fill_rectangle(  h->mvd_cache[0][scan8[0]], 4, 4, 8, pack16to32(0,0), 4);
            fill_rectangle(  h->mv_cache[0][scan8[0]], 4, 4, 8, pack16to32(mx,my), 4);
            write_back_motion(h, mb_type);
			
            s->current_picture.mb_type[mb_xy]= mb_type; //FIXME SKIP type
            s->current_picture.qscale_table[mb_xy]= s->qscale;
            h->slice_table[ mb_xy ]= h->slice_num;
            h->cbp_table[mb_xy] = 0;
            h->last_qscale_diff = 0;			
			
            return 0;			
        }
    }
	
    if( ( mb_type = decode_cabac_mb_type( h ) ) < 0 ) 
        return -1;
	
    if( h->slice_type == P_TYPE ) 
	{
        if( mb_type < 5)
		{
            partition_count= p_mb_type_info[mb_type].partition_count;
            mb_type=         p_mb_type_info[mb_type].type;
        }
		else 
		{
            mb_type -= 5;
            goto decode_intra_mb;
        }
    }
	else
	{
//		assert(h->slice_type == I_TYPE);
decode_intra_mb:
        partition_count = 0;
        cbp= i_mb_type_info[mb_type].cbp;
        h->intra16x16_pred_mode= i_mb_type_info[mb_type].pred_mode;
        mb_type= i_mb_type_info[mb_type].type;
    }
	
    s->current_picture.mb_type[mb_xy]= mb_type;
    h->slice_table[ mb_xy ]= h->slice_num;
	
    if(IS_INTRA_PCM(mb_type))
	{
        /* TODO */
        h->cbp_table[mb_xy] = 0xf +4*2;
        s->current_picture.qscale_table[mb_xy]= s->qscale;
        return -1;
    }
	
    fill_caches(h, mb_type);
	
    if( IS_INTRA( mb_type ) )
	{
        if( IS_INTRA4x4( mb_type ) ) 
		{
            int i;
            for( i = 0; i < 16; i++ )
			{
                int pred = pred_intra_mode( h, i );
                h->intra4x4_pred_mode_cache[ scan8[i] ] = decode_cabac_mb_intra4x4_pred_mode( h, pred );
				
                //av_log( s->avctx, AV_LOG_ERROR, "i4x4 pred=%d mode=%d\n", pred, h->intra4x4_pred_mode_cache[ scan8[i] ] );
            }
            write_back_intra_pred_mode(h);
            if( check_intra4x4_pred_mode(h) < 0 ) return -1;
        }
		else
		{
            h->intra16x16_pred_mode= check_intra_pred_mode( h, h->intra16x16_pred_mode );
            if( h->intra16x16_pred_mode < 0 ) return -1;
        }
        h->chroma_pred_mode_table[mb_xy] =
        h->chroma_pred_mode              = decode_cabac_mb_chroma_pre_mode( h );
		
        h->chroma_pred_mode= check_intra_pred_mode( h, h->chroma_pred_mode );
        if( h->chroma_pred_mode < 0 ) return -1;
    } 
	else if( partition_count == 4 ) 
	{
        int i, j, sub_partition_count[4], list, ref[2][4];
		
        /* Only P-frame */
        for( i = 0; i < 4; i++ )
		{
            h->sub_mb_type[i] = decode_cabac_mb_sub_type( h );
            sub_partition_count[i]= p_sub_mb_type_info[ h->sub_mb_type[i] ].partition_count;
            h->sub_mb_type[i]=      p_sub_mb_type_info[ h->sub_mb_type[i] ].type;
        }
		
        for( list = 0; list < 2; list++ ) 
		{
            if( h->ref_count[list] > 0 ) 
			{
                for( i = 0; i < 4; i++ ) 
				{
                    if(IS_DIR(h->sub_mb_type[i], 0, list) && !IS_DIRECT(h->sub_mb_type[i]))
					{
                        if( h->ref_count[list] > 1 )
                            ref[list][i] = decode_cabac_mb_ref( h, list, 4*i );
                        else
                            ref[list][i] = 0;
                    }
					else 
					{
                        ref[list][i] = -1;
                    }
                    h->ref_cache[list][ scan8[4*i]   ]=h->ref_cache[list][ scan8[4*i]+1 ]=
						h->ref_cache[list][ scan8[4*i]+8 ]=h->ref_cache[list][ scan8[4*i]+9 ]= ref[list][i];
                }
            }
        }
		
        for(list=0; list<2; list++)
		{			
            for(i=0; i<4; i++)
			{
                if(IS_DIR(h->sub_mb_type[i], 0, list) && !IS_DIRECT(h->sub_mb_type[i]))
				{
                    const int sub_mb_type= h->sub_mb_type[i];
                    const int block_width= (sub_mb_type & (MB_TYPE_16x16|MB_TYPE_16x8)) ? 2 : 1;
                    for(j=0; j<sub_partition_count[i]; j++)
					{
                        int mpx, mpy;
                        int mx, my;
                        const int index= 4*i + block_width*j;
                        int16_t (* mv_cache)[2]= &h->mv_cache[list][ scan8[index] ];
                        int16_t (* mvd_cache)[2]= &h->mvd_cache[list][ scan8[index] ];
                        pred_motion(h, index, block_width, list, h->ref_cache[list][ scan8[index] ], &mpx, &mpy);
						
                        mx = mpx + decode_cabac_mb_mvd( h, list, index, 0 );
                        my = mpy + decode_cabac_mb_mvd( h, list, index, 1 );
						
                        if(IS_SUB_8X8(sub_mb_type))
						{
                            mv_cache[ 0 ][0]= mv_cache[ 1 ][0]=
							mv_cache[ 8 ][0]= mv_cache[ 9 ][0]= mx;
                            mv_cache[ 0 ][1]= mv_cache[ 1 ][1]=
							mv_cache[ 8 ][1]= mv_cache[ 9 ][1]= my;
						
                            mvd_cache[ 0 ][0]= mvd_cache[ 1 ][0]=
							mvd_cache[ 8 ][0]= mvd_cache[ 9 ][0]= mx - mpx;
                            mvd_cache[ 0 ][1]= mvd_cache[ 1 ][1]=
							mvd_cache[ 8 ][1]= mvd_cache[ 9 ][1]= my - mpy;
                        }
						else if(IS_SUB_8X4(sub_mb_type))
						{
                            mv_cache[ 0 ][0]= mv_cache[ 1 ][0]= mx;
                            mv_cache[ 0 ][1]= mv_cache[ 1 ][1]= my;
							
                            mvd_cache[ 0 ][0]= mvd_cache[ 1 ][0]= mx- mpx;
                            mvd_cache[ 0 ][1]= mvd_cache[ 1 ][1]= my - mpy;
                        }
						else if(IS_SUB_4X8(sub_mb_type))
						{
                            mv_cache[ 0 ][0]= mv_cache[ 8 ][0]= mx;
                            mv_cache[ 0 ][1]= mv_cache[ 8 ][1]= my;
							
                            mvd_cache[ 0 ][0]= mvd_cache[ 8 ][0]= mx - mpx;
                            mvd_cache[ 0 ][1]= mvd_cache[ 8 ][1]= my - mpy;
                        }
						else
						{
//                            assert(IS_SUB_4X4(sub_mb_type));
                            mv_cache[ 0 ][0]= mx;
                            mv_cache[ 0 ][1]= my;
							
                            mvd_cache[ 0 ][0]= mx - mpx;
                            mvd_cache[ 0 ][1]= my - mpy;
                        }
                    }                
				}
				else
				{
                    uint32_t *p= (uint32_t *)&h->mv_cache[list][ scan8[4*i] ][0];
                    uint32_t *pd= (uint32_t *)&h->mvd_cache[list][ scan8[4*i] ][0];
                    p[0] = p[1] = p[8] = p[9] = 0;
                    pd[0]= pd[1]= pd[8]= pd[9]= 0;
                }
            }
        }
    }
	else if( !IS_DIRECT(mb_type) )
	{
        int list, mx, my, i, mpx, mpy;
        if(IS_16X16(mb_type))
		{
            for(list=0; list<2; list++)
			{
                if(IS_DIR(mb_type, 0, list))
				{
                    if(h->ref_count[list] > 0 )
					{
                        const int ref = h->ref_count[list] > 1 ? decode_cabac_mb_ref( h, list, 0 ) : 0;
                        fill_rectangle(&h->ref_cache[list][ scan8[0] ], 4, 4, 8, ref, 1);
                    }
                }
            }

            for(list=0; list<2; list++)
			{
                if(IS_DIR(mb_type, 0, list))
				{
                    pred_motion(h, 0, 4, list, h->ref_cache[list][ scan8[0] ], &mpx, &mpy);
					
                    mx = mpx + decode_cabac_mb_mvd( h, list, 0, 0 );
                    my = mpy + decode_cabac_mb_mvd( h, list, 0, 1 );
					
                    fill_rectangle(h->mvd_cache[list][ scan8[0] ], 4, 4, 8, pack16to32(mx-mpx,my-mpy), 4);
                    fill_rectangle(h->mv_cache[list][ scan8[0] ], 4, 4, 8, pack16to32(mx,my), 4);
                }
            }
        }
        else if(IS_16X8(mb_type))
		{
            for(list=0; list<2; list++)
			{
                if(h->ref_count[list]>0)
				{
                    for(i=0; i<2; i++)
					{
                        if(IS_DIR(mb_type, i, list))
						{
                            const int ref= h->ref_count[list] > 1 ? decode_cabac_mb_ref( h, list, 8*i ) : 0;
                            fill_rectangle(&h->ref_cache[list][ scan8[0] + 16*i ], 4, 2, 8, ref, 1);
                        }
                    }
                }
            }
            for(list=0; list<2; list++)
			{
                for(i=0; i<2; i++)
				{
                    if(IS_DIR(mb_type, i, list))
					{
                        pred_16x8_motion(h, 8*i, list, h->ref_cache[list][scan8[0] + 16*i], &mpx, &mpy);
                        mx = mpx + decode_cabac_mb_mvd( h, list, 8*i, 0 );
                        my = mpy + decode_cabac_mb_mvd( h, list, 8*i, 1 );
						
                        fill_rectangle(h->mvd_cache[list][ scan8[0] + 16*i ], 4, 2, 8, pack16to32(mx-mpx,my-mpy), 4);
                        fill_rectangle(h->mv_cache[list][ scan8[0] + 16*i ], 4, 2, 8, pack16to32(mx,my), 4);
                    }
                }
            }
        }
		else
		{
//            assert(IS_8X16(mb_type));
            for(list=0; list<2; list++)
			{
                if(h->ref_count[list]>0)
				{
                    for(i=0; i<2; i++)
					{
                        if(IS_DIR(mb_type, i, list))
						{ //FIXME optimize
                            const int ref= h->ref_count[list] > 1 ? decode_cabac_mb_ref( h, list, 4*i ) : 0;
                            fill_rectangle(&h->ref_cache[list][ scan8[0] + 2*i ], 2, 4, 8, ref, 1);
                        }
                    }
                }
            }
            for(list=0; list<2; list++)
			{
                for(i=0; i<2; i++)
				{
                    if(IS_DIR(mb_type, i, list))
					{
                        pred_8x16_motion(h, i*4, list, h->ref_cache[list][ scan8[0] + 2*i ], &mpx, &mpy);
                        mx = mpx + decode_cabac_mb_mvd( h, list, 4*i, 0 );
                        my = mpy + decode_cabac_mb_mvd( h, list, 4*i, 1 );
						
                        fill_rectangle(h->mvd_cache[list][ scan8[0] + 2*i ], 2, 4, 8, pack16to32(mx-mpx,my-mpy), 4);
                        fill_rectangle(h->mv_cache[list][ scan8[0] + 2*i ], 2, 4, 8, pack16to32(mx,my), 4);
                    }
                }
            }
        }
    }
	
	if( IS_INTER( mb_type ) )
        write_back_motion( h, mb_type );
	
    if( !IS_INTRA16x16( mb_type ) )
	{
        cbp  = decode_cabac_mb_cbp_luma( h );
        cbp |= decode_cabac_mb_cbp_chroma( h ) << 4;
    }
	
    h->cbp_table[mb_xy] = cbp;
	
    if( cbp || IS_INTRA16x16( mb_type ) )
	{
        const uint8_t *scan, *dc_scan;
        int dqp;
		
        if(IS_INTERLACED(mb_type))
		{
            scan= field_scan;
            dc_scan= luma_dc_field_scan;
        }
		else
		{
            scan= zigzag_scan;
            dc_scan= luma_dc_zigzag_scan;
        }
		
        h->last_qscale_diff = dqp = decode_cabac_mb_dqp( h );
        s->qscale += dqp;
        if(((unsigned)s->qscale) > 51){
            if(s->qscale<0) s->qscale+= 52;
            else            s->qscale-= 52;
        }
        h->chroma_qp = get_chroma_qp(h, s->qscale);
		
        if( IS_INTRA16x16( mb_type ) )
		{
            int i;

            if( decode_cabac_residual( h, h->mb, 0, 0, dc_scan, s->qscale, 16) < 0)
                return -1;
            if( cbp&15 )
			{
                for( i = 0; i < 16; i++ )
				{
                     if( decode_cabac_residual(h, h->mb + 16*i, 1, i, scan + 1, s->qscale, 15) < 0 )
                        return -1;
                }
            } 
			else 
			{
                fill_rectangle(&h->non_zero_count_cache[scan8[0]], 4, 4, 8, 0, 1);
            }
        }
		else 
		{
            int i8x8, i4x4;
            for( i8x8 = 0; i8x8 < 4; i8x8++ )
			{
                if( cbp & (1<<i8x8) ) 
				{
                    for( i4x4 = 0; i4x4 < 4; i4x4++ )
					{
                        const int index = 4*i8x8 + i4x4;

                        if( decode_cabac_residual(h, h->mb + 16*index, 2, index, scan, s->qscale, 16) < 0 )
                            return -1;
                    }
                }
				else 
				{
                    uint8_t * const nnz= &h->non_zero_count_cache[ scan8[4*i8x8] ];
                    nnz[0] = nnz[1] = nnz[8] = nnz[9] = 0;
                }
            }
        }
		
        if( cbp&0x30 )
		{
            int c;
            for( c = 0; c < 2; c++ )
			{
                if( decode_cabac_residual(h, h->mb + 256 + 16*4*c, 3, c, chroma_dc_scan, h->chroma_qp, 4) < 0)
                    return -1;
            }
        }
		
        if( cbp&0x20 )
		{
            int c, i;
            for( c = 0; c < 2; c++ )
			{
                for( i = 0; i < 4; i++ )
				{
                    const int index = 16 + 4 * c + i;

                    if( decode_cabac_residual(h, h->mb + 16*index, 4, index - 16, scan + 1, h->chroma_qp, 15) < 0)
                        return -1;
                }
            }
        }
		else 
		{
            uint8_t * const nnz= &h->non_zero_count_cache[0];
            nnz[ scan8[16]+0 ] = nnz[ scan8[16]+1 ] =nnz[ scan8[16]+8 ] =nnz[ scan8[16]+9 ] =
				nnz[ scan8[20]+0 ] = nnz[ scan8[20]+1 ] =nnz[ scan8[20]+8 ] =nnz[ scan8[20]+9 ] = 0;
        }
    }
	else 
	{
        memset( &h->non_zero_count_cache[8], 0, 8*5 );
    }
	
    s->current_picture.qscale_table[mb_xy]= s->qscale;
    write_back_non_zero_count(h);
	
    return 0;
}

static void filter_mb_edgev( H264Context *h, uint8_t *pix, int stride, int bS[4], int qp ) 
{
    int i, d;
    const int index_a = clip( qp + h->slice_alpha_c0_offset, 0, 51 );
    const int alpha = alpha_table[index_a];
    const int beta  = beta_table[clip( qp + h->slice_beta_offset, 0, 51 )];
	
    for( i = 0; i < 4; i++ ) 
	{
        if( bS[i] == 0 )
		{
            pix += 4 * stride;
            continue;
        }
		
        if( bS[i] < 4 )
		{
            const int tc0 = tc0_table[index_a][bS[i] - 1];

            for( d = 0; d < 4; d++ )
			{
                const int p0 = pix[-1];
                const int p1 = pix[-2];
                const int p2 = pix[-3];
                const int q0 = pix[0];
                const int q1 = pix[1];
                const int q2 = pix[2];
				
                if( ABS( p0 - q0 ) < alpha && ABS( p1 - p0 ) < beta && ABS( q1 - q0 ) < beta ) 
				{
                    int tc = tc0;
                    int i_delta;
					
                    if( ABS( p2 - p0 ) < beta )
					{
                        pix[-2] = p1 + clip( ( p2 + ( ( p0 + q0 + 1 ) >> 1 ) - ( p1 << 1 ) ) >> 1, -tc0, tc0 );
                        tc++;
                    }
                    if( ABS( q2 - q0 ) < beta ) 
					{
                        pix[1] = q1 + clip( ( q2 + ( ( p0 + q0 + 1 ) >> 1 ) - ( q1 << 1 ) ) >> 1, -tc0, tc0 );
                        tc++;
                    }
					
                    i_delta = clip( (((q0 - p0 ) << 2) + (p1 - q1) + 4) >> 3, -tc, tc );
                    pix[-1] = clip_uint8( p0 + i_delta );    /* p0' */
                    pix[0]  = clip_uint8( q0 - i_delta );    /* q0' */
                }
                pix += stride;
            }
        }
		else
		{
            /* 4px edge length */
            for( d = 0; d < 4; d++ ) 
			{
                const int p0 = pix[-1];
                const int p1 = pix[-2];
                const int p2 = pix[-3];
				
                const int q0 = pix[0];
                const int q1 = pix[1];
                const int q2 = pix[2];
				
                if( ABS( p0 - q0 ) < alpha && ABS( p1 - p0 ) < beta && ABS( q1 - q0 ) < beta ) 
				{					
                    if(ABS( p0 - q0 ) < (( alpha >> 2 ) + 2 ))
					{
                        if( ABS( p2 - p0 ) < beta)
                        {
                            const int p3 = pix[-4];
                            /* p0', p1', p2' */
                            pix[-1] = ( p2 + 2*p1 + 2*p0 + 2*q0 + q1 + 4 ) >> 3;
                            pix[-2] = ( p2 + p1 + p0 + q0 + 2 ) >> 2;
                            pix[-3] = ( 2*p3 + 3*p2 + p1 + p0 + q0 + 4 ) >> 3;
                        }
						else 
						{
                            /* p0' */
                            pix[-1] = ( 2*p1 + p0 + q1 + 2 ) >> 2;
                        }
                        if( ABS( q2 - q0 ) < beta)
                        {
                            const int q3 = pix[3];
                            /* q0', q1', q2' */
                            pix[0] = ( p1 + 2*p0 + 2*q0 + 2*q1 + q2 + 4 ) >> 3;
                            pix[1] = ( p0 + q0 + q1 + q2 + 2 ) >> 2;
                            pix[2] = ( 2*q3 + 3*q2 + q1 + q0 + p0 + 4 ) >> 3;
                        }
						else 
						{
                            /* q0' */
                            pix[0] = ( 2*q1 + q0 + p1 + 2 ) >> 2;
                        }
                    }
					else
					{
                        /* p0', q0' */
                        pix[-1] = ( 2*p1 + p0 + q1 + 2 ) >> 2;
                        pix[ 0] = ( 2*q1 + q0 + p1 + 2 ) >> 2;
                    }
                }
                pix += stride;
            }
        }
    }
}

static void filter_mb_edgecv( H264Context *h, uint8_t *pix, int stride, int bS[4], int qp )
{
    int i, d;
    const int index_a = clip( qp + h->slice_alpha_c0_offset, 0, 51 );
    const int alpha = alpha_table[index_a];
    const int beta  = beta_table[clip( qp + h->slice_beta_offset, 0, 51 )];
	
    for( i = 0; i < 4; i++ )
	{
        if( bS[i] == 0 )
		{
            pix += 2 * stride;
            continue;
        }
		
        if( bS[i] < 4 )
		{
            const int tc = tc0_table[index_a][bS[i] - 1] + 1;
            /* 2px edge length (because we use same bS than the one for luma) */
            for( d = 0; d < 2; d++ )
			{
                const int p0 = pix[-1];
                const int p1 = pix[-2];
                const int q0 = pix[0];
                const int q1 = pix[1];
				
                if( ABS( p0 - q0 ) < alpha && ABS( p1 - p0 ) < beta && ABS( q1 - q0 ) < beta ) 
				{
                    const int i_delta = clip( (((q0 - p0 ) << 2) + (p1 - q1) + 4) >> 3, -tc, tc );
					
                    pix[-1] = clip_uint8( p0 + i_delta );    /* p0' */
                    pix[0]  = clip_uint8( q0 - i_delta );    /* q0' */
                }
                pix += stride;
            }
        }
		else
		{
            /* 2px edge length (because we use same bS than the one for luma) */
            for( d = 0; d < 2; d++ )
			{
                const int p0 = pix[-1];
                const int p1 = pix[-2];
                const int q0 = pix[0];
                const int q1 = pix[1];
				
                if( ABS( p0 - q0 ) < alpha && ABS( p1 - p0 ) < beta && ABS( q1 - q0 ) < beta )
				{					
                    pix[-1] = ( 2*p1 + p0 + q1 + 2 ) >> 2;   /* p0' */
                    pix[0]  = ( 2*q1 + q0 + p1 + 2 ) >> 2;   /* q0' */
                }
                pix += stride;
            }
        }
    }
}

static void filter_mb_edgeh( H264Context *h, uint8_t *pix, int stride, int bS[4], int qp )
{
    int i, d;
    const int index_a = clip( qp + h->slice_alpha_c0_offset, 0, 51 );
    const int alpha = alpha_table[index_a];
    const int beta  = beta_table[clip( qp + h->slice_beta_offset, 0, 51 )];
    const int pix_next  = stride;
	
    for( i = 0; i < 4; i++ )
	{
        if( bS[i] == 0 )
		{
            pix += 4;
            continue;
        }
		
        if( bS[i] < 4 )
		{
            const int tc0 = tc0_table[index_a][bS[i] - 1];

            for( d = 0; d < 4; d++ )
			{
                const int p0 = pix[-1*pix_next];
                const int p1 = pix[-2*pix_next];
                const int p2 = pix[-3*pix_next];
                const int q0 = pix[0];
                const int q1 = pix[1*pix_next];
                const int q2 = pix[2*pix_next];
				
                if( ABS( p0 - q0 ) < alpha && ABS( p1 - p0 ) < beta && ABS( q1 - q0 ) < beta )
				{					
                    int tc = tc0;
                    int i_delta;
					
                    if( ABS( p2 - p0 ) < beta )
					{
                        pix[-2*pix_next] = p1 + clip( ( p2 + ( ( p0 + q0 + 1 ) >> 1 ) - ( p1 << 1 ) ) >> 1, -tc0, tc0 );
                        tc++;
                    }

                    if( ABS( q2 - q0 ) < beta )
					{
                        pix[pix_next] = q1 + clip( ( q2 + ( ( p0 + q0 + 1 ) >> 1 ) - ( q1 << 1 ) ) >> 1, -tc0, tc0 );
                        tc++;
                    }
					
                    i_delta = clip( (((q0 - p0 ) << 2) + (p1 - q1) + 4) >> 3, -tc, tc );
                    pix[-pix_next] = clip_uint8( p0 + i_delta );    /* p0' */
                    pix[0]         = clip_uint8( q0 - i_delta );    /* q0' */
                }
                pix++;
            }
        }
		else
		{
            /* 4px edge length */
            for( d = 0; d < 4; d++ ) 
			{
                const int p0 = pix[-1*pix_next];
                const int p1 = pix[-2*pix_next];
                const int p2 = pix[-3*pix_next];
                const int q0 = pix[0];
                const int q1 = pix[1*pix_next];
                const int q2 = pix[2*pix_next];
				
                if( ABS( p0 - q0 ) < alpha && ABS( p1 - p0 ) < beta && ABS( q1 - q0 ) < beta )
				{					
                    const int p3 = pix[-4*pix_next];
                    const int q3 = pix[ 3*pix_next];
					
                    if(ABS( p0 - q0 ) < (( alpha >> 2 ) + 2 ))
					{
                        if( ABS( p2 - p0 ) < beta) 
						{
                            /* p0', p1', p2' */
                            pix[-1*pix_next] = ( p2 + 2*p1 + 2*p0 + 2*q0 + q1 + 4 ) >> 3;
                            pix[-2*pix_next] = ( p2 + p1 + p0 + q0 + 2 ) >> 2;
                            pix[-3*pix_next] = ( 2*p3 + 3*p2 + p1 + p0 + q0 + 4 ) >> 3;
                        }
						else
						{
                            /* p0' */
                            pix[-1*pix_next] = ( 2*p1 + p0 + q1 + 2 ) >> 2;
                        }
                        if( ABS( q2 - q0 ) < beta) 
						{
                            /* q0', q1', q2' */
                            pix[0*pix_next] = ( p1 + 2*p0 + 2*q0 + 2*q1 + q2 + 4 ) >> 3;
                            pix[1*pix_next] = ( p0 + q0 + q1 + q2 + 2 ) >> 2;
                            pix[2*pix_next] = ( 2*q3 + 3*q2 + q1 + q0 + p0 + 4 ) >> 3;
                        } 
						else 
						{
                            /* q0' */
                            pix[0*pix_next] = ( 2*q1 + q0 + p1 + 2 ) >> 2;
                        }
                    }
					else
					{
                        /* p0', q0' */
                        pix[-1*pix_next] = ( 2*p1 + p0 + q1 + 2 ) >> 2;
                        pix[ 0*pix_next] = ( 2*q1 + q0 + p1 + 2 ) >> 2;
                    }
                }
                pix++;
            }
        }
    }
}

static void filter_mb_edgech( H264Context *h, uint8_t *pix, int stride, int bS[4], int qp )
{
    int i, d;
    const int index_a = clip( qp + h->slice_alpha_c0_offset, 0, 51 );
    const int alpha = alpha_table[index_a];
    const int beta  = beta_table[clip( qp + h->slice_beta_offset, 0, 51 )];
    const int pix_next  = stride;
	
    for( i = 0; i < 4; i++ )
    {
        if( bS[i] == 0 )
		{
            pix += 2;
            continue;
        }
		
        if( bS[i] < 4 ) 
		{
            int tc = tc0_table[index_a][bS[i] - 1] + 1;
            /* 2px edge length (see deblocking_filter_edgecv) */
            for( d = 0; d < 2; d++ )
			{
                const int p0 = pix[-1*pix_next];
                const int p1 = pix[-2*pix_next];
                const int q0 = pix[0];
                const int q1 = pix[1*pix_next];
				
                if( ABS( p0 - q0 ) < alpha && ABS( p1 - p0 ) < beta && ABS( q1 - q0 ) < beta )
				{					
                    int i_delta = clip( (((q0 - p0 ) << 2) + (p1 - q1) + 4) >> 3, -tc, tc );
					
                    pix[-pix_next] = clip_uint8( p0 + i_delta );    /* p0' */
                    pix[0]         = clip_uint8( q0 - i_delta );    /* q0' */
                }
                pix++;
            }
        }
		else
		{
            /* 2px edge length (see deblocking_filter_edgecv) */
            for( d = 0; d < 2; d++ )
			{
                const int p0 = pix[-1*pix_next];
                const int p1 = pix[-2*pix_next];
                const int q0 = pix[0];
                const int q1 = pix[1*pix_next];
				
                if( ABS( p0 - q0 ) < alpha && ABS( p1 - p0 ) < beta && ABS( q1 - q0 ) < beta )
				{					
                    pix[-pix_next] = ( 2*p1 + p0 + q1 + 2 ) >> 2;   /* p0' */
                    pix[0]         = ( 2*q1 + q0 + p1 + 2 ) >> 2;   /* q0' */
                }
                pix++;
            }
        }
    }
}

static void filter_mb( H264Context *h, int mb_x, int mb_y, uint8_t *img_y, uint8_t *img_cb, uint8_t *img_cr)
{
	MpegEncContext * const s = &h->s;
	const int mb_xy= mb_x + mb_y*s->mb_stride;
	int linesize, uvlinesize;
	int dir;
	
	if( h->sps.mb_aff )
		return;

	linesize = s->linesize;
	uvlinesize = s->uvlinesize;
	
	/* dir : 0 -> vertical edge, 1 -> horizontal edge */
	for( dir = 0; dir < 2; dir++ )
	{
		int start = 0;
		int edge;
		
		/* test picture boundary */
		if( ( dir == 0 && mb_x == 0 ) || ( dir == 1 && mb_y == 0 ) )
			start = 1;
		
		/* Calculate bS */
		for( edge = start; edge < 4; edge++ )
		{
			/* mbn_xy: neighbour macroblock (how that works for field ?) */
			int mbn_xy = edge > 0 ? mb_xy : ( dir == 0 ? mb_xy -1 : mb_xy - s->mb_stride );
			int bS[4];
			int qp;
			
			if( IS_INTRA( s->current_picture.mb_type[mb_xy] ) || IS_INTRA( s->current_picture.mb_type[mbn_xy] ) )
			{
				bS[0] = bS[1] = bS[2] = bS[3] = ( edge == 0 ? 4 : 3 );
			}
			else
			{
				int i;
				for( i = 0; i < 4; i++ )
				{
					int x = dir == 0 ? edge : i;
					int y = dir == 0 ? i    : edge;
					int b_idx= 8 + 4 + x + 8*y;
					int bn_idx= b_idx - (dir ? 8:1);
					
					if( h->non_zero_count_cache[b_idx] != 0 || h->non_zero_count_cache[bn_idx] != 0 )
					{
						bS[i] = 2;
					}
					else if( h->slice_type == P_TYPE )
					{
						if( h->ref_cache[0][b_idx] != h->ref_cache[0][bn_idx] ||
							ABS( h->mv_cache[0][b_idx][0] - h->mv_cache[0][bn_idx][0] ) >= 4 ||
							ABS( h->mv_cache[0][b_idx][1] - h->mv_cache[0][bn_idx][1] ) >= 4 )
						{
							bS[i] = 1;
						}
						else
						{
							bS[i] = 0;
						}
					}
					else
					{
						/* FIXME Add support for B frame */
						return;
					}
				}
				
				if(bS[0]+bS[1]+bS[2]+bS[3] == 0)
				{
					continue;
				}
			}
			
			/* Filter edge */
			qp = ( s->qscale + s->current_picture.qscale_table[mbn_xy] + 1 ) >> 1;
			if( dir == 0 )
			{
				filter_mb_edgev( h, &img_y[4*edge], linesize, bS, qp );
				if( (edge&1) == 0 )
				{
					int chroma_qp = ( h->chroma_qp + get_chroma_qp( h, s->current_picture.qscale_table[mbn_xy] ) + 1 ) >> 1;
					filter_mb_edgecv( h, &img_cb[2*edge], uvlinesize, bS, chroma_qp );
					filter_mb_edgecv( h, &img_cr[2*edge], uvlinesize, bS, chroma_qp );
				}
			}
			else
			{
				filter_mb_edgeh( h, &img_y[4*edge*linesize], linesize, bS, qp );
				if( (edge&1) == 0 )
				{
					int chroma_qp = ( h->chroma_qp + get_chroma_qp( h, s->current_picture.qscale_table[mbn_xy] ) + 1 ) >> 1;
					filter_mb_edgech( h, &img_cb[2*edge*uvlinesize], uvlinesize, bS, chroma_qp );
					filter_mb_edgech( h, &img_cr[2*edge*uvlinesize], uvlinesize, bS, chroma_qp );
				}
			}
		}
	}
}

static  void hl_decode_mb(H264Context *h)
{
    MpegEncContext * const s = &h->s;
    const int mb_x= s->mb_x;
    const int mb_y= s->mb_y;
    const int mb_xy= mb_x + mb_y*s->mb_stride;
    const int mb_type= s->current_picture.mb_type[mb_xy];
    uint8_t  *dest_y, *dest_cb, *dest_cr;
    int linesize, uvlinesize /*dct_offset*/;
    int i;
	
    dest_y  = s->current_picture.data[0] + (mb_y * 16* s->linesize  ) + mb_x * 16;
    dest_cb = s->current_picture.data[1] + (mb_y * 8 * s->uvlinesize) + mb_x * 8;
    dest_cr = s->current_picture.data[2] + (mb_y * 8 * s->uvlinesize) + mb_x * 8;
	
    if (h->mb_field_decoding_flag)
	{
        linesize = s->linesize * 2;
        uvlinesize = s->uvlinesize * 2;
        if(mb_y&1)
		{ //FIXME move out of this func?
            dest_y -= s->linesize*15;
            dest_cb-= s->linesize*7;
            dest_cr-= s->linesize*7;
        }
    }
	else
	{
        linesize = s->linesize;
        uvlinesize = s->uvlinesize;
		//        dct_offset = s->linesize * 16;
    }
	
    if(!IS_INTRA(mb_type))
	{
		hl_motion(h, dest_y, dest_cb, dest_cr,s->dsp.put_h264_qpel_pixels_tab, s->dsp.put_h264_chroma_pixels_tab, s->dsp.avg_h264_qpel_pixels_tab, s->dsp.avg_h264_chroma_pixels_tab);
    }
	else
	{
		if(h->deblocking_filter)
		{
            xchg_mb_border(h, dest_y, dest_cb, dest_cr, linesize, uvlinesize, 1);
		}
		
        h->pred8x8[ h->chroma_pred_mode ](dest_cb, uvlinesize);
        h->pred8x8[ h->chroma_pred_mode ](dest_cr, uvlinesize);
	
        if(IS_INTRA4x4(mb_type))
		{
            for(i=0; i<16; i++)
			{
                uint8_t * const ptr= dest_y + h->block_offset[i];
                uint8_t *topright= ptr + 4 - linesize;
                const int topright_avail= (h->topright_samples_available<<i)&0x8000;
                const int dir= h->intra4x4_pred_mode_cache[ scan8[i] ];
                int tr;
				
                if(!topright_avail)
				{
                    tr= ptr[3 - linesize]*0x01010101;
                    topright= (uint8_t*) &tr;
                }
				else if(i==5 && h->deblocking_filter)
				{
                    tr= *(uint32_t*)h->top_border[mb_x+1];
                    topright= (uint8_t*) &tr;
                }
				
                h->pred4x4[ dir ](ptr, topright, linesize);
                if(h->non_zero_count_cache[ scan8[i] ])
				{
					h264_add_idct_c(ptr, h->mb + i*16, linesize);
                }
            }
        }
		else
		{
            h->pred16x16[ h->intra16x16_pred_mode ](dest_y , linesize);
			h264_luma_dc_dequant_idct_c(h->mb, s->qscale);
        }
        if(h->deblocking_filter)
		{
            xchg_mb_border(h, dest_y, dest_cb, dest_cr, linesize, uvlinesize, 0);
		}
    }

	
    if(!IS_INTRA4x4(mb_type))
	{
		for(i=0; i<16; i++)
		{
			if(h->non_zero_count_cache[ scan8[i] ] || h->mb[i*16])
			{
				uint8_t * const ptr= dest_y + h->block_offset[i];
				h264_add_idct_c(ptr, h->mb + i*16, linesize);
			}
		}      
    }	
 
    chroma_dc_dequant_idct_c(h->mb + 16*16, h->chroma_qp);
    chroma_dc_dequant_idct_c(h->mb + 16*16+4*16, h->chroma_qp);

	for(i=16; i<16+4; i++)
	{
		if(h->non_zero_count_cache[ scan8[i] ] || h->mb[i*16])
		{
			uint8_t * const ptr= dest_cb + h->block_offset[i];
			h264_add_idct_c(ptr, h->mb + i*16, uvlinesize);
		}
	}
	for(i=20; i<20+4; i++)
	{
		if(h->non_zero_count_cache[ scan8[i] ] || h->mb[i*16])
		{
			uint8_t * const ptr= dest_cr + h->block_offset[i];
			h264_add_idct_c(ptr, h->mb + i*16, uvlinesize);
		}
	}   

    if(h->deblocking_filter)
	{
        backup_mb_border(h, dest_y, dest_cb, dest_cr, linesize, uvlinesize);
        filter_mb(h, mb_x, mb_y, dest_y, dest_cb, dest_cr);
    }	
}

static int decode_slice(H264Context *h)
{
	MpegEncContext * const s = &h->s;
	
	s->mb_skip_run= -1;
	
	if( h->pps.cabac )
	{
		int i;
		
		align_get_bits( &s->gb );		

		ff_init_cabac_states( &h->cabac, ff_h264_lps_range, ff_h264_mps_state, ff_h264_lps_state, 64 );
		ff_init_cabac_decoder( &h->cabac,(uint8_t*)s->gb.buffer + get_bits_count(&s->gb)/8,( s->gb.size_in_bits - get_bits_count(&s->gb) ) );

		for( i= 0; i < 399; i++ )
		{
			int pre;
			if( h->slice_type == I_TYPE )
			{
				pre = clip( ((cabac_context_init_I[i][0] * s->qscale) >>4 ) + cabac_context_init_I[i][1], 1, 126 );
			}
			else
			{
				pre = clip( ((cabac_context_init_PB[h->cabac_init_idc][i][0] * s->qscale) >>4 ) + cabac_context_init_PB[h->cabac_init_idc][i][1], 1, 126 );
			}
			
			if( pre <= 63 )
			{
				h->cabac_state[i] = 2 * ( 63 - pre ) + 0;
			}
			else
			{
				h->cabac_state[i] = 2 * ( pre - 64 ) + 1;
			}
		}
		
		for(;;)
		{
			int ret = decode_mb_cabac(h);
			int eos = get_cabac_terminate( &h->cabac ); /* End of Slice flag */
			
			hl_decode_mb(h);
			
			if( ret < 0 )
				return -1;
			
			if( ++s->mb_x >= s->mb_width )
			{
				s->mb_x = 0;
				++s->mb_y;
			}
			
			if( eos || s->mb_y >= s->mb_height )
				return 0;			
		}		
	}
	else
	{
		for(;;)
		{
			int ret = decode_mb_cavlc(h);
			
			hl_decode_mb(h);
			
			if(ret>=0 && h->sps.mb_aff)
			{ 
				s->mb_y++;
				ret = decode_mb_cavlc(h);
				
				hl_decode_mb(h);
				s->mb_y--;
			}
			
			if(ret<0)
				return -1;
			
			if(++s->mb_x >= s->mb_width)
			{
				s->mb_x=0;

				if(++s->mb_y >= s->mb_height)
				{
					if(get_bits_count(&s->gb) == s->gb.size_in_bits )
						return 0;
					else
						return -1;
				}
			}
			
			if(get_bits_count(&s->gb) >= s->gb.size_in_bits && s->mb_skip_run<=0)
			{
				if(get_bits_count(&s->gb) == s->gb.size_in_bits )
					return 0;
				else
					return -1;
			}
		}
	}
	
	return -1;
}

static int decode_vui_parameters(H264Context *h, SPS *sps)
{
	MpegEncContext * const s = &h->s;
	int aspect_ratio_info_present_flag, aspect_ratio_idc;
	
	aspect_ratio_info_present_flag= get_bits1(&s->gb);
	
	if( aspect_ratio_info_present_flag )
		aspect_ratio_idc= get_bits(&s->gb, 8);
	
	return 0;
}

static int decode_seq_parameter_set(H264Context *h)
{
	MpegEncContext * const s = &h->s;
	int profile_idc, level_idc;
	int sps_id, i;
	SPS *sps;
	
	profile_idc= get_bits(&s->gb, 8);
	get_bits1(&s->gb);   //constraint_set0_flag
	get_bits1(&s->gb);   //constraint_set1_flag
	get_bits1(&s->gb);   //constraint_set2_flag
	get_bits(&s->gb, 5); // reserved
	level_idc= get_bits(&s->gb, 8);
	sps_id= get_ue_golomb(&s->gb);
	
	sps= &h->sps_buffer[ sps_id ];
	sps->profile_idc= profile_idc;
	sps->level_idc= level_idc;
	
	sps->log2_max_frame_num= get_ue_golomb(&s->gb) + 4;
	sps->poc_type= get_ue_golomb(&s->gb);
	
	if(sps->poc_type == 0)
	{ //FIXME #define
		sps->log2_max_poc_lsb= get_ue_golomb(&s->gb) + 4;
	}
	else if(sps->poc_type == 1)
	{//FIXME #define
		sps->delta_pic_order_always_zero_flag= get_bits1(&s->gb);
		sps->offset_for_non_ref_pic= get_se_golomb(&s->gb);
		sps->offset_for_top_to_bottom_field= get_se_golomb(&s->gb);
		sps->poc_cycle_length= get_ue_golomb(&s->gb);
		
		for(i=0; i<sps->poc_cycle_length; i++)
		{
			sps->offset_for_ref_frame[i]= get_se_golomb(&s->gb);
		}
	}
	
	sps->ref_frame_count= get_ue_golomb(&s->gb);
	sps->gaps_in_frame_num_allowed_flag= get_bits1(&s->gb);
	sps->mb_width= get_ue_golomb(&s->gb) + 1;
	sps->mb_height= get_ue_golomb(&s->gb) + 1;
	sps->frame_mbs_only_flag= get_bits1(&s->gb);
	if(!sps->frame_mbs_only_flag)
	{
		sps->mb_aff= get_bits1(&s->gb);
	}
	else
	{
		sps->mb_aff= 0;
	}
	
	sps->direct_8x8_inference_flag= get_bits1(&s->gb);
	
	sps->crop= get_bits1(&s->gb);
	if(sps->crop)
	{
		sps->crop_left  = get_ue_golomb(&s->gb);
		sps->crop_right = get_ue_golomb(&s->gb);
		sps->crop_top   = get_ue_golomb(&s->gb);
		sps->crop_bottom= get_ue_golomb(&s->gb);		
	}
	else
	{
		sps->crop_left  = 0;
		sps->crop_right = 0;
		sps->crop_top   = 0;
		sps->crop_bottom= 0;
	}
	
	sps->vui_parameters_present_flag= get_bits1(&s->gb);
	if( sps->vui_parameters_present_flag )
		decode_vui_parameters(h, sps);
	
	return 0;
}

static int decode_picture_parameter_set(H264Context *h)
{
	MpegEncContext * const s = &h->s;
	int pps_id= get_ue_golomb(&s->gb);
	PPS *pps= &h->pps_buffer[pps_id];
	
	pps->sps_id= get_ue_golomb(&s->gb);
	pps->cabac= get_bits1(&s->gb);
	pps->pic_order_present= get_bits1(&s->gb);
	pps->slice_group_count= get_ue_golomb(&s->gb) + 1;

	if(pps->slice_group_count > 1 )
	{
		pps->mb_slice_group_map_type= get_ue_golomb(&s->gb);	
	}
	pps->ref_count[0]= get_ue_golomb(&s->gb) + 1;
	pps->ref_count[1]= get_ue_golomb(&s->gb) + 1;
	if(pps->ref_count[0] > 32 || pps->ref_count[1] > 32)
	{
		return -1;
	}
	
	pps->weighted_pred= get_bits1(&s->gb);
	pps->weighted_bipred_idc= get_bits(&s->gb, 2);
	pps->init_qp= get_se_golomb(&s->gb) + 26;
	pps->init_qs= get_se_golomb(&s->gb) + 26;
	pps->chroma_qp_index_offset= get_se_golomb(&s->gb);
	pps->deblocking_filter_parameters_present= get_bits1(&s->gb);
	pps->constrained_intra_pred= get_bits1(&s->gb);
	pps->redundant_pic_cnt_present = get_bits1(&s->gb);	
	
	return 0;
}

static int decode_nal_units(H264Context *h, uint8_t *buf, int buf_size)
{
	MpegEncContext * s = &h->s;
	int buf_index=0;
	
	for(;;)
	{
		int consumed;
		int dst_length;
		int bit_length;
		uint8_t *ptr;
		
		for(; buf_index + 3 < buf_size; buf_index++)
		{
			if(buf[buf_index] == 0 && buf[buf_index+1] == 0 && buf[buf_index+2] == 1)
				break;
		}
		
		if(buf_index+3 >= buf_size)
			break;
		
		buf_index+=3;
		
		ptr= decode_nal(h, buf + buf_index, &dst_length, &consumed, buf_size - buf_index);
		if(ptr[dst_length - 1] == 0)
			dst_length--;

		bit_length= 8*dst_length - decode_rbsp_trailing(ptr + dst_length - 1);
		
		buf_index += consumed;
		
		if(h->nal_ref_idc < s->hurry_up)
			continue;

		switch(h->nal_unit_type)
		{
		case NAL_IDR_SLICE:
			idr(h); //FIXME ensure we dont loose some frames if there is reordering
		case NAL_SLICE:
			init_get_bits(&s->gb, ptr, bit_length);
			h->intra_gb_ptr= h->inter_gb_ptr= &s->gb;
			s->data_partitioning = 0;
			
			if(decode_slice_header(h) < 0)
				return -1;

			if(h->redundant_pic_count==0)
				decode_slice(h);

			break;
		case NAL_DPA:
			init_get_bits(&s->gb, ptr, bit_length);
			h->intra_gb_ptr= h->inter_gb_ptr= NULL;
			s->data_partitioning = 1;
			
			if(decode_slice_header(h) < 0)
				return -1;

			break;
		case NAL_DPB:
			init_get_bits(&h->intra_gb, ptr, bit_length);
			h->intra_gb_ptr= &h->intra_gb;
			break;
		case NAL_DPC:
			init_get_bits(&h->inter_gb, ptr, bit_length);
			h->inter_gb_ptr= &h->inter_gb;
			
			if(h->redundant_pic_count==0 && h->intra_gb_ptr && s->data_partitioning)
				decode_slice(h);

			break;
		case NAL_SEI:
			break;
		case NAL_SPS:
			init_get_bits(&s->gb, ptr, bit_length);
			decode_seq_parameter_set(h);
			
			break;
		case NAL_PPS:
			init_get_bits(&s->gb, ptr, bit_length);
			
			decode_picture_parameter_set(h);
			
			break;
		case NAL_PICTURE_DELIMITER:
			break;
		case NAL_FILTER_DATA:
			break;
		default:
			break;
		}
		
		s->current_picture.pict_type= s->pict_type;
		s->current_picture.key_frame= s->pict_type == I_TYPE;
	}
	
	if(!s->current_picture_ptr)
		return buf_index; //no frame
	
	h->prev_frame_num_offset= h->frame_num_offset;
	h->prev_frame_num= h->frame_num;
	if(s->current_picture_ptr->reference)
	{
		h->prev_poc_msb= h->poc_msb;
		h->prev_poc_lsb= h->poc_lsb;
	}
	if(s->current_picture_ptr->reference)
	{
		execute_ref_pic_marking(h, h->mmco, h->mmco_index);
	}
	else
	{
//		assert(h->mmco_index==0);
	}
	
	MPV_frame_end(s);
	
	return buf_index;
}

static int get_consumed_bytes(MpegEncContext *s, int pos, int buf_size)
{
	if(pos==0)
		pos=1; //avoid infinite loops (i doubt thats needed but ...)

	if(pos+10>buf_size)
		pos=buf_size; // oops ;)
	
	return pos;
}

int decode_frame(AVCodecContext *avctx, void *data, int *data_size,uint8_t *buf, int buf_size)
{
	H264Context *h = avctx->priv_data;
	MpegEncContext *s = &h->s;
	AVFrame *pict = data;
	int buf_index;
	
	*data_size = 0;
	
	if (buf_size == 0)
		return 0;
	
	buf_index=decode_nal_units(h, buf, buf_size);
	if(buf_index < 0)
		return -1;
	
	if(!s->current_picture_ptr)
		return -1;
	
	*pict= *(AVFrame*)&s->current_picture;
//	assert(pict->data[0]);
	
	*data_size = sizeof(AVFrame);
	return get_consumed_bytes(s, buf_index, buf_size);
}

int decode_end(AVCodecContext *avctx)
{
	H264Context *h = avctx->priv_data;
	MpegEncContext *s = &h->s;
	
	free_tables(h);
	MPV_common_end(s);
	
	av_freep(&h->rbsp_buffer);
	decode_free_vlc(h);
	
	return 0;
}
