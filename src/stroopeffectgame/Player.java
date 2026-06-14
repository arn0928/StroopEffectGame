package stroopeffectgame;

import java.io.*;

public class Player implements Serializable {
    private static final long serialVersionUID = 4L; // 升級版本號至 4L
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

    // --- 新增：遊戲統計數據結構 ---
    public static class ModeStats implements Serializable {
        public double totalPlayTime = 0;
        public int totalQuestions = 0;
        public int correctAnswers = 0;
        public int[] colorCorrect = new int[6]; // 對應6種顏色
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

    public ModeStats getModeStats(String mode) {
        switch(mode) {
            case "NORMAL": return normalStats;
            case "ENDLESS": return endlessStats;
            case "PRACTICE": return practiceStats;
            case "MULTIPLAYER": return multiplayerStats;
            default: return normalStats;
        }
    }

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

    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.out.println("存檔失敗: " + e.getMessage());
        }
    }

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
