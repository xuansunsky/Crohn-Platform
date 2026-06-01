package com.xuan.croprogram.util;

import java.util.List;

public final class CityNormalizer {

    private static final List<String> KNOWN_CITIES = List.of(
            "北京", "上海", "广州", "深圳", "杭州", "成都", "重庆", "武汉",
            "南京", "西安", "长沙", "天津", "苏州", "郑州", "福州", "厦门",
            "青岛", "大连", "昆明", "合肥", "济南", "内江", "宁波", "无锡"
    );

    private CityNormalizer() {
    }

    public static String normalize(String city) {
        if (city == null) {
            return null;
        }

        String cleaned = city.replaceAll("\\s+", "").trim();
        if (cleaned.isEmpty()) {
            return "";
        }

        for (String knownCity : KNOWN_CITIES) {
            if (cleaned.equals(knownCity) || cleaned.equals(knownCity + "市") || cleaned.contains(knownCity + "市")) {
                return knownCity;
            }
        }

        if (cleaned.endsWith("市") && cleaned.length() > 1) {
            return cleaned.substring(0, cleaned.length() - 1);
        }

        return cleaned;
    }
}
