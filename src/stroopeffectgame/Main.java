package stroopeffectgame;

import java.util.Scanner;

public class Main {
    public static final Scanner scanner = new Scanner(System.in);
    private static Player player;

    public static void main(String[] args) {
        player = Player.load();
        
        while (true) {
            UI.clearScreen();
            System.out.println(UI.CYAN + "=== 終端色彩判斷遊戲 ===" + UI.RESET);
            // 新增金幣獲得提示
            System.out.println(UI.YELLOW + "【金幣規則】普通模式: 答對 +1 | 無盡模式: 答對 +5 / 答錯 -5" + UI.RESET);
            System.out.println("1. 開始遊戲 (從第 1 關開始)");
            
            String continueText = player.maxSavePoint >= 21 ? "(已解鎖最高至第 " + player.maxSavePoint + " 關)" : "(尚未解鎖)";
            System.out.println("2. 從存檔點繼續 " + continueText);
            
            String endlessText = player.cleared100 ? "(已開啟)" : "(尚未通關100關)";
            System.out.println("3. 無盡模式 " + endlessText);
            System.out.println("4. 商店系統");
            System.out.println("5. 離開並存檔");
            System.out.print("\n請輸入選項: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    new Game(player, 1, false).start();
                    break;
                case "2":
                    if (player.maxSavePoint >= 21) {
                        selectSavePoint();
                    } else {
                        System.out.println("您尚未解鎖任何存檔點（需通關第 20 關）！請按 Enter 繼續...");
                        scanner.nextLine();
                    }
                    break;
                case "3":
                    if (player.cleared100) {
                        new Game(player, 1, true).start();
                    } else {
                        System.out.println("必須先通關 100 關才能開啟無盡模式！請按 Enter 繼續...");
                        scanner.nextLine();
                    }
                    break;
                case "4":
                    openShop();
                    break;
                case "5":
                    player.save();
                    System.out.println("遊戲已存檔，感謝遊玩！");
                    System.exit(0);
                    break;
                default:
                    System.out.println("無效的選項！請按 Enter 繼續...");
                    scanner.nextLine();
            }
        }
    }

    private static void selectSavePoint() {
        while (true) {
            UI.clearScreen();
            System.out.println(UI.YELLOW + "=== 選擇存檔點 ===" + UI.RESET);
            System.out.println("請選擇要開始的關卡階段：\n");
            
            if (player.maxSavePoint >= 21) {
                System.out.println("1. 從第 21 關開始");
                System.out.println("   [難度提升] 出現新顏色與文字：粉色\n");
            }
            if (player.maxSavePoint >= 41) {
                System.out.println("2. 從第 41 關開始");
                System.out.println("   [難度提升] 文字前後有機率出現驚嘆號 (！)，此時判斷邏輯需【完全相反】\n");
            }
            if (player.maxSavePoint >= 61) {
                System.out.println("3. 從第 61 關開始");
                System.out.println("   [難度提升] 文字後方有機率出現干擾視覺的隨機背景顏色\n");
            }
            if (player.maxSavePoint >= 81) {
                System.out.println("4. 從第 81 關開始");
                System.out.println("   [難度提升] 通關獎勵時間減少，由每關 +3.5 秒降為 +3 秒\n");
            }
            System.out.println("0. 返回主選單");
            System.out.print("\n請輸入選項: ");

            String input = scanner.nextLine().trim();
            if (input.equals("0")) break;

            int startLevel = 0;
            switch (input) {
                case "1":
                    if (player.maxSavePoint >= 21) startLevel = 21;
                    break;
                case "2":
                    if (player.maxSavePoint >= 41) startLevel = 41;
                    break;
                case "3":
                    if (player.maxSavePoint >= 61) startLevel = 61;
                    break;
                case "4":
                    if (player.maxSavePoint >= 81) startLevel = 81;
                    break;
            }

            if (startLevel > 0) {
                new Game(player, startLevel, false).start();
                break; 
            } else {
                System.out.println("無效的選項或尚未解鎖此存檔點！請按 Enter 繼續...");
                scanner.nextLine();
            }
        }
    }

    private static void openShop() {
        while (true) {
            UI.clearScreen();
            System.out.println(UI.YELLOW + "=== 商店 ===" + UI.RESET);
            System.out.println("持有金幣: " + player.coins);
            
            System.out.println("1. 快來拿裝甲包（血量+5，【僅限下一次遊玩生效】）- 20 金幣 " + (player.hasArmor ? "(已裝備)" : ""));
            System.out.println("2. 全對（按 \\ 通過目前關卡，可用 5 次，【僅限下一次遊玩生效】）- 40 金幣 " + (player.skips > 0 ? "(已裝備 " + player.skips + " 次)" : ""));
            System.out.println("3. 水影片（初始遊戲時間+20秒，【僅限下一次遊玩生效】）- 50 金幣 " + (player.hasWaterVideo ? "(已裝備)" : ""));
            
            // 新商品：白飯吃到飽
            if (player.cleared100) {
                System.out.println("4. 白飯吃到飽（無盡模式答錯不扣金幣，【僅限下一次無盡生效】）- 20 金幣 " + (player.hasUnlimitedRice ? "(已裝備)" : ""));
            } else {
                System.out.println(UI.RED + "4. ???（需先通關 100 關解鎖新商品）" + UI.RESET);
            }
            
            // 封印之書順延為選項 5
            if (!player.hasForbiddenJutsu) {
                System.out.println("5. 封印之書（禁術，【永久生效】）- 700 金幣");
            } else {
                System.out.println("5. 封印之書（禁術，【永久生效】）- (已解除封印)");
            }
            
            System.out.println("0. 離開商店");
            System.out.print("\n請選擇要購買的物品: ");

            String input = scanner.nextLine().trim();
            if (input.equals("0")) break;

            buyItem(input);
        }
    }

    private static void buyItem(String choice) {
        switch (choice) {
            case "1":
                if (!player.hasArmor && player.spendCoins(20)) player.hasArmor = true;
                break;
            case "2":
                if (player.spendCoins(40)) player.skips += 5;
                break;
            case "3":
                if (!player.hasWaterVideo && player.spendCoins(50)) player.hasWaterVideo = true;
                break;
            case "4":
                if (player.cleared100) {
                    if (!player.hasUnlimitedRice && player.spendCoins(20)) player.hasUnlimitedRice = true;
                } else {
                    System.out.println("尚未解鎖此商品！");
                }
                break;
            case "5":
                if (!player.hasForbiddenJutsu && player.spendCoins(700)) {
                    player.hasForbiddenJutsu = true;
                    UI.clearScreen();
                    System.out.println(UI.PURPLE_BG + UI.WHITE + " ！！！已解除封印之書（禁術）！！！ 時間將不再流逝。 " + UI.RESET);
                    System.out.println("感謝玩家遊玩並達成此成就！\n");
                    System.out.println("請按 Enter 繼續...");
                    scanner.nextLine();
                }
                break;
        }
        player.save();
    }
}