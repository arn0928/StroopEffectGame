package stroopeffectgame;

/**
 * 代表一道新題目：隨機決定顯示的文字、文字顏色（與可能的背景顏色），
 * 以及玩家應作答的預期結果。
 */
public class Question {
    public String word;
    public String fgColorCode;
    public String bgColorCode;
    public boolean hasExclamation;
    public boolean expectedAnswer;
    public int colorIndex; // 文字顏色於 WORDS/FGS 陣列中的索引，供統計時對應顏色使用

    private static final String[] WORDS = {"紅", "黃", "藍", "綠", "紫", "粉"};
    private static final String[] FGS = {UI.RED, UI.YELLOW, UI.BLUE, UI.GREEN, UI.PURPLE, UI.PINK};
    private static final String[] BGS = {UI.RED_BG, UI.YELLOW_BG, UI.BLUE_BG, UI.GREEN_BG, UI.PURPLE_BG, UI.PINK_BG};

    /**
     * 依關卡規則產生一道題目。
     * <p>
     * {@code rand} 由呼叫端傳入並共用同一個實例，多人模式下只要種子碼相同，
     * 各端就能依相同順序消耗亂數、產生完全一致的關卡序列。
     *
     * @param level     目前關卡數，決定可用顏色數量與是否強制套用教學關卡規則
     * @param isEndless 是否套用無盡模式規則（六色、必有反轉與背景幹擾）
     * @param rand      共用的亂數產生器
     */
    public Question(int level, boolean isEndless, java.util.Random rand) {
        int colorCount = (level >= 21 || isEndless) ? 6 : 5;

        // 21、41、61 關為教學關卡，強制套用對應的新規則以便玩家熟悉
        boolean forcePink = (level == 21 && !isEndless);
        boolean forceExclamation = (level == 41 && !isEndless);
        boolean forceBg = (level == 61 && !isEndless);

        expectedAnswer = rand.nextBoolean();

        int wordIdx = rand.nextInt(colorCount);
        int fgIdx = expectedAnswer ? wordIdx : rand.nextInt(colorCount);

        // 預期答案為「不同」時，確保文字顏色與文字字義不會剛好相同
        while (!expectedAnswer && fgIdx == wordIdx) {
            fgIdx = rand.nextInt(colorCount);
        }

        if (forcePink) {
            wordIdx = 5;
            fgIdx = expectedAnswer ? 5 : rand.nextInt(5);
        }

        word = WORDS[wordIdx];
        fgColorCode = FGS[fgIdx];
        colorIndex = fgIdx;

        if (forceExclamation || ((level >= 41 || isEndless) && rand.nextBoolean())) {
            // 反轉術式：實際判斷邏輯與一般題目相反
            hasExclamation = true;
            expectedAnswer = !expectedAnswer;
        } else {
            hasExclamation = false;
        }

        if (forceBg || ((level >= 61 || isEndless) && rand.nextBoolean())) {
            int bgIdx;
            do {
                bgIdx = rand.nextInt(colorCount);
            } while (bgIdx == fgIdx); // 背景顏色需與文字顏色不同，避免文字被背景蓋住而無法辨識
            bgColorCode = BGS[bgIdx];
        } else {
            bgColorCode = "";
        }
    }
}
