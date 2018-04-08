package io.github.vladimirmi.localradio.utils;


import android.arch.core.util.Function;

import java.util.Arrays;

/**
 * Created by Vladimir Mikhalev 07.04.2018.
 */

public class StringUtils {

    public static class Builder {

        private String value;

        public Builder(String value) {
            this.value = value;
        }

        public Builder substringAfter(String delimiter, String ifMissingDelimiter) {
            value = StringUtils.substringAfter(value, delimiter, ifMissingDelimiter);
            return this;
        }

        public Builder substringAfter(String delimiter) {
            return substringAfter(delimiter, value);
        }

        public Builder substringBefore(String delimiter, String ifMissingDelimiter) {
            value = StringUtils.substringBefore(value, delimiter, ifMissingDelimiter);
            return this;
        }

        public Builder substringBefore(String delimiter) {
            return substringBefore(delimiter, value);
        }

        public Builder substringBeforeLast(String delimiter, String ifMissingDelimiter) {
            value = StringUtils.substringBeforeLast(value, delimiter, ifMissingDelimiter);
            return this;
        }

        public Builder substringBeforeLast(String delimiter) {
            return substringBeforeLast(delimiter, value);
        }

        public Builder trim(Character... chars) {
            value = StringUtils.trim(value, chars);
            return this;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static String substringAfter(String string, String delimiter, String ifMissingDelimiter) {
        int index = string.indexOf(delimiter);
        if (index == -1) {
            return ifMissingDelimiter;
        } else {
            return string.substring(index + delimiter.length(), string.length());
        }
    }

    public static String substringBefore(String string, String delimiter, String ifMissingDelimiter) {
        int index = string.indexOf(delimiter);
        if (index == -1) {
            return ifMissingDelimiter;
        } else {
            return string.substring(0, index);
        }
    }

    public static String substringBeforeLast(String string, String delimiter, String ifMissingDelimiter) {
        int index = string.lastIndexOf(delimiter);
        if (index == -1) {
            return ifMissingDelimiter;
        } else {
            return string.substring(0, index);
        }
    }

    public static String trim(String string, Character... chars) {
        return trim(string, input -> {
            for (Character aChar : chars) {
                if (aChar.equals(input)) {
                    return true;
                }
            }
            return false;
        });
    }

    public static String trim(String string, Function<Character, Boolean> predicate) {
        char[] chars = string.toCharArray();
        int start = 0;
        int end = chars.length - 1;
        boolean startFound = false;

        while (start <= end) {
            int index = startFound ? start : end;
            boolean match = predicate.apply(chars[index]);

            if (!startFound) {
                if (!match) {
                    startFound = true;
                } else {
                    start += 1;
                }
            } else {
                if (!match)
                    break;
                else
                    end -= 1;
            }
        }
        char[] dest = Arrays.copyOfRange(chars, start, end + 1);

        return new String(dest);
    }

}
