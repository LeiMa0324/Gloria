package users.taxiUser;

import users.stockUser.stockHighFrequencyEventTypeEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum taxiHighFrequencyEventEnum {

    //100 high frequency event types
    CW(341083),
    SY(323008),
    IA(312565),
    MT(277166),
    BN(263646),
    GJ(260712),
    EA(240903),
    FN(239001),
    SO(237648),
    MW(236491),
    EW(235144),
    TG(213885),
    AQ(207883),
    UL(199682),
    ES(196612),
    EG(193955),
    VB(192744),
    LG(184334),
    LE(176786),
    YU(172647),
    AH(171971),
    QS(168909),
    YL(162192),
    CM(159760),
    UZ(158047),
    MG(156061),
    NR(150555),
    JX(131929),
    IG(121447),
    LU(114423),
    FI(106876),
    FK(100504),
    PW(98820),
    KH(95181),
    DK(95123),
    HM(94653),
    ZE(78145),
    XC(69422),
    VX(66864),
    ZX(62631),
    DC(57346),
    EK(56277),
    NP(41419),
    VT(39372),
    LZ(38440),
    PQ(28802),
    NY(27021),
    WZ(22589),
    MK(15647),
    MQ(14953),
    LQ(14539),
    MI(14038),
    OK(12931),
    OM(12756),
    YH(11830),
    LX(11306),
    XQ(8574),
    MN(8302),
    WK(7930),
    QU(6959),
    AI(6907),
    NT(6812),
    KG(6635),
    RS(6098),
    TB(5982),
    FZ(5864),
    VF(5257),
    NO(4915),
    AB(4409),
    SL(4049),
    OX(3987),
    IB(3871),
    EY(3830),
    WT(3421),
    HR(3399),
    ZG(3350),
    ZK(2655),
    RH(2214),
    OI(2133),
    RI(2044),
    NH(1986),
    FV(1889),
    DG(1828),
    LV(1676),
    NB(1485),
    YF(1414),
    CS(1409),
    IX(1348),
    ZM(1273),
    GN(1108),
    JB(1077),
    WE(1074),
    DB(1067),
    VL(1037),
    YX(1032),
    LI(969),
    OZ(933),
    KJ(916),
    WF(915),
    HB(871);

    public Integer frequency;
    private static final List<taxiHighFrequencyEventEnum> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));

    private static final int SIZE = VALUES.size();

    private static final Random RANDOM = new Random();

    taxiHighFrequencyEventEnum(int frequency) {
        this.frequency = frequency;
    }
}
