package gloria.users.busUser;



import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum busHighFrequencyEventEnum {
    QOS(959),
    XUO(794),
    HRS(691),
    EJG(690),
    KGX(681),
    YMF(678),
    ZSX(673),
    UZM(665),
    UXD(662),
    ZHJ(650),
    JCN(646),
    EQR(642),
    NMT(595),
    EIS(592),
    WZS(589),
    MCF(589),
    PSE(580),
    JVF(577),
    HCK(575),
    EGX(573),
    SLJ(566),
    ULO(562),
    OYU(558),
    XPG(557),
    JSU(556),
    XOS(551),
    SNK(544),
    ZTM(539),
    GXJ(537),
    VZU(534),
    ZAL(528),
    PFE(522),
    CKW(521),
    UWY(521),
    JTZ(520),
    HAI(520),
    DAX(520),
    WAJ(516),
    NKE(515),
    AOE(515),
    BPV(514),
    ZVO(514),
    HLR(512),
    GXA(510),
    MRE(509),
    QFZ(508),
    IXO(505),
    IJK(503),
    IFM(503),
    USR(501),
    DPS(500),
    SGM(499),
    HGK(499),
    EVO(499),
    FXO(495),
    ASN(495),
    CYJ(494),
    NMA(493),
    GVN(493),
    JSI(490),
    KJC(489),
    KPI(489),
    ZPE(489),
    BLZ(488),
    MHF(487),
    NVH(487),
    HUE(485),
    HFY(484),
    EFB(482),
    HZC(481),
    VBP(481),
    MEW(481),
    DSO(481),
    FMP(479),
    JXH(478),
    TRD(474),
    VEA(472),
    MEC(471),
    FUQ(470),
    DOP(470),
    XJM(469),
    UQE(469),
    APT(468),
    JKX(467),
    ZKH(465),
    BJG(463),
    CKG(463),
    BLE(462),
    PHX(461),
    LTM(461),
    YBS(461),
    JPC(460),
    GUP(459),
    IMK(458),
    URH(458),
    LXH(458),
    FOL(457),
    BGX(457),
    TOQ(456);


    public Integer frequency;
    private static final List<busHighFrequencyEventEnum> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));

    private static final int SIZE = VALUES.size();

    private static final Random RANDOM = new Random();

    busHighFrequencyEventEnum(int frequency) {
        this.frequency = frequency;
    }
}
