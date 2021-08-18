package users.stockUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum stockLowFrequencyEventTypeEnum {

    THCA(1),
    TSCA(2),
    CLDB(3),
    PHUN(4),
    LMST(5),
    OXLC(6),
    RNSC(7),
    ASRV(8),
    KRMA(9),
    RILYG(10),
    POLA (11),
    UNTY (12),
    IGLD (13),
    MTBC (14),
    ITIC (15),
    MNDO (16),
    GENC (17),
    AFINP(18),
    GECCN(19),
    CCNE (20),
    CTRC (21),
    SGC  (22),
    IDSY (23),
    CHNGU(24),
    IPKW (25),
    AIHS (26),
    TMCXW(27),
    FNWB (28),
    LTRX (29),
    INMB (30),
    CSTR (31),
    ENG  (32),
    CACG (33),
    CHFS (34),
    AGNCB(35),
    PAVM (36),
    ANIX (37),
    RFIL (38),
    IMV  (39),
    NCNA (40),
    AZRX (41),
    ENOB (42),
    MACK (43),
    ULBI (44),
    GIFI (45),
    ABIO (46),
    TWMC (47),
    PUI  (48),
    NHTC (49),
    PLIN (50),
    KBSF (51),
    SNES (52),
    FCBC (53),
    KMPH (54),
    HWKN (55),
    PTMN (56),
    ATOS (57),
    FSV  (58),
    CBMG (59),
    CPAH (60),
    SUNS (61),
    QUIK (62),
    QCRH (63),
    RILYO(64),
    REED (65),
    KLDO (66),
    BATRA(67),
    PLW  (68),
    YORW (69),
    PRTO (70),
    TGA  (71),
    SPHS (72),
    AWSM (73),
    CIGI (74),
    ALT  (75),
    THOR (76),
    GRVY (77),
    ATOM (78),
    TNXP (79),
    VONV (80),
    ZEUS (81),
    SLRC (82),
    CORV (83),
    CGEN (84),
    KERN (85),
    SSSS (86),
    ALBO (87),
    SBT  (88),
    PFIE (89),
    SINT (90),
    INTL (91),
    CBIO (92),
    SASR (93),
    AGFS (94),
    GAIA (95),
    LFVN (96),
    OBSV (97),
    TOPS (98),
    ICAD (99),
    PDP  (100);



    public Integer frequency;

    stockLowFrequencyEventTypeEnum(int frequency) {
        this.frequency = frequency;
    }



    private static final Random RANDOM = new Random();
    private static final List<stockLowFrequencyEventTypeEnum> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();

    public static stockLowFrequencyEventTypeEnum randomEvent()  {
        return  VALUES.get(RANDOM.nextInt(SIZE));

    }


}
