package stroopeffectgame;

/**
 * 終端機畫面輸出的共用工具：ANSI 顏色代碼、考慮全角字寬與顏色碼長度的
 * 文字欄位對齊，以及畫面清除、出題排版與答題特效顯示。
 */
public class UI {
    // 文字顏色
    public static final String RESET = "[0m";
    public static final String RED = "[1;38;2;255;85;85m";
    public static final String YELLOW = "[1;38;2;255;220;60m";
    public static final String BLUE = "[1;38;2;90;160;255m";
    public static final String GREEN = "[1;38;2;90;230;90m";
    public static final String PURPLE = "[1;38;2;190;90;255m";
    public static final String PINK = "[1;38;2;255;110;190m";
    public static final String CYAN = "[1;38;2;60;220;220m";
    public static final String WHITE = "[1;38;2;255;255;255m";

    // 背景顏色
    public static final String RED_BG = "[48;2;140;20;20m";
    public static final String YELLOW_BG = "[48;2;150;130;0m";
    public static final String BLUE_BG = "[48;2;20;60;140m";
    public static final String GREEN_BG = "[48;2;20;110;20m";
    public static final String PURPLE_BG = "[48;2;90;20;140m";
    public static final String PINK_BG = "[48;2;150;40;110m";

    /**
     * 將文字右側補上空白至指定顯示寬度，供多欄排版對齊使用。
     * 寬度計算會排除 ANSI 顏色碼並將全角字元視為兩個字元寬。
     *
     * @param text  欲補齊的文字，可包含 ANSI 顏色碼
     * @param width 目標顯示寬度
     * @return 補齊空白後的文字；若原文字已達到或超過目標寬度，僅補一個空白作為欄位間隔
     */
    public static String padRight(String text, int width) {
        int padding = width - visibleWidth(text);
        if (padding <= 0) return text + " ";
        StringBuilder sb = new StringBuilder(text);
        for (int i = 0; i < padding; i++) sb.append(' ');
        return sb.toString();
    }

    /**
     * 計算文字在終端機上的實際顯示寬度：先移除 ANSI 顏色碼（不佔顯示寬度），
     * 再將全角字元（中文、全角符號等）計為 2，其餘字元計為 1。
     *
     * @param text 欲計算寬度的文字，可包含 ANSI 顏色碼
     * @return 文字的顯示寬度
     */
    public static int visibleWidth(String text) {
        String plain = text.replaceAll(java.util.regex.Pattern.quote(String.valueOf((char) 27) + "[") + "[0-9;]*m", "");
        int width = 0;
        for (int i = 0; i < plain.length(); ) {
            int cp = plain.codePointAt(i);
            width += isWideCodePoint(cp) ? 2 : 1;
            i += Character.charCount(cp);
        }
        return width;
    }

    /** 判斷字元是否屬於需以雙倍寬度顯示的全角字元（CJK 文字、全角符號、假名、韓文等）。 */
    private static boolean isWideCodePoint(int cp) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(cp);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
            || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
            || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            || block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
            || block == Character.UnicodeBlock.HIRAGANA
            || block == Character.UnicodeBlock.KATAKANA
            || block == Character.UnicodeBlock.HANGUL_SYLLABLES;
    }

    /** 清除終端機畫面內容，將游標移回左上角。 */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** 將題目以指定的文字顏色與背景顏色排版顯示於畫面中央。 */
    public static void displayQuestion(Question q) {
        String pad = q.hasExclamation ? "！" : " ";
        String displayText = pad + q.word + pad;

        System.out.println(q.bgColorCode + q.fgColorCode + "                  " + RESET);
        System.out.println(q.bgColorCode + q.fgColorCode + "      " + displayText + "      " + RESET);
        System.out.println(q.bgColorCode + q.fgColorCode + "                  " + RESET);
        System.out.println("\n(輸入答案並按 Enter)");
    }

    /** 播放答對時的提示音與畫面特效，並短暫停頓讓玩家確認結果。 */
    public static void playCorrectEffect() {
        try {
            System.out.print("\007");
            clearScreen();
            System.out.println("\n\n\n\n" + GREEN_BG + WHITE + "\n\n    O  正確！ O    \n\n" + RESET);
            Thread.sleep(160);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** 播放答錯時的提示音與畫面特效，並短暫停頓讓玩家確認結果。 */
    public static void playWrongEffect() {
        try {
            System.out.print("\007");
            clearScreen();
            System.out.println("\n\n\n\n" + RED_BG + WHITE + "\n\n    X  錯誤！ X    \n\n" + RESET);
            Thread.sleep(280);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
