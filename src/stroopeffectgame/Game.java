package stroopeffectgame;

public class Game {
    private Player player;
    private int currentLevel;
    private boolean isEndless;
    private int hp;
    private double remainingTime;
    private double lastTimeDiff = 0.0;
    private int lastCoinGained = 0;
    
    // 記錄這局無盡模式是否有白飯吃到飽加持
    private boolean endlessNoCoinLoss = false;

    public Game(Player player, int startLevel, boolean isEndless) {
        this.player = player;
        this.currentLevel = startLevel;
        this.isEndless = isEndless;
        
        this.hp = 5 + (player.hasArmor ? 5 : 0);
        this.remainingTime = 15.0 + (player.hasWaterVideo ? 20.0 : 0.0);
        
        boolean usedItem = false;
        if (player.hasArmor) {
            player.hasArmor = false;
            usedItem = true;
        }
        if (player.hasWaterVideo) {
            player.hasWaterVideo = false;
            usedItem = true;
        }
        // 僅在無盡模式時消耗白飯吃到飽道具
        if (isEndless && player.hasUnlimitedRice) {
            player.hasUnlimitedRice = false;
            this.endlessNoCoinLoss = true;
            usedItem = true;
        }
        
        if (usedItem) {
            player.save();
        }
    }

    public void start() {
        while (hp > 0 && (isEndless || currentLevel <= 100)) {
            
            if (currentLevel == 21 && !player.seenTutorial21) {
                showTutorial(21);
                player.seenTutorial21 = true;
                player.save();
            } else if (currentLevel == 41 && !player.seenTutorial41) {
                showTutorial(41);
                player.seenTutorial41 = true;
                player.save();
            } else if (currentLevel == 61 && !player.seenTutorial61) {
                showTutorial(61);
                player.seenTutorial61 = true;
                player.save();
            } else if (currentLevel == 81 && !player.seenTutorial81) {
                showTutorial(81);
                player.seenTutorial81 = true;
                player.save();
            }
            
            Question q = new Question(currentLevel, isEndless);
            boolean isQuestionActive = true;

            while (isQuestionActive) {
                UI.clearScreen();
                
                System.out.print(UI.BLUE + "第 " + currentLevel + (isEndless ? " 關 (無盡)" : " 關") + UI.RESET + " | ");
                System.out.print(UI.RED + "血量: " + hp + UI.RESET + " | ");
                
                if (player.hasForbiddenJutsu) {
                    System.out.print(UI.PURPLE + "時間: (時間暫停) " + UI.RESET);
                } else {
                    String timeDiffStr = String.format("%s%.2f秒", lastTimeDiff >= 0 ? "+" : "", lastTimeDiff);
                    System.out.printf(UI.YELLOW + "時間: %.2f秒 (%s) " + UI.RESET, remainingTime, timeDiffStr);
                }
                
                // 動態處理正負號的顯示
                String coinDiffStr = lastCoinGained >= 0 ? "+" + lastCoinGained : String.valueOf(lastCoinGained);
                System.out.println("| 金幣: " + player.coins + " (" + coinDiffStr + ")");
                
                System.out.println("--------------------------------------------------");
                System.out.println("操作: 相同直接按 Enter | 不同按 ' 再按 Enter");
                System.out.println("功能: 輸入 [ 暫停遊戲 | 輸入 ] 儲存並回主選單" + (player.skips > 0 ? " | 按 \\ 使用全對" : ""));
                System.out.println("--------------------------------------------------\n");

                UI.displayQuestion(q); 

                long startTime = System.currentTimeMillis();
                String input = Main.scanner.nextLine().trim().toLowerCase();
                long endTime = System.currentTimeMillis();
                
                if (input.equals("[")) {
                    boolean continueGame = showPauseMenu();
                    if (continueGame) {
                        q = new Question(currentLevel, isEndless);
                        continue; 
                    } else {
                        endPlaythrough(); 
                        player.save();
                        return; 
                    }
                }
                
                if (input.equals("]")) {
                    endPlaythrough(); 
                    player.save();
                    System.out.println(UI.YELLOW + "\n已儲存進度，正在返回主選單..." + UI.RESET);
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    return;
                }

                double elapsed = (endTime - startTime) / 1000.0;
                if (!player.hasForbiddenJutsu) {
                    remainingTime -= elapsed;
                }

                if (remainingTime <= 0 && !player.hasForbiddenJutsu) {
                    System.out.println(UI.RED_BG + UI.WHITE + " 時間到！遊戲結束。 " + UI.RESET);
                    endPlaythrough(); 
                    Main.scanner.nextLine();
                    return;
                }

                boolean isTrue = input.equals("");
                boolean isFalse = input.equals("'");
                boolean useSkip = input.equals("\\") && player.skips > 0;

                if (useSkip) {
                    player.skips--;
                    handleCorrect(elapsed);
                    isQuestionActive = false; 
                } else if ((isTrue && q.expectedAnswer) || (isFalse && !q.expectedAnswer)) {
                    handleCorrect(elapsed);
                    isQuestionActive = false; 
                } else {
                    handleWrong();
                    isQuestionActive = false; 
                }
            }

            if (!isEndless && (currentLevel == 20 || currentLevel == 40 || currentLevel == 60 || currentLevel == 80)) {
                player.maxSavePoint = Math.max(player.maxSavePoint, currentLevel + 1);
                player.save();
            }
            
            currentLevel++;
        }

        if (hp <= 0) {
            System.out.println(UI.RED + "\n血量歸零，遊戲結束！" + UI.RESET);
        } else if (!isEndless && currentLevel > 100) {
            System.out.println(UI.GREEN + "\n恭喜通關 100 關！無盡模式已開啟！" + UI.RESET);
            // 解鎖時跳出商店新商品通知
            if (!player.cleared100) {
                System.out.println(UI.YELLOW + "【系統通知】商店有新商品可購買！" + UI.RESET);
            }
            player.cleared100 = true;
        }
        
        endPlaythrough(); 
        player.save();
        System.out.println("請按 Enter 回到主選單...");
        Main.scanner.nextLine();
    }

