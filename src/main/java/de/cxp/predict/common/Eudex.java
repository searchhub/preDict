package de.cxp.predict.common;

// Eudex: A blazingly fast phonetic reduction/hashing algorithm
//
// Eudex is a Soundex-esque phonetic reduction/hashing algorithm, providing locality sensitive "hashes" of words, based on the spelling and pronunciation. 
// fEudex is about two orders of magnitude faster than Soundex, and several orders of magnitude faster than Levenshtein distance, 
// making it feasible to run on large sets of strings in very short time.
// Works with, but not limited to, English, Catalan, German, Spanish, Italian, and Swedish.
//


public class Eudex {

    private final static int LETTERS = 26;

    private final static long[] PHONES = new long[] {
            0, // a
            0b01001000, // b
            0b00001100, // c
            0b00011000, // d
            0, // e
            0b01000100, // f
            0b00001000, // g
            0b00000100, // h
            1, // i
            0b00000101, // j
            0b00001001, // k
            0b10100000, // l
            0b00000010, // m
            0b00010010, // n
            0, // o
            0b01001001, // p
            0b10101000, // q
            0b10100001, // r
            0b00010100, // s
            0b00011101, // t
            1, // u
            0b01000101, // v
            0b00000000, // w
            0b10000100, // x
            1, // y
            0b10010100 // z
    };

    private final static long[] PHONES_C1 = new long[] {
            PHONES['s'-'a'] ^ 1, // ß
            0, // à
            0, // á
            0, // â
            0, // ã
            0, // ä [æ]
            1, // å [oː]
            0, // æ [æ]
            PHONES['z' - 'a'] ^ 1, // ç [t͡ʃ]
            1, // è
            1, // é
            1, // ê
            1, // ë
            1, // ì
            1, // í
            1, // î
            1, // ï
            0b00010101, // ð [ð̠] (represented as a non-plosive T)
            0b00010111, // ñ [nj] (represented as a combination of n and j)
            0, // ò
            0, // ó
            0, // ô
            0, // õ
            1, // ö [ø]
            ~0, // ÷
            1, // ø [ø]
            1, // ù
            1, // ú
            1, // û
            1, // ü
            1, // ý
            0b00010101, // þ [ð̠] (represented as a non-plosive T)
            1, // ÿ
    };


    private final static long[] INJECTIVE_PHONES = new long[] {
            0b10000100, // a*
            0b00100100, // b
            0b00000110, // c
            0b00001100, // d
            0b11011000, // e*
            0b00100010, // f
            0b00000100, // g
            0b00000010, // h
            0b11111000, // i*
            0b00000011, // j
            0b00000101, // k
            0b01010000, // l
            0b00000001, // m
            0b00001001, // n
            0b10010100, // o*
            0b00100101, // p
            0b01010100, // q
            0b01010001, // r
            0b00001010, // s
            0b00001110, // t
            0b11100000, // u*
            0b00100011, // v
            0b00000000, // w
            0b01000010, // x
            0b11100100, // y*
            0b01001010 // z
    };

    private final static long[] INJECTIVE_PHONES_C1 = new long[] {
            INJECTIVE_PHONES['s' - 'a'] ^ 1, // ß
            INJECTIVE_PHONES[0] ^ 1, // à
            INJECTIVE_PHONES[0] ^ 1, // á
            0b10000000, // â
            0b10000110, // ã
            0b10100110, // ä [æ]
            0b11000010, // å [oː]
            0b10100111, // æ [æ]
            0b01010100, // ç [t͡ʃ]
            INJECTIVE_PHONES['e' - 'a'] ^ 1, // è
            INJECTIVE_PHONES['e' - 'a'] ^ 1, // é
            INJECTIVE_PHONES['e' - 'a'] ^ 1, // ê
            0b11000110, // ë [ə] or [œ]
            INJECTIVE_PHONES['i' - 'a'] ^ 1, // ì
            INJECTIVE_PHONES['i' - 'a'] ^ 1, // í
            INJECTIVE_PHONES['i' - 'a'] ^ 1, // î
            INJECTIVE_PHONES['i' - 'a'] ^ 1, // ï
            0b00001011, // ð [ð̠] (represented as a non-plosive T)
            0b00001011, // ñ [nj] (represented as a combination of n and j)
            INJECTIVE_PHONES['o' - 'a'] ^ 1, // ò
            INJECTIVE_PHONES['o' - 'a'] ^ 1, // ó
            INJECTIVE_PHONES['o' - 'a'] ^ 1, // ô
            INJECTIVE_PHONES['o' - 'a'] ^ 1, // õ
            0b11011100, // ö [œ] or [ø]
            ~0, // ÷
            0b11011101,// ø [œ] or [ø]
            INJECTIVE_PHONES['u' - 'a'] ^ 1, // ù
            INJECTIVE_PHONES['u' - 'a'] ^ 1, // ú
            INJECTIVE_PHONES['u' - 'a'] ^ 1, // û
            INJECTIVE_PHONES['y' - 'a'] ^ 1, // ü
            INJECTIVE_PHONES['y' - 'a'] ^ 1, // ý
            0b00001011, // þ [ð̠] (represented as a non-plosive T)
            INJECTIVE_PHONES['y' - 'a'] ^ 1 // ÿ
    };

    public static long encode(String str) {
        char[] ch = str.toCharArray();
        int entry = ch.length > 0 ? ((ch[0] | 32) - 'a') & 0xFF: 0;
        long first_byte = 0L;
        if (entry < LETTERS) {
            first_byte = INJECTIVE_PHONES[entry];
        } else if (entry >= 0xDF && entry < 0xFF) {
            first_byte = INJECTIVE_PHONES_C1[(entry - 0xDF)];
        }
        long res = 0L;
        int n = 1;
        int b = 1;
        while (n != 0 && b < ch.length) {
            entry = ((ch[b] | 32) - 'a') & 0xFF;
            if (entry <= 'z') {
                Long x;
                if (entry < LETTERS) {
                    x = PHONES[entry];
                } else if (entry >= 0xDF && entry < 0xFF) {
                    x = PHONES_C1[entry - 0xDF];
                } else {
                    b++;
                    continue;
                }
                if ((res & 0xFE) != (x & 0xFE)) {
                    res = res << 8;
                    res = res | x;
                    n = n << 1;
                }
            }
            b++;
        }
        return res | (first_byte << 56L);
    }

    public static long distance(String a, String b) {
        return distance(encode(a), encode(b));
    }

    public static long distance(long a, long b) {
        long dist = a ^ b;
        return Long.bitCount(dist & 0xFF) +
                Long.bitCount((dist >> 8) & 0xFF) * 2 +
                Long.bitCount((dist >> 16) & 0xFF) * 4 +
                Long.bitCount((dist >> 24) & 0xFF) * 8 +
                Long.bitCount((dist >> 32) & 0xFF) * 16 +
                Long.bitCount((dist >> 40) & 0xFF) * 32 +
                Long.bitCount((dist >> 48) & 0xFF) * 64 +
                Long.bitCount((dist >> 56) & 0xFF) * 128;
    }

    public static boolean similar(String a, String b) {
        return distance(a, b) < 10;
    }
}
