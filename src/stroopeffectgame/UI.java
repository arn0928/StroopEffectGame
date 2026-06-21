package stroopeffectgame;

public class UI {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31;1m";
    public static final String YELLOW = "\u001B[33;1m";
    public static final String BLUE = "\u001B[94;1m";
    public static final String GREEN = "\u001B[32;1m";
    public static final String PURPLE = "\u001B[35;1m";
    public static final String PINK = "\u001B[1;38;5;206m";
    // 補回介面排版需要的 CYAN (青色)
    public static final String CYAN = "\u001B[36;1m";
    public static final String WHITE = "\u001B[37;1m";

    public static final String RED_BG = "\u001B[41m";
    public static final String YELLOW_BG = "\u001B[43m";
    public static final String BLUE_BG = "\u001B[104m";
    public static final String GREEN_BG = "\u001B[42m";
    public static final String PURPLE_BG = "\u001B[45m";
    public static final String PINK_BG = "\u001B[48;5;206m";

    public static String padRight(String text, int width) {
        int padding = width - visibleWidth(text);
        if (padding <= 0) return text + " ";
        StringBuilder sb = new StringBuilder(text);
        for (int i = 0; i < padding; i++) sb.append(' ');
        return sb.toString();
    }

    public static int visibleWidth(String text) {
        String plain = text.replaceAll("\\[[;\\d]*m", "");
        int width = 0;
        for (int i = 0; i < plain.length(); ) {
            int cp = plain.codePointAt(i);
            width += isWideCodePoint(cp) ? 2 : 1;
            i += Character.charCount(cp);
        }
        return width;
    }

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

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void displayQuestion(Question q) {
        String pad = q.hasExclamation ? "！" : " ";
        String displayText = pad + q.word + pad;

        System.out.println(q.bgColorCode + q.fgColorCode + "                  " + RESET);
        System.out.println(q.bgColorCode + q.fgColorCode + "      " + displayText + "      " + RESET);
        System.out.println(q.bgColorCode + q.fgColorCode + "                  " + RESET);
        System.out.println("\n(輸入答案並按 Enter)");
    }

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
