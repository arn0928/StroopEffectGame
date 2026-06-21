package stroopeffectgame;

import java.util.Scanner;

public class Main {
    public static final Scanner scanner = new Scanner(System.in);
    private static Player player;
    private static final int RIGHT_INFO_COLUMN = 32;
    private static final int SHOP_EFFECT_COLUMN = 26;
    private static final int SHOP_PRICE_COLUMN = 76;

    public static void main(String[] args) {
        player = Player.load();
        
        while (true) {
            UI.clearScreen();
            System.out.println(UI.CYAN + "=== 終端色彩判斷遊戲 ===" + UI.RESET);
            
            printRightInfoRow("1. 開始遊戲 (普通模式)", "[" + UI.YELLOW + "金幣: 答對+1" + UI.RESET + "]");
            
            String contTxt = player.maxSavePoint >= 21 ? "已解鎖最高至第 " + player.maxSavePoint + " 關" : "尚未解鎖";
            printRightInfoRow("2. 從存檔點繼續", "[" + UI.YELLOW + "金幣: 答對+1" + UI.RESET + " | " + contTxt + "]");
            
            String endTxt = player.cleared100 ? "已開啟" : "尚未通關100關";
            printRightInfoRow("3. 無盡模式 (難度全開)", "[" + UI.YELLOW + "金幣: 答對+5 / 答錯-5" + UI.RESET + " | " + endTxt + "]");
            
            System.out.println("4. 商店");
            printRightInfoRow("5. 多人模式", "[不限" + UI.CYAN + "時間" + UI.RESET + "與" + UI.RED + "血量" + UI.RESET + " | " + UI.YELLOW + "金幣: 無" + UI.RESET + "]");
            printRightInfoRow("6. 練習模式", "[不限" + UI.CYAN + "時間" + UI.RESET + "與" + UI.RED + "血量" + UI.RESET + " | " + UI.YELLOW + "金幣: 無" + UI.RESET + "]");
            
            System.out.println("7. 遊戲統計");
            System.out.println("8. 刪除存檔");
            System.out.println("9. 離開並存檔");
            System.out.print("\n請輸入選項: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1": new Game(player, 1, false, "NORMAL", null).start(); break;
                case "2":
                    if (player.maxSavePoint >= 21) selectSavePoint();
                    else { System.out.println("您尚未解鎖任何存檔點！請按 Enter 繼續..."); scanner.nextLine(); }
                    break;
                case "3":
                    if (player.cleared100) new Game(player, 1, true, "ENDLESS", null).start();
                    else { System.out.println("必須先通關 100 關！請按 Enter 繼續..."); scanner.nextLine(); }
                    break;
                case "4": openShop(); break;
                case "5": setupCustomGame("MULTIPLAYER"); break;
                case "6": setupCustomGame("PRACTICE"); break;
                case "7": showStatistics(); break;
                case "8":
                    System.out.print(UI.RED + "確定要刪除存檔嗎？(y/n): " + UI.RESET);
                    if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                        new java.io.File("savegame.dat").delete();
                        player = new Player(); 
                        System.out.println(UI.GREEN + "\n存檔已成功刪除！請按 Enter 返回主選單..." + UI.RESET);
                        scanner.nextLine();
                    }
                    break;
                case "9": player.save(); System.out.println("感謝遊玩！"); System.exit(0); break;
                case "99":
                    player.cleared100 = true; player.maxSavePoint = 81; player.coins += 10000; player.forbiddenBookRevealed = true; player.save();
                    System.out.println(UI.PURPLE + "\n【密技】已強制解鎖並獲得金幣！請按 Enter 繼續..." + UI.RESET);
                    scanner.nextLine();
                    break;
                default:
                    System.out.println("無效的選項！請按 Enter 繼續...");
                    scanner.nextLine();
            }
        }
    }

    private static void printRightInfoRow(String left, String right) {
        System.out.println(UI.padRight(left, RIGHT_INFO_COLUMN) + right);
    }

    private static void printShopRow(String left, String info, String price) {
        String leftColumns = UI.padRight(left, SHOP_EFFECT_COLUMN) + info;
        System.out.println(UI.padRight(leftColumns, SHOP_PRICE_COLUMN) + UI.YELLOW + price + UI.RESET);
    }

    private static void setupCustomGame(String mode) {
        UI.clearScreen();
        System.out.println(UI.YELLOW + "=== " + (mode.equals("MULTIPLAYER") ? "多人模式" : "練習模式") + " ===" + UI.RESET);
        System.out.println("請選擇起始規則：");
        System.out.println("1. 從第 1 關開始 (至100關結束)");
        System.out.println("2. 從已解鎖的存檔點開始 (至100關結束)");
        System.out.println("3. 無盡模式規則 (難度全開，無限關卡)");
        System.out.println("0. 返回主選單");
        System.out.print("\n請選擇: ");
        
        String choice = scanner.nextLine().trim();
        int startLevel = 1;
        boolean isEndless = false;
        
        if (choice.equals("0")) return;
        if (choice.equals("2")) {
            if (player.maxSavePoint >= 21) {
                System.out.print("請選擇起始關卡 (21, 41, 61, 81): ");
                try {
                    int lvl = Integer.parseInt(scanner.nextLine().trim());
                    if ((lvl==21 || lvl==41 || lvl==61 || lvl==81) && lvl <= player.maxSavePoint) startLevel = lvl;
                    else { System.out.println("解鎖不足或無效關卡！按 Enter 返回..."); scanner.nextLine(); return; }
                } catch (Exception e) { System.out.println("輸入無效！按 Enter 返回..."); scanner.nextLine(); return; }
            } else { System.out.println("尚未解鎖存檔點！按 Enter 返回..."); scanner.nextLine(); return; }
        } else if (choice.equals("3")) isEndless = true;
        else if (!choice.equals("1")) { System.out.println("無效選擇！按 Enter 返回..."); scanner.nextLine(); return; }
        
        Long seed = null;
        if (mode.equals("MULTIPLAYER")) {
            System.out.println(UI.CYAN + "\n[多人連線設定]" + UI.RESET);
            System.out.println("請所有玩家輸入【相同的種子碼】(英文數字皆可)，以確保在不同設備生成相同關卡。");
            System.out.print("請輸入種子碼 (留空則隨機生成): ");
            String seedStr = scanner.nextLine().trim();
            if (!seedStr.isEmpty()) seed = (long) seedStr.hashCode();
        }
        
        new Game(player, startLevel, isEndless, mode, seed).start();
    }

    private static void showStatistics() {
        while (true) {
            UI.clearScreen();
            System.out.println(UI.CYAN + "=== 遊戲統計 ===" + UI.RESET);
            System.out.println("1. 普通模式");
            System.out.println("2. 無盡模式");
            System.out.println("3. 多人模式");
            System.out.println("4. 練習模式");
            System.out.println("0. 返回主選單");
            System.out.print("\n請選擇要查看的模式: ");
            
            String input = scanner.nextLine().trim();
            if (input.equals("0")) break;
            
            String mode = ""; String modeName = "";
            switch(input) {
                case "1": mode = "NORMAL"; modeName = "普通模式"; break;
                case "2": mode = "ENDLESS"; modeName = "無盡模式"; break;
                case "3": mode = "MULTIPLAYER"; modeName = "多人模式"; break;
                case "4": mode = "PRACTICE"; modeName = "練習模式"; break;
                default: continue;
            }
            
            Player.ModeStats ms = player.getModeStats(mode);
            UI.clearScreen();
            System.out.println(UI.YELLOW + "--- [" + modeName + "] 統計數據 ---" + UI.RESET);
            System.out.printf("總遊玩時間: %.2f 秒\n", ms.totalPlayTime);
            System.out.printf("總答題數: %d 題\n", ms.totalQuestions);
            double acc = ms.totalQuestions > 0 ? (ms.correctAnswers * 100.0 / ms.totalQuestions) : 0;
            System.out.printf("總正確率: %d/%d (%.1f%%)\n\n", ms.correctAnswers, ms.totalQuestions, acc);
            
            System.out.println("[各顏色準確率與平均作答時間]");
            String[] colorNames = {"紅", "黃", "藍", "綠", "紫", "粉"};
            String[] colorCodes = {UI.RED, UI.YELLOW, UI.BLUE, UI.GREEN, UI.PURPLE, UI.PINK};
            for(int i=0; i<6; i++) {
                double cAcc = ms.colorTotal[i] > 0 ? (ms.colorCorrect[i] * 100.0 / ms.colorTotal[i]) : 0;
                double cAvg = ms.colorTotal[i] > 0 ? (ms.colorTime[i] / ms.colorTotal[i]) : 0;
                System.out.printf("%s%s%s: 正確率 %d/%d (%.1f%%) | 平均 %.2f 秒\n", 
                    colorCodes[i], colorNames[i], UI.RESET, ms.colorCorrect[i], ms.colorTotal[i], cAcc, cAvg);
            }
            
            System.out.println(UI.PURPLE + "\n[反轉術式 (!)]" + UI.RESET);
            double rAcc = ms.reverseTotal > 0 ? (ms.reverseCorrect * 100.0 / ms.reverseTotal) : 0;
            double rAvg = ms.reverseTotal > 0 ? (ms.reverseTime / ms.reverseTotal) : 0;
            System.out.printf("正確率 %d/%d (%.1f%%) | 平均 %.2f 秒\n\n", ms.reverseCorrect, ms.reverseTotal, rAcc, rAvg);
            
            System.out.println("請按 Enter 返回..."); scanner.nextLine();
        }
    }

    private static void selectSavePoint() {
        while (true) {
            UI.clearScreen();
            System.out.println(UI.YELLOW + "=== 選擇存檔點 ===" + UI.RESET);
            if (player.maxSavePoint >= 21) System.out.println("1. 從第 21 關開始");
            if (player.maxSavePoint >= 41) System.out.println("2. 從第 41 關開始");
            if (player.maxSavePoint >= 61) System.out.println("3. 從第 61 關開始");
            if (player.maxSavePoint >= 81) System.out.println("4. 從第 81 關開始");
            System.out.println("0. 返回");
            System.out.print("\n請選擇: ");
            String input = scanner.nextLine().trim();
            if (input.equals("0")) break;
            
            int startLevel = 0;
            switch (input) {
                case "1": if (player.maxSavePoint >= 21) startLevel = 21; break;
                case "2": if (player.maxSavePoint >= 41) startLevel = 41; break;
                case "3": if (player.maxSavePoint >= 61) startLevel = 61; break;
                case "4": if (player.maxSavePoint >= 81) startLevel = 81; break;
            }
            if (startLevel > 0) { new Game(player, startLevel, false, "NORMAL", null).start(); break; } 
            else { System.out.println("無效的選項！請按 Enter 繼續..."); scanner.nextLine(); }
        }
    }

    private static void openShop() {
        while (true) {
            UI.clearScreen();
            System.out.println(UI.YELLOW + "=== 商店 ===" + UI.RESET);
            System.out.println("持有金幣: " + player.coins + "\n");
            
            printShopRow("1. " + UI.RED + "快來拿裝甲包" + UI.RESET,
                "[單次生效 | 血量 +5" + (player.hasArmor ? " | 已裝備" : "") + "]",
                "20 金幣");
            printShopRow("2. " + UI.GREEN + "全對" + UI.RESET,
                "[單次生效 | 按 \\ 通過目前關卡，可用 5 次" + (player.skips > 0 ? " | 已裝備 " + player.skips + " 次" : "") + "]",
                "40 金幣");
            printShopRow("3. " + UI.YELLOW + "水影片" + UI.RESET,
                "[單次生效 | 初始遊戲時間 +20 秒" + (player.hasWaterVideo ? " | 已裝備" : "") + "]",
                "50 金幣");
            
            if (player.cleared100) {
                printShopRow("4. " + UI.WHITE + "白飯吃到飽" + UI.RESET,
                    "[單次無盡 | 無盡模式答錯不扣金幣" + (player.hasUnlimitedRice ? " | 已裝備" : "") + "]",
                    "20 金幣");
            } else {
                printShopRow("4. " + UI.RED + "？？？？？？" + UI.RESET,
                    "[尚未解鎖 | 需先通關 100 關]",
                    "??? 金幣");
            }
            
            boolean showForbiddenBookDetail = player.forbiddenBookRevealed || player.hasForbiddenJutsu;
            String forbiddenPrice = Player.FORBIDDEN_BOOK_PRICE + " 金幣" + (player.hasForbiddenJutsu ? " (已購買)" : "");
            if (showForbiddenBookDetail) {
                printShopRow("5. " + UI.PURPLE + "封印之書(禁術)" + UI.RESET,
                    "[" + (player.hasForbiddenJutsu ? "已購買" : "可購買") + " | 輸入 = 啟動該輪時間暫停]",
                    forbiddenPrice);
            } else {
                printShopRow("5. " + UI.PURPLE + "封印之書(禁術)" + UI.RESET,
                    "[效果未揭曉 | 遊戲結束後金幣達標揭曉]",
                    forbiddenPrice);
            }
            
            System.out.println("\n0. 離開商店");
            System.out.print("請選擇要購買的物品: ");
            
            String input = scanner.nextLine().trim();
            if (input.equals("0")) break;
            buyItem(input);
            System.out.println("請按 Enter 繼續...");
            scanner.nextLine();
        }
    }

    private static void buyItem(String choice) {
        switch (choice) {
            case "1":
                if (player.hasArmor) System.out.println("已裝備裝甲包。");
                else if (player.spendCoins(20)) player.hasArmor = true;
                break;
            case "2":
                if (player.spendCoins(40)) player.skips += 5;
                break;
            case "3":
                if (player.hasWaterVideo) System.out.println("已裝備水影片。");
                else if (player.spendCoins(50)) player.hasWaterVideo = true;
                break;
            case "4":
                if (!player.cleared100) System.out.println("需先通關 100 關才能解鎖。");
                else if (player.hasUnlimitedRice) System.out.println("已裝備白飯吃到飽。");
                else if (player.spendCoins(20)) player.hasUnlimitedRice = true;
                break;
            case "5":
                if (!player.forbiddenBookRevealed && !player.hasForbiddenJutsu) {
                    System.out.println("封印之書(禁術)的效果尚未揭曉。");
                } else if (player.hasForbiddenJutsu) {
                    System.out.println("已購買封印之書(禁術)。");
                } else if (player.spendCoins(Player.FORBIDDEN_BOOK_PRICE)) {
                    player.hasForbiddenJutsu = true;
                    player.forbiddenBookRevealed = true;
                }
                break;
            default:
                System.out.println("無效的選項！");
        }
        player.save();
    }
}
