package stroopeffectgame;

public class Question {
    public String word;
    public String fgColorCode;
    public String bgColorCode;
    public boolean hasExclamation;
    public boolean expectedAnswer;
    public int colorIndex; // 紀錄這是哪個顏色，方便統計

    private static final String[] WORDS = {"紅", "黃", "藍", "綠", "紫", "粉"};
    private static final String[] FGS = {UI.RED, UI.YELLOW, UI.BLUE, UI.GREEN, UI.PURPLE, UI.PINK};
    private static final String[] BGS = {UI.RED_BG, UI.YELLOW_BG, UI.BLUE_BG, UI.GREEN_BG, UI.PURPLE_BG, UI.PINK_BG};

    // 修改：將 Random 作為參數傳入，確保多人模式的隨機序列完全一致
    public Question(int level, boolean isEndless, java.util.Random rand) {
        int colorCount = (level >= 21 || isEndless) ? 6 : 5;
        
        boolean forcePink = (level == 21 && !isEndless);
        boolean forceExclamation = (level == 41 && !isEndless);
        boolean forceBg = (level == 61 && !isEndless);

        expectedAnswer = rand.nextBoolean();

        int wordIdx = rand.nextInt(colorCount);
        int fgIdx = expectedAnswer ? wordIdx : rand.nextInt(colorCount);
        
        while (!expectedAnswer && fgIdx == wordIdx) {
            fgIdx = rand.nextInt(colorCount);
        }

        if (forcePink) {
            wordIdx = 5; 
            fgIdx = expectedAnswer ? 5 : rand.nextInt(5);
        }

        word = WORDS[wordIdx];
        fgColorCode = FGS[fgIdx];
        colorIndex = fgIdx; // 存下顯示顏色的 Index，供 Game.java 統計使用

        if (forceExclamation || ((level >= 41 || isEndless) && rand.nextBoolean())) {
            hasExclamation = true;
            expectedAnswer = !expectedAnswer; 
        } else {
            hasExclamation = false;
        }

        if (forceBg || ((level >= 61 || isEndless) && rand.nextBoolean())) {
            int bgIdx;
            do {
                bgIdx = rand.nextInt(colorCount);
            } while (bgIdx == fgIdx); 
            bgColorCode = BGS[bgIdx];
        } else {
            bgColorCode = ""; 
        }
    }
}