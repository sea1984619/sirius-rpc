/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sirius.common.util;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    /**
     * 换算法？ MD5  SHA-1 MurMurHash???
     *
     * @param value the value
     * @return the byte []
     */
    public static byte[] messageDigest(String value) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(value.getBytes("UTF-8"));
            return md5.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No such algorithm named md5", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding of" + value, e);
        }
    }

    /**
     * Hash long.
     *
     * @param digest the digest
     * @param index  the number
     * @return the long
     */
    public static long hash(byte[] digest, int index) {
        long f = ((long) (digest[3 + index * 4] & 0xFF) << 24)
            | ((long) (digest[2 + index * 4] & 0xFF) << 16)
            | ((long) (digest[1 + index * 4] & 0xFF) << 8)
            | (digest[index * 4] & 0xFF);
        return f & 0xFFFFFFFFL;
    }

}