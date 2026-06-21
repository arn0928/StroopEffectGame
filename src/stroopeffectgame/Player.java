package stroopeffectgame;

import java.io.*;

/**
 * 玩家的可序列化存檔資料：持有的金幣與道具、進度解鎖狀態，
 * 以及各遊戲模式的歷史統計數據。
 */
public class Player implements Serializable {
    // 存檔結構異動時遞增此版本號，避免讀取到不相容的舊存檔
    private static final long serialVersionUID = 4L;
    private static final String SAVE_FILE = "savegame.dat";
    public static final int FORBIDDEN_BOOK_PRICE = 700;

    public int coins = 0;
    public int maxSavePoint = 0;
    public boolean cleared100 = false;

    public boolean hasArmor = false;
    public int skips = 0;
    public boolean hasWaterVideo = false;
    public boolean hasForbiddenJutsu = false;
    public boolean hasUnlimitedRice = false;
    public boolean forbiddenBookRevealed = false;

    public boolean seenTutorial21 = false;
    public boolean seenTutorial41 = false;
    public boolean seenTutorial61 = false;
    public boolean seenTutorial81 = false;

    /** 單一遊戲模式的累計統計數據。 */
    public static class ModeStats implements Serializable {
        public double totalPlayTime = 0;
        public int totalQuestions = 0;
        public int correctAnswers = 0;
        public int[] colorCorrect = new int[6]; // 索引對應 Question 的六種顏色
        public int[] colorTotal = new int[6];
        public double[] colorTime = new double[6];
        public int reverseCorrect = 0;
        public int reverseTotal = 0;
        public double reverseTime = 0;
    }

    public ModeStats normalStats = new ModeStats();
    public ModeStats endlessStats = new ModeStats();
    public ModeStats practiceStats = new ModeStats();
    public ModeStats multiplayerStats = new ModeStats();

    /**
     * 取得指定遊戲模式對應的統計資料。
     *
     * @param mode 遊戲模式代稱（NORMAL／ENDLESS／PRACTICE／MULTIPLAYER）
     * @return 對應模式的統計資料；未知代稱時回傳普通模式的統計
     */
    public ModeStats getModeStats(String mode) {
        switch(mode) {
            case "NORMAL": return normalStats;
            case "ENDLESS": return endlessStats;
            case "PRACTICE": return practiceStats;
            case "MULTIPLAYER": return multiplayerStats;
            default: return normalStats;
        }
    }

    /**
     * 嘗試扣除指定金額的金幣。
     *
     * @param amount 欲扣除的金幣數量
     * @return 餘額足夠並成功扣款時為 true，否則為 false
     */
    public boolean spendCoins(int amount) {
        if (coins >= amount) {
            coins -= amount;
            System.out.println("購買成功！");
            return true;
        } else {
            System.out.println("金幣不足！");
            return false;
        }
    }

    /** 將目前的玩家資料序列化寫入存檔檔案。 */
    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.out.println("存檔失敗: " + e.getMessage());
        }
    }

    /**
     * 讀取存檔檔案並還原玩家資料；檔案不存在或讀取失敗時回傳全新的存檔。
     *
     * @return 還原後的玩家資料，或一個全新的 {@code Player} 實例
     */
    public static Player load() {
        File file = new File(SAVE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (Player) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("讀取存檔失敗，建立新檔案。");
            }
        }
        return new Player();
    }
}
