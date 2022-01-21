package gloria.users.stockUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum stockHighFrequencyEventTypeEnum {
//100 high frequency event types
ARDX(471),
 TQQQ(459),
 QQQ (457),
 TVIX(454),
 CDTX(453),
 ROKU(450),
 SQQQ(448),
 NAKD(441),
 AMD (438),
 COUP(437),
 HSGX(433),
 HQY (430),
 MSFT(427),
 TLRY(426),
 MDCO(425),
 FB  (424),
 TSLA(423),
 NVDA(422),
 LYFT(421),
 AMZN(420),
 OKTA(418),
 PDD (416),
 MU  (415),
 ULTA(414),
 CONN(411),
 AMRN(409),
 JD  (408),
 TEUM(407),
 CRWD(406),
 MDB (404),
 LULU(402),
 AAPL (401),
 ENDP(400),
 CRZO(399),
 ZNGA(398),
 BBBY(397),
 ALXN(396),
 AMBA(395),
 WBA (394),
 ALGN(393),
 NKTR(392),
 FITB(391),
 ON  (390),
 TWOU(389),
 NTRS(388),
 FTNT(387),
 FANG(386),
 SFM (385),
 RUN (384),
 INFN(383),
 NAVI(382),
 ACGL(381),
 TRNX(380),
 AAXJ(379),
 TRMB(378),
 PPC (377),
 PFPT(376),
 SMTC(375),
 SGMO(374),
 BLDR(373),
 TWNK(372),
 HIMX(371),
 CARG(370),
 ALKS(369),
 PACB(368),
 PLAY(367),
 PINC(366),
 RIOT(365),
 FSLR(364),
 LPSN(363),
 LITE(362),
 PGNX(361),
 TGTX(360),
 PTLA(359),
 TSG (358),
 SPPI(357),
 PEGI(356),
 QURE(355),
 GLPI(354),
 TRUE(353),
 FATE(352),
 RRR (351),
 KIRK(349),
 GOGO(348),
 NBEV(347),
 NBIX(346),
 CYRX(345),
 STML(344),
 GPRE(343),
 ALTR(342),
 VRRM(341),
 TERP(340),
 CDXS(339),
 SBLK(338),
 HRTX(337),
 GDS (336),
 CYBR(335),
 FLIR(334),
 DOX (333),
 CBAY(332);




    public Integer frequency;
    private static final List<stockHighFrequencyEventTypeEnum> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));

    private static final int SIZE = VALUES.size();

    private static final Random RANDOM = new Random();

    stockHighFrequencyEventTypeEnum(int frequency) {
        this.frequency = frequency;
    }


}
