package stroopeffectgame;

import java.io.*;

public class Player implements Serializable {
    // 版本號更新為 3L，避免與舊存檔衝突導致錯誤
    private static final long serialVersionUID = 3L; 
    private static final String SAVE_FILE = "savegame.dat";

    public int coins = 0;
    public int maxSavePoint = 0;
    public boolean cleared100 = false;
    
    public boolean hasArmor = false;
    public int skips = 0;
    public boolean hasWaterVideo = false;
    public boolean hasForbiddenJutsu = false;
    
    // 新增：白飯吃到飽道具 (無盡模式防扣錢)
    public boolean hasUnlimitedRice = false;
    
    public boolean seenTutorial21 = false;
    public boolean seenTutorial41 = false;
    public boolean seenTutorial61 = false;
    public boolean seenTutorial81 = false;

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