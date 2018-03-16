package com.optic.simpleblog.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by User on 24/01/2018.
 */

public class RandomName {


    public static String randomName() {
        // class variable
        final String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";

        final java.util.Random rand = new java.util.Random();

        // consider using a Map<String,Boolean> to say whether the identifier is being used or not
        final Set<String> identifiers = new HashSet<String>();

        StringBuilder builder = new StringBuilder();
        while(builder.toString().length() == 0) {
            int length = rand.nextInt(5)+15;
            for(int i = 0; i < length; i++) {
                builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
            }
            if(identifiers.contains(builder.toString())) {
                builder = new StringBuilder();
            }
        }
        return builder.toString();
    }

}
