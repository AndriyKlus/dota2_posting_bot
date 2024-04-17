package com.andriyklus.dota2.util;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FlagConverterUtil {

    private static Map<String, String> countryFlag;

    @PostConstruct
    private void init() {
        countryFlag = new HashMap<>();
        countryFlag.put("Philippines", "\uD83C\uDDF5\uD83C\uDDED");
        countryFlag.put("Malaysia", "\uD83C\uDDF2\uD83C\uDDFE");
        countryFlag.put("Peru", "\uD83C\uDDF5\uD83C\uDDEA");
        countryFlag.put("Bolivia", "\uD83C\uDDE7\uD83C\uDDF4");
        countryFlag.put("Mongolia", "\uD83C\uDDF2\uD83C\uDDF3");
        countryFlag.put("Kyrgyzstan", "\uD83C\uDDF0\uD83C\uDDEC");
        countryFlag.put("Brazil", "\uD83C\uDDE7\uD83C\uDDF7");
        countryFlag.put("Uruguay", "\uD83C\uDDFA\uD83C\uDDFE");
        countryFlag.put("China", "\uD83C\uDDE8\uD83C\uDDF3");
        countryFlag.put("Netherlands", "\uD83C\uDDF3\uD83C\uDDF1");
        countryFlag.put("United Kingdom", "\uD83C\uDDEC\uD83C\uDDE7");
        countryFlag.put("Sweden", "\uD83C\uDDF8\uD83C\uDDEA");
        countryFlag.put("Myanmar", "\uD83C\uDDF2\uD83C\uDDF2");
        countryFlag.put("North Macedonia", "\uD83C\uDDF2\uD83C\uDDF0");
        countryFlag.put("South Korea", "\uD83C\uDDF0\uD83C\uDDF7");
        countryFlag.put("Poland", "\uD83C\uDDF5\uD83C\uDDF1");
        countryFlag.put("Germany", "\uD83C\uDDE9\uD83C\uDDEA");
        countryFlag.put("Spain", "\uD83C\uDDEA\uD83C\uDDF8");
        countryFlag.put("Russia", "\uD83C\uDDF7\uD83C\uDDFA");
        countryFlag.put("Australia", "\uD83C\uDDE6\uD83C\uDDFA");
        countryFlag.put("Norway", "\uD83C\uDDF3\uD83C\uDDF4");
        countryFlag.put("United States", "\uD83C\uDDFA\uD83C\uDDF8");
        countryFlag.put("Cambodia", "\uD83C\uDDF0\uD83C\uDDED");
        countryFlag.put("Hungary", "\uD83C\uDDED\uD83C\uDDFA");
        countryFlag.put("Romania", "\uD83C\uDDF7\uD83C\uDDF4");
        countryFlag.put("Ukraine", "\uD83C\uDDFA\uD83C\uDDE6");
        countryFlag.put("Argentina", "\uD83C\uDDE6\uD83C\uDDF7");
        countryFlag.put("Belarus", "\uD83C\uDDE7\uD83C\uDDFE");
        countryFlag.put("Egypt", "\uD83C\uDDEA\uD83C\uDDEC");
        countryFlag.put("Indonesia", "\uD83C\uDDEE\uD83C\uDDE9");
    }

    public static String getFlag(String country) {
        return countryFlag.get(country);
    }

}
