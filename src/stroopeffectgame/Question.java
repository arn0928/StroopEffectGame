package stroopeffectgame;

import java.util.Random;

public class Question {
    public String word;
    public String fgColorCode;
    public String bgColorCode;
    public boolean hasExclamation;
    public boolean expectedAnswer;

    private static final String[] WORDS = {"紅", "黃", "藍", "綠", "紫", "粉"};
    private static final String[] FGS = {UI.RED, UI.YELLOW, UI.BLUE, UI.GREEN, UI.PURPLE, UI.PINK};
    private static final String[] BGS = {UI.RED_BG, UI.YELLOW_BG, UI.BLUE_BG, UI.GREEN_BG, UI.PURPLE_BG, UI.PINK_BG};

    public Question(int level, boolean isEndless) {
        Random rand = new Random();
        int colorCount = (level >= 21 || isEndless) ? 6 : 5;
        
        boolean forcePink = (level == 21 && !isEndless);
        boolean forceExclamation = (level == 41 && !isEndless);
        boolean forceBg = (level == 61 && !isEndless);

        // 決定預期答案 (50% 機率為真)
        expectedAnswer = rand.nextBoolean();

        int wordIdx = rand.nextInt(colorCount);
        int fgIdx = expectedAnswer ? wordIdx : rand.nextInt(colorCount);
        
        // 確保為假時，顏色絕對不同
        while (!expectedAnswer && fgIdx == wordIdx) {
            fgIdx = rand.nextInt(colorCount);
        }

        if (forcePink) {
            wordIdx = 5; // 確保第 21 關一定出現粉色
            fgIdx = expectedAnswer ? 5 : rand.nextInt(5);
        }

        word = WORDS[wordIdx];
        fgColorCode = FGS[fgIdx];

        // 驚嘆號邏輯
        if (forceExclamation || ((level >= 41 || isEndless) && rand.nextBoolean())) {
            hasExclamation = true;
            expectedAnswer = !expectedAnswer; // 邏輯反轉
        } else {
            hasExclamation = false;
        }

        // 背景顏色邏輯
        if (forceBg || ((level >= 61 || isEndless) && rand.nextBoolean())) {
            int bgIdx;
            do {
                bgIdx = rand.nextInt(colorCount);
            } while (bgIdx == fgIdx); // 背景不能和文字顏色一樣
            bgColorCode = BGS[bgIdx];
        } else {
            bgColorCode = ""; // 無背景
        }
    }
}