    private void showTutorial(int level) {
        UI.clearScreen();
        System.out.println(UI.CYAN + "---時間倒數暫停---" + UI.RESET);
        System.out.println(UI.YELLOW + "------遊戲提示------" + UI.RESET);
        System.out.print(UI.RED + "目前狀態: 血量: " + hp + UI.RESET + " | ");
        System.out.printf(UI.YELLOW + "剩餘時間: %.2f秒" + UI.RESET + " | ", remainingTime);
        System.out.println("金幣: " + player.coins);
        System.out.println("--------------------------------\n");

        if (level == 21) {
            System.out.println("【難度提升】新顏色加入！");
            System.out.println("接下來將會出現新顏色與文字：" + UI.PINK + "粉色" + UI.RESET);
        } else if (level == 41) {
            System.out.println(UI.PURPLE + "---！反轉術式！---" + UI.RESET);
            System.out.println("遇到 ！顏色！ 需輸入相反的判斷。");
            
            String[] words = {"紅", "黃", "藍", "綠", "紫", "粉"};
            String[] fgs = {UI.RED, UI.YELLOW, UI.BLUE, UI.GREEN, UI.PURPLE, UI.PINK};
            java.util.Random rand = new java.util.Random();
            
            int wIdx = rand.nextInt(6);
            int cIdx;
            do { cIdx = rand.nextInt(6); } while(cIdx == wIdx);
            
            String exampleText = fgs[cIdx] + "！" + words[wIdx] + "！" + UI.RESET;
            System.out.println("例如：" + exampleText + " (顯示為" + words[cIdx] + "色的「" + words[wIdx] + "」字)，需按下Enter");
        } else if (level == 61) {
            System.out.println("【難度提升】視覺干擾！");
            System.out.println("文字後方開始有機率出現隨機背景顏色，請專注文字，不要被背景色干擾判斷！");
        } else if (level == 81) {
            System.out.println("【難度提升】時間緊迫！");
            System.out.println("通關獎勵時間減少！接下來每通過一關的獎勵時間將降為 +2 秒。");
        }
        
        System.out.println("\n按下Enter進入 " + level + " 關");
        Main.scanner.nextLine();
    }

    private boolean showPauseMenu() {
        while (true) {
            UI.clearScreen();
            System.out.println(UI.YELLOW + "=== 遊戲暫停 ===" + UI.RESET);
            System.out.println(UI.CYAN + "注意：為了防止作弊，解除暫停後將會「重新生成」新題目！" + UI.RESET);
            System.out.println("1. 繼續遊戲 (重製當前關卡題目)");
            System.out.println("2. 儲存並離開遊戲 (未使用的全對道具將會消失)");
            System.out.print("\n請選擇操作: ");
            
            String choice = Main.scanner.nextLine().trim();
            if (choice.equals("1")) {
                return true;
            } else if (choice.equals("2")) {
                return false;
            }
        }
    }

    private void handleCorrect(double elapsed) {
        UI.playCorrectEffect();
        
        double timeReward;
        if (isEndless) {
            timeReward = 2.0;
        } else if (currentLevel >= 80) {
            timeReward = 2.0;
        } else {
            timeReward = 3.0;
        }

        if (!player.hasForbiddenJutsu) {
            remainingTime += timeReward;
            lastTimeDiff = timeReward - elapsed;
        } else {
            lastTimeDiff = 0.0;
        }

        // 修改無盡模式獎勵為 +5
        int coinReward = isEndless ? 5 : 1;
        if (!isEndless && (currentLevel == 20 || currentLevel == 40 || currentLevel == 60 || currentLevel == 80 || currentLevel == 100)) {
            coinReward = 10;
        }
        player.coins += coinReward;
        lastCoinGained = coinReward;
    }

    private void handleWrong() {
        UI.playWrongEffect();
        hp--;
        lastTimeDiff = 0.0;
        
        // 修改無盡模式的扣錢機制 (若無白飯吃到飽加持則扣 5 元)
        if (isEndless && !endlessNoCoinLoss) {
            int loss = Math.min(5, player.coins); // 避免金幣扣成負數
            player.coins -= loss;
            lastCoinGained = -loss;
        } else {
            lastCoinGained = 0;
        }
    }
    
    private void endPlaythrough() {
        if (player.skips > 0) {
            player.skips = 0;
        }
    }
}