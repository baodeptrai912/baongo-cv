package com.example.baoNgoCv.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LocationType {
    HANOI("Hanoi"),
    HO_CHI_MINH_CITY("Ho Chi Minh City"),
    DA_NANG("Da Nang"),
    CAN_THO("Can Tho"),
    HAI_PHONG("Hai Phong"),
    BIEN_HOA("Bien Hoa"),
    NHA_TRANG("Nha Trang"),
    HUE("Hue"),
    VUNG_TAU("Vung Tau"),
    REMOTE("Remote"),
    HYBRID("Hybrid"),
    ON_SITE("On-site"),
    OTHER_PROVINCES("Other Provinces");

    private final String displayName;
}